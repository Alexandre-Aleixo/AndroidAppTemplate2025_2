package com.ifpr.androidapptemplate.ui.tarefas

import com.ifpr.androidapptemplate.R

data class IconeOpcao(
    val id: Int,
    val drawableResId: Int,
    val nome: String
)

object IconeHelper {

    val iconesDisponiveis = listOf(
        IconeOpcao(id = 0, drawableResId = 0, nome = "Sem Ícone"),
        IconeOpcao(id = 1, drawableResId = R.drawable.ic_home_black_24dp, nome = "Casa"),
        IconeOpcao(id = 2, drawableResId = R.drawable.ic_work_black_24dp, nome = "Trabalho"),
        IconeOpcao(id = 3, drawableResId = R.drawable.ic_school_black_24dp, nome = "Estudo"),
        IconeOpcao(id = 4, drawableResId = R.drawable.ic_shopping_cart_black_24dp, nome = "Compras"),
        IconeOpcao(id = 5, drawableResId = R.drawable.ic_event_black_24dp, nome = "Evento")
    )

    fun getDrawableResId(id: Int): Int {
        return iconesDisponiveis.find { it.id == id }?.drawableResId ?: 0
    }
}