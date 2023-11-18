package crossgame.android.domain.models.notifications

import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val message: String,
    val description: String,
    val date: String,
    val type: NotificationType,
    val state: NotificationState
)



