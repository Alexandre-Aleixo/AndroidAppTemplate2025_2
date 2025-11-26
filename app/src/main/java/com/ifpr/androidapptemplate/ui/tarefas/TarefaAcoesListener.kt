package com.ifpr.androidapptemplate.ui.tarefas

import com.ifpr.androidapptemplate.baseclasses.Tarefa


interface TarefaAcoesListener {

    fun onStatusAlterado(tarefa: Tarefa, estaConcluida: Boolean)


    fun onDeletarTarefa(tarefa: Tarefa)
}