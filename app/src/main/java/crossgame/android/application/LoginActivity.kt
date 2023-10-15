package crossgame.android.application

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    private lateinit var googleSingInClient: GoogleSignInClient
    private var senhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail().requestProfile().build()
        googleSingInClient = GoogleSignIn.getClient(this, gso)


        binding.btnNaoPossuiConta.setOnClickListener { redirectToCadastro() }
        binding.btnSingIn.setOnClickListener {
            register(
                UserRequest(
                    binding.editTextUsername.text.toString(),
                    binding.editTextSenha.text.toString()
                )
            )
        }
        binding.buttonGoogle.setOnClickListener { singInGoogle() }
        binding.mostrarSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            mostrarOcultarSenha(binding.editTextSenha, senhaVisivel)
        }
    }

    private fun mostrarOcultarSenha(editText: EditText, visivel: Boolean) {
        if (visivel) {
            editText.transformationMethod = null
        } else {
            editText.transformationMethod = android.text.method.PasswordTransformationMethod()
        }
        editText.setSelection(editText.text.length)
    }
    private fun singInGoogle() {

        var signInIntent = googleSingInClient.signInIntent
        launcher.launch(signInIntent)

    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    register(
                        UserRequest(
                            task.result.familyName.toString(),
                            task.result.idToken.toString()
                        )
                    )
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }


            } else {
                Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show()
            }
        }

    private fun redirectToCadastro() {
        startActivity(Intent(baseContext, SingupActivity::class.java))
    }

    private fun redirectToMatch() {
        startActivity(Intent(baseContext, NotificationsActivity::class.java))
    }

    private fun register(userRequest: UserRequest) {
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
                    } else {
                        Toast.makeText(baseContext, "Usuario não cadastrado !", Toast.LENGTH_SHORT)
                            .show()
                        googleSingInClient.signOut()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(
                        baseContext,
                        "Erro na conexão com a internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })

    }
}