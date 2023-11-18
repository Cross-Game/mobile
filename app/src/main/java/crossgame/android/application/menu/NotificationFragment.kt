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
import com.google.android.material.snackbar.Snackbar
import crossgame.android.application.databinding.FragmentNotificationBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.notifications.NotificationResponse
import crossgame.android.domain.models.notifications.NotificationState
import crossgame.android.service.NotificationService
import crossgame.android.ui.adapters.notification.SnackbarNotifier
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationFragment : Fragment(), SnackbarNotifier {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var recyclerView: RecyclerView
    private var notificationList: List<NotificationResponse> = mutableListOf()
    private lateinit var notificationAdapter: NotificationAdapter


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
        recyclerView = binding.recyclerView
        notificationAdapter = NotificationAdapter(this,requireContext(), notificationList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = notificationAdapter
         retrieveNotifications()

        return binding.root
    }


    private fun retrieveNotifications() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", 0).toLong()

        val rest = Rest.getInstance()
        val service = rest.create(NotificationService::class.java)

        service.retrieveNotifications(userId).enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(
                call: Call<List<NotificationResponse>>,
                response: Response<List<NotificationResponse>>
            ) {
                if (response.isSuccessful){
                    notificationList = response.body()!!.filter { it.state != NotificationState.CANCELLED }
                    notificationAdapter.updateData(notificationList)

                    }
            }

            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                Log.e("GET", "Falha ao listar notificações", t)
                t.printStackTrace()
  }

        })
    }

    override fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()

    }


}
