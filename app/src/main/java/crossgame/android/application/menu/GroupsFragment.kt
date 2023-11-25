package crossgame.android.application.menu

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import crossgame.android.application.ChatRoomActivity
import crossgame.android.application.R
import crossgame.android.application.databinding.BsCreatinRoomBinding
import crossgame.android.application.databinding.BsGameListBinding
import crossgame.android.application.databinding.FragmentGroupsBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.games.ImageGame
import crossgame.android.domain.models.rooms.CreateRoom
import crossgame.android.domain.models.rooms.Room
import crossgame.android.service.GamesService
import crossgame.android.service.RoomService
import crossgame.android.ui.adapters.games.GamesAdapter
import crossgame.android.ui.adapters.room.RoomAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding
    private lateinit var rootView: View
    private lateinit var adapterRooms: RoomAdapter
    private var listRoom: MutableList<Room> = mutableListOf()
    private var isShowingMyRooms = false
    private lateinit var gamesAdapter: GamesAdapter
    private var originalGamesList: List<GameResponse> = mutableListOf()
    var gamesForFilter = mutableListOf<String>()
    private lateinit var recyclerViewRoom: RecyclerView

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

        recyclerViewRoom = binding.listOfRooms
        rootView = binding.root

        recyclerViewRoom.layoutManager =
            LinearLayoutManager(
                binding.root.context,
            )

        binding.addingRoom.setOnClickListener { showBottomSheetCreateRoom() }

        binding.buttonSort.setOnClickListener {
            Log.i("ButtonSort", "Abrindo modal")
            CoroutineScope(Dispatchers.Main).launch {
                showBottomSheetGameFilter()
            }
        }

        adapterRooms =
            RoomAdapter(listRoom, binding.root.context, getIdUserSigned(), getUserSignedName())
        recyclerViewRoom.adapter = adapterRooms

        listGames();
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
                            retrieveMyRooms()
                            binding.idMyRoomsButton.setTextColor(resources.getColor(R.color.seed))
                            binding.idPublicRoomsButtom.setTextColor(resources.getColor(R.color.white))
                            return true
                        }

                        return false
                    } else if (e2.x - e1.x > sensitivity) {
                        val updateRoomsViewWithTransition = updateRoomsViewWithTransition(false)

                        if (updateRoomsViewWithTransition) {
                            retrievePublicRooms()
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
        retrievePublicRooms()

        binding.idMyRoomsButton.setOnClickListener {
            retrieveMyRooms()
            isShowingMyRooms = true
            binding.idMyRoomsButton.setTextColor(resources.getColor(R.color.seed))
            binding.idPublicRoomsButtom.setTextColor(resources.getColor(R.color.white))
        }

        binding.idPublicRoomsButtom.setOnClickListener {
            retrievePublicRooms()
            isShowingMyRooms = false
            binding.idPublicRoomsButtom.setTextColor(resources.getColor(R.color.seed))
            binding.idMyRoomsButton.setTextColor(resources.getColor(R.color.white))
        }
    }


    private fun retrievePublicRooms() {
        listRoom.clear()
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

    }

    private fun retrieveMyRooms() {
        val filter = listRoom.filter {
            it.idUserAdmin == getIdUserSigned()
        }

        listRoom.clear()
        listRoom.addAll(filter)
        adapterRooms.notifyDataSetChanged()

        layoutWithoutRooms()
    }


    private fun showBottomSheetCreateRoom() {
        val dialog = BottomSheetDialog(binding.root.context)
        val sheetBinding: BsCreatinRoomBinding =
            BsCreatinRoomBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)

        val recyclerView = sheetBinding.listOfGame

        var gameName: String = ""

        gamesAdapter = GamesAdapter(requireContext(), mutableListOf()) { nomeItem, idItem ->
            Toast.makeText(requireContext(), nomeItem, Toast.LENGTH_SHORT).show()
            gameName = nomeItem
        }
        recyclerView.layoutManager =
            GridLayoutManager(requireContext(), 1, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = gamesAdapter

        sheetBinding.createRoomidButtom.setOnClickListener {
            createRoom(sheetBinding, gameName)
        }

        sheetBinding.scrollViewCreateRoom.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            private var initialHeight = 0

            override fun onPreDraw(): Boolean {
                if (initialHeight == 0) {
                    initialHeight = sheetBinding.scrollViewCreateRoom.height
                    return true
                }

                val currentHeight = sheetBinding.scrollViewCreateRoom.height

                var testValue = initialHeight + 100
                if (testValue > currentHeight) {
                    sheetBinding.scrollViewCreateRoom.scrollBy(0, 3000)
                } else {
                    sheetBinding.scrollViewCreateRoom.scrollBy(0, 0)
                }

                return true
            }
        })

        with(dialog.behavior) {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.show()



        this.listGames()
        gamesAdapter.updateData(originalGamesList)
    }


    private fun createRoom(sheetBinding: BsCreatinRoomBinding, gameGame: String) {
        if (validateCreateData(sheetBinding, gameGame)) {
            val roomToCreate = CreateRoom(
                sheetBinding.editTextNome.text.toString(),
                gameGame,
                mutableListOf(),
                sheetBinding.editTextDescricao.text.toString(),
                getIdUserSigned(),
                mutableListOf(),
                10
            )

            Rest.getInstance()
                .create(RoomService::class.java)
                .createRoom(getIdUserSigned(), roomToCreate)
                .enqueue(object : Callback<Room> {
                    override fun onResponse(call: Call<Room>, response: Response<Room>) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                exibirSnackbar(
                                    "Sucesso ao criar sala. Você será redirecinado para o grupo.",
                                    true
                                )
                                val intent = Intent(context, ChatRoomActivity::class.java)
                                intent.putExtra("idGroup", response.body()!!.id)
                                intent.putExtra("gameName", response.body()!!.gameName)
                                intent.putExtra("groupName", response.body()!!.name)
                                context!!.startActivity(intent)
                            }
                        } else {
                            Log.e("Error", "Houve um erro ao cria uma sala!")
                            exibirSnackbar(
                                "Ops! Ocorreu um erro ao criar a sala. Por favor, tente novamente.",
                                false
                            )
                        }
                    }

                    override fun onFailure(call: Call<Room>, t: Throwable) {
                        Log.e("Error", "Houve um erro ao cria uma sala!", t)
                        exibirSnackbar(
                            "Ops! Ocorreu um erro ao criar a sala. Por favor, tente novamente.",
                            false
                        )
                    }
                })
        } else {
            exibirSnackbar(
                "Ops! Ocorreu um erro ao criar a sala. Verifique as informações preenchidas e tente novamente.",
                false
            )
        }

    }

    private fun validateCreateData(sheetBinding: BsCreatinRoomBinding, gameGame: String): Boolean {
        if (sheetBinding.editTextNome.text.toString().isEmpty() ||
            gameGame.isEmpty()
        ) {
            return false
        }
        return true
    }


    private fun listGames() {
        val rest = Rest.getInstance()
        val service = rest.create(GamesService::class.java)

        service.listarJogos().enqueue(object : Callback<List<GameResponse>> {
            override fun onResponse(
                call: Call<List<GameResponse>>,
                response: Response<List<GameResponse>>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    originalGamesList = apiResponse?.map {
                        val imageGame = ImageGame(
                            it.imageGame.id, it.imageGame.typeImage,
                            it.imageGame.link, it.imageGame.image_id
                        )
                        GameResponse(
                            it.id, it.platformsType, imageGame, it.gameGenres, it.name,
                            it.platforms, it.cover, it.genres
                        )
                    } ?: emptyList()
                } else {
                    Log.e("Error", "Houve um erro ao listar os jogos")
                    exibirSnackbar("Ops! Ocorreu um erro ao obter a listagem de jogos.", false)
                }
            }

            override fun onFailure(call: Call<List<GameResponse>>, t: Throwable) {
                exibirSnackbar("Ops! Ocorreu um erro ao obter a listagem de jogos.", false)
            }
        })
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
        return sharedPreferences.getInt("id", 4).toLong()
    }

    private fun exibirSnackbar(mensagem: String, isSucess: Boolean = true) {
        val snackbar = Snackbar.make(rootView, mensagem, Snackbar.LENGTH_SHORT)

        if (isSucess) {
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.sucess))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        snackbar.show()
    }

    private suspend fun showBottomSheetGameFilter() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding: BsGameListBinding =
            BsGameListBinding.inflate(layoutInflater, null, false);
        val chipGroup = sheetBinding.listOfGames;


        originalGamesList.map { jogo ->
            val newChip = Chip(
                requireContext(),
                null,
                com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated
            )

            val isSelected = gamesForFilter.contains(jogo.name)
            newChip.isSelected = isSelected
            newChip.setChipBackgroundColorResource(
                if (newChip.isSelected) R.color.md_theme_dark_onPrimary
                else R.color.md_theme_dark_inverseOnSurface
            )
            newChip.isCloseIconVisible = isSelected
            newChip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(R.color.white)))
            newChip.text = jogo.name
            newChip.tag = jogo.name
            newChip.setOnClickListener { enableOrRemoveChipFilter(newChip) }
            newChip.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.white)))
            chipGroup.addView(newChip)
        }

        dialog.setContentView(sheetBinding.root)
        dialog.show()

        dialog.setOnDismissListener {
            filterRoomsByGame()
        }
    }

    fun filterRoomsByGame(){

        var listFilter = listRoom;

        if (!gamesForFilter.isEmpty()) {
             listFilter = listRoom.filter { room ->
                room.gameName in gamesForFilter
            }.toMutableList()
        }


        adapterRooms =
            RoomAdapter(listFilter, binding.root.context, getIdUserSigned(), getUserSignedName())
        recyclerViewRoom.adapter = adapterRooms
    }

    fun enableOrRemoveChipFilter(view: View) {
        if (view is Chip) {
            val chip = view as Chip
            val gameName = chip.tag as String

            if (chip.isSelected) {
                chip.isSelected = false
                chip.isCloseIconVisible = false
                chip.setChipBackgroundColorResource(R.color.md_theme_dark_inverseOnSurface)
                gamesForFilter.remove(gameName)

            } else {
                chip.isSelected = true
                chip.setChipBackgroundColorResource(R.color.md_theme_dark_onPrimary)
                chip.isCloseIconVisible = true
                chip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(R.color.white)))
                Log.i("INFO", "Adicionando jogo " + gameName.toString() + " para filtragem")
                gamesForFilter.add(gameName)
            }
            filterRoomsByGame()
        }
    }
}
