package com.ifpr.androidapptemplate.ui.tarefas

import com.ifpr.androidapptemplate.baseclasses.Tarefa
import android.content.Context

interface TarefaAcoesListener {
    fun onStatusAlterado(context: Context, tarefa: Tarefa, estaConcluida: Boolean)

    fun onDeletarTarefa(tarefa: Tarefa)
}