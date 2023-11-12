package crossgame.android.domain.models.rooms

import crossgame.android.domain.models.user.User

data class Room(
    val id: Long,
    val name: String,
//    val capacity: Int,
//    val gameName: String,
//    val rankName: String,
//    val levelGame: String,
    val users: MutableList<User>,
//    val isPrivate: Boolean,
//    val tokeAccess: String,
    val description: String
//    val isTerminated: Boolean,
//    val idUserAdmin: Long,
)
