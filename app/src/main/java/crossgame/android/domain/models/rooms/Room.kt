package crossgame.android.domain.models.rooms

data class Room(
    val id: Long,
    val idUserAdmin: Integer,
    val roomName: String,
    val capacity : Integer,
    val gameName : String,
    val description : String,
    val tokenAcess : String,
    val terminated : Boolean,
//    val usersInRoom : MutableList<User>
)
