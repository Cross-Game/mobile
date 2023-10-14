package crossgame.android.domain.models.user

data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String
)
