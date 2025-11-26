package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class NotificationData(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: NotificationType = NotificationType.IMMEDIATE
)

enum class NotificationType {
    IMMEDIATE,
    DEADLINE
}