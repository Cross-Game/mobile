package crossgame.android.application

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.GsonBuilder
import crossgame.android.application.databinding.ActivitySingupBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.user.UserRegisterRequest
import crossgame.android.service.AutenticationUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SingupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingupBinding
    private lateinit var googleSingInClient: GoogleSignInClient
    private var senhaVisivel = false
    private var confirmarSenhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail().requestProfile().build()
        googleSingInClient = GoogleSignIn.getClient(this, gso)

        binding.imageMostraSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            mostrarOcultarSenha(binding.editTextSenha, senhaVisivel)
        }

        binding.imageMostrarConfirmarSenha.setOnClickListener {
            confirmarSenhaVisivel = !confirmarSenhaVisivel
            mostrarOcultarSenha(binding.editTextConfirmarSenha, confirmarSenhaVisivel)
        }

        binding.editTextConfirmarSenha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validarSenha()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.editTextEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validarEmail(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.editTextSenha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validarSenhaEmTempoReal(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.btnRegistrar.setOnClickListener { cadastrar(
            binding.editTextNome.text.toString(),
            binding.editTextEmail.text.toString(),
            binding.editTextSenha.text.toString(),
            binding.editTextConfirmarSenha.text.toString()
        ) }

        binding.btnPossuiConta.setOnClickListener { telaEntrar() }

        binding.buttonGoogle.setOnClickListener { signUpGoogle() }
    }

    private fun signUpGoogle() {
        var signInIntent = googleSingInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    cadastrar(
                        task.result.displayName.toString(),
                        task.result.email.toString(),
                        task.result.idToken.toString(),
                        task.result.idToken.toString()
                    )
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show()
            }
        }

    private fun cadastrar(nome: String, email: String, senha: String, confirmarSenha: String) {
        googleSingInClient.signOut()
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(
                this,
                "Email inválido. Verifique o formato do email.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!isValidPassword(senha)) {
            Toast.makeText(
                this,
                "Senha inválida. A senha deve conter pelo menos 12 caracteres com pelo menos uma letra maiúscula.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (senha != confirmarSenha) {
            Toast.makeText(
                this,
                "As senhas não coincidem. Verifique sua senha e confirmação de senha.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val userRegisterRequest =
            UserRegisterRequest(nome.toString(), email.toString(), senha.toString(), "USER")

        Rest.getInstance()
            .create(AutenticationUser::class.java)
            .singUp(userRegisterRequest).enqueue(object : Callback<GsonBuilder> {
                override fun onResponse(
                    call: Call<GsonBuilder>,
                    response: Response<GsonBuilder>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@SingupActivity,
                            "Cadastro realizado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@SingupActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = "Erro ao registrar: " + response.message()
                        Toast.makeText(this@SingupActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GsonBuilder>, t: Throwable) {
                    val errorMessage = "Erro de comunicação com o servidor: " + t.message
                    Toast.makeText(this@SingupActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })

    }


    private fun telaEntrar() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 12 && password.any { it.isUpperCase() }
    }

    private fun mostrarOcultarSenha(editText: EditText, visivel: Boolean) {
        if (visivel) {
            editText.transformationMethod = null
        } else {
            editText.transformationMethod = android.text.method.PasswordTransformationMethod()
        }
        editText.setSelection(editText.text.length)
    }

    private fun validarSenha() {
        val senha = binding.editTextSenha.text.toString()
        val confirmarSenha = binding.editTextConfirmarSenha.text.toString()

        if (senha.isNotEmpty() || confirmarSenha.isNotEmpty()) {
            if (senha != confirmarSenha) {
                binding.textInputSenha.error = "Senhas não coincidem"
                binding.textInputConfirmarSenha.error = "Senhas não coincidem"
            } else {
                binding.textInputSenha.error = null
                binding.textInputConfirmarSenha.error = null
            }
        }
    }

    private fun validarEmail(email: String) {
        if (!isValidEmail(email)) {
            binding.editTextEmail.error = "Email inválido Deve conter @ e .com"
        } else {
            binding.editTextEmail.error = null
        }
    }

    private fun validarSenhaEmTempoReal(password: String) {
        if (!isValidPassword(password)) {
            binding.imageMostraSenha.isGone = true
            binding.editTextSenha.error =
                "A senha deve conter pelo menos 12 caracteres e pelo menos uma letra maiúscula"
        } else {
            binding.imageMostraSenha.isGone = false
            binding.editTextSenha.error = null
        }
    }
}
