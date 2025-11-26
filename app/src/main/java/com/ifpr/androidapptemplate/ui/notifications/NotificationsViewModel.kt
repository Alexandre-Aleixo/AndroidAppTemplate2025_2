package com.ifpr.androidapptemplate.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ifpr.androidapptemplate.baseclasses.NotificationData
import com.ifpr.androidapptemplate.data.repository.NotificationRepository

class NotificationsViewModel : ViewModel() {

    private val repository = NotificationRepository()


    val notificationList: LiveData<List<NotificationData>> = repository.getNotifications()

}