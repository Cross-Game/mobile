package crossgame.android.service

import crossgame.android.domain.models.feedbacks.Feedback
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FeedbackService {

    @GET("/feedbacks/{userId}")
    fun listar(@Path("userId") userId: Long): Call<List<Feedback>>
}