import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.databinding.CardNotificationFriendshipBinding
import crossgame.android.application.databinding.CardNotificationGroupBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.notifications.NotificationResponse
import crossgame.android.domain.models.notifications.NotificationState
import crossgame.android.domain.models.notifications.NotificationType
import crossgame.android.domain.models.users.UserFriend
import crossgame.android.service.FriendsService
import crossgame.android.service.NotificationService
import crossgame.android.ui.adapters.notification.SnackbarNotifier
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface NotificationActionListener {
    fun onAccept(notification: NotificationResponse)
    fun onReject(notification: NotificationResponse)
}

class NotificationAdapter(
    private val snackbarNotifier: SnackbarNotifier,
    private val context: Context,
    private var notificationList: List<NotificationResponse>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), NotificationActionListener {

    inner class NotificationFriendViewHolder(private val binding: CardNotificationFriendshipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val notificationMessage = binding.message
        val notificationDate = binding.date
        val acceptButton = binding.acceptInvite
        val rejectButton = binding.refuseInvite
    }

    inner class NotificationGroupViewHolder(private val binding: CardNotificationGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val notificationGroupMessage = binding.notificationGroupText
        val notificationGroupDate = binding.date
        val acceptButton = binding.acceptInvite
        val rejectButton = binding.refuseInvite
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NotificationType.FRIEND_REQUEST.ordinal -> {
                val binding =
                    CardNotificationFriendshipBinding.inflate(LayoutInflater.from(context), parent, false)
                NotificationFriendViewHolder(binding)
            }
            NotificationType.GROUP_INVITE.ordinal -> {
                val binding =
                    CardNotificationGroupBinding.inflate(LayoutInflater.from(context), parent, false)
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
                friendHolder.notificationDate.text = currentNotification.date.substring(11, 16)

                friendHolder.acceptButton.setOnClickListener {
                    onAccept(currentNotification)
                }
                friendHolder.rejectButton.setOnClickListener {
                    onReject(currentNotification)
                }
            }
            NotificationType.GROUP_INVITE.ordinal -> {
                val groupHolder = holder as NotificationGroupViewHolder
                groupHolder.notificationGroupMessage.text = currentNotification.message
                groupHolder.notificationGroupDate.text = currentNotification.date.substring(11, 16)
                groupHolder.acceptButton.setOnClickListener {
                    onAccept(currentNotification)
                }
                groupHolder.rejectButton.setOnClickListener {
                    onReject(currentNotification)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun getItemViewType(position: Int): Int {
        return notificationList[position].type.ordinal
    }

    fun updateData(notifications: List<NotificationResponse>) {
        notificationList = notifications.filter { it.state != NotificationState.CANCELLED }
        notifyDataSetChanged()
    }


    override fun onAccept(notification: NotificationResponse) {

        val sharedPreferences =
            context.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", 0).toLong()

        val rest = Rest.getInstance(context)
        when (notification.type) {
            NotificationType.FRIEND_REQUEST -> {
                  val service = rest.create(FriendsService::class.java)

                  service.confirmFriendRequest(userId,notification.description ).enqueue(object :
                      Callback<UserFriend> {
                      override fun onResponse(call: Call<UserFriend>, response: Response<UserFriend>) {
                          if (response.isSuccessful){
                              (snackbarNotifier as? SnackbarNotifier)?.showSnackbar("Amizade aceita!")

                              removeNotification(notification.id,notificationList)

                          }                      }

                      override fun onFailure(call: Call<UserFriend>, t: Throwable) {
                          Log.i("PATCH",t.message.toString())
                      }

                  }
                  )

            }
            NotificationType.GROUP_INVITE -> {
                (snackbarNotifier as? SnackbarNotifier)?.showSnackbar("Group Notification accepted")

                // Lógica para aceitar um convite de grupo
                // Chame a API relevante para aceitar esse convite de grupo
                // Exemplo: apiService.acceptGroupInvite(notification.id)
            }
        }
    }

    override fun onReject(notification: NotificationResponse) {
        val sharedPreferences =
            context.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", 0).toLong()

        val rest = Rest.getInstance(context)
        when (notification.type) {
            NotificationType.FRIEND_REQUEST -> {
                val service = rest.create(FriendsService::class.java)

                service.decliningFriendRequest(userId,notification.description ).enqueue(object :
                    Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful){
                            (snackbarNotifier as? SnackbarNotifier)?.showSnackbar("Pedido removido!")
                            removeNotification(notification.id,notificationList)

                        }           }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.i("PATCH",t.message.toString())
                    }

                }
                )
            }
            NotificationType.GROUP_INVITE -> {
                (snackbarNotifier as? SnackbarNotifier)?.showSnackbar("Group Notification Rejected")

                // Lógica para recusar um convite de grupo
                // Chame a API relevante para recusar esse convite de grupo
                // Exemplo: apiService.rejectGroupInvite(notification.id)
            }
        }
    }

    private fun removeNotification(notificationId:Long,notificationList: List<NotificationResponse>){

        val rest = Rest.getInstance(context)
        val service = rest.create(NotificationService::class.java)

        service.removeNotification(notificationId).enqueue(object :
            Callback<NotificationResponse> {
            override fun onResponse(
                call: Call<NotificationResponse>,
                response: Response<NotificationResponse>
            ) {
               if (response.isSuccessful){
                   val updatedList = notificationList.filter { it.id != notificationId }

                   updateData(updatedList)
                   Log.i("PATCH","Notification removed")
               }
            }

            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                Log.i("PATCH",t.message.toString())
            }
        })
    }


}
