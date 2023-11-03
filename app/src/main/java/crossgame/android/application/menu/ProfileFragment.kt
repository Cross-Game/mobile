package crossgame.android.application.menu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import crossgame.android.application.AddGamesActivity
import crossgame.android.application.AddInterestsActivity
import crossgame.android.application.FeedbacksActivity
import crossgame.android.application.PlatformsActivity
import crossgame.android.application.R
import crossgame.android.application.databinding.ActivityBsEditProfileBinding
import crossgame.android.application.databinding.FragmentProfileBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.feedbacks.Feedback
import crossgame.android.domain.models.user.UserList
import crossgame.android.service.AutenticationUser
import crossgame.android.service.FeedbackService
import crossgame.android.service.UserFriendService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val PICK_IMAGE_REQUEST = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(
            inflater,
            container,
            false
        )
        binding.imageJogador.setImageResource(R.drawable.carbon_user_avatar_empty)
        binding.btnSettingProfile.setOnClickListener { showBottomSheet() }
        binding.btnAddPhoto.setOnClickListener { updatePhotoUser() }
        updateNameUser()
        updateFeedbacksUser()
        updateFriendsUser()
        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    //falar o pq temos que acessar a galeria do usuario
                }
            }
        }
    }

    private fun openGallery() {
        launcher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let { uri ->

                    binding.imageJogador.setImageURI(uri)
                    //Ajeitar para salvar no banco como binario
//                    val imageBytes: ByteArray? = getImageBytesFromUri(uri)
//                    imageBytes?.let {
//                        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), it)
//                        val body =
//                            MultipartBody.Part.createFormData("image", "image.jpg", requestFile)
//                        Rest.getInstance(requireActivity()).create(AutenticationUser::class.java)
//                            .uploadImage(body).enqueue(object : Callback<Unit> {
//                                override fun onResponse(
//                                    call: Call<Unit>,
//                                    response: Response<Unit>
//                                ) {
//                                    if (response.isSuccessful) {
//                                        getPhotoUser()
//                                    }
//                                }
//
//                                override fun onFailure(call: Call<Unit>, t: Throwable) {
//                                    Log.i("GET", "Falha ao fazer upload")
//                                }
//                            })
//                    }
                }
            }
        }

    fun getImageBytesFromUri(uri: Uri): ByteArray? {
        val file = File(uri.toString())
        val inputStream: InputStream = FileInputStream(file)
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
        val sheetBinding: ActivityBsEditProfileBinding =
            ActivityBsEditProfileBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)
        dialog.show()

        sheetBinding.textVerFeedbacks.setOnClickListener { navigation("Feedback") }
        sheetBinding.textEditarFoto.setOnClickListener { navigation("Foto") }
        sheetBinding.textEditarInteresse.setOnClickListener { navigation("Interesse") }
        sheetBinding.textEditarJogo.setOnClickListener { navigation("Jogo") }
        sheetBinding.textEditarPlataforma.setOnClickListener { navigation("Plataforma") }
    }

    private fun navigation(navigate: String) {
        when (navigate) {
            "Feedback" -> startActivity(Intent(binding.root.context, FeedbacksActivity::class.java))
            "Foto" -> startActivity(Intent(binding.root.context, FeedbacksActivity::class.java))
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
            .getPhoto(7L).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Log.i("GET", "Sucesso ao buscar Imagem")
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
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.i("GET", "Falha ao listar Feedbacks")
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

    }

    private fun updatePlatformsUser() {

    }

    private fun updateInterestsUser() {

    }
}