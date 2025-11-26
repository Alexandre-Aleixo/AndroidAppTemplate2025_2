package com.ifpr.androidapptemplate.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.data.repository.NotificationRepository
import com.ifpr.androidapptemplate.baseclasses.NotificationType

object NotificationHelper {

    const val CHANNEL_ID = "tarefa_channel"
    private const val CHANNEL_NAME = "Notificações de Tarefas"

    private val repository = NotificationRepository()

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerta para prazo final e mudanças nas tarefas."
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showImmediateNotification(context: Context, title: String, message: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        repository.saveNotification(title, message, NotificationType.IMMEDIATE)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun showDeadlineNotification(context: Context, title: String, message: String) {
        val notificationId = System.currentTimeMillis().toInt()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        repository.saveNotification(title, message, NotificationType.DEADLINE)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}