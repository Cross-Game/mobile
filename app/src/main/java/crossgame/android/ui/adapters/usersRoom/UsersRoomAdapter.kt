package crossgame.android.ui.adapters.usersRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import crossgame.android.application.R
import crossgame.android.application.databinding.UserPortraitRoomLayoutBinding
import crossgame.android.domain.models.user.UserInRoom

class UsersRoomAdapter(private val users: MutableList<UserInRoom>, private val context: Context) :
    Adapter<UsersRoomAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, userInRoom: UserInRoom)
    }

    class ViewHolder(binding: UserPortraitRoomLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val userPortrait = binding.portraitId
        val image = binding.imageView6
        val name = binding.idUserNameOnUserPortraitRoom

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            UserPortraitRoomLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.image.setImageResource(R.drawable.carbon_user_avatar_empty)
        holder.name.text = user.name

        holder.userPortrait.setOnClickListener {
            onItemClickListener?.onItemClick(position, user)
        }

    }
}