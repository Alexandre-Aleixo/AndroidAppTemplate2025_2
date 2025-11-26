package com.ifpr.androidapptemplate.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.NotificationData
import com.ifpr.androidapptemplate.baseclasses.NotificationType
import com.ifpr.androidapptemplate.utils.formatarDataCriacao // Assumindo esta função de extensão
import java.util.Date

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    var listaNotificacoes: List<NotificationData> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconType: ImageView = itemView.findViewById(R.id.icon_notification_type)
        val title: TextView = itemView.findViewById(R.id.text_notification_title)
        val message: TextView = itemView.findViewById(R.id.text_notification_message)
        val timestamp: TextView = itemView.findViewById(R.id.text_notification_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = listaNotificacoes[position]
        val context = holder.itemView.context

        holder.title.text = notification.title
        holder.message.text = notification.message

        val formattedDate = notification.timestamp.formatarDataCriacao()
        holder.timestamp.text = "Recebido em $formattedDate"

        when (notification.type) {
            NotificationType.DEADLINE -> {
                holder.iconType.setImageResource(R.drawable.ic_warning_24)
                holder.iconType.setColorFilter(ContextCompat.getColor(context, R.color.red_delete))
            }
            NotificationType.IMMEDIATE -> {
                holder.iconType.setImageResource(R.drawable.ic_notifications_black_24dp)
                holder.iconType.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
            }
        }
    }

    override fun getItemCount(): Int = listaNotificacoes.size
}