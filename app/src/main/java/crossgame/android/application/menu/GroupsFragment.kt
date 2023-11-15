package crossgame.android.application.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import crossgame.android.application.R
import crossgame.android.application.databinding.BsEditProfileBinding
import crossgame.android.application.databinding.BsCreatinRoomBinding
import crossgame.android.application.databinding.EmptyListComponentBinding
import crossgame.android.application.databinding.FragmentGroupsBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.rooms.Room
import crossgame.android.domain.models.user.User
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

        binding.addingRoom.setOnClickListener { showBottomSheet() }

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
                    Room(
                        1L,
                        "teste",
                        mutableListOf(User(1L, "Teste2", "Emails", "TEste", true)),
                        "testeDescrição"
                    ),
                    Room(
                        2L,
                        "abuda",
                        mutableListOf(User(1L, "Teste2", "Emails", "TEste", true)),
                        "ajksdhkhsg"
                    )
                )
            )
            layoutWithoutRooms()
        }
    }

    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(binding.root.context)
        val sheetBinding: BsCreatinRoomBinding =
            BsCreatinRoomBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)

        with(dialog.behavior) {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.show()
    }


    private fun layoutWithoutRooms() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.listOfRooms)
        val emptyLayout = view?.findViewById<View>(R.id.empty_list)
        val buttomCreateRoom = view?.findViewById<View>(R.id.createRoomIdComponent)

        if (listRoom.isEmpty()) {
            recyclerView?.visibility = View.GONE
            emptyLayout?.visibility = View.VISIBLE
            buttomCreateRoom?.setOnClickListener {
                showBottomSheet()
            }
        } else {
            recyclerView?.visibility = View.VISIBLE
            emptyLayout?.visibility = View.GONE
        }
    }

}