package crossgame.android.infra

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.users.BaseUser
import crossgame.android.domain.models.users.UserFriend
import crossgame.android.domain.models.users.UserMatch
import crossgame.android.service.AutenticationUser
import crossgame.android.service.FriendsService
import crossgame.android.service.GamesService
import crossgame.android.service.UserFriendService
import crossgame.android.service.UsersService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.ByteArrayOutputStream
import java.io.InputStream

class MatchHelper {

    private val friendsService = Rest.getInstance().create(FriendsService::class.java)
    private val friendsService2 = Rest.getInstance().create(UserFriendService::class.java)
    private val usersService = Rest.getInstance().create(UsersService::class.java)
    private val gamesService = Rest.getInstance().create(GamesService::class.java)
    private val imageService = Rest.getInstance().create(AutenticationUser::class.java)

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
        try {
            val allUsers = getAllUsers()
            val friends = getFriends(id)

            Log.i("TESTE", "Todos os usuários: " + allUsers.joinToString { it.toString() })
            Log.i("TESTE", "Amigos: " + friends.joinToString { it.toString() })

            val nonFriends = allUsers.filter { user ->
                user.id != id && friends.none { it.friendUserId == user.id }
            }

            Log.i("TESTE", "Nao Amigos: " + nonFriends.joinToString { it.toString() })

            return@withContext nonFriends
        } catch (e: Exception) {
            Log.e("EXCEPTION", "Erro ao filtrar usuários não amigos: ${e.message}", e)
            return@withContext emptyList()
        }
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

    suspend fun getUsersForMatch(id: Long): List<UserMatch> = coroutineScope {
        val users = getUsersNotFriends(id)
        Log.i("Teste", "Quantidade de matchers: " + users.size)

        val response = mutableListOf<UserMatch>()

        val deferredUserMatches = users.map { user ->
            async {
                val gamesResponse = getGames(user.id)
                val imageResponse = getImage(user.id)
                val imageDefault = "10"

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
                Log.i("Info", "Pessoa Adicionada: " + userForMatch.toString())
                userForMatch
            }
        }

        response.addAll(deferredUserMatches.awaitAll())
        Log.i("TESTE", "Users for Match: " + response.joinToString { it.toString() })

        return@coroutineScope response
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

    suspend fun getImage(friendUserId: Long): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response = imageService.getPhoto(friendUserId).execute()
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        if (responseBody.contentLength() > 0) {
                            val inputStream: InputStream = responseBody.byteStream()
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            return@withContext Base64.encodeToString(byteArray, Base64.DEFAULT)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GET", "Falha ao obter a foto de perfil", e)
            }

            // Se não houver imagem, retorne uma string que representa um drawable padrão
            return@withContext "default_image"
        }
    }


    private fun logChamada(metodo : String) {
        Log.i("INFO", "chamei a função " + metodo.toUpperCase())
    }

    private fun logSucesso(metodo : String) {
        Log.i("SUCESSO", "sucesso ao responder " + metodo.toUpperCase())
    }
}