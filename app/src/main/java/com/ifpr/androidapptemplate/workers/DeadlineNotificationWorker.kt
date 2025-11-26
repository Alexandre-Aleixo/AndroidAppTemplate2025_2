package com.ifpr.androidapptemplate.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ifpr.androidapptemplate.R
// Importe o objeto que contém a constante
import com.ifpr.androidapptemplate.utils.NotificationHelper

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
        // NOVO: Recebe o tipo de notificação agendada
        val notificationType = inputData.getString("NOTIFICATION_TYPE") ?: return Result.failure()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        // Usa o hash do ID da tarefa como ID único da notificação
        val notificationId = taskId.hashCode()

        // ####################################################################
        // LÓGICA DE MENSAGEM DINÂMICA
        // ####################################################################
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
            else -> return Result.failure() // Tipo desconhecido
        }
        // ####################################################################


        // Usa NotificationHelper.CHANNEL_ID para resolver a referência
        val builder = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Use um ícone adequado
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(icon) // Usa a prioridade definida na lógica
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())

        return Result.success()
    }
}