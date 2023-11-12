package crossgame.android.application

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import crossgame.android.application.databinding.ActivityChatRoomBinding
import crossgame.android.domain.models.messages.MessageInGroup
import crossgame.android.domain.models.user.UserInRoom
import crossgame.android.domain.models.user.UserPhoto
import crossgame.android.ui.adapters.message.MessageAdapter
import crossgame.android.ui.adapters.usersRoom.UsersRoomAdapter

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterMessages: MessageAdapter
    private var listMessageInGroup = mutableListOf<MessageInGroup>()

    private lateinit var adapterUsersRoom: UsersRoomAdapter
    private var listUsersOnRoom = mutableListOf<UserInRoom>()

    private var isSelected: Boolean = false

    private var positionUser: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewMessages = binding.listofmessagesinroom

        recyclerViewMessages.layoutManager = LinearLayoutManager(this)

        val recyclerViewUsers = binding.listOfUsersOnRoom

        recyclerViewUsers.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        db = Firebase.firestore

        adapterMessages = MessageAdapter(listMessageInGroup, this)
        adapterUsersRoom = UsersRoomAdapter(listUsersOnRoom, this)

        showOptionsUsers()

        recyclerViewMessages.adapter = adapterMessages
        recyclerViewUsers.adapter = adapterUsersRoom

        val idGroup = intent.getLongExtra("idGroup", -1L)

        retrieveMessages(idGroup)

        val findViewById = findViewById<View>(R.id.imageButton6)
        findViewById.setOnClickListener {
            sendMessage(idGroup, findViewById<EditText>(R.id.textMessage).text.toString());
            scroolToBottom()
        }

        findViewById<Button>(R.id.button_back_rooms).setOnClickListener {
            finish()
        }
    }

    private fun showOptionsUsers() {
        adapterUsersRoom.setOnItemClickListener(object : UsersRoomAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, userInRoom: UserInRoom) {

                if (positionUser == position && isSelected) {
                    binding.includeSelectOptions.root.visibility = View.GONE
                    binding.include.root.visibility = View.VISIBLE
                    positionUser = position
                    isSelected = false
                } else {
                    binding.includeSelectOptions.root.visibility = View.VISIBLE
                    binding.include.root.visibility = View.GONE

                    positionUser = position
                    isSelected = true
                    configureOptionsOfUsers(userInRoom)
                }
            }
        })
    }

    private fun configureOptionsOfUsers(userInRoom: UserInRoom) {
        binding.includeSelectOptions.itemFeedbackButtomRoom.setOnClickListener {
            sendFeedbackToUserInRooom(userInRoom)
        }

        binding.includeSelectOptions.itemFeedbackButtomRoom.setOnClickListener {
            sendFriendRequestInRooom(userInRoom)
        }
    }

    private fun sendFriendRequestInRooom(userInRoom: UserInRoom) {
        // TODO: create a friend request link

//        val api = Rest.getInstance().create(RoomService::class.java)
//
//        api.


    }

    private fun sendFeedbackToUserInRooom(userInRoom: UserInRoom) {
        // TODO: create a feedBack link
    }


    override fun onStart() {
        super.onStart()
        retriveUsersInRooms()
    }


    private fun sendMessage(idGroup: Long, text: String) {
        val sendMessageInGroup = MessageInGroup(
            Timestamp.now(),
            idGroup,
            "imgUser",
            text,
            1L
        ) // todo receber referencias do usuario

        db.collection("messages")
            .add(sendMessageInGroup)
            .addOnSuccessListener {
                adapterMessages.notifyDataSetChanged()
                clearText()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

    private fun scroolToBottom() {
        binding.listofmessagesinroom.smoothScrollToPosition(binding.listofmessagesinroom.adapter!!.itemCount + 2)
    }

    private fun clearText() {
        findViewById<EditText>(R.id.textMessage).setText("")
    }

    private fun retrieveMessages(idGroup: Long) {

        db.collection("messages").orderBy("createdAt").whereEqualTo("idGroup", idGroup)
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

                listMessageInGroup.clear()

                for (document in result!!) {
                    val documentFirebase = document.data
                    listMessageInGroup.add(
                        MessageInGroup(
                            documentFirebase["createdAt"] as Timestamp,
                            documentFirebase["idGroup"] as Long,
                            documentFirebase["photoUrl"] as String,
                            documentFirebase["text"] as String,
                            documentFirebase["uid"] as Long
                        )
                    )
                }
                adapterMessages.notifyDataSetChanged()
            }
        adapterMessages.notifyDataSetChanged()
    }

    private fun retriveUsersInRooms() {
        listUsersOnRoom.addAll(
            mutableListOf(
                UserInRoom(1L, "teste", UserPhoto("teste")),
                UserInRoom(2L, "teste", UserPhoto("teste")),
                UserInRoom(3L, "teste", UserPhoto("teste")),
                UserInRoom(4L, "teste", UserPhoto("teste")),
                UserInRoom(5L, "teste", UserPhoto("teste")),
                UserInRoom(6L, "teste", UserPhoto("teste")),
                UserInRoom(7L, "teste", UserPhoto("teste")),
                UserInRoom(8L, "teste", UserPhoto("teste")),
                UserInRoom(9L, "teste", UserPhoto("teste"))
            )
        )
    }

}