package com.ifpr.androidapptemplate.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.baseclasses.NotificationData
import com.ifpr.androidapptemplate.baseclasses.NotificationType
import com.ifpr.androidapptemplate.data.UserAuth
import java.util.UUID

class NotificationRepository {

    private val db = FirebaseDatabase.getInstance()
    private val userId = UserAuth.currentUserId

    private fun getNotificationsRef() = userId?.let {
        db.getReference("users").child(it).child("notifications")
    }


    fun saveNotification(title: String, message: String, type: NotificationType) {
        val ref = getNotificationsRef() ?: return
        val notificationId = UUID.randomUUID().toString()

        val notification = NotificationData(
            id = notificationId,
            title = title,
            message = message,
            type = type
        )

        ref.child(notificationId).setValue(notification)
    }


    fun getNotifications(): LiveData<List<NotificationData>> {
        val liveData = MutableLiveData<List<NotificationData>>()
        val ref = getNotificationsRef() ?: return liveData

        ref.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = snapshot.children.mapNotNull { child ->
                    child.getValue(NotificationData::class.java)
                }.sortedByDescending { it.timestamp }

                liveData.value = notifications
            }

            override fun onCancelled(error: DatabaseError) {
                liveData.value = emptyList()
            }
        })
        return liveData
    }
}