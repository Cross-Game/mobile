package crossgame.android.application

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import crossgame.android.application.databinding.ActivityLoginBinding
import crossgame.android.application.menu.ProfileFragment
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.user.UserRequest
import crossgame.android.domain.models.user.UserResponse
import crossgame.android.service.AutenticationUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSingInClient: GoogleSignInClient
    private var senhaVisivel = false
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rootView = findViewById(android.R.id.content)


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
                            task.result.displayName.toString(),
                            task.result.idToken.toString()
                        )
                    )
                } else {
                    Log.e("launcher", task.exception?.message.toString());
                }


            } else {
                Log.e("launcher", result.toString());
            }
        }

    private fun redirectToCadastro() {
        startActivity(Intent(baseContext, SingupActivity::class.java))
    }

    private fun redirectToMatch() {
        startActivity(Intent(baseContext, ProfileActivity::class.java))
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
                        editor.putString("TOKEN", response.body()?.encodedToken.toString())
                        editor.apply()
                        exibirSnackbar("Sucesso ao realizar login! Bem-vindo de volta!")
                        decodeJWT(response.body()?.encodedToken.toString())
                        redirectToMatch()
                    } else {

                        exibirSnackbar("Falha ao realizar login. Usuário ou senha inválidos.", false)
                        googleSingInClient.signOut()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("register", "Falha ao registrar usuário: " + t.message)

                    exibirSnackbar("Ops! Ocorreu uma falha ao realizar login. Por favor, tente novamente.", false)
                }

            })

    }

    private fun decodeJWT(token: String){
        try {
            val decodedJWT: DecodedJWT = JWT.decode(token)
            val payload = decodedJWT.claims

            val usernameUser = payload["username"]?.asString()
            val emailUser = payload["email"]?.asString()
            val idUser = payload["id"]?.asInt()

            savaSharedPreference(usernameUser, emailUser, idUser!!)

        } catch (exception: Exception) {
            googleSingInClient.signOut()
            Log.e("decodeJWT", "Erro ao decodificar token: " + exception.message.toString() )
        }
    }

    private fun savaSharedPreference(usernameUser: String?, emailUser: String?, idUser: Int) {
        val sharedPreferences = getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", usernameUser)
        editor.putString("email", emailUser)
        editor.putInt("id", idUser)
        editor.apply()
    }

    private fun exibirSnackbar(mensagem: String, isSucess : Boolean = true) {
        val snackbar = Snackbar.make(rootView, mensagem, Snackbar.LENGTH_SHORT)

        if (isSucess) {
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.sucess))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        else {
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.error))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        snackbar.show()
    }
}