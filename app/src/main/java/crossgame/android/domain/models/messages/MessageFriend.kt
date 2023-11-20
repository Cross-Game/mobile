package crossgame.android.domain.models.messages

import com.google.firebase.Timestamp

data class MessageFriend(
    var createdAt: Timestamp,
    var text: String,
    var uid: Long,
    var uid2: Long,
    var users: List<Long>
)
