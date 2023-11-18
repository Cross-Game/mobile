package crossgame.android.service

import crossgame.android.domain.models.feedbacks.Feedback
import crossgame.android.domain.models.feedbacks.SendFeedBack
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FeedbackService {
    @GET("/feedbacks/{userId}")
    fun listar(@Path("userId") userId: Long): Call<List<Feedback>>

    @POST("feedback/{userId}")
    fun sendFeedBackToUser(@Path("userId") userId: Long, @Body feedBack: SendFeedBack): Call<Feedback>
}