package crossgame.android.domain.models.feedbacks

import java.time.LocalDate

data class Feedback (
    val id: Int,
    val userGivenFeedback: String,
    val behavior: Int,
    val skill: Int,
    val feedbackText: String,
    val feedbackGivenDate: String
)

