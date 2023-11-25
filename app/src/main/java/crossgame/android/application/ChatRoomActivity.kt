package crossgame.android.application

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
import crossgame.android.domain.models.feedbacks.UserAndFeedback
import crossgame.android.domain.models.friends.FriendAdd
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.messages.MessageInGroup
import crossgame.android.domain.models.rooms.Room
import crossgame.android.domain.models.user.UserInRoom
import crossgame.android.domain.models.user.UserPhoto
import crossgame.android.service.AutenticationUser
import crossgame.android.service.FeedbackService
import crossgame.android.service.GamesService
import crossgame.android.service.NotificationService
import crossgame.android.service.RoomService
import crossgame.android.service.UserFriendService
import crossgame.android.ui.adapters.message.MessageAdapter
import crossgame.android.ui.adapters.usersRoom.UsersRoomAdapter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterMessages: MessageAdapter
    private var listMessageInGroup = mutableListOf<MessageInGroup>()
    private var gameName: String = ""

    private lateinit var adapterUsersRoom: UsersRoomAdapter
    private var listUsersOnRoom = mutableListOf<UserInRoom>()
    private var idGroup: Long = -1

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

        db.clearPersistence()

        adapterMessages = MessageAdapter(listMessageInGroup, this)
        adapterUsersRoom = UsersRoomAdapter(listUsersOnRoom, this)

        showOptionsUsers()

        recyclerViewMessages.adapter = adapterMessages
        recyclerViewUsers.adapter = adapterUsersRoom

        idGroup = intent.getLongExtra("idGroup", -1L)
        gameName = intent.getStringExtra("gameName")!!

        retrieveMessages(idGroup)

        val findViewById = findViewById<View>(R.id.imageButton6)
        findViewById.setOnClickListener {
            sendMessage(idGroup, findViewById<EditText>(R.id.textMessage).text.toString());
        }

        findViewById<Button>(R.id.button_back_rooms).setOnClickListener {
            exitFromRoom()
        }

        adjusteSizeInput(binding)
    }

    private fun adjusteSizeInput(binding: ActivityChatRoomBinding) {
        binding.scrollViewSendMessage.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            private var initialHeight = 0

            override fun onPreDraw(): Boolean {
                if (initialHeight == 0) {
                    initialHeight = binding.scrollViewSendMessage.height
                    return true
                }

                val currentHeight = binding.scrollViewSendMessage.height

                var testValue = initialHeight
                if (testValue > currentHeight) {
                    binding.scrollViewSendMessage.scrollBy(0, 3000)

                    val novaAltura = 600
                    val params = binding.listofmessagesinroom.layoutParams
                    params.height = novaAltura
                    binding.listofmessagesinroom.layoutParams = params

                    val novaAltura1 = 250
                    val params2 = binding.linearLayout4.layoutParams
                    params2.height = novaAltura1
                    binding.linearLayout4.layoutParams = params2
                } else {
                    val novaAltura = 1200
                    val params = binding.listofmessagesinroom.layoutParams
                    params.height = novaAltura
                    binding.listofmessagesinroom.layoutParams = params
                    binding.scrollViewSendMessage.scrollBy(0, 0)

                    val novaAltura1 = 400
                    val params2 = binding.linearLayout4.layoutParams
                    params2.height = novaAltura1
                    binding.linearLayout4.layoutParams = params2
                }

                return true
            }
        })
    }

    private fun exitFromRoom() {
        Rest.getInstance()
            .create(RoomService::class.java)
            .exitFromRoom(getIdUserSigned(), idGroup)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        finish()
                    } else {
                        Log.e("Error", "Erro ao sair da sala")
                        Toast.makeText(
                            baseContext,
                            "Não foi possível sair da sala",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("Error", "Erro ao sair da sala", t)
                    Toast.makeText(
                        baseContext,
                        "Não foi possível sair da sala",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        retriveUsersInRooms(idGroup)
        retrieveImageGame(gameName)
    }

    private fun retrieveImageGame(gameName: String) {
        val rest = Rest.getInstance(this)
        val service = rest.create(GamesService::class.java)
        var link: String? = ""

        service.retrieveGameByName(gameName).enqueue(object : Callback<GameResponse> {
            override fun onResponse(
                call: Call<GameResponse>,
                response: Response<GameResponse>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    link = apiResponse?.imageGame?.link

                    Glide.with(baseContext)
                        .load(link)
                        .placeholder(R.drawable.game_example_2)
                        .into(findViewById(R.id.imageView13))
                } else {
                    Log.e("Error", "Houve um erro ao buscar a imagem da sala")
                    Toast.makeText(
                        baseContext,
                        "Houve um erro ao buscar a imagem da sala",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<GameResponse>, t: Throwable) {
                Log.e("GET", "Falha ao listar os Jogos", t)
            }
        })
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
                    binding.includeSelectOptions.imageView7.setImageResource(R.drawable.carbon_user_avatar_empty)

                    getPhotoUser(userInRoom.id, binding)

                    positionUser = position
                    isSelected = true
                    configureOptionsOfUsers(userInRoom)
                }
            }
        })
    }

    private fun getPhotoUser(userId: Long, binding: ActivityChatRoomBinding) {
        val rest = Rest.getInstance(baseContext)
//        val rest = Rest.getInstance(this)
        rest.create(AutenticationUser::class.java).getPhoto(userId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()?.contentLength()?.toInt() != 0) {
                            val inputStream: InputStream = response.body()!!.byteStream()
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

                            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
                            val decodedByte =
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                            Glide.with(baseContext)
                                .load(bitmap)
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.includeSelectOptions.imageView7)
                        }
                    } else {
                        Log.i("GET", "Ops, imagem incompatível !")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.i("GET", "Falha ao obter a foto de perfil")
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
        val api = Rest.getInstance(this).create(UserFriendService::class.java)
        api.addFriendToAnUser(
            this.getIdUserSigned(),
            FriendAdd
                (
                userInRoom.name,
                userInRoom.id,
                FriendshipState.SENDED
            )
        )
            .enqueue(
                object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

                        if (response.isSuccessful) {

                            sendNotificationFriendship(userInRoom.id)

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
    }

    private fun sendNotificationFriendship(friendId: Long) {

        val userNameCurrentUser = getUserSignedName()
        Rest.getInstance(this).create(NotificationService::class.java)
            .createNotificationFriendship(
                friend = friendId,
                userNameCurrentUser = userNameCurrentUser
            ).enqueue(object : Callback<Unit> {
                override fun onResponse(
                    call: Call<Unit>,
                    response: Response<Unit>
                ) {
                    if (response.isSuccessful) {
                        Log.i("Notification", "Notificação enviada para usuário de id $friendId")
                    } else {
                        Log.i(
                            "Notification",
                            "Falha ao enviar notificação para usuário de id $friendId"
                        )
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.i(
                        "Notification",
                        "Falha ao enviar notificação para usuário de id $friendId",
                        t
                    )
                }

            })
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
                scroolToBottom()
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

    private fun scroolToBottom() {
        if (binding.listofmessagesinroom.adapter!!.itemCount > 1) {
            binding.listofmessagesinroom.smoothScrollToPosition(binding.listofmessagesinroom.adapter!!.itemCount + 1)
        }
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
                scroolToBottom()
            }
        adapterMessages.notifyDataSetChanged()
        scroolToBottom()
    }

    private fun retriveUsersInRooms(idGroup: Long) {
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
                        adapterUsersRoom.notifyDataSetChanged()
                    } else {
                        Log.e("Error", "Houve um erro ao buscar os usuários")
                        Toast.makeText(
                            baseContext,
                            "Houve um erro ao buscar os usuários",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Room>, t: Throwable) {
                    Log.e("Error", "Houve um erro ao buscar os usuários", t)
                    Toast.makeText(
                        baseContext,
                        "Houve um erro ao buscar os usuários",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun showBottomSheetFeedback(userInRoom: UserInRoom) {
        val dialog = BottomSheetDialog(binding.root.context)
        val sheetBinding: BsGivinFeedbackBinding =
            BsGivinFeedbackBinding.inflate(layoutInflater, null, false)

        sheetBinding.textNomeUsuario.text = userInRoom.name

        dialog.setContentView(sheetBinding.root)

        dialog.show()

        with(sheetBinding) {
            sendFeedBackToUser.setOnClickListener {
                val comportamento = ratingBarComportamento.rating.toInt()
                val habilidade = ratingBarHabilidade.rating.toInt()
                val descricao = editTextDescricaoFeedback.text.toString()
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

        Rest.getInstance(this)
            .create(FeedbackService::class.java)
            .sendFeedBackToUser(
                userInRoom.id, SendFeedBack(
                    this.getUserSignedName(),
                    comportamento,
                    habilidade,
                    descricao
                )
            ).enqueue(object : Callback<UserAndFeedback> {
                override fun onResponse(
                    call: Call<UserAndFeedback>,
                    response: Response<UserAndFeedback>
                ) {
                    if (response.isSuccessful) {
                        Log.i("Room", "Feedback Enviado!")
                        Toast.makeText(baseContext, "Enviado FeedBack", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Log.i("Error", "Erro ao Enviar feedback")
                        Toast.makeText(
                            baseContext,
                            "Erro ao Enviar feedback",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

                override fun onFailure(call: Call<UserAndFeedback>, t: Throwable) {
                    Log.i("Error", "Erro ao Enviar feedback")
                    Toast.makeText(baseContext, "Erro ao Enviar feedback", Toast.LENGTH_SHORT)
                        .show()
                }
            })
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