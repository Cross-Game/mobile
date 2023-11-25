package crossgame.android.application

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import crossgame.android.application.databinding.ActivityChatRoomBinding
import crossgame.android.application.databinding.BsGameListBinding
import crossgame.android.application.databinding.BsGivinFeedbackBinding
import crossgame.android.application.databinding.BsInvitinFriendsBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.enums.FriendshipState
import crossgame.android.domain.models.feedbacks.SendFeedBack
import crossgame.android.domain.models.feedbacks.UserAndFeedback
import crossgame.android.domain.models.friends.FriendAdd
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.messages.MessageInGroup
import crossgame.android.domain.models.notifications.NotificationState
import crossgame.android.domain.models.notifications.NotificationType
import crossgame.android.domain.models.rooms.Room
import crossgame.android.domain.models.user.UserInRoom
import crossgame.android.domain.models.user.UserPhoto
import crossgame.android.domain.models.user.UserPictureGeneric
import crossgame.android.domain.models.users.UserFriend
import crossgame.android.service.AutenticationUser
import crossgame.android.service.FeedbackService
import crossgame.android.service.FriendsService
import crossgame.android.service.GamesService
import crossgame.android.service.NotificationService
import crossgame.android.service.RoomService
import crossgame.android.service.UserFriendService
import crossgame.android.ui.adapters.message.MessageAdapter
import crossgame.android.ui.adapters.usersRoom.UsersRoomAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var rootView: View
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterMessages: MessageAdapter
    private var listMessageInGroup = mutableListOf<MessageInGroup>()
    private var gameName: String = ""

    private lateinit var adapterUsersRoom: UsersRoomAdapter
    private var listUsersOnRoom = mutableListOf<UserInRoom>()
    private var idGroup: Long = -1

    private var groupName = "";
    private var isSelected: Boolean = false

    private var positionUser: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rootView = findViewById(android.R.id.content)

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
        groupName = intent.getStringExtra("groupName")!!



        retrieveMessages(idGroup)

        val findViewById = findViewById<View>(R.id.imageButton6)
        findViewById.setOnClickListener {
            sendMessage(idGroup, findViewById<EditText>(R.id.textMessage).text.toString());
        }

        findViewById<Button>(R.id.button_back_rooms).setOnClickListener {
            exitFromRoom()
        }

        binding.buttonInviteFriend.setOnClickListener { lifecycleScope.launch {
            showBottomSheetDialogFriends()
        }
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
                        exibirSnackbar("Não foi possível sair da sala. Tente novamente", false)
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e("Error", "Erro ao sair da sala", t)
                    exibirSnackbar("Não foi possível sair da sala. Tente novamente", false)
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
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(findViewById(R.id.imageView13))
                } else {
                    Log.e("retrieveImageGame", "Houve um erro ao buscar a imagem da sala. Imagem default selecionada.")
                    exibirSnackbar("Ops! Ocorreu um erro ao buscar imagem da sala.", false)
                }
            }

            override fun onFailure(call: Call<GameResponse>, t: Throwable) {
                Log.e("retrieveImageGame", "Falha ao buscar imagem da sala: " + t.message)
                Log.e("retrieveImageGame", "Houve um erro ao buscar a imagem da sala. Imagem default selecionada.")
            }
        })
    }

    private fun showOptionsUsers() {
        adapterUsersRoom.setOnItemClickListener(object : UsersRoomAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, userInRoom: UserInRoom) {


                if(userInRoom.id != getIdUserSigned()) {
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
                        Log.e("getPhotoUser", "Ops, imagem incompatível !")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("getPhotoUser", "Falha ao obter a foto de perfil")
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

                            exibirSnackbar("Convite para entrar na sala enviado!")
                        } else {
                            exibirSnackbar("Falha ao enviar convite para o jogador ${userInRoom.name}. Tente novamente.")
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.e("sendFriendRequestInRooom", "Falha ao enviar convite para entrar na sala: " + t.message)
                        exibirSnackbar("Falha ao enviar convite para o jogador ${userInRoom.name}. Tente novamente.")
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

                    } else {
                        Log.e(
                            "sendNotificationFriendship",
                            "Falha ao enviar notificação para usuário de id $friendId"
                        )
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e(
                        "sendNotificationFriendship",
                        "Falha ao enviar notificação para usuário de id $friendId",
                        t
                    )
                }

            })
    }

    private suspend fun sendNotificationInviteRoom(friendId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = Rest.getInstance(baseContext)
                    .create(NotificationService::class.java)
                    .createNotificationWithQuery(
                        friendId,
                        message = "Você foi convidado para entrar no grupo $groupName",
                        description = idGroup.toString(),
                        type = NotificationType.GROUP_INVITE,
                        state = NotificationState.AWAITING
                    ).execute()

                if (response.isSuccessful) {
                    return@withContext true
                } else {
                    Log.e("ERRO", "Código de erro: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("EXCEPTION", "Erro durante o envio da notificação: ${e.message}")
            }
            return@withContext false
        }
    }

    private fun sendMessage(idGroup: Long, text: String) {
        val sendMessageInGroup = MessageInGroup(
            Timestamp.now(),
            idGroup,
            UserPictureGeneric.get(), // todo user Photo
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
                    exibirSnackbar("Ops! Encontramos uma falha ao recuperar mensagens. Por favor, tente recarregar a página.")
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
                        exibirSnackbar("Ops! Encontramos uma falha ao carregar usuários. Por favor, tente recarregar a página.", false)
                    }
                }

                override fun onFailure(call: Call<Room>, t: Throwable) {
                    Log.e("Error", "Houve um erro ao buscar os usuários", t)
                    exibirSnackbar("Ops! Encontramos uma falha ao carregar usuários. Por favor, tente recarregar a página.", false)
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
                        exibirSnackbar( "Feedback para ${userInRoom.name} enviado com sucesso!", true)
                    } else {
                        Log.i("Error", "Erro ao Enviar feedback")
                        exibirSnackbar("Erro ao enviar feedback para ${userInRoom.name}. Por favor, tente novamente.", false)
                    }
                }

                override fun onFailure(call: Call<UserAndFeedback>, t: Throwable) {
                    Log.i("Error", "Erro ao Enviar feedback: " + t.message)
                    exibirSnackbar("Erro ao enviar feedback para ${userInRoom.name}. Por favor, tente novamente.", false)
                }
            })
    }

    private fun getUserSignedName(): String {
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)

        return sharedPreferences.getString("username", "Meu nome").toString()
    }

    private fun getIdUserSigned(): Long {
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id", 4).toLong()
    }

    private suspend fun showBottomSheetDialogFriends(){
        val dialog = BottomSheetDialog(this)
        val sheetBinding : BsInvitinFriendsBinding = BsInvitinFriendsBinding.inflate(layoutInflater, null, false);
        val chipGroup = sheetBinding.listOfPlayers;

        var friends = getFriends()
        friends.map { amigo ->
            val newChip = Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)

            newChip.isSelected = false
            newChip.setChipBackgroundColorResource(R.color.md_theme_dark_inverseOnSurface)
            newChip.text = amigo.username
            newChip.tag = amigo.friendUserId
            newChip.setOnClickListener { sucessOrRetryChipFilter(newChip) }
            newChip.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.white)))
            chipGroup.addView(newChip)
        }

        dialog.setContentView(sheetBinding.root)
        dialog.show()

        dialog.setOnDismissListener {}
    }

    private fun sucessOrRetryChipFilter(view : View){
        if (view is Chip) {
            val chip = view as Chip
            chip.isChipIconVisible = true
            var sucess = true;

            lifecycleScope.launch {
                if (!chip.tag.equals(true)) {
                     sucess = sendNotificationInviteRoom(chip.tag.toString().toLong());
                }


            if (sucess){
                chip.tag = sucess;
            } else {
                chip.setChipIconResource(R.drawable.baseline_replay_24)
                chip.setChipBackgroundColorResource(R.color.label_information_background)
            }

            if (chip.tag.equals(true)){
                chip.isSelected = true;
                chip.setChipIconResource(R.drawable.baseline_check_24)
                chip.setChipBackgroundColorResource(R.color.md_theme_dark_onPrimary)

            }
        }
        }
    }

    private suspend fun getFriends(): List<UserFriend> = withContext(Dispatchers.IO) {
        try {
            val response =  Rest.getInstance(baseContext).create(FriendsService::class.java).retrieveFriendsForUserById(getIdUserSigned()).execute()
            if (response.isSuccessful) {
                return@withContext response.body() ?: emptyList()
            }
            Log.e("ERRO", response.body().toString())
            exibirSnackbar("Falha ao carregar amigos. Por favor, tente novamente.", false)
        } catch (e: Exception) {
            exibirSnackbar("Falha ao carregar amigos. Por favor, tente novamente.", false)
            Log.e("EXCEPTION", e.message.toString())
        }
        return@withContext emptyList()
    }

    private fun exibirSnackbar(mensagem: String, isSucess : Boolean = true) {
        val snackbar = Snackbar.make(rootView, mensagem, Snackbar.LENGTH_SHORT)

        if (isSucess) {
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.sucess))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        else {
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.error))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        snackbar.show()
    }

}