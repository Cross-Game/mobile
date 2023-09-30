package crossgame.android.domain.models.notifications

import java.time.LocalDateTime

data class Notification(
    val id: Long,
//    val notificationType : NotificationType,
    val message : String,
    val description : String,
    val date : LocalDateTime
)
