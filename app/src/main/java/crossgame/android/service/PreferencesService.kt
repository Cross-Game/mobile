package crossgame.android.service

import crossgame.android.domain.models.preferences.Preference
import crossgame.android.domain.models.users.UserPreference
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PreferencesService {

    @GET("/preferences/{userId}")
    fun listar(@Path("userId") userId: Long): Call<UserPreference>

    @POST("/preferences/{userId}")
    fun adicionar(@Path("userId") userId: Long, @Body preferences: Set<String>): Call<Void>

    @DELETE("/preferences/{userId}/name/{name}")
    fun deletar(@Path("userId") userId: Long, @Path("name") namePreference : String) : Call<Void>
}