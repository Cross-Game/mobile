package crossgame.android.ui.adapters.notification
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.ChatRoomActivity
import crossgame.android.application.databinding.CardNotificationFriendshipBinding
import crossgame.android.application.databinding.CardNotificationGroupBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.notifications.NotificationResponse
import crossgame.android.domain.models.notifications.NotificationState
import crossgame.android.domain.models.notifications.NotificationType
import crossgame.android.domain.models.rooms.Room
import crossgame.android.domain.models.users.UserFriend
import crossgame.android.service.FriendsService
import crossgame.android.service.NotificationService
import crossgame.android.service.RoomService
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
                rest.create(RoomService::class.java)
                    .addCommonUser(userId, notification.description.toLong())
                    .enqueue(object : Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            if (response.isSuccessful) {
                                val intent = Intent(context, ChatRoomActivity::class.java)
                                intent.putExtra("idGroup", notification.description.toLong())
                                val gameName = retrieveGameNameRoom(notification.description.toLong())
                                intent.putExtra("gameName",gameName)
                                context.startActivity(intent)
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Log.e("Room", "Erro ao entrar na sala!")

                            (snackbarNotifier as? SnackbarNotifier)?.showSnackbar("Erro ao entrar na sala!")

                        }
                    })
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
                (snackbarNotifier as? SnackbarNotifier)?.showSnackbar("Group Notification Rejected");
                    removeNotification(notification.id,notificationList)
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

    private fun retrieveGameNameRoom(id : Long) : String{
        var gameName = "";
        val rest = Rest.getInstance(context)
        val service = rest.create(RoomService::class.java)
        service.retrieveRoomById(id).enqueue(object : Callback<Room>{
            override fun onResponse(call: Call<Room>, response: Response<Room>) {
                if(response.isSuccessful){
                    gameName = response.body()!!.gameName;
                }
            }

            override fun onFailure(call: Call<Room>, t: Throwable) {
                Log.i("Retrieve",t.message.toString())

            }

        })
        return  gameName
    }


}

