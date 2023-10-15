package crossgame.android.application


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityLoginBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.user.UserRequest
import crossgame.android.domain.models.user.UserResponse
import crossgame.android.service.AutenticationUser
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var username: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNaoPossuiConta.setOnClickListener { redirectToCadastro() }
        binding.btnSingIn.setOnClickListener { register() }
    }

    private fun redirectToCadastro() {
        startActivity(Intent(baseContext, SingupActivity::class.java))
    }

    private fun redirectToMatch() {
        startActivity(Intent(baseContext, NotificationsActivity::class.java))
        finish()
    }

    private fun register() {
        username = binding.editTextUsername.text.toString()
        password = binding.editTextSenha.text.toString()
        val userRequest = UserRequest(
            username.toString(), password.toString()
        )
        Rest.getInstance()
            .create(AutenticationUser::class.java)
            .singIn(userRequest)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.isSuccessful) {
                        val prefs =
                            getSharedPreferences("AUTH", MODE_PRIVATE)
                        val editor = prefs.edit()
                        editor.putString("TOKEN", response.body().toString())
                        editor.apply()
                        Toast.makeText(
                                baseContext,
                        "Login Realizado !",
                        Toast.LENGTH_SHORT
                        ).show()
                        redirectToMatch()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(baseContext, "username ou password incorreto !", Toast.LENGTH_SHORT).show()
                }

            })

    }
}