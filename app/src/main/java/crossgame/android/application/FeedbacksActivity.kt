package crossgame.android.application

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import crossgame.android.application.databinding.ActivityFeedbacksBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.feedbacks.Feedback
import crossgame.android.service.FeedbackService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedbacksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbacksBinding

    var userFeedbacks = mutableListOf<Feedback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbacksBinding.inflate(layoutInflater)
        buildingServices()
        getAllUserFeedbacksInDatabase()
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener { backProfile() }
    }

    private fun backProfile() {
        finish()
    }

    private fun buildingServices() {
        var feedbacksService = Rest.getInstance(this).create(FeedbackService::class.java)
        feedbacksService = Rest.getInstance(this).create(FeedbackService::class.java)
    }

    fun getAllUserFeedbacksInDatabase() {
        Log.i("GET", "Listando Feedbacks")
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val idUser = sharedPreferences.getInt("id", 0)
        var feedbacksService = Rest.getInstance(this).create(FeedbackService::class.java)
        feedbacksService.listar(idUser.toLong()).enqueue(object : Callback<List<Feedback>> {
            override fun onResponse(
                call: Call<List<Feedback>>,
                response: Response<List<Feedback>>
            ) {
                if (response.isSuccessful) {
                    Log.i("GET", "Sucesso ao listar Feedbacks")
                    val apiResponse = response.body()
                    if (apiResponse?.isEmpty() == null) {
                        createCardEmpty()
                    } else {
                        apiResponse?.forEach { feedback ->
                            userFeedbacks.add(feedback)
                            criarCardsFeedbacks(feedback)
                        }
                        atualizarQuantidadeDeFeedbacks()
                    }
                }
            }

            override fun onFailure(call: Call<List<Feedback>>, t: Throwable) {
                Log.e("ERROR", "Erro ao Feedbacks: " + t.message.toString())
            }
        })
    }

    fun countFeedbacksInDatabase(): Int {
        return userFeedbacks.size
    }

    fun atualizarQuantidadeDeFeedbacks() {
        binding.textTotal.text = countFeedbacksInDatabase().toString();
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

    fun createCardEmpty() {
        val messageOfEmpty = "Você ainda não possui Feedbacks !"

        val textView = TextView(this)
        textView.text = messageOfEmpty
        textView.setTextColor(resources.getColor(R.color.seed))
        textView.setTextSize(18.0F)
        textView.gravity = Gravity.CENTER
        binding.body.addView(textView)
    }
}