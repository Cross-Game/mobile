package crossgame.android.domain.httpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Rest {
    val baseUrl = "http://{ip da sua maquina}:8080/"

    fun getInstance(): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}