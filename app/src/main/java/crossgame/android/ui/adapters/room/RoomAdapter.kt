package crossgame.android.ui.adapters.room

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import crossgame.android.application.ChatRoomActivity
import crossgame.android.application.databinding.CardRoomLayoutBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.rooms.Room
import crossgame.android.service.RoomService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomAdapter(
    private val rooms: MutableList<Room>,
    private val context: Context,
    private val idUser: Long,
    private val userName: String
) :
    Adapter<RoomAdapter.ViewHolder>() {

    private val isTeste: Boolean = true

    class ViewHolder(binding: CardRoomLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val button = binding.buttonEnterRoom
        val nameRoom = binding.nameRoomId
        val description = binding.descriptionRoomId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = CardRoomLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = rooms[position];

        holder.nameRoom.text = room.name
        holder.description.text = room.description
        val adminId: Long = room.idUserAdmin
        val roomId: Long = room.id

        holder.button.setOnClickListener {
            enterRoom(adminId, roomId, holder)
        }
    }

    private fun enterRoom(managerUserId: Long, roomId: Long, holder: ViewHolder) {
        if (!isTeste) {
            Rest.getInstance()
                .create(RoomService::class.java)
                .addCommonUser(idUser, roomId)
                .enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            val intent = Intent(context, ChatRoomActivity::class.java)
                            intent.putExtra("idGroup", roomId)
                            context.startActivity(intent)
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        TODO("Not yet implemented")
                    }
                })
        } else {
            val context = holder.button.context
            val intent = Intent(context, ChatRoomActivity::class.java)
            intent.putExtra("idGroup", roomId)
            context.startActivity(intent)
        }
    }
}

