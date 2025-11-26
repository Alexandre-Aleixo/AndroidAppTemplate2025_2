package com.ifpr.androidapptemplate.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.utils.NotificationHelper

class DeadlineNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString("TASK_ID") ?: return Result.failure()
        val taskTitle = inputData.getString("TASK_TITLE") ?: return Result.failure()
        val notificationType = inputData.getString("NOTIFICATION_TYPE") ?: return Result.failure()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val notificationId = taskId.hashCode()

        val (title, text, icon) = when (notificationType) {
            "APPROACHING" -> Triple(
                "Prazo Final Próximo! 🚨",
                "A tarefa '$taskTitle' tem apenas 1 hora restante para o prazo!",
                NotificationCompat.PRIORITY_HIGH
            )
            "DEADLINE_MISSED" -> Triple(
                "Prazo Finalizado! 🔔",
                "O prazo da tarefa '$taskTitle' expirou. Verifique o status.",
                NotificationCompat.PRIORITY_DEFAULT
            )
            else -> return Result.failure()
        }

        val builder = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(icon)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())

        return Result.success()
    }
}