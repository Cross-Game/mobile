package crossgame.android.domain.models.notifications

import java.time.LocalDateTime

data class Notification(
    val id: Long,
    val message : String,
    val description : String,
    val date : LocalDateTime,
    val  notificationState : NotificationState,
    val notificatioType: NotificationType

)
