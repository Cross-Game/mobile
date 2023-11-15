import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.databinding.CardNotificationFriendshipBinding
import crossgame.android.application.databinding.CardNotificationGroupBinding
import crossgame.android.domain.models.notifications.Notification
import crossgame.android.domain.models.notifications.NotificationType
import java.time.format.DateTimeFormatter

class NotificationAdapter(private val context: Context, private val notificationList: List<Notification>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class NotificationFriendViewHolder(private val binding: CardNotificationFriendshipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val notificationDate = binding.date
        val notificationMessage = binding.message
    }

    inner class NotificationGroupViewHolder(private val binding: CardNotificationGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val notificationGroupDate = binding.date
        val notificationGroupMessage = binding.notificationGroupText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NotificationType.FRIEND_REQUEST.ordinal -> {
                val binding = CardNotificationFriendshipBinding.inflate(LayoutInflater.from(context), parent, false)
                NotificationFriendViewHolder(binding)
            }
            NotificationType.GROUP_INVITE.ordinal -> {
                val binding = CardNotificationGroupBinding.inflate(LayoutInflater.from(context), parent, false)
                NotificationGroupViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentNotification = notificationList[position]

        when (holder.itemViewType) {
            NotificationType.FRIEND_REQUEST.ordinal -> {
                val friendHolder = holder as NotificationFriendViewHolder
                friendHolder.notificationMessage.text = currentNotification.message
                friendHolder.notificationDate.text = currentNotification.date.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
            }
            NotificationType.GROUP_INVITE.ordinal -> {
                val groupHolder = holder as NotificationGroupViewHolder
                groupHolder.notificationGroupMessage.text = currentNotification.message
                groupHolder.notificationGroupDate.text = currentNotification.date.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
            }
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun getItemViewType(position: Int): Int {
        return notificationList[position].notificatioType.ordinal
    }
}
