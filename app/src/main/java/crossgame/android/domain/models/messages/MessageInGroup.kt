package crossgame.android.domain.models.messages

import com.google.firebase.Timestamp

data class MessageInGroup(
    var createdAt: Timestamp,
    var idGroup: Long,
    var photoUrl: String,
    var text: String,
    var uid: Long,
)
