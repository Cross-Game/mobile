package crossgame.android.application.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.R
import crossgame.android.application.databinding.FragmentGroupsBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.rooms.Room
import crossgame.android.service.RoomService
import crossgame.android.ui.adapters.room.RoomAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding
    private lateinit var adapterRooms: RoomAdapter
    private var listRoom: MutableList<Room> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(
            inflater,
            container,
            false
        )

        val recyclerViewRoom = binding.listOfRooms
        recyclerViewRoom.layoutManager = LinearLayoutManager(binding.root.context)


        adapterRooms = RoomAdapter(listRoom, binding.root.context)
        recyclerViewRoom.adapter = adapterRooms

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        retrieveRooms(true)
    }

    private fun retrieveRooms(isTeste: Boolean) {
        if (!isTeste) {
            val api = Rest.getInstance().create(RoomService::class.java)

            api.retrieveAll().enqueue(object : Callback<List<Room>> {
                override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {

                    if (response.isSuccessful) {
                        response.body()?.let { listRoom.addAll(it) }
                        adapterRooms.notifyDataSetChanged()
                        layoutWithoutRooms()
                    } else {
                        layoutWithoutRooms()
                    }
                }

                override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                    layoutWithoutRooms()
                }
            })

        } else {
            listRoom.addAll(
                mutableListOf(
                    Room("teste", "testeDescrição")
                )
            )
            layoutWithoutRooms()
        }
    }

    private fun layoutWithoutRooms() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.listOfRooms)
        val emptyLayout = view?.findViewById<View>(R.id.empty_list)

        if (listRoom.isEmpty()) {
            recyclerView?.visibility = View.GONE
            emptyLayout?.visibility = View.VISIBLE
        } else {
            recyclerView?.visibility = View.VISIBLE
            emptyLayout?.visibility = View.GONE
        }
    }

}