package com.ifpr.androidapptemplate.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TIMESTAMP_DESCONHECIDO = 0L
private const val FORMATO_DATA_HORA_CRIACAO = "dd/MM/yyyy HH:mm"
private const val FORMATO_DATA_HORA_PRAZO = "dd/MM/yyyy às HH:mm"


fun Long.formatarDataCriacao(): String {
    return if (this == TIMESTAMP_DESCONHECIDO) {
        "Data Desconhecida"
    } else {
        val formatter = SimpleDateFormat(FORMATO_DATA_HORA_CRIACAO, Locale.getDefault())
        formatter.format(Date(this))
    }
}

fun Long.formatarPrazoParaExibicao(): String {
    val formatter = SimpleDateFormat(FORMATO_DATA_HORA_PRAZO, Locale.getDefault())
    return formatter.format(Date(this))
}