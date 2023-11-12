package crossgame.android.application.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import crossgame.android.application.databinding.FragmentNotificationBinding
import crossgame.android.domain.models.friends.Friends
import crossgame.android.domain.models.notifications.Notification
import crossgame.android.domain.models.notifications.NotificationState
import crossgame.android.domain.models.notifications.NotificationType
import crossgame.android.ui.adapters.friends.FriendsAdapter
import crossgame.android.ui.adapters.notification.NotificationAdapter
import java.time.LocalDateTime

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var recyclerView: RecyclerView
    private var notificationList: List<Notification> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(
            inflater,
            container,
            false
        )

        notificationList = listOf(
            Notification(
                id = 1,
                message = "New friend request from John Doe",
                description = "John Doe has sent you a friend request.",
                notificatioType = NotificationType.FRIEND_REQUEST,
                notificationState = NotificationState.AWAITING,
                date = LocalDateTime.of(2023, 11, 11, 18, 45)
            ),
            Notification(
                id = 2,
                message = "New game invitation from Jane Smith",
                description = "Jane Smith has invited you to play a game.",
                notificatioType = NotificationType.FRIEND_REQUEST,
                notificationState = NotificationState.AWAITING,
                date = LocalDateTime.of(2023, 11, 11, 18, 30)
            ),
            Notification(
                id = 3,
                message = "Tournament reminder: Weekly Clash",
                description = "The Weekly Clash tournament is about to start. Don't miss out on the chance to win prizes!",
                notificatioType = NotificationType.FRIEND_REQUEST,
                notificationState = NotificationState.AWAITING,
                date = LocalDateTime.of(2023, 11, 11, 17, 45)
            ),
            Notification(
                id = 4,
                message = "Reward received: 100 gold coins",
                description = "You have received a reward of 100 gold coins for completing a daily challenge.",
                notificatioType = NotificationType.FRIEND_REQUEST,
                notificationState = NotificationState.AWAITING,
                date = LocalDateTime.of(2023, 11, 11, 16, 0)
            )
        )

        val notificationAdapter = NotificationAdapter(requireContext(), notificationList)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = notificationAdapter

        return binding.root
    }
}
