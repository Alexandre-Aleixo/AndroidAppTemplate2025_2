package com.ifpr.androidapptemplate.baseclasses

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tarefa(
    var id: String? = null,
    val descricao: String = "",
    val concluida: Boolean = false,
    val iconeResId: Int = 0,

    @get:PropertyName("dataCriacao")
    val dataCriacao: Long = 0L
) : Parcelable