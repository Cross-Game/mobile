package crossgame.android.application.menu

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import crossgame.android.application.AddGamesActivity
import crossgame.android.application.AddInterestsActivity
import crossgame.android.application.FeedbacksActivity
import crossgame.android.application.PlatformsActivity
import crossgame.android.application.R
import crossgame.android.application.databinding.BsEditProfileBinding
import crossgame.android.application.databinding.FragmentProfileBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.feedbacks.Feedback
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.games.ImageGame
import crossgame.android.domain.models.platforms.GameplayPlatformType
import crossgame.android.domain.models.preferences.Preference
import crossgame.android.domain.models.user.UserList
import crossgame.android.domain.models.users.UserPreference
import crossgame.android.service.AutenticationUser
import crossgame.android.service.FeedbackService
import crossgame.android.service.GamesService
import crossgame.android.service.PlatformsService
import crossgame.android.service.PreferencesService
import crossgame.android.service.UserFriendService
import crossgame.android.ui.adapters.games.GamesAdapter
import crossgame.android.ui.adapters.interesses.PreferenceAdapter
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var rootView: View
    private var originalGamesList: List<GameResponse> = mutableListOf()
    private var preferenceList: List<Preference> = mutableListOf()
    private lateinit var gamesAdapter: GamesAdapter
    private lateinit var preferenceAdapter: PreferenceAdapter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var preferencesService: PreferencesService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setInverseBackgroundForced(true)
        progressDialog.setTitle("Carregando...")
        progressDialog.show()
        binding = FragmentProfileBinding.inflate(
            inflater,
            container,
            false
        )
        binding.imageJogador.setImageResource(R.drawable.carbon_user_avatar_empty)
        binding.btnSettingProfile.setOnClickListener { showBottomSheet() }
        binding.btnAddPhoto.setOnClickListener { updatePhotoUser() }
        rootView = binding.root

        val recyclerView = binding.listGames
        gamesAdapter = GamesAdapter(requireContext(), mutableListOf()) {
                nomeItem, idItem ->
            Toast.makeText(requireContext(), nomeItem, Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = GridLayoutManager(requireContext(),1, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = gamesAdapter

        val recyclerViewInterest = binding.listInteresse
        preferenceAdapter = PreferenceAdapter(requireContext(), mutableListOf())
        recyclerViewInterest.layoutManager = GridLayoutManager(requireContext(), 1, RecyclerView.HORIZONTAL, false)
        recyclerViewInterest.adapter = preferenceAdapter

        getPhotoUser()
        updateNameUser()
        updateFeedbacksUser()
        updateFriendsUser()
        updateGamesUser()
        updateInterestsUser()
        updatePlatformsUser()

        val timer = object : CountDownTimer(2500, 1000) {

            override fun onTick(millisUntilFinished: Long) {
            }
            override fun onFinish() {
                progressDialog.dismiss()
            }
        }
        timer.start()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateGamesUser()
        updateInterestsUser()
        updatePlatformsUser()
    }

    private fun openGallery() {
        launcher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let { uri ->
                    val imageBytes: ByteArray? = getImageBytesFromUri(uri)
                    imageBytes?.let {
                        val sharedPreferences =
                            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
                        val idUser = sharedPreferences.getInt("id", 0)
                        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), it)
                        Rest.getInstance(requireActivity()).create(AutenticationUser::class.java)
                            .uploadImage(idUser.toLong(), requestBody).enqueue(object : Callback<Unit> {
                                override fun onResponse(
                                    call: Call<Unit>,
                                    response: Response<Unit>
                                ) {
                                    if (response.isSuccessful) {
                                        getPhotoUser()
                                        exibirSnackbar("Foto de perfil atualizada com sucesso!", true)
                                    } else {
                                        exibirSnackbar("Ops! Ocorreu uma falha ao atualizar foto de perfil. Por favor, tente novamente.", false)
                                    }
                                }

                                override fun onFailure(call: Call<Unit>, t: Throwable) {
                                    Log.i("GET", t.message.toString())
                                    exibirSnackbar("Ops! Ocorreu uma falha ao atualizar foto de perfil. Por favor, tente novamente.", false)
                                }
                            })
                    }
                }
            }
        }

    fun getImageBytesFromUri(uri: Uri): ByteArray? {
        val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(uri)
        inputStream.let {
            val imageBitmap: Bitmap = BitmapFactory.decodeStream(it)
            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            return outputStream.toByteArray()
        }
        return null
    }

    private fun updatePhotoUser() {
        openGallery()
    }

    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(binding.root.context)
        val sheetBinding: BsEditProfileBinding  =
            BsEditProfileBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)
        dialog.show()

        sheetBinding.textVerFeedbacks.setOnClickListener { navigation("Feedback") }
        sheetBinding.textEditarInteresse.setOnClickListener { navigation("Interesse") }
        sheetBinding.textEditarJogo.setOnClickListener { navigation("Jogo") }
        sheetBinding.textEditarPlataforma.setOnClickListener { navigation("Plataforma") }
    }

    private fun navigation(navigate: String) {
        when (navigate) {
            "Feedback" -> startActivity(Intent(binding.root.context, FeedbacksActivity::class.java))
            "Interesse" -> startActivity(
                Intent(
                    binding.root.context,
                    AddInterestsActivity::class.java
                )
            )

            "Jogo" -> startActivity(Intent(binding.root.context, AddGamesActivity::class.java))
            "Plataforma" -> startActivity(
                Intent(
                    binding.root.context,
                    PlatformsActivity::class.java
                )
            )
        }
    }

    private fun getPhotoUser() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val idUser = sharedPreferences.getInt("id", 0)
        Rest.getInstance(requireActivity()).create(AutenticationUser::class.java)
            .getPhoto(idUser.toLong()).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()?.contentLength()?.toInt() != 0) {
                            val inputStream: InputStream = response.body()!!.byteStream()
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

                            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
                            val decodedByte =
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            binding.imageJogador.setImageBitmap(decodedByte)
                        }
                    } else {
                        exibirSnackbar("Ops! Ocorreu uma falha ao carregar imagem de perfil!", false)
                        Log.i("GET", "Ops, imagem incompatível !")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    exibirSnackbar("Ops! Ocorreu uma falha ao carregar imagem de perfil!", false)
                    Log.i("GET", "Falha ao atualizar a foto de perfil")
                }
            })
    }

    private fun updateNameUser() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Meu nome")
        binding.textNameUser.text = username
    }

    private fun updateFeedbacksUser() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val idUser = sharedPreferences.getInt("id", 0)
        Rest.getInstance(requireActivity()).create(FeedbackService::class.java)
            .listar(idUser.toLong()).enqueue(object : Callback<List<Feedback>> {
                override fun onResponse(
                    call: Call<List<Feedback>>,
                    response: Response<List<Feedback>>
                ) {
                    if (response.isSuccessful) {
                        Log.i("GET", "Sucesso ao listar Feedbacks")
                        val apiResponse = response.body()
                        val size = apiResponse?.size
                        if (size != null) {
                            binding.qtdFeedback.text = "Feedbacks \n $size"
                        }
                    }
                }

                override fun onFailure(call: Call<List<Feedback>>, t: Throwable) {
                    Log.i("GET", "Falha ao listar Feedbacks")
                }
            })
    }

    private fun updateFriendsUser() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val idUser = sharedPreferences.getInt("id", 0)
        Rest.getInstance(requireActivity()).create(UserFriendService::class.java)
            .listarFriend(idUser.toLong()).enqueue(object : Callback<List<UserList>> {
                override fun onResponse(
                    call: Call<List<UserList>>,
                    response: Response<List<UserList>>
                ) {
                    if (response.isSuccessful) {
                        Log.i("GET", "Sucesso ao listar Feedbacks")
                        val apiResponse = response.body()
                        val size = apiResponse?.size
                        if (size != null) {
                            changeBackgroundColorImageProfileByLevel(size)
                            binding.qtdFriends.text = "Amigos \n $size"
                        }
                    }
                }

                override fun onFailure(call: Call<List<UserList>>, t: Throwable) {
                    Log.i("GET", "Falha ao listar Feedbacks")
                }
            })
    }

    private fun updateGamesUser() {
        val rest = Rest.getInstance(requireContext())
        val service = rest.create(GamesService::class.java)
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val idUser = sharedPreferences.getInt("id", 0).toLong()
        service.listar(idUser).enqueue(object : Callback<List<GameResponse>> {
            override fun onResponse(
                call: Call<List<GameResponse>>,
                response: Response<List<GameResponse>>
            ) {
                if (response.isSuccessful) {
                    Log.i("GET", "Listagem de amigos realizada com sucesso")
                    val apiResponse = response.body()

                    originalGamesList = apiResponse?.map {
                        val imageGame = ImageGame(it.imageGame.id, it.imageGame.typeImage,
                            it.imageGame.link, it.imageGame.image_id)
                        GameResponse(it.id, it.platformsType, imageGame, it.gameGenres, it.name,
                            it.platforms, it.cover, it.genres)
                    } ?: emptyList()
                    gamesAdapter.updateData(originalGamesList)
                }
            }

            override fun onFailure(call: Call<List<GameResponse>>, t: Throwable) {
                Log.e("GET", "Falha ao listar os Jogos", t)
            }
        })
    }

    private fun updatePlatformsUser() {
        val sharedPreferences = requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val id = sharedPreferences.getInt("id", 0)
        var platformsService = Rest.getInstance(requireActivity()).create(PlatformsService::class.java)
        platformsService.retrieveGamePlatformsForUserById(id.toLong()).enqueue(object : Callback<List<GameplayPlatformType>>{
            override fun onResponse(
                call: Call<List<GameplayPlatformType>>,
                response: Response<List<GameplayPlatformType>>
            ) {
                    if (response.isSuccessful){
                    showPlatformas(response.body())
                }
            }

            override fun onFailure(call: Call<List<GameplayPlatformType>>, t: Throwable) {
                Log.e("ERROR", "ERRO AO OBTER PLATAFORMAS: " + t.message.toString())
            }

        })
    }

    private fun updateInterestsUser() {
        Log.i("GET", "Listando Preferencias")
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        var id = sharedPreferences.getInt("id", 0).toLong()
        preferencesService = Rest.getInstance(requireContext()).create(PreferencesService::class.java)
        preferencesService.listar(id).enqueue(object : Callback<UserPreference> {
            override fun onResponse(
                call: Call<UserPreference>,
                response: Response<UserPreference>
            ) {
                if (response.isSuccessful) {
                    Log.i("GET", "Sucesso ao listar Preferencias")
                    val preferences = response.body()?.preferences
                    preferenceList = preferences?.map {
                        Preference(it.id, it.preferences)
                    } ?: emptyList()
                    preferenceAdapter.updateData(preferenceList)
                } else {
                    Log.i("ERRO", "Response falhou !")
                }
            }

            override fun onFailure(call: Call<UserPreference>, t: Throwable) {
                Log.e("ERROR", "ERRO AO OBTER PREFERENCIAS: " + t.message.toString())
            }
        })
    }

    private fun showPlatformas(body: List<GameplayPlatformType>?) {
        body?.map {
            when (it.name) {
                "PC" -> binding.computerImage.visibility = View.VISIBLE
                "XBOX" -> binding.xboxImage.visibility = View.VISIBLE
                "PLAYSTATION" -> binding.playstationImage.visibility = View.VISIBLE
                else -> binding.mobileImage.visibility = View.VISIBLE
            }

        }
    }

    private fun changeBackgroundColorImageProfileByLevel(qtdAmigos : Int){
        when (qtdAmigos) {
            in 0..5 -> binding.backgroundProfileUser.background = ColorDrawable(Color.parseColor("#CCAE9201"))
            in 6..12 -> binding.backgroundProfileUser.background = ColorDrawable(Color.parseColor("#CCC0C0C0"))
            in 13..21 -> binding.backgroundProfileUser.background = ColorDrawable(Color.parseColor("#B300B2D1"))
            else -> binding.backgroundProfileUser.background = ColorDrawable(Color.parseColor("#51E00F02"))
        }
    }
    private fun exibirSnackbar(mensagem: String, isSucess : Boolean = true) {
        val snackbar = Snackbar.make(rootView, mensagem, Snackbar.LENGTH_SHORT)

        if (isSucess) {
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.sucess))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        else {
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        snackbar.show()
    }

}