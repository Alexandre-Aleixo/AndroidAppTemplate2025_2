package com.ifpr.androidapptemplate.ui.tarefas

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Tarefa


interface TarefaAcoesListener {
    fun onStatusAlterado(tarefa: Tarefa, estaConcluida: Boolean)
}

typealias OnTarefaClickListener = (Tarefa) -> Unit

class TarefaAdapter(
    private val listener: TarefaAcoesListener,
    private val clickListener: OnTarefaClickListener
) : RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder>() {

    var listaTarefas: List<Tarefa> = emptyList()

    class TarefaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descricao: TextView = itemView.findViewById(R.id.text_tarefa_descricao)
        val checkboxConcluida: CheckBox = itemView.findViewById(R.id.checkbox_tarefa_concluida)
        val iconeCategoria: ImageView = itemView.findViewById(R.id.image_view_icone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarefaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarefa, parent, false)
        return TarefaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarefaViewHolder, position: Int) {
        val tarefaAtual = listaTarefas[position]
        val context = holder.itemView.context

        holder.descricao.text = tarefaAtual.descricao

        if (tarefaAtual.iconeResId != 0) {
            holder.iconeCategoria.setImageResource(tarefaAtual.iconeResId)
            holder.iconeCategoria.visibility = View.VISIBLE
            aplicarCorDinamica(holder.descricao, holder.iconeCategoria, tarefaAtual.iconeResId, tarefaAtual.concluida)
        } else {
            holder.iconeCategoria.visibility = View.GONE
            aplicarCorDinamica(holder.descricao, holder.iconeCategoria, 0, tarefaAtual.concluida)
        }

        holder.checkboxConcluida.setOnCheckedChangeListener(null)
        holder.checkboxConcluida.isChecked = tarefaAtual.concluida
        aplicarEstiloTexto(holder.descricao, tarefaAtual.concluida)

        holder.checkboxConcluida.setOnCheckedChangeListener { _, estaConcluida ->
            listener.onStatusAlterado(tarefaAtual, estaConcluida)
        }

        holder.itemView.setOnClickListener {
            clickListener(tarefaAtual)
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

    private fun aplicarCorDinamica(
        descricaoView: TextView,
        iconeView: ImageView,
        iconeResId: Int,
        estaConcluida: Boolean
    ) {
        val context = descricaoView.context
        val corResId = CorHelper.getColorResId(iconeResId)
        val corFinal = ContextCompat.getColor(context, corResId)

        if (estaConcluida) {
            val corCinza = ContextCompat.getColor(context, R.color.gray_light)
            descricaoView.setTextColor(corCinza)
            ImageViewCompat.setImageTintList(iconeView, ContextCompat.getColorStateList(context, R.color.gray_light))
        } else {
            descricaoView.setTextColor(corFinal)
            ImageViewCompat.setImageTintList(iconeView, ContextCompat.getColorStateList(context, corResId))
        }
    }
}