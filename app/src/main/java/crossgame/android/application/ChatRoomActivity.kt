package crossgame.android.application

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import crossgame.android.application.databinding.ActivityChatRoomBinding
import crossgame.android.application.databinding.BsGivinFeedbackBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.enums.FriendshipState
import crossgame.android.domain.models.feedbacks.SendFeedBack
import crossgame.android.domain.models.friends.FriendAdd
import crossgame.android.domain.models.messages.MessageInGroup
import crossgame.android.domain.models.rooms.Room
import crossgame.android.domain.models.user.UserInRoom
import crossgame.android.domain.models.user.UserPhoto
import crossgame.android.service.FeedbackService
import crossgame.android.service.RoomService
import crossgame.android.service.UserFriendService
import crossgame.android.ui.adapters.message.MessageAdapter
import crossgame.android.ui.adapters.usersRoom.UsersRoomAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.util.Date

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterMessages: MessageAdapter
    private var listMessageInGroup = mutableListOf<MessageInGroup>()

    private lateinit var adapterUsersRoom: UsersRoomAdapter
    private var listUsersOnRoom = mutableListOf<UserInRoom>()
    private var idGroup: Long = -1

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

        idGroup = intent.getLongExtra("idGroup", -1L)

        retrieveMessages(idGroup)

        val findViewById = findViewById<View>(R.id.imageButton6)
        findViewById.setOnClickListener {
            sendMessage(idGroup, findViewById<EditText>(R.id.textMessage).text.toString());
            scroolToBottom()
        }

        findViewById<Button>(R.id.button_back_rooms).setOnClickListener {
            finish() // todo sair da sala
        }
    }

    override fun onStart() {
        super.onStart()
        retriveUsersInRooms(idGroup)
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
            showBottomSheetFeedback(userInRoom)
        }

        binding.includeSelectOptions.itemFriendshipButtomRoom.setOnClickListener {
            sendFriendRequestInRooom(userInRoom)
        }
    }

    private fun sendFriendRequestInRooom(userInRoom: UserInRoom) {
        if (!isTeste) {
            val api = Rest.getInstance().create(UserFriendService::class.java)
            api.addFriendToAnUser(
                this.getIdUserSigned(),
                FriendAdd
                    (
                    this.getUserSignedName(),
                    userInRoom.id,
                    FriendshipState.SENDED
                )
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

    private fun sendMessage(idGroup: Long, text: String) {
        val sendMessageInGroup = MessageInGroup(
            Timestamp.now(),
            idGroup,
            "imgUser", // todo user Photo
            text,
            getIdUserSigned()
        )

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

    private fun retriveUsersInRooms(idGroup: Long) {
        if (!isTeste) {
            Rest.getInstance()
                .create(RoomService::class.java)
                .retrieveRoomById(idGroup)
                .enqueue(object : Callback<Room> {
                    override fun onResponse(call: Call<Room>, response: Response<Room>) {

                        if (response.isSuccessful) {
                            var body = response.body()

                            body?.user?.forEach {
                                listUsersOnRoom.add(
                                    UserInRoom(
                                        it.id, it.username, UserPhoto("teste")
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<Room>, t: Throwable) {
                        TODO("Not yet implemented")
                    }
                })
        } else {
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
    }

    private fun showBottomSheetFeedback(userInRoom: UserInRoom) {
        val dialog = BottomSheetDialog(binding.root.context)
        val sheetBinding: BsGivinFeedbackBinding =
            BsGivinFeedbackBinding.inflate(layoutInflater, null, false)

        sheetBinding.textNomeUsuario.text = userInRoom.name

        dialog.setContentView(sheetBinding.root)

        dialog.show()

        with(sheetBinding) {
            val comportamento = ratingBarComportamento.rating.toInt()
            val habilidade = ratingBarHabilidade.rating.toInt()
            val descricao = editTextDescricao.text.toString()
            sendFeedBackToUser.setOnClickListener {
                sendFeedback(
                    userInRoom,
                    descricao,
                    habilidade,
                    comportamento
                )
            }
        }
    }

    private fun sendFeedback(
        userInRoom: UserInRoom,
        descricao: String,
        habilidade: Int,
        comportamento: Int
    ) {

        if (!isTeste) {
            Rest.getInstance()
                .create(FeedbackService::class.java)
                .sendFeedBackToUser(
                    userInRoom.id, SendFeedBack(
                        this.getUserSignedName(),
                        comportamento,
                        habilidade,
                        descricao,
                        Date.from(Instant.now())
                    )
                )
        } else {
            Log.i(null, "$habilidade")
            Toast.makeText(baseContext, "Enviado FeedBack", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserSignedName(): String {
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)

        return sharedPreferences.getString("username", "").toString()
    }

    private fun getIdUserSigned(): Long {
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id", -1).toLong()
    }
}