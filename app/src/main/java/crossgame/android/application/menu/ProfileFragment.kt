package crossgame.android.application.menu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import crossgame.android.application.AddGamesActivity
import crossgame.android.application.AddInterestsActivity
import crossgame.android.application.FeedbacksActivity
import crossgame.android.application.PlatformsActivity
import crossgame.android.application.databinding.ActivityBsEditProfileBinding
import crossgame.android.application.databinding.FragmentProfileBinding
import crossgame.android.domain.httpClient.AuthInterception
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.feedbacks.Feedback
import crossgame.android.domain.models.user.User
import crossgame.android.domain.models.user.UserList
import crossgame.android.domain.models.user.UserResponse
import crossgame.android.service.FeedbackService
import crossgame.android.service.UserFriendService
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding


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
        binding.btnSettingProfile.setOnClickListener { showBottomSheet() }
        updateNameUser()
        updateFeedbacksUser()
        updateFriendsUser()
        return binding.root
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
        when(navigate){
            "Feedback" -> startActivity(Intent(binding.root.context, FeedbacksActivity::class.java))
            "Foto" -> startActivity(Intent(binding.root.context, FeedbacksActivity::class.java))
            "Interesse" -> startActivity(Intent(binding.root.context, AddInterestsActivity::class.java))
            "Jogo" -> startActivity(Intent(binding.root.context, AddGamesActivity::class.java))
            "Plataforma" -> startActivity(Intent(binding.root.context, PlatformsActivity::class.java))
        }
    }

    private fun updatePhotoUser(){
        //TODO: Deixar para depois de atualizar todas as infromações do usuario
    }
    private fun updateNameUser(){
        val sharedPreferences = requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Sem Nome")
        binding.textNameUser.text = username
    }
    private fun updateFeedbacksUser(){
        val sharedPreferences = requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
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
                    if(size != null){
                        binding.qtdFeedback.text = "Feedbacks \n $size"
                    }
                }
            }

            override fun onFailure(call: Call<List<Feedback>>, t: Throwable) {
                Log.i("GET", "Falha ao listar Feedbacks")
            }
        })
    }
    private fun updateFriendsUser(){
        val sharedPreferences = requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
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
                    if(size != null){
                        binding.qtdFriends.text = "Amigos \n $size"
                    }
                }
            }

            override fun onFailure(call: Call<List<UserList>>, t: Throwable) {
                Log.i("GET", "Falha ao listar Feedbacks")
            }
        })
    }
    private fun updateGamesUser(){

    }
    private fun updatePlatformsUser(){

    }
    private fun updateInterestsUser(){

    }
}