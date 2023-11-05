package crossgame.android.domain.models.user

import java.util.Date

data class UserList(
    val id: Long,
    val username:String,
    val friendUserId: Long,
    val friendshipStartDate: Date,
    val friendshipState: Boolean
)
