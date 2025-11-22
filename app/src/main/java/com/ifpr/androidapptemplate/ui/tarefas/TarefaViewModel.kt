package com.ifpr.androidapptemplate.ui.tarefas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ifpr.androidapptemplate.baseclasses.Tarefa
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

class TarefaViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String? = auth.currentUser?.uid

    private val databaseRef: DatabaseReference? = currentUserId?.let { uid ->
        FirebaseDatabase.getInstance().getReference("users")
            .child(uid)
            .child("tarefas") // <-- Nó de tarefas dentro do usuário
    }

    private val _listaTarefas = MutableLiveData<List<Tarefa>>()
    val listaTarefas: LiveData<List<Tarefa>> = _listaTarefas

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tarefas = mutableListOf<Tarefa>()
            for (taskSnapshot in snapshot.children) {
                val tarefa = taskSnapshot.getValue(Tarefa::class.java)
                tarefa?.let { tarefas.add(it) }
            }
            _listaTarefas.value = tarefas
        }

        override fun onCancelled(error: DatabaseError) {
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

    fun adicionarTarefa(descricao: String) {
        if (databaseRef != null) {
            val taskId = databaseRef.push().key
            if (taskId != null) {
                val novaTarefa = Tarefa(id = taskId, descricao = descricao)
                databaseRef.child(taskId).setValue(novaTarefa)
            }
        }
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
}