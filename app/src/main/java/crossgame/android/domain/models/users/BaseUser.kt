package crossgame.android.domain.models.users

data class BaseUser(
    val id : Int,
    val username : String,
    val email : String,
    val role : String,
    val isOnline : Boolean
)
