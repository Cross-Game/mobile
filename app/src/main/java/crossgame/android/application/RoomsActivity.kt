package crossgame.android.application

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import crossgame.android.application.databinding.ActivityRoomsBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.rooms.Room
import crossgame.android.service.RoomService
import crossgame.android.ui.adapters.room.RoomAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomsBinding
    private lateinit var adapterRooms: RoomAdapter
    private var listRoom: MutableList<Room> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewRoom = binding.listOfRooms
        recyclerViewRoom.layoutManager = LinearLayoutManager(this)


        adapterRooms = RoomAdapter(listRoom, this)
        recyclerViewRoom.adapter = adapterRooms

        retrieveRooms()
    }

    private fun retrieveRooms() {

        val api = Rest.getInstance().create(RoomService::class.java)

        api.retrieveAll().enqueue(object : Callback<List<Room>> {
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {

                if (response.isSuccessful) {
                    response.body()?.let { listRoom.addAll(it) }
                    adapterRooms.notifyDataSetChanged()
                } else {
                    layoutWithoutRooms()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                layoutWithoutRooms()
            }
        })
    }

    private fun layoutWithoutRooms() {
        binding.listOfRooms.removeAllViews()
        layoutInflater.inflate(R.layout.empty_list_component, binding.listOfRooms)
    }
}