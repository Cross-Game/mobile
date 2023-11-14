package crossgame.android.application

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import crossgame.android.application.databinding.ActivityChatRoomBinding
import crossgame.android.application.databinding.BsCreatinRoomBinding
import crossgame.android.application.databinding.BsGivinFeedbackBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.enums.FriendshipState
import crossgame.android.domain.models.friends.FriendAdd
import crossgame.android.domain.models.messages.MessageInGroup
import crossgame.android.domain.models.user.UserInRoom
import crossgame.android.domain.models.user.UserPhoto
import crossgame.android.service.UserFriendService
import crossgame.android.ui.adapters.message.MessageAdapter
import crossgame.android.ui.adapters.usersRoom.UsersRoomAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterMessages: MessageAdapter
    private var listMessageInGroup = mutableListOf<MessageInGroup>()

    private lateinit var adapterUsersRoom: UsersRoomAdapter
    private var listUsersOnRoom = mutableListOf<UserInRoom>()

    private var isSelected: Boolean = false

    private var positionUser: Int = -1

    private var isTeste = true

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

        db.clearPersistence()

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
                    binding.includeSelectOptions.textView7.text = userInRoom.name

                    positionUser = position
                    isSelected = true
                    configureOptionsOfUsers(userInRoom)
                }
            }
        })
    }

    private fun configureOptionsOfUsers(userInRoom: UserInRoom) {
        binding.includeSelectOptions.itemFeedbackButtomRoom.setOnClickListener {
            showBottomSheet()
        }

        binding.includeSelectOptions.itemFriendshipButtomRoom.setOnClickListener {
            sendFriendRequestInRooom(userInRoom)
        }
    }

    private fun sendFriendRequestInRooom(userInRoom: UserInRoom) {
        // TODO: create a friend request link

        if (!isTeste) {
            val api = Rest.getInstance().create(UserFriendService::class.java)

            api.addFriendToAnUser(
                1L,
                FriendAdd("MyUserName", userInRoom.id, FriendshipState.SENDED)
            )
                .enqueue(
                    object : Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

                            if (response.isSuccessful) {
                                Toast.makeText(baseContext, "pedido enviado", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(baseContext, response.message(), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
        } else {
            Toast.makeText(baseContext, "Enviado id: ${userInRoom.id}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendFeedbackToUserInRooom(userInRoom: UserInRoom) {
        Toast.makeText(baseContext, " FeedBackEnviado", Toast.LENGTH_SHORT).show()
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
                            documentFirebase["photoURL"] as String,
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
                UserInRoom(2L, "teste2", UserPhoto("teste")),
                UserInRoom(3L, "teste3", UserPhoto("teste")),
                UserInRoom(4L, "teste4", UserPhoto("teste")),
                UserInRoom(5L, "teste5", UserPhoto("teste")),
                UserInRoom(6L, "teste6", UserPhoto("teste")),
                UserInRoom(7L, "teste7", UserPhoto("teste")),
                UserInRoom(8L, "teste8", UserPhoto("teste")),
                UserInRoom(9L, "teste9", UserPhoto("teste"))
            )
        )
    }

    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(binding.root.context)
        val sheetBinding: BsGivinFeedbackBinding =
            BsGivinFeedbackBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)

        dialog.show()
    }
}