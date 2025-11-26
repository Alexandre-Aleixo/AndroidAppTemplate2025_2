package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Representa um registro de notificação salvo no Firebase para exibição no app.
 */
@IgnoreExtraProperties
data class NotificationData(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: NotificationType = NotificationType.IMMEDIATE // Tipo de alerta gerado
)

/**
 * Define o tipo de evento que gerou a notificação.
 */
enum class NotificationType {
    IMMEDIATE, // Alerta imediato (Criação, Exclusão, etc.)
    DEADLINE   // Alerta de prazo agendado pelo WorkManager
}