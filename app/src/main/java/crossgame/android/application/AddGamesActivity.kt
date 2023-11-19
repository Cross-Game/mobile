package crossgame.android.application
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
    private var isSearchBarOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGamesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setInverseBackgroundForced(true)
        progressDialog.setTitle("Carregando...")
        progressDialog.show()

        // Dentro do método `onCreate` após a configuração do botão de pesquisa:
        binding.buttonSearch.setOnClickListener {
            toggleSearchBar()
        }

        binding.closeSearchButton.setOnClickListener {
            toggleSearchBar()
        }
        binding.searchiconText.setOnClickListener {
            val gameName = binding.searchEditText.text.toString()
            if (gameName.isNotEmpty()) {
                searchGameByName(gameName)
            }
            binding.searchEditText.setText("") // Limpar o texto da barra de pesquisa

        }

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

    private fun toggleSearchBar() {
        if (!isSearchBarOpen) {
            binding.searchEditText.visibility = View.VISIBLE
            binding.closeSearchButton.visibility = View.VISIBLE
            binding.buttonSearch.visibility = View.GONE
            binding.buttonBack.visibility = View.GONE
            binding.searchiconText.visibility = View.VISIBLE
            isSearchBarOpen = true
        } else {
            binding.searchEditText.visibility = View.GONE
            binding.closeSearchButton.visibility = View.GONE
            binding.buttonSearch.visibility = View.VISIBLE
            binding.buttonBack.visibility = View.VISIBLE
            binding.searchiconText.visibility = View.GONE
            binding.searchEditText.setText("")
            isSearchBarOpen = false
        }
    }



    private fun searchGameByName(gameName: String) {
        val rest = Rest.getInstance(this)
        val service = rest.create(GamesService::class.java)

        service.retrieveGameIgdbByName(gameName).enqueue(object : Callback<GameResponse> {
            override fun onResponse(call: Call<GameResponse>, response: Response<GameResponse>) {
                if (response.isSuccessful) {
                    val game = response.body()
                    Toast.makeText(baseContext, "Jogo encontrado: ${game?.name}", Toast.LENGTH_SHORT).show()

                    // Recarregar a lista de jogos após encontrar o jogo
                    getAllGames()
                } else {
                    // Trate a falha na chamada da API
                    Toast.makeText(baseContext, "Falha ao buscar o jogo", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GameResponse>, t: Throwable) {
                // Trate a falha na requisição
                Log.e("GET", "Falha ao buscar o jogo", t)
                Toast.makeText(baseContext, "Falha ao buscar o jogo", Toast.LENGTH_SHORT).show()
            }
        })
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

