package crossgame.android.domain.models.notifications

data class NotificationRequest(
    val message: String,
    val description: String,
    val type: NotificationType,
    val state: NotificationState
)