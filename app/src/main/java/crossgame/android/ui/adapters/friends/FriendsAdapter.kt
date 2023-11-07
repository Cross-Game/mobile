package crossgame.android.ui.adapters.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import crossgame.android.application.databinding.UserItemCardBinding
import crossgame.android.domain.models.friends.Friends
import com.bumptech.glide.request.RequestOptions

class FriendsAdapter(private val context: Context, private val friendList: List<Friends>) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    class FriendViewHolder(private val binding: UserItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
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

        // Carregar a imagem com Glide e aplicar uma máscara circular
        Glide.with(context)
            .load(currentFriend.userProfileImageUrl)
            .apply(RequestOptions.circleCropTransform()) // Aplica a máscara circular
            .into(holder.userProfileImageView)

        holder.userNameTextView.text = currentFriend.userName
        holder.userMessageTextView.text = currentFriend.userMessage
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}

