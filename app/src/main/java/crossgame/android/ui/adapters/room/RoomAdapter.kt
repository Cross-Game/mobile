package crossgame.android.ui.adapters.room

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import crossgame.android.application.ChatRoomActivity
import crossgame.android.application.databinding.CardRoomLayoutBinding
import crossgame.android.domain.models.rooms.Room
import crossgame.android.domain.models.user.User

class RoomAdapter(private val rooms: MutableList<Room>, private val context: Context) :
    Adapter<RoomAdapter.ViewHolder>() {

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

        holder.button.setOnClickListener {
            val context = holder.button.context
            val intent = Intent(context, ChatRoomActivity::class.java)
            intent.putExtra("idGroup", room.id)
            context.startActivity(intent)
        }
    }
}

