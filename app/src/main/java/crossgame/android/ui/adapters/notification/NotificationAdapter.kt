package crossgame.android.ui.adapters.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.databinding.CardNotificationFriendshipBinding
import crossgame.android.application.databinding.CardNotificationGroupBinding
import crossgame.android.domain.models.notifications.Notification
import crossgame.android.domain.models.notifications.NotificationType
import java.time.format.DateTimeFormatter

class NotificationAdapter(
    private val context: Context,
    private val notificationList: List<Notification>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class FriendshipViewHolder(private val binding: CardNotificationFriendshipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val notificationDate = binding.date
        val notificationMessage = binding.message
        val acceptInviteButton: ImageButton = binding.acceptInvite
    }

    inner class GroupViewHolder(private val binding: CardNotificationGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
//        val notificationDate = binding
//        val notificationMessage = binding.message
//        val joinGroupButton: ImageButton = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NotificationType.FRIEND_REQUEST.ordinal ->
                FriendshipViewHolder(
                    CardNotificationFriendshipBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            NotificationType.GROUP_INVITE.ordinal ->
                GroupViewHolder(
                    CardNotificationGroupBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentNotification = notificationList[position]

        when (holder) {
            is FriendshipViewHolder -> {
                // Configurar o layout para notificação de amizade
                holder.notificationMessage.text = currentNotification.message
                holder.notificationDate.text =
                    currentNotification.date.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
                // Configurar o clique do botão
                holder.acceptInviteButton.setOnClickListener {
                    // Lógica para clique de botão de amizade
                }
            }
//            is GroupViewHolder -> {
//                // Configurar o layout para convite para grupo
//                holder.notificationMessage.text = currentNotification.message
//                holder.notificationDate.text =
//                    currentNotification.date.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
//                // Configurar o clique do botão
//                holder.joinGroupButton.setOnClickListener {
//                    // Lógica para clique de botão de convite para grupo
//                }
//            }
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
//
//    override fun getItemViewType(position: Int): Int {
//        return notificationList[position].type.ordinal
//    }
}
