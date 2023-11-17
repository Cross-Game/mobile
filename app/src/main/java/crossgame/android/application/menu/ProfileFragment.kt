package crossgame.android.application.menu

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import crossgame.android.domain.models.user.UserList
import crossgame.android.service.AutenticationUser
import crossgame.android.service.FeedbackService
import crossgame.android.service.GamesService
import crossgame.android.service.UserFriendService
import crossgame.android.ui.adapters.games.GamesAdapter
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
    private var originalGamesList: List<GameResponse> = mutableListOf()
    private lateinit var gamesAdapter: GamesAdapter
    private lateinit var progressDialog: ProgressDialog

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

        val recyclerView = binding.listGames
        gamesAdapter = GamesAdapter(requireContext(), mutableListOf()) {
                nomeItem, idItem ->
            Toast.makeText(requireContext(), nomeItem, Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = GridLayoutManager(requireContext(),1, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = gamesAdapter

        getPhotoUser()
        updateNameUser()
        updateFeedbacksUser()
        updateFriendsUser()
        updateGamesUser()

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
                                        Toast.makeText(context, "Imagem de perfil atualizada!",
                                            Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, response.code().toString(),
                                            Toast.LENGTH_LONG).show()
                                    }
                                }

                                override fun onFailure(call: Call<Unit>, t: Throwable) {
                                    Log.i("GET", t.message.toString())
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
                        Log.i("GET", "Ops, imagem incompatível !")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.i("GET", "Falha ao atualizar a foto de perfil")
                }
            })
    }

    private fun updateNameUser() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Sem Nome")
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

    }

    private fun updateInterestsUser() {

    }
}