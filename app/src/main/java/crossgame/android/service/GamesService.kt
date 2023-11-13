package crossgame.android.service

import crossgame.android.domain.models.games.GameResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GamesService {
    @GET("/user-games/{userId}/games")
    fun listar(@Path("userId") userId: Long): Call<List<GameResponse>>

    @GET("/games-api/")
    fun listarJogos() : Call<List<GameResponse>>
}