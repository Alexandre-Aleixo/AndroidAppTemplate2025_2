package com.ifpr.androidapptemplate.ui.tarefas

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Tarefa

interface TarefaAcoesListener {
    fun onStatusAlterado(tarefa: Tarefa, estaConcluida: Boolean)
}

class TarefaAdapter(private val listener: TarefaAcoesListener) :
    RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder>() {

    private var listaTarefas: List<Tarefa> = emptyList()

    class TarefaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descricao: TextView = itemView.findViewById(R.id.text_tarefa_descricao)
        val checkboxConcluida: CheckBox = itemView.findViewById(R.id.checkbox_tarefa_concluida)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarefaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarefa, parent, false)
        return TarefaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarefaViewHolder, position: Int) {
        val tarefaAtual = listaTarefas[position]

        holder.descricao.text = tarefaAtual.descricao

        holder.checkboxConcluida.setOnCheckedChangeListener(null)
        holder.checkboxConcluida.isChecked = tarefaAtual.concluida

        aplicarEstiloTexto(holder.descricao, tarefaAtual.concluida)

        holder.checkboxConcluida.setOnCheckedChangeListener { _, estaConcluida ->
            listener.onStatusAlterado(tarefaAtual, estaConcluida)
        }
    }

    override fun getItemCount(): Int = listaTarefas.size

    fun atualizarLista(novaLista: List<Tarefa>) {
        listaTarefas = novaLista
        notifyDataSetChanged()
    }

    private fun aplicarEstiloTexto(textView: TextView, estaConcluida: Boolean) {
        if (estaConcluida) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}