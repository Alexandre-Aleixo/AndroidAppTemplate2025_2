package com.ifpr.androidapptemplate.baseclasses

import java.util.concurrent.TimeUnit

data class Tarefa(

    var id: String? = null,
    val descricao: String = "",
    val concluida: Boolean = false,
    val iconeResId: Int = 0,
    val dataCriacao: Long = System.currentTimeMillis()
)