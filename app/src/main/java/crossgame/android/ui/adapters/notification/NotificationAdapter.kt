package crossgame.android.ui.adapters.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.databinding.CardNotificationFriendshipBinding
import crossgame.android.domain.models.notifications.Notification
import java.time.format.DateTimeFormatter

class NotificationAdapter(private val context: Context, private val notificationList: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>()  {

    class NotificationViewHolder(private val binding: CardNotificationFriendshipBinding) : RecyclerView.ViewHolder(binding.root) {
        val notificationDate = binding.date
        val notificationMessage = binding.message
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = CardNotificationFriendshipBinding.inflate(LayoutInflater.from(context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val currentNotification = notificationList[position]
        holder.notificationMessage.text = currentNotification.message
        holder.notificationDate.text = currentNotification.date.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}