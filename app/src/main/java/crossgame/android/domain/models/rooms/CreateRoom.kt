package crossgame.android.domain.models.rooms

import crossgame.android.domain.models.user.User

data class CreateRoom(
    val roomName: String,
    val gameName: String,
    val usersInRoom: MutableList<User>,
    val description: String,
    val idUserAdmin: Long,
    val usersHistoryId : MutableList<Long>,
    val capacity: Int

)
