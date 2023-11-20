package crossgame.android.application

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import crossgame.android.application.databinding.ActivityChatFriendBinding
import crossgame.android.domain.models.messages.MessageFriend
import crossgame.android.ui.adapters.message.MessageWithFriendAdapter

class ChatFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatFriendBinding
    private var friendId = -1L
    private var friendUserName = ""

    private lateinit var adapterMessages: MessageWithFriendAdapter
    private var listMessageWithFriend = mutableListOf<MessageFriend>()

    private var friendShipIds: List<Long> = listOf()

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)



        friendId = intent.getLongExtra("friendId", -1L)
        friendUserName = intent.getStringExtra("friendUserName")!!

        val recyclerViewMessages = binding.listOfMessagesInChatWithFriends
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)

        db = Firebase.firestore
        db.clearPersistence()

        adapterMessages = MessageWithFriendAdapter(listMessageWithFriend, this)

        recyclerViewMessages.adapter = adapterMessages

        friendShipIds = listOf(getIdUserSigned(), friendId)
        retrieveMessages()

        binding.imageButton4.setOnClickListener {
            finish()
        }

        val findViewById = findViewById<View>(R.id.imageButton6)
        findViewById.setOnClickListener {
            sendMessage(findViewById<EditText>(R.id.textMessage).text.toString());
            scroolToBottom()
        }

    }

    override fun onStart() {
        super.onStart()
        setFriendInformation()
    }

    private fun setFriendInformation() {
        binding.friendUserNameId.text = friendUserName
    }

    private fun retrieveMessages() {

        var sortedFriendShipIds = friendShipIds.sorted()

        db.collection("messagesWithUsers").orderBy("createdAt")
            .whereEqualTo("users", sortedFriendShipIds)
            .addSnapshotListener()
            { result, error ->
                if (error != null) {
                    Log.w("Tag", "Error listening for messages.", error)
                    Toast.makeText(
                        baseContext,
                        "Erro ao recuperar as mensagens",
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
                adapterMessages.notifyDataSetChanged()
            }
        adapterMessages.notifyDataSetChanged()
    }

    private fun sendMessage(text: String) {

        var sortedFriendShipIds = friendShipIds.sorted()
        val sendMessageToUser = MessageFriend(
            Timestamp.now(),
            text,
            getIdUserSigned(), // todo user Photo
            friendId,
            sortedFriendShipIds
        )

        db.collection("messagesWithUsers")
            .add(sendMessageToUser)
            .addOnSuccessListener {
                adapterMessages.notifyDataSetChanged()
                clearText()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

    private fun clearText() {
        findViewById<EditText>(R.id.textMessage).setText("")
    }

    private fun scroolToBottom() {
        binding.listOfMessagesInChatWithFriends.smoothScrollToPosition(binding.listOfMessagesInChatWithFriends.adapter!!.itemCount + 2)
    }

    private fun getUserSignedName(): String {
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)

        return sharedPreferences.getString("username", "KakaLopz").toString()
    }

    private fun getIdUserSigned(): Long {
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id", 4).toLong()
    }

}