package crossgame.android.domain.models.user

data class User(
    val username: String,
    val password: String,
    val email: String,
    val token: String
)
