package crossgame.android.domain.httpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Rest {

    //val baseUrl = "https://61c71fed90318500175472ff.mockapi.io/api/"
    //val baseUrl = "http://localhost:8080"
    val baseUrl = "http://192.168.15.9:8080"

    fun getInstance(): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}