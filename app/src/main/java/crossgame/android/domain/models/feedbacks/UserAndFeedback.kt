package crossgame.android.domain.models.feedbacks

data class UserAndFeedback(
    val id: Long,
    val username : String,
    val email:String,
    val role : String,
    val isOnline : Boolean,
    val feedback: Feedback
)

