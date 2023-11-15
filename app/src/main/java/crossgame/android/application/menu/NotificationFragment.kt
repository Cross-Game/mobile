package crossgame.android.application.menu

import NotificationAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.databinding.FragmentNotificationBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.friends.Friends
import crossgame.android.domain.models.notifications.Notification
import crossgame.android.domain.models.notifications.NotificationState
import crossgame.android.domain.models.notifications.NotificationType
import crossgame.android.domain.models.user.UserList
import crossgame.android.service.AutenticationUser
import crossgame.android.service.NotificationService
import crossgame.android.service.UserFriendService
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

            notificationList = retrieveNotifications()


        val notificationAdapter = NotificationAdapter(requireContext(), notificationList)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = notificationAdapter

        return binding.root
    }


    private fun retrieveNotifications() : List<Notification> {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", 0).toLong()

        val rest = Rest.getInstance(requireActivity())
        val service = rest.create(NotificationService::class.java)

        service.retrieveNotifications(userId).enqueue(object : Callback<List<Notification>> {
            override fun onResponse(
                call: Call<List<Notification>>,
                response: Response<List<Notification>>
            ) {
                if (response.isSuccessful){
                    notificationList = response.body()!!
                    }
            }

            override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                println("Erro")
            }

        })
        return  notificationList
    }
}
