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

    // Lista de dados de notificações, atualizada via setter
    var listaNotificacoes: List<NotificationData> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged() // Notifica a RecyclerView para redesenhar
        }

    // ViewHolder: Mapeia os componentes do layout item_notification.xml
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

        // Usa a função de extensão de data criada no pacote utils
        val formattedDate = notification.timestamp.formatarDataCriacao()
        holder.timestamp.text = "Recebido em $formattedDate"

        // Configura o ícone e a cor baseados no tipo de notificação
        when (notification.type) {
            NotificationType.DEADLINE -> {
                // Necessário ter R.drawable.ic_warning_24 e R.color.red_delete no projeto
                holder.iconType.setImageResource(R.drawable.ic_warning_24)
                holder.iconType.setColorFilter(ContextCompat.getColor(context, R.color.red_delete))
            }
            NotificationType.IMMEDIATE -> {
                // Necessário ter R.drawable.ic_notifications_black_24dp
                holder.iconType.setImageResource(R.drawable.ic_notifications_black_24dp)
                holder.iconType.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
            }
        }
    }

    override fun getItemCount(): Int = listaNotificacoes.size
}