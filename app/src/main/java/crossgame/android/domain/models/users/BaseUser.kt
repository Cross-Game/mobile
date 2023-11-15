package crossgame.android.domain.models.users

data class BaseUser(
    val id : Long,
    val username : String,
    val email : String,
    val role : String,
    val isOnline : Boolean
)
