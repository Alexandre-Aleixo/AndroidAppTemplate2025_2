package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.Exclude

data class Tarefa(
    @get:Exclude var id: String? = null,
    val descricao: String = "",
    val concluida: Boolean = false,
    val iconeResId: Int = 0
)