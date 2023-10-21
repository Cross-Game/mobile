package crossgame.android.domain.httpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Rest {
    val baseUrl = "https://matchmakingapi.sytes.net/"

    fun getInstance(): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}