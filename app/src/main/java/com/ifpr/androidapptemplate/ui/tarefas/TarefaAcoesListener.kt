package com.ifpr.androidapptemplate.ui.tarefas

import com.ifpr.androidapptemplate.baseclasses.Tarefa
// import android.content.Context <-- REMOVIDO, pois não é mais usado na interface

interface TarefaAcoesListener {
    // CORREÇÃO: Context removido da assinatura!
    fun onStatusAlterado(tarefa: Tarefa, estaConcluida: Boolean)

    // CORREÇÃO: Context deve ser removido daqui também se o ViewModel não usa.
    fun onDeletarTarefa(tarefa: Tarefa)
}