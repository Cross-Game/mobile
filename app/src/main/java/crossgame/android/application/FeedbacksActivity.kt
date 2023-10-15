package crossgame.android.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.QuickContactBadge
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import crossgame.android.application.databinding.ActivityAddInterestsBinding
import crossgame.android.application.databinding.ActivityFeedbacksBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.feedbacks.Feedback
import crossgame.android.domain.models.users.UserPreference
import crossgame.android.service.FeedbackService
import crossgame.android.service.PreferencesService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedbacksActivity : AppCompatActivity() {
    private lateinit var binding : ActivityFeedbacksBinding

    var feedbacksService = Rest.getInstance().create(FeedbackService::class.java)

    var id = 1L

    var userFeedbacks = mutableListOf<Feedback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbacksBinding.inflate(layoutInflater)
        getAllUserFeedbacksInDatabase()
        buildingServices()
        setContentView(binding.root)
    }

    private fun buildingServices() {
        feedbacksService = Rest.getInstance().create(FeedbackService::class.java)
    }

    fun getAllUserFeedbacksInDatabase() {
        Log.i("GET", "Listando Feedbacks")
        feedbacksService.listar(id).enqueue(object : Callback<List<Feedback>> {
            override fun onResponse(
                call: Call<List<Feedback>>,
                response: Response<List<Feedback>>
            ) {
                if (response.isSuccessful) {
                    Log.i("GET", "Sucesso ao listar Feedbacks")
                    val apiResponse = response.body()
                    apiResponse?.forEach { feedback ->
                        userFeedbacks.add(feedback)
                        criarCardsFeedbacks(feedback)
                    }
                }
            }

            override fun onFailure(call: Call<List<Feedback>>, t: Throwable) {
                Log.e("ERROR", "Erro ao Feedbacks: " + t.message.toString())
            }
        })
    }

    fun criarCardsFeedbacks(feedback: Feedback) {
        val cardView = layoutInflater.inflate(R.layout.card_feedback, null) as CardView
        val userNameTextView = cardView.findViewById<TextView>(R.id.text_nameUser)
        val behaviorRatingBar = cardView.findViewById<RatingBar>(R.id.ratingBar_comportamento)
        val skillRatingBar = cardView.findViewById<RatingBar>(R.id.ratingBar_habilidade)
        val messageTextView = cardView.findViewById<TextView>(R.id.input_message)

        userNameTextView.text = feedback.userGivenFeedback
        behaviorRatingBar.rating = feedback.behavior.toFloat()
        skillRatingBar.rating = feedback.skill.toFloat()
        messageTextView.text = feedback.feedbackText

        // Adicione o cardView à sua exibição principal ou layout, por exemplo:
        binding.body.addView(cardView)
    }
}