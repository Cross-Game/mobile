package crossgame.android.domain.models.user

data class User (
    val id: Long,
    val username:String,
    val email: String,
    val role: String,
    val isOnline: Boolean
)
