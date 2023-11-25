package crossgame.android.ui.adapters.friends

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import crossgame.android.application.ChatFriendActivity
import crossgame.android.application.databinding.UserItemCardBinding
import crossgame.android.domain.models.friends.Friends
import crossgame.android.domain.models.messages.MessageFriend

class FriendsAdapter(
    private val context: Context,
    private var friendList: List<Friends>
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    private var listMessageWithFriend = mutableListOf<MessageFriend>()
    private var db: FirebaseFirestore = Firebase.firestore
    private var friendShipIds: List<Long> = listOf()

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

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatFriendActivity::class.java)
            intent.putExtra("friendId", currentFriend.friendUserId)
            intent.putExtra("friendUserName", currentFriend.username)
            context.startActivity(intent)
        }

        holder.userNameTextView.text = currentFriend.username

        friendShipIds = listOf(getIdUserSigned(), currentFriend.friendUserId)

        retrieveMessages(holder)
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

    fun getData(): List<Friends> {
        return friendList
    }

    private fun retrieveMessages(holder: FriendViewHolder) {

        var sortedFriendShipIds = friendShipIds.sorted()

        db.collection("messagesWithUsers").orderBy("createdAt")
            .whereEqualTo("users", sortedFriendShipIds)
            .addSnapshotListener()
            { result, error ->
                if (error != null) {
                    Log.w("Tag", "Error listening for messages.", error)
                    Toast.makeText(
                        holder.itemView.context,
                        "Error listening for messages.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                listMessageWithFriend.clear()

                for (document in result!!) {
                    val documentFirebase = document.data
                    listMessageWithFriend.add(
                        MessageFriend(
                            documentFirebase["createdAt"] as Timestamp,
                            documentFirebase["text"] as String,
                            documentFirebase["uid"] as Long,
                            documentFirebase["uid2"] as Long,
                            documentFirebase["users"] as List<Long>
                        )
                    )
                }

                if (listMessageWithFriend.size > 0) {
                    holder.userMessageTextView.text =
                        listMessageWithFriend[listMessageWithFriend.size - 1].text
                } else {
                    holder.userMessageTextView.text = ""
                }
            }
    }

    private fun getIdUserSigned(): Long {
        val sharedPreferences =
            context.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id", 4).toLong()
    }
}
