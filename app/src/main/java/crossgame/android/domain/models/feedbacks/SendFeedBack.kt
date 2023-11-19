package crossgame.android.domain.models.feedbacks

import java.time.LocalDate

data class SendFeedBack(
    val userGivenFeedback: String,
    val behavior: Int,
    val skill: Int,
    val feedbackText: String
)
