package crossgame.android.application.menu

import NotificationAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.databinding.FragmentNotificationBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.notifications.Notification
import crossgame.android.domain.models.notifications.NotificationState
import crossgame.android.domain.models.notifications.NotificationType
import crossgame.android.service.AutenticationUser
import crossgame.android.service.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        CoroutineScope(Dispatchers.Main).launch {
            notificationList = fetchNotifications()

        }
        val notificationAdapter = NotificationAdapter(requireContext(), notificationList)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = notificationAdapter

        return binding.root
    }

    private suspend fun fetchNotifications(): List<Notification> {
        val sharedPreferences = requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", 0).toLong()
        val rest = Rest.getInstance(requireActivity())

        return withContext(Dispatchers.IO) {
            val response = rest.create(NotificationService::class.java).retrieveNotifications(1).execute()
            if (response.isSuccessful) {
                return@withContext response.body() ?: emptyList()
            } else {
                emptyList()
            }
        }
    }


}
