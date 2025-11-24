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

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tarefas = mutableListOf<Tarefa>()
            for (taskSnapshot in snapshot.children) {
                val tarefa = taskSnapshot.getValue(Tarefa::class.java)
                tarefa?.let {
                    val tarefaComId = it.copy(id = taskSnapshot.key)
                    tarefas.add(tarefaComId)
                }
            }
            _listaTarefas.value = tarefas.sortedBy { it.concluida }
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

    fun adicionarNovaTarefa(tarefa: Tarefa) {
        if (databaseRef != null) {
            val taskId = databaseRef.push().key
            if (taskId != null) {
                val novaTarefa = tarefa.copy(id = taskId)
                databaseRef.child(taskId).setValue(novaTarefa)
            }
        }
    }

    fun adicionarTarefa(descricao: String, iconeResId: Int = 0) {
        val novaTarefa = Tarefa(descricao = descricao, iconeResId = iconeResId)
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