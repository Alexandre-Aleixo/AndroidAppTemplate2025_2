package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.PropertyName
import java.util.concurrent.TimeUnit

data class Tarefa(

    var id: String? = null,
    val descricao: String = "",
    val concluida: Boolean = false,
    val iconeResId: Int = 0,

    @get:PropertyName("dataCriacao")
    val dataCriacao: Long = 0L
)