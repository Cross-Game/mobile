package crossgame.android.infra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.View
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.feedbacks.Feedback
import crossgame.android.domain.models.feedbacks.MediaFeedback
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.platforms.GameplayPlatformType
import crossgame.android.domain.models.users.BaseUser
import crossgame.android.domain.models.users.UserFriend
import crossgame.android.domain.models.users.UserMatch
import crossgame.android.service.AutenticationUser
import crossgame.android.service.FeedbackService
import crossgame.android.service.FriendsService
import crossgame.android.service.GamesService
import crossgame.android.service.PlatformsService
import crossgame.android.service.PreferencesService
import crossgame.android.service.UserFriendService
import crossgame.android.service.UsersService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class MatchHelper(private val context: Context) {

    private val friendsService = Rest.getInstance(context).create(FriendsService::class.java)
    private val friendsService2 = Rest.getInstance(context).create(UserFriendService::class.java)
    private val usersService = Rest.getInstance(context).create(UsersService::class.java)
    private val gamesService = Rest.getInstance(context).create(GamesService::class.java)
    private val imageService = Rest.getInstance(context).create(AutenticationUser::class.java)
    private val feedbackService = Rest.getInstance(context).create(FeedbackService::class.java)
    private val platformsService = Rest.getInstance(context).create(PlatformsService::class.java)
    private val interestingService = Rest.getInstance(context).create(PreferencesService::class.java)

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
                val feedbackRespone = getMediaFeedbacks(user.id)
                val preferenceResponse = getInteresses(user.id)
                val platformResponse = getPlatforms(user.id)
                val friendsResponse = getFriends(user.id)

                val userForMatch = UserMatch(
                    baseUser = BaseUser(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        role = user.role,
                        isOnline = user.isOnline
                    ),
                    games = gamesResponse,
                    img = imageResponse ?: imageDefault,
                    feedback = MediaFeedback(
                        mediaComportamento = feedbackRespone.mediaComportamento,
                        mediaHabilidade = feedbackRespone.mediaHabilidade,
                        quantidadeFeedbacks = feedbackRespone.quantidadeFeedbacks,
                    ),
                    preference = preferenceResponse,
                    platforms = platformResponse,
                    qtdFriends = friendsResponse.size
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

    private fun getFeedbacks(id: Long) : List<Feedback> {
        val response = feedbackService.listar(id).execute()
        if (response.isSuccessful) {
            logSucesso("getFeedbacks")
            return response.body() ?: emptyList();
        }
        return emptyList();
    }

    private fun getPlatforms(id: Long) : List<String> {
        val response = platformsService.retrieveGamePlatformsForUserById(id).execute()
        if (response.isSuccessful) {
            logSucesso("getFeedbacks")
            return bindingPlatform(response.body())
        }
        return emptyList();
    }

    private fun getInteresses(id: Long) : List<String> {
        val listOfPreferences = mutableListOf<String>();

        val response = interestingService.listar(id).execute()
        if (response.isSuccessful) {
            logSucesso("getInteresses")

            val preferences = response.body()?.preferences
            preferences?.forEach { preferences ->
                listOfPreferences.add(preferences.preferences)
            }
            return listOfPreferences;
        }
        return emptyList();
    }

    private fun getMediaFeedbacks(id: Long): MediaFeedback {
        val feedbacks = getFeedbacks(id)

        if (feedbacks.isNotEmpty()) {
            var totalComportamento = 0
            var totalHabilidade = 0

            feedbacks.forEach {
                totalComportamento += it.behavior
                totalHabilidade += it.skill
            }

            val mediaComportamento = totalComportamento / feedbacks.size
            val mediaHabilidade = totalHabilidade / feedbacks.size
            val quantidadeFeedbacks = feedbacks.size

            return MediaFeedback(mediaComportamento, mediaHabilidade, quantidadeFeedbacks)
        } else {

            return MediaFeedback(0, 0, 0)
        }
    }

    private fun bindingPlatform(body : List<GameplayPlatformType>?) : List<String>{
        var response = mutableListOf<String>()

        body?.map {
            when (it.name) {
                "PC" -> response.add("PC")
                "XBOX" ->  response.add("XBOX")
                "PLAYSTATION" ->  response.add("PLAYSTATION")
                else -> response.add("MOBILE")
            }
        }
        return response;
    }
    private fun logChamada(metodo : String) {
        Log.i("INFO", "chamei a função " + metodo.toUpperCase())
    }

    private fun logSucesso(metodo : String) {
        Log.i("SUCESSO", "sucesso ao responder " + metodo.toUpperCase())
    }
}