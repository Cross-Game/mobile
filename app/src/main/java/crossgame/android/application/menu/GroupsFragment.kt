package crossgame.android.application.menu

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import crossgame.android.application.R
import crossgame.android.application.databinding.BsCreatinRoomBinding
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
    private var isShowingMyRooms = false

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

        binding.addingRoom.setOnClickListener { showBottomSheetCreateRoom() }

        adapterRooms =
            RoomAdapter(listRoom, binding.root.context, getIdUserSigned(), getUserSignedName())
        recyclerViewRoom.adapter = adapterRooms

        val gestureDetector =
            GestureDetectorCompat(binding.root.context, object : SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    val sensitivity = 50f
                    if (e1!!.x - e2.x > sensitivity) {
                        val updateRoomsViewWithTransition = updateRoomsViewWithTransition(true)

                        if (updateRoomsViewWithTransition) {
                            retrieveMyRooms(true)
                            binding.idMyRoomsButton.setTextColor(resources.getColor(R.color.seed))
                            binding.idPublicRoomsButtom.setTextColor(resources.getColor(R.color.white))
                            return true
                        }

                        return false
                    } else if (e2.x - e1.x > sensitivity) {
                        val updateRoomsViewWithTransition = updateRoomsViewWithTransition(false)

                        if (updateRoomsViewWithTransition) {
                            retrievePublicRooms(true)
                            binding.idPublicRoomsButtom.setTextColor(resources.getColor(R.color.seed))
                            binding.idMyRoomsButton.setTextColor(resources.getColor(R.color.white))
                            return true
                        }
                        return false
                    }
                    return super.onFling(e1, e2, velocityX, velocityY)
                }
            })

        binding.listOfRooms.setOnTouchListener { v: View?, event: MotionEvent? ->
            gestureDetector.onTouchEvent(
                event!!
            )
        }

        return binding.root
    }

    private fun updateRoomsViewWithTransition(isSwipeToLeft: Boolean): Boolean {

        if (isShowingMyRooms != isSwipeToLeft) {
            isShowingMyRooms = isSwipeToLeft
            val transitionDistance = binding.listOfRooms.width.toFloat()


            val translationXAnimator = ObjectAnimator.ofFloat(
                binding.listOfRooms,
                View.TRANSLATION_X,
                binding.listOfRooms.translationX,
                if (isSwipeToLeft) -transitionDistance else transitionDistance
            )

            translationXAnimator.duration = 500

            translationXAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.listOfRooms.translationX = 0f
                }
            })

            translationXAnimator.start()
            return true
        }

        return false
    }

    override fun onStart() {
        super.onStart()
        retrievePublicRooms(true)

        binding.idMyRoomsButton.setOnClickListener {
            retrieveMyRooms(true)
            isShowingMyRooms = true
            binding.idMyRoomsButton.setTextColor(resources.getColor(R.color.seed))
            binding.idPublicRoomsButtom.setTextColor(resources.getColor(R.color.white))
        }

        binding.idPublicRoomsButtom.setOnClickListener {
            retrievePublicRooms(true)
            isShowingMyRooms = false
            binding.idPublicRoomsButtom.setTextColor(resources.getColor(R.color.seed))
            binding.idMyRoomsButton.setTextColor(resources.getColor(R.color.white))
        }
    }


    private fun retrievePublicRooms(isTeste: Boolean) {
        listRoom.clear()
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
                        "testeDescrição",
                        2L
                    ),
                    Room(
                        2L,
                        "abuda",
                        mutableListOf(User(1L, "Teste2", "Emails", "TEste", true)),
                        "ajksdhkhsg",
                        3L
                    )
                )
            )
            adapterRooms.notifyDataSetChanged()
            layoutWithoutRooms()
        }
    }

    private fun retrieveMyRooms(isTeste: Boolean) {
        listRoom.clear()
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
                        "testeDescrição",
                        1L
                    ),
                )
            )
            adapterRooms.notifyDataSetChanged()
            layoutWithoutRooms()
        }
    }

    private fun showBottomSheetCreateRoom() {
        val dialog = BottomSheetDialog(binding.root.context)
        val sheetBinding: BsCreatinRoomBinding =
            BsCreatinRoomBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)

        createRoom(sheetBinding)

        with(dialog.behavior) {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.show()
    }

    private fun createRoom(sheetBinding: BsCreatinRoomBinding) {
//        Rest.getInstance()
//            .create(RoomService::class.java)
//            .createRoom()
    }


    private fun layoutWithoutRooms() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.listOfRooms)
        val emptyLayout = view?.findViewById<View>(R.id.empty_list)
        val buttomCreateRoom = view?.findViewById<View>(R.id.createRoomIdComponent)

        if (listRoom.isEmpty()) {
            recyclerView?.visibility = View.GONE
            emptyLayout?.visibility = View.VISIBLE
            buttomCreateRoom?.setOnClickListener {
                showBottomSheetCreateRoom()
            }
        } else {
            recyclerView?.visibility = View.VISIBLE
            emptyLayout?.visibility = View.GONE
        }
    }

    private fun getUserSignedName(): String {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences.getString("username", "teste2").toString()
    }

    private fun getIdUserSigned(): Long {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id", 0).toLong()
    }

}