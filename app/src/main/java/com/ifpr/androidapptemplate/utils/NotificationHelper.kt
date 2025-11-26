package com.ifpr.androidapptemplate.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ifpr.androidapptemplate.R

const val CHANNEL_ID = "tarefa_channel"
const val CHANNEL_NAME = "Notificações de Tarefas"

/**
 * Cria o canal de notificação que é obrigatório a partir do Android O (Oreo, API 26).
 * Deve ser chamado na inicialização do aplicativo (ex: no onCreate da MainActivity).
 */
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT // Define a importância da notificação
        ).apply {
            description = "Alerta para prazo final e mudanças nas tarefas."
        }

        // Registra o canal no sistema
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * Exibe uma notificação imediata (usada para eventos de Criação/Exclusão).
 */
fun showImmediateNotification(context: Context, title: String, message: String, notificationId: Int) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        // **IMPORTANTE:** Você precisa ter um recurso de ícone em seu drawable (ex: ic_notification)
        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    // Dispara a notificação
    NotificationManagerCompat.from(context).notify(notificationId, builder.build())
}