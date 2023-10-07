package crossgame.android.service

import crossgame.android.domain.models.preferences.Preference
import crossgame.android.domain.models.users.UserPreference
import retrofit2.Call
import retrofit2.http.GET

interface PreferencesService {

    @GET("/preferences/{userId}")
    fun listar(userId : Long): Call<UserPreference>
}