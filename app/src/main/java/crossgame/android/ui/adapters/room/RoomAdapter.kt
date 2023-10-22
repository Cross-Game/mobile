package crossgame.android.ui.adapters.room

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import crossgame.android.application.R
import crossgame.android.application.databinding.CardRoomLayoutBinding
import crossgame.android.domain.models.rooms.Room

class RoomAdapter(private val rooms: List<Room>, private val context: Context) :
    Adapter<RoomAdapter.ViewHolder>() {

    class ViewHolder(binding: CardRoomLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
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
        holder.nameRoom.text = rooms[position].name
        holder.description.text = rooms[position].description
    }
}

