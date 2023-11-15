package crossgame.android.domain.models.users

import java.util.Date

data class UserFriend(
    val id : Long,
    val username: String,
    val friendUserId: Long,
    val friendshipStartDate: Date,
    val friendshipState: String
)