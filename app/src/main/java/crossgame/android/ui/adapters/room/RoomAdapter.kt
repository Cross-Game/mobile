package crossgame.android.ui.adapters.room

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import crossgame.android.application.ChatRoomActivity
import crossgame.android.application.R
import crossgame.android.application.databinding.CardRoomLayoutBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.games.ImageGame
import crossgame.android.domain.models.rooms.Room
import crossgame.android.service.AutenticationUser
import crossgame.android.service.GamesService
import crossgame.android.service.RoomService
import crossgame.android.ui.adapters.usersRoom.UsersRoomAdapter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class RoomAdapter(
    private val rooms: MutableList<Room>,
    private val context: Context,
    private val idUser: Long,
    private val userName: String
) :
    Adapter<RoomAdapter.ViewHolder>() {

    private val isTeste: Boolean = true
    private lateinit var usersInRoomAdapter: UsersInRoomAdapter

    class ViewHolder(binding: CardRoomLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val button = binding.buttonEnterRoom
        val nameRoom = binding.nameRoomId
        val description = binding.descriptionRoomId
        val imageGame = binding.imageGameRoom
        val listOfusers = binding.listOfUsers
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = CardRoomLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = rooms[position];

        holder.nameRoom.text = room.name
        holder.description.text = room.description
        val adminId: Long = room.idUserAdmin
        val roomId: Long = room.id!!

        holder.imageGame.setImageResource(R.drawable.game_example_2)

        val recyclerViewRoom = holder.listOfusers
        recyclerViewRoom.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL,
            false
        )

        usersInRoomAdapter =
            UsersInRoomAdapter(room.user, context)
        recyclerViewRoom.adapter = usersInRoomAdapter

        this.retrieveImageGame(room.gameName, holder)

        holder.button.setOnClickListener {
            enterRoom(adminId, roomId, holder, room.gameName)
        }
    }

    private fun retrieveImageGame(gameName: String, holder: ViewHolder) {
        val rest = Rest.getInstance()
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

                    Glide.with(context)
                        .load(link)
                        .placeholder(R.drawable.game_example_2)
                        .into(holder.imageGame)
                } else {
                    Log.e("Error", "Houve um erro ao buscar a imagem da sala")
                    Toast.makeText(
                        context,
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

    private fun enterRoom(
        managerUserId: Long,
        roomId: Long,
        holder: ViewHolder,
        roomGameName: String
    ) {
        if (true) {
            Rest.getInstance()
                .create(RoomService::class.java)
                .addCommonUser(idUser, roomId)
                .enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            val intent = Intent(context, ChatRoomActivity::class.java)
                            intent.putExtra("idGroup", roomId)
                            intent.putExtra("gameName", roomGameName)
                            context.startActivity(intent)
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.e("Room", "Erro ao entrar na sala!")

                        Toast.makeText(
                            holder.itemView.context,
                            "Erro ao entrar na sala!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        } else {
            val context = holder.button.context
            val intent = Intent(context, ChatRoomActivity::class.java)
            intent.putExtra("idGroup", roomId)
            context.startActivity(intent)
        }
    }
}

