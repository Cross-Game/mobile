package crossgame.android.infra

import android.util.Log
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.users.BaseUser
import crossgame.android.domain.models.users.UserFriend
import crossgame.android.domain.models.users.UserMatch
import crossgame.android.service.FriendsService
import crossgame.android.service.GamesService
import crossgame.android.service.UsersService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MatchHelper {

    private val friendsService = Rest.getInstance().create(FriendsService::class.java)
    private val usersService = Rest.getInstance().create(UsersService::class.java)
    private val gamesService = Rest.getInstance().create(GamesService::class.java)

    private suspend fun getFriends(id: Long): List<UserFriend> = withContext(Dispatchers.IO) {
        try {
            logChamada("getFriends")
            val response = friendsService.retrieveFriendsForUserById(id).execute()
            if (response.isSuccessful) {

                logSucesso("getFriends")
                return@withContext response.body() ?: emptyList()
            }
            Log.e("ERRO", response.body().toString())
        } catch (e: Exception) {
            Log.e("EXCEPTION", e.message.toString())
        }
        return@withContext emptyList()
    }

    private suspend fun getUsersNotFriends(id: Long): List<BaseUser> = withContext(Dispatchers.IO) {
        val allUsers = getAllUsers()
        val friends = getFriends(id)

        Log.i("TESTE", "Quantidade de usuários: " + allUsers.size)
        Log.i("TESTE", "Quantidade de amigos: " + friends.size)

        val nonFriends = allUsers.filter { user ->
            friends.none { it.friendUserId == user.id }
        }

        return@withContext nonFriends
    }

    private suspend fun getAllUsers(): List<BaseUser> = withContext(Dispatchers.IO) {
        try {
            logChamada("getAllUsers")
            val response = usersService.GetAllUsers().execute()
            if (response.isSuccessful) {
                logSucesso("getAllUsers")
                return@withContext response.body() ?: emptyList()
            }
            Log.e("ERRO", response.body().toString())
        } catch (e: Exception) {
            Log.e("EXCEPTION", e.toString())
        }
        return@withContext emptyList()
    }

    private suspend fun getGames(id: Long) : List<GameResponse> = withContext(Dispatchers.IO) {
        try {
            logChamada("getGames")
            val response = gamesService.listar(id).execute()
            if (response.isSuccessful) {

                logSucesso("getGames" + response.body()?.size)
                return@withContext response.body() ?: emptyList()
            }
            Log.e("ERRO", response.body().toString())

        }
        catch (e: Exception) {
            Log.e("EXCEPTION", e.toString())
        }
        return@withContext emptyList();
    }

    suspend fun getUsersForMatch(id: Long) : List<UserMatch> {
        val users = getUsersNotFriends(id)
        Log.i("Teste", "Quantidade de matchers: " + users.size)

        val response = mutableListOf<UserMatch>()

        users.forEach { user ->
            val gamesResponse = getGames(user.id)
            val imageResponse = getImage(user.id)
            val imageDefault = "10";

            if (gamesResponse.isNotEmpty()) {
                val userForMatch = UserMatch(
                    baseUser = BaseUser(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        role = user.role,
                        isOnline = user.isOnline
                    ),
                    games = gamesResponse,
                    img = imageResponse ?: imageDefault
                )
                response.add(userForMatch)
                Log.i("Info", "Pessoa Adicionada: " + userForMatch.toString())
            }
        }
        return response;
    }

    suspend fun getGames() : List<GameResponse> = withContext(Dispatchers.IO) {
        try {
            logChamada("getGames")
            val response = gamesService.listarJogos().execute()
            if (response.isSuccessful){
                logSucesso("getGames")
                return@withContext response.body() ?: emptyList();
            }
            Log.e("ERRO", response.body().toString())
        }
        catch (e : Exception){
            Log.e("EXCEPTION", e.toString())
        }
        return@withContext emptyList();
    }

    private fun getImage(id : Long) : String{
        return "123";
    }

    private fun logChamada(metodo : String) {
        Log.i("INFO", "chamei a função " + metodo.toUpperCase())
    }

    private fun logSucesso(metodo : String) {
        Log.i("SUCESSO", "sucesso ao responder " + metodo.toUpperCase())
    }
}