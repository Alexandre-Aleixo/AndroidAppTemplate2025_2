package com.ifpr.androidapptemplate.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.data.repository.NotificationRepository // NOVA IMPORTAÇÃO
import com.ifpr.androidapptemplate.baseclasses.NotificationType // NOVA IMPORTAÇÃO

object NotificationHelper {

    const val CHANNEL_ID = "tarefa_channel"
    private const val CHANNEL_NAME = "Notificações de Tarefas"

    // NOVO: Instância do repositório para salvar no Firebase
    private val repository = NotificationRepository()

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
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // *** NOVO: SALVA a notificação no Firebase (In-App) ***
        repository.saveNotification(title, message, NotificationType.IMMEDIATE)

        // Dispara a notificação
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    /**
     * Exibe o alerta de prazo que foi agendado pelo WorkManager.
     * (Esta função precisa ser adicionada, pois é chamada pelo DeadlineWorker)
     */
    fun showDeadlineNotification(context: Context, title: String, message: String) {
        val notificationId = System.currentTimeMillis().toInt()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning_24) // Sugestão: Ícone diferente para alerta de prazo
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridade alta para prazo
            .setAutoCancel(true)

        // *** NOVO: SALVA a notificação de prazo no Firebase (In-App) ***
        repository.saveNotification(title, message, NotificationType.DEADLINE)

        // Dispara a notificação
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}