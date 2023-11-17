package crossgame.android.application
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import crossgame.android.application.databinding.ActivityAddGamesBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.games.GameRequestPost
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.games.ImageGame
import crossgame.android.service.GamesService
import crossgame.android.ui.adapters.games.GamesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

class AddGamesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAddGamesBinding
    private var originalGamesList: List<GameResponse> = mutableListOf()
    private lateinit var gamesAdapter: GamesAdapter
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGamesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setInverseBackgroundForced(true)
        progressDialog.setTitle("Carregando...")
        progressDialog.show()
        val recyclerView = binding.listGames
        gamesAdapter = GamesAdapter(this, mutableListOf()) {
            nomeItem, idItem ->
            insertGameForUser(nomeItem, idItem)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = gamesAdapter

        getAllGames()
        binding.buttonBack.setOnClickListener { backScreen() }
        val timer = object : CountDownTimer(3000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
            }
            override fun onFinish() {
                progressDialog.dismiss()
            }
        }
        timer.start()
    }

    private fun backScreen() {
        finish()
    }

    fun getAllGames(){
        val rest = Rest.getInstance(this)
        val service = rest.create(GamesService::class.java)

        service.listarJogos().enqueue(object : Callback<List<GameResponse>> {
            override fun onResponse(
                call: Call<List<GameResponse>>,
                response: Response<List<GameResponse>>
            ) {
                if (response.isSuccessful) {
                    Log.i("GET", "Listagem de amigos realizada com sucesso")
                    val apiResponse = response.body()

                    // Limpa a lista existente e adiciona novos amigos
                    originalGamesList = apiResponse?.map {
                        val imageGame = ImageGame(it.imageGame.id, it.imageGame.typeImage,
                            it.imageGame.link, it.imageGame.image_id)
                        GameResponse(it.id, it.platformsType, imageGame, it.gameGenres, it.name,
                            it.platforms, it.cover, it.genres)
                    } ?: emptyList()

                    // Notifica o adaptador sobre a mudança nos dados
                    gamesAdapter.updateData(originalGamesList)
                }
            }

            override fun onFailure(call: Call<List<GameResponse>>, t: Throwable) {
                Log.e("GET", "Falha ao listar os Jogos", t)
            }
        })
    }

    fun getAllUserGames(){

    }

    fun insertGameForUser(nomeItem: String, idItem: Long) {
        val rest = Rest.getInstance(baseContext)
        val service = rest.create(GamesService::class.java)
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        var idUser = sharedPreferences.getInt("id", 0).toLong()

        val body = GameRequestPost(GenericGamersIds = listOf(idItem.toInt()))
        service.saveGameIntoUser(idItem, idUser, body).enqueue(object : Callback<GsonConverterFactory> {
            override fun onResponse(
                call: Call<GsonConverterFactory>,
                response: Response<GsonConverterFactory>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(baseContext, "Jogo Adicionado: $nomeItem", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(baseContext, "Jogo não Adicionado: $nomeItem", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GsonConverterFactory>, t: Throwable) {
                Log.e("GET", "Falha ao listar os Jogos", t)
            }
        })
    }

    fun deleteGameForUser(){

    }

    fun compareListOfGames(){

    }
}

