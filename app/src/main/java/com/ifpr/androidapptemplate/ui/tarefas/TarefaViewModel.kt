package com.ifpr.androidapptemplate.ui.tarefas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ifpr.androidapptemplate.workers.DeadlineNotificationWorker
import com.ifpr.androidapptemplate.utils.NotificationHelper
import com.ifpr.androidapptemplate.baseclasses.Tarefa
import com.ifpr.androidapptemplate.data.UserAuth
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit
import com.ifpr.androidapptemplate.ui.tarefas.OpcaoOrdenacao
import androidx.lifecycle.MediatorLiveData // NOVA IMPORTAÇÃO ESSENCIAL

class TarefaViewModel(application: Application) : AndroidViewModel(application) {

    private val currentUserId: String? = UserAuth.currentUserId

    private val databaseRef: DatabaseReference? = currentUserId?.let { uid ->
        FirebaseDatabase.getInstance().getReference("users")
            .child(uid)
            .child("tarefas")
    }

    // ####################################################################
    // MUDANÇAS PARA BUSCA E FILTRO
    // ####################################################################

    // 1. LiveData que armazena TODAS as tarefas do Firebase (Lista bruta)
    private val _todasAsTarefas = MutableLiveData<List<Tarefa>>()

    // 2. LiveData para armazenar o texto de busca
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    // 3. MediatorLiveData: Combina _todasAsTarefas, _searchQuery e _opcaoOrdenacao
    private val _listaTarefas = MediatorLiveData<List<Tarefa>>()
    val listaTarefas: LiveData<List<Tarefa>> = _listaTarefas


    // Lógica de Ordenação
    private val _opcaoOrdenacao = MutableLiveData(OpcaoOrdenacao.STATUS)
    val opcaoOrdenacao: LiveData<OpcaoOrdenacao> = _opcaoOrdenacao

    // ####################################################################
    // valueEventListener - ALIMENTA _todasAsTarefas
    // ####################################################################
    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tarefas = mutableListOf<Tarefa>()
            for (taskSnapshot in snapshot.children) {
                // ... (Sua lógica de parsing de tarefas) ...
                val taskMap = taskSnapshot.value as? HashMap<String, Any>

                if (taskMap != null) {
                    val dataCriacaoAny = taskMap["dataCriacao"]
                    val dataCriacaoValue: Long = when (dataCriacaoAny) {
                        is String -> dataCriacaoAny.toLongOrNull() ?: 0L
                        is Long -> dataCriacaoAny
                        else -> 0L
                    }

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
            // MUDANÇA: Alimenta _todasAsTarefas. O MediatorLiveData recalcula _listaTarefas.
            _todasAsTarefas.value = tarefas
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("TarefaViewModel", "Falha na leitura do Firebase: ${error.message}")
        }
    }

    // ####################################################################
    // init - CONFIGURAÇÃO DO MEDIATORLIVE DATA
    // ####################################################################
    init {
        databaseRef?.addValueEventListener(valueEventListener)
        if (currentUserId == null) {
            _todasAsTarefas.value = emptyList()
        }

        // Configura o MediatorLiveData para reagir a TUDO:
        // 1. Quando o Firebase (via _todasAsTarefas) muda
        _listaTarefas.addSource(_todasAsTarefas) { aplicarFiltroEOrdenacao() }
        // 2. Quando o usuário muda o texto de busca
        _listaTarefas.addSource(_searchQuery) { aplicarFiltroEOrdenacao() }
        // 3. Quando o usuário muda a ordenação
        _listaTarefas.addSource(_opcaoOrdenacao) { aplicarFiltroEOrdenacao() }
    }

    // ... (onCleared inalterado)

    override fun onCleared() {
        super.onCleared()
        databaseRef?.removeEventListener(valueEventListener)
    }

    // ####################################################################
    // setOrdenacao e NOVO setSearchQuery
    // ####################################################################

    // NOVO: Método para o Fragment chamar quando o texto de busca mudar
    fun setSearchQuery(query: String) {
        // Normaliza para lowercase e remove espaços em branco (trim) para a busca
        val newQuery = query.trim()
        if (_searchQuery.value != newQuery) {
            _searchQuery.value = newQuery
        }
    }

    // Método setOrdenacao (Inalterado, mas agora dispara o MediatorLiveData)
    fun setOrdenacao(opcao: OpcaoOrdenacao) {
        if (_opcaoOrdenacao.value != opcao) {
            _opcaoOrdenacao.value = opcao
        }
    }

    // ####################################################################
    // Lógica de Filtro e Ordenação (NOVA CENTRALIZAÇÃO)
    // ####################################################################

    private fun aplicarFiltroEOrdenacao() {
        val query = _searchQuery.value ?: ""
        val todas = _todasAsTarefas.value ?: emptyList()
        val ordenacao = _opcaoOrdenacao.value ?: OpcaoOrdenacao.STATUS

        // 1. APLICA O FILTRO DE BUSCA
        val tarefasFiltradas = if (query.isBlank()) {
            todas
        } else {
            // Filtra pela descrição, ignorando maiúsculas/minúsculas
            todas.filter {
                it.descricao.contains(query, ignoreCase = true)
            }
        }

        // 2. APLICA A ORDENAÇÃO (reutiliza o método existente)
        _listaTarefas.value = aplicarOrdenacao(tarefasFiltradas, ordenacao)
    }

    // Lógica de Aplicação da Ordenação (método existente)
    private fun aplicarOrdenacao(tarefas: List<Tarefa>, opcao: OpcaoOrdenacao): List<Tarefa> {
        return when (opcao) {
            OpcaoOrdenacao.STATUS -> tarefas.sortedWith(
                compareBy<Tarefa> { it.concluida }
                    .thenByDescending { it.dataCriacao }
            )
            OpcaoOrdenacao.ALFABETICA -> tarefas.sortedBy { it.descricao }
            OpcaoOrdenacao.MAIS_RECENTE -> tarefas.sortedByDescending { it.dataCriacao }
        }
    }


    // ####################################################################
    // LÓGICA DE NOTIFICAÇÕES E PERSISTÊNCIA (Inalteradas, mas mantidas aqui)
    // ####################################################################

    private fun scheduleDeadlineNotification(tarefa: Tarefa) {
        tarefa.prazo?.let { deadline ->
            val now = System.currentTimeMillis()
            val oneHourBefore = TimeUnit.HOURS.toMillis(1)
            val notificationTime = deadline - oneHourBefore

            if (deadline > now && notificationTime > now) {
                val delay = notificationTime - now

                WorkManager.getInstance(getApplication()).cancelAllWorkByTag("DEADLINE_${tarefa.id}")

                val inputData = Data.Builder()
                    .putString("TASK_ID", tarefa.id)
                    .putString("TASK_TITLE", tarefa.descricao)
                    .build()

                val deadlineRequest = OneTimeWorkRequestBuilder<DeadlineNotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("DEADLINE_${tarefa.id}")
                    .build()

                WorkManager.getInstance(getApplication()).enqueue(deadlineRequest)
            }
        }
    }

    private fun handleTaskCreated(tarefa: Tarefa) {
        val idUnico = tarefa.id.hashCode() + 1
        NotificationHelper.showImmediateNotification(getApplication(), "Tarefa Criada ✅", "A tarefa '${tarefa.descricao}' foi adicionada.", idUnico)
        scheduleDeadlineNotification(tarefa)
    }

    private fun handleTaskUpdated(tarefa: Tarefa) {
        if (tarefa.concluida) {
            WorkManager.getInstance(getApplication()).cancelAllWorkByTag("DEADLINE_${tarefa.id}")
        } else {
            scheduleDeadlineNotification(tarefa)
        }
    }

    private fun handleTaskDeleted(tarefa: Tarefa) {
        val idUnico = tarefa.id.hashCode() + 2
        NotificationHelper.showImmediateNotification(getApplication(), "Tarefa Excluída 🗑️", "A tarefa '${tarefa.descricao}' foi removida.", idUnico)
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag("DEADLINE_${tarefa.id}")
    }

    private fun adicionarNovaTarefa(tarefa: Tarefa) {
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
                        handleTaskCreated(tarefaParaSalvar)
                    }
                }
            }
        }
    }

    fun adicionarTarefa(descricao: String, iconeResId: Int = 0, prazo: Long? = null) {
        val timestampAtual = System.currentTimeMillis()
        val novaTarefa = Tarefa(
            descricao = descricao,
            iconeResId = iconeResId,
            dataCriacao = timestampAtual,
            prazo = prazo
        )
        adicionarNovaTarefa(novaTarefa)
    }

    fun atualizarStatusTarefa(tarefa: Tarefa, estaConcluida: Boolean) {
        if (databaseRef != null && tarefa.id != null) {
            val updates = hashMapOf<String, Any>("concluida" to estaConcluida)
            databaseRef.child(tarefa.id!!).updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val tarefaAtualizada = tarefa.copy(concluida = estaConcluida)
                    handleTaskUpdated(tarefaAtualizada)
                }
            }
        }
    }

    fun deletarTarefa(tarefa: Tarefa) {
        if (databaseRef != null && tarefa.id != null) {
            databaseRef.child(tarefa.id!!).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    handleTaskDeleted(tarefa)
                }
            }
        }
    }

    fun atualizarDescricaoTarefa(tarefa: Tarefa) {
        if (databaseRef != null && tarefa.id != null) {
            val updates = hashMapOf<String, Any>(
                "descricao" to tarefa.descricao,
                "iconeResId" to tarefa.iconeResId,
                "prazo" to (tarefa.prazo ?: "null")
            )

            databaseRef.child(tarefa.id!!).updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    handleTaskUpdated(tarefa)
                }
            }
        } else {
            Log.w("TarefaViewModel", "Não foi possível atualizar: Referência nula ou ID da tarefa ausente.")
        }
    }
}