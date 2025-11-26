package com.ifpr.androidapptemplate.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.utils.CHANNEL_ID

/**
 * Worker responsável por disparar a notificação de prazo final.
 * É agendado pelo ViewModel para rodar em um horário específico.
 */
class DeadlineNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Recebe os dados da tarefa que foram passados no agendamento
        val taskId = inputData.getString("TASK_ID") ?: return Result.failure()
        val taskTitle = inputData.getString("TASK_TITLE") ?: return Result.failure()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        // Usa o hash do ID da tarefa como ID único da notificação
        val notificationId = taskId.hashCode()

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Use um ícone adequado
            .setContentTitle("Prazo Final Próximo! 🚨")
            .setContentText("A tarefa '$taskTitle' tem o prazo final em breve.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())

        return Result.success()
    }
}