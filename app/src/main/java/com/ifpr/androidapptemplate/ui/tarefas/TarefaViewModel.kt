package com.ifpr.androidapptemplate.ui.tarefas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ifpr.androidapptemplate.baseclasses.Tarefa
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

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

                    val iconeResIdValue = (taskMap["iconeResId"] as? Long)?.toInt() ?: 0

                    val tarefa = Tarefa(
                        id = taskSnapshot.key,
                        descricao = taskMap["descricao"] as? String ?: "",
                        concluida = taskMap["concluida"] as? Boolean ?: false,
                        iconeResId = iconeResIdValue,
                        dataCriacao = dataCriacaoValue
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

    fun adicionarNovaTarefa(tarefa: Tarefa) {
        if (databaseRef != null) {
            val taskId = databaseRef.push().key
            if (taskId != null) {
                val tarefaParaSalvar = tarefa.copy(id = taskId)

                val taskMap = hashMapOf<String, Any>(
                    "id" to tarefaParaSalvar.id!!,
                    "descricao" to tarefaParaSalvar.descricao,
                    "concluida" to tarefaParaSalvar.concluida,
                    "iconeResId" to tarefaParaSalvar.iconeResId,
                    "dataCriacao" to tarefaParaSalvar.dataCriacao.toString()
                )

                databaseRef.child(taskId).setValue(taskMap)
            }
        }
    }

    fun adicionarTarefa(descricao: String, iconeResId: Int = 0) {
        val timestampAtual = System.currentTimeMillis()
        val novaTarefa = Tarefa(
            descricao = descricao,
            iconeResId = iconeResId,
            dataCriacao = timestampAtual
        )
        adicionarNovaTarefa(novaTarefa)
    }

    fun atualizarStatusTarefa(tarefa: Tarefa, estaConcluida: Boolean) {
        if (databaseRef != null && tarefa.id != null) {
            val updates = hashMapOf<String, Any>("concluida" to estaConcluida)
            databaseRef.child(tarefa.id!!).updateChildren(updates)
        }
    }

    fun deletarTarefa(tarefa: Tarefa) {
        if (databaseRef != null && tarefa.id != null) {
            databaseRef.child(tarefa.id!!).removeValue()
        }
    }

    fun atualizarDescricaoTarefa(tarefa: Tarefa) {
        if (databaseRef != null && tarefa.id != null) {
            val updates = hashMapOf<String, Any>(
                "descricao" to tarefa.descricao,
                "iconeResId" to tarefa.iconeResId
            )

            databaseRef.child(tarefa.id!!).updateChildren(updates)
        } else {
            Log.w("TarefaViewModel", "Não foi possível atualizar: Referência nula ou ID da tarefa ausente.")
        }
    }
}