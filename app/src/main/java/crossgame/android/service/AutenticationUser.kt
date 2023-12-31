package crossgame.android.service

import com.google.gson.GsonBuilder
import crossgame.android.domain.models.user.UserRegisterRequest
import crossgame.android.domain.models.user.UserRequest
import crossgame.android.domain.models.user.UserResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AutenticationUser {

    @POST("/user-auth")
    fun singIn(@Body userRequest: UserRequest) :
            Call<UserResponse>
    @POST("/users")
    fun singUp(@Body userRegisterRequest: UserRegisterRequest) :
            Call<GsonBuilder>

    @PATCH("/users/{userId}/picture")
    @Headers("Content-Type: image/jpeg")
    fun uploadImage(@Path("userId") userId: Long, @Body image: RequestBody) : Call<Unit>;

    @GET("/users/{userId}/picture")
    fun getPhoto(@Path("userId") userId: Long) : Call<ResponseBody>
}