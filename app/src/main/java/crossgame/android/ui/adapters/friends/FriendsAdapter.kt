package crossgame.android.ui.adapters.friends

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.databinding.UserItemCardBinding
import crossgame.android.domain.models.friends.Friends
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class FriendsAdapter(
    private val context: Context,
    private var friendList: List<Friends>
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    class FriendViewHolder(private val binding: UserItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val userProfileImageView = binding.userProfileImageView
        val userNameTextView = binding.userNameTextView
        val userMessageTextView = binding.userMessageTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = UserItemCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentFriend = friendList[position]

        // Verifica se friendPhoto não é nulo antes de tentar carregar com Glide
        currentFriend.friendPhoto?.let { photo ->
            // Carrega a foto do amigo usando Glide na ImageView
            Glide.with(context)
                .load(photo)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.userProfileImageView)
        }

        holder.userNameTextView.text = currentFriend.username
        // holder.userMessageTextView.text = currentFriend.userMessage
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    fun updateData(newFriendList: List<Friends>) {
        friendList = newFriendList
        notifyDataSetChanged()
    }

    fun updateFriendPhoto(friendUserId: Long, photo: Bitmap) {
        // Encontrar o amigo na lista e atualizar a foto
        val friendToUpdate = friendList.find { it.friendUserId == friendUserId }
        friendToUpdate?.let {
            it.friendPhoto = photo
            notifyDataSetChanged()
        }
    }
}
