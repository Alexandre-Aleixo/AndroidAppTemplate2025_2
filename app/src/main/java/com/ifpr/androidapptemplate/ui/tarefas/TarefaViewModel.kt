package com.ifpr.androidapptemplate.ui.tarefas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ifpr.androidapptemplate.baseclasses.Tarefa
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ifpr.androidapptemplate.workers.DeadlineNotificationWorker
import com.ifpr.androidapptemplate.utils.showImmediateNotification
import java.util.concurrent.TimeUnit

class TarefaViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String? = auth.currentUser?.uid

    private val databaseRef: DatabaseReference? = currentUserId?.let { uid ->
        FirebaseDatabase.getInstance().getReference("users")
            .child(uid)
            .child("tarefas")
    }

    private val _listaTarefas = MutableLiveData<List<Tarefa>>()
    val listaTarefas: LiveData<List<Tarefa>> = _listaTarefas

    private var _ordenacaoAtual = OpcaoOrdenacao.STATUS

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tarefas = mutableListOf<Tarefa>()
            for (taskSnapshot in snapshot.children) {
                val taskMap = taskSnapshot.value as? HashMap<String, Any>

                if (taskMap != null) {
                    val dataCriacaoAny = taskMap["dataCriacao"]
                    val dataCriacaoValue: Long = when (dataCriacaoAny) {
                        is String -> dataCriacaoAny.toLongOrNull() ?: 0L
                        is Long -> dataCriacaoAny
                        else -> 0L
                    }

                    // Leitura do campo 'prazo'
                    val prazoAny = taskMap["prazo"]
                    val prazoValue: Long? = when (prazoAny) {
                        is String -> prazoAny.toLongOrNull()
                        is Long -> prazoAny
                        else -> null
                    }

                    val iconeResIdValue = (taskMap["iconeResId"] as? Long)?.toInt() ?: 0

                    val tarefa = Tarefa(
                        id = taskSnapshot.key,
                        descricao = taskMap["descricao"] as? String ?: "",
                        concluida = taskMap["concluida"] as? Boolean ?: false,
                        iconeResId = iconeResIdValue,
                        dataCriacao = dataCriacaoValue,
                        prazo = prazoValue
                    )
                    tarefas.add(tarefa)
                }
            }
            _listaTarefas.value = aplicarOrdenacao(tarefas, _ordenacaoAtual)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("TarefaViewModel", "Falha na leitura do Firebase: ${error.message}")
        }
    }

    init {
        databaseRef?.addValueEventListener(valueEventListener)
        if (currentUserId == null) {
            _listaTarefas.value = emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        databaseRef?.removeEventListener(valueEventListener)
    }

    fun setOrdenacao(novaOpcao: OpcaoOrdenacao) {
        if (novaOpcao != _ordenacaoAtual) {
            _ordenacaoAtual = novaOpcao
            _listaTarefas.value?.let {
                _listaTarefas.value = aplicarOrdenacao(it, novaOpcao)
            }
        }
    }

    private fun aplicarOrdenacao(lista: List<Tarefa>, opcao: OpcaoOrdenacao): List<Tarefa> {
        return when (opcao) {
            OpcaoOrdenacao.STATUS -> lista.sortedWith(
                compareBy<Tarefa> { it.concluida }
                    .thenByDescending { it.dataCriacao }
            )
            OpcaoOrdenacao.ALFABETICA -> lista.sortedBy { it.descricao.toLowerCase() }
            OpcaoOrdenacao.MAIS_RECENTE -> lista.sortedByDescending { it.dataCriacao }
        }
    }

    // ####################################################################
    // LÓGICA DE NOTIFICAÇÕES (NOVO)
    // ####################################################################

    private fun scheduleDeadlineNotification(context: Context, tarefa: Tarefa) {
        tarefa.prazo?.let { deadline ->
            val now = System.currentTimeMillis()
            // Notificar 1 hora antes do prazo
            val oneHourBefore = TimeUnit.HOURS.toMillis(1)
            val notificationTime = deadline - oneHourBefore

            // Só agenda se o prazo e o tempo de notificação estiverem no futuro
            if (deadline > now && notificationTime > now) {
                val delay = notificationTime - now

                // 1. Cancela qualquer agendamento anterior (em caso de edição)
                WorkManager.getInstance(context).cancelAllWorkByTag("DEADLINE_${tarefa.id}")

                // 2. Cria a requisição
                val inputData = Data.Builder()
                    .putString("TASK_ID", tarefa.id)
                    .putString("TASK_TITLE", tarefa.descricao)
                    .build()

                val deadlineRequest = OneTimeWorkRequestBuilder<DeadlineNotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("DEADLINE_${tarefa.id}")
                    .build()

                // 3. Enfileira a requisição
                WorkManager.getInstance(context).enqueue(deadlineRequest)
            }
        }
    }

    private fun handleTaskCreated(context: Context, tarefa: Tarefa) {
        val idUnico = tarefa.id.hashCode() + 1
        showImmediateNotification(context, "Tarefa Criada ✅", "A tarefa '${tarefa.descricao}' foi adicionada.", idUnico)
        scheduleDeadlineNotification(context, tarefa)
    }

    private fun handleTaskUpdated(context: Context, tarefa: Tarefa) {
        // Se a tarefa foi concluída, cancelamos a notificação de prazo, se não, reagendamos.
        if (tarefa.concluida) {
            WorkManager.getInstance(context).cancelAllWorkByTag("DEADLINE_${tarefa.id}")
        } else {
            scheduleDeadlineNotification(context, tarefa)
        }
    }

    private fun handleTaskDeleted(context: Context, tarefa: Tarefa) {
        val idUnico = tarefa.id.hashCode() + 2
        showImmediateNotification(context, "Tarefa Excluída 🗑️", "A tarefa '${tarefa.descricao}' foi removida.", idUnico)
        WorkManager.getInstance(context).cancelAllWorkByTag("DEADLINE_${tarefa.id}")
    }

    // ####################################################################
    // MÉTODOS DE PERSISTÊNCIA ATUALIZADOS (REQUEREM CONTEXT)
    // ####################################################################

    private fun adicionarNovaTarefa(context: Context, tarefa: Tarefa) {
        if (databaseRef != null) {
            val taskId = databaseRef.push().key
            if (taskId != null) {
                val tarefaParaSalvar = tarefa.copy(id = taskId)

                val taskMap = hashMapOf<String, Any>(
                    "id" to tarefaParaSalvar.id!!,
                    "descricao" to tarefaParaSalvar.descricao,
                    "concluida" to tarefaParaSalvar.concluida,
                    "iconeResId" to tarefaParaSalvar.iconeResId,
                    "dataCriacao" to tarefaParaSalvar.dataCriacao,
                    "prazo" to (tarefaParaSalvar.prazo ?: "null")
                )

                databaseRef.child(taskId).setValue(taskMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        handleTaskCreated(context, tarefaParaSalvar) // CHAMA NOTIFICAÇÃO
                    }
                }
            }
        }
    }

    fun adicionarTarefa(context: Context, descricao: String, iconeResId: Int = 0, prazo: Long? = null) {
        val timestampAtual = System.currentTimeMillis()
        val novaTarefa = Tarefa(
            descricao = descricao,
            iconeResId = iconeResId,
            dataCriacao = timestampAtual,
            prazo = prazo
        )
        adicionarNovaTarefa(context, novaTarefa)
    }

    fun atualizarStatusTarefa(context: Context, tarefa: Tarefa, estaConcluida: Boolean) {
        if (databaseRef != null && tarefa.id != null) {
            val updates = hashMapOf<String, Any>("concluida" to estaConcluida)
            databaseRef.child(tarefa.id!!).updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val tarefaAtualizada = tarefa.copy(concluida = estaConcluida)
                    handleTaskUpdated(context, tarefaAtualizada) // Trata o reagendamento/cancelamento
                }
            }
        }
    }

    fun deletarTarefa(context: Context, tarefa: Tarefa) {
        if (databaseRef != null && tarefa.id != null) {
            databaseRef.child(tarefa.id!!).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    handleTaskDeleted(context, tarefa) // CHAMA NOTIFICAÇÃO E CANCELA PRAZO
                }
            }
        }
    }

    fun atualizarDescricaoTarefa(context: Context, tarefa: Tarefa) {
        if (databaseRef != null && tarefa.id != null) {
            val updates = hashMapOf<String, Any>(
                "descricao" to tarefa.descricao,
                "iconeResId" to tarefa.iconeResId,
                "prazo" to (tarefa.prazo ?: "null")
            )

            databaseRef.child(tarefa.id!!).updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    handleTaskUpdated(context, tarefa) // REAGENDAMENTO DO PRAZO
                }
            }
        } else {
            Log.w("TarefaViewModel", "Não foi possível atualizar: Referência nula ou ID da tarefa ausente.")
        }
    }
}