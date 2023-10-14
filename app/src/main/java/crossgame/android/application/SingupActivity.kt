package crossgame.android.application


import android.content.Intent
import android.net.Uri

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone

import com.google.gson.GsonBuilder
import crossgame.android.application.databinding.ActivitySingupBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.token.TokenResponse
import crossgame.android.domain.models.user.UserRegisterRequest

import crossgame.android.service.AutenticationUser
import crossgame.android.service.DiscordApiService

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


class SingupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingupBinding
    private var senhaVisivel = false
    private var confirmarSenhaVisivel = false

    private val CLIENT_ID = "1102730864972009612"
    private val CLIENT_SECRET = "WBk5k6uAQNLFJ0nZFBBbwtuzP9XUUKkn"
    private val REDIRECT_URI = "myapp://discord_auth_callback"
    private var redirectIntent: Intent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent != null && intent.data != null && intent.data.toString()
                .startsWith(REDIRECT_URI)
        ) {
            redirectIntent = intent
        }

        setupUI()


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

        binding.textDiscordSingup.setOnClickListener {
            iniciarAutenticacaoDiscord()
        }



        binding.btnRegistrar.setOnClickListener { cadastrar() }

        binding.btnPossuiConta.setOnClickListener { telaEntrar() }
    }

    private fun setupUI() {
        binding.textDiscordSingup.setOnClickListener {
            if (redirectIntent != null) {
                val uri = redirectIntent?.data
                if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {
                    val code = uri.getQueryParameter("code")
                    if (code != null) {
                        trocarCodigoPorTokenDeAcesso(code)
                    } else if (uri.getQueryParameter("error") != null) {
                        // Lida com erros de autorização aqui
                    }
                }
            }
        }

    }

    private fun iniciarAutenticacaoDiscord() {
        val authUrl = "https://discord.com/api/oauth2/authorize" +
                "?client_id=$CLIENT_ID" +
                "&redirect_uri=$REDIRECT_URI" +
                "&response_type=code" +
                "&scope=identify"  // Escopo de acesso

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
    }

    private fun trocarCodigoPorTokenDeAcesso(code: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://discord.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(DiscordApiService::class.java)

        val call = service.trocarCodigoPorToken(
            CLIENT_ID, CLIENT_SECRET, code, REDIRECT_URI, "authorization_code"
        )

        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    val accessToken = tokenResponse?.access_token

                    if (accessToken != null) {
                        // Implemente aqui a lógica para autenticar o usuário com o token
                    } else {
                        // Lida com cenários em que o token de acesso está ausente na resposta
                        val errorMessage = "Erro: Token de acesso ausente na resposta."
                        Toast.makeText(this@SingupActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Lida com erros na solicitação de token
                    val errorMessage = "Erro na solicitação de token: ${response.message()}"
                    Toast.makeText(this@SingupActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                // Lida com erros de comunicação
                val errorMessage = "Erro de comunicação com o servidor: ${t.message}"
                Toast.makeText(this@SingupActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cadastrar() {
        val nome = binding.editTextNome.text.toString()
        val email = binding.editTextEmail.text.toString()
        val senha = binding.editTextSenha.text.toString()
        val confirmarSenha = binding.editTextConfirmarSenha.text.toString()

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
