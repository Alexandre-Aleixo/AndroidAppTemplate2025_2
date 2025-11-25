package com.ifpr.androidapptemplate.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatarDataCriacao(): String {
    if (this == 0L) {
        return "Data Desconhecida"
    }

    val formatador = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val data = Date(this)
    return formatador.format(data)
}