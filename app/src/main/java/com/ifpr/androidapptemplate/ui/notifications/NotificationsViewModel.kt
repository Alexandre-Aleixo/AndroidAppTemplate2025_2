package com.ifpr.androidapptemplate.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ifpr.androidapptemplate.baseclasses.NotificationData
import com.ifpr.androidapptemplate.data.repository.NotificationRepository

class NotificationsViewModel : ViewModel() {

    private val repository = NotificationRepository()

    /**
     * LiveData que observa a lista de notificações do Firebase em tempo real.
     * O repositório já cuida da busca e ordenação das mais recentes.
     */
    val notificationList: LiveData<List<NotificationData>> = repository.getNotifications()

    // Não precisamos de lógica complexa aqui, apenas expomos os dados.
}