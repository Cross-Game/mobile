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
import crossgame.android.application.databinding.ActivitySingupBinding.inflate
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.token.TokenResponse
import crossgame.android.domain.models.user.DiscordUserResponse
import crossgame.android.domain.models.user.UserRegisterRequest
import crossgame.android.service.AutenticationUser
import crossgame.android.service.DiscordApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SingupActivity : AppCompatActivity() {
    // Constantes
    private val CLIENT_ID = "1102730864972009612"
    private val CLIENT_SECRET = "WBk5k6uAQNLFJ0nZFBBbwtuzP9XUUKkn"
    private val REDIRECT_URI = "myapp://cross-game"

    private val DISCORD_BASE_URL = "https://discord.com/api/v10/"

    private lateinit var binding: ActivitySingupBinding
    private var senhaVisivel = false
    private var discordAccessToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupUI()

        binding.imageMostraSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            mostrarOcultarSenha(binding.editTextSenha, senhaVisivel)
        }

        binding.editTextConfirmarSenha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validarSenha()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.editTextEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validarEmail(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.editTextSenha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validarSenhaEmTempoReal(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.textDiscordSingup.setOnClickListener {
            iniciarAutenticacaoDiscord()
        }

        binding.btnRegistrar.setOnClickListener { cadastrar() }
        binding.btnPossuiConta.setOnClickListener { telaEntrar() }
    }

    private fun setupUI() {
        binding.textDiscordSingup.setOnClickListener {
            if (isRedirectedFromDiscord()) {
                val code = extractCodeFromIntent()
                if (code != null) {
                    trocarCodigoPorTokenDeAcesso(code)
                } else {
                    exibirMensagemErro("Erro de autorização")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        processarCodigoDiscord()
    }

    private fun isRedirectedFromDiscord(): Boolean {
        return intent?.data != null && intent.data.toString().startsWith(REDIRECT_URI)
    }

    private fun extractCodeFromIntent(): String? {
        return intent.data?.getQueryParameter("code")
    }

    private fun trocarCodigoPorTokenDeAcesso(code: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(DISCORD_BASE_URL)
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
                        discordAccessToken = accessToken
                        obterInformacoesUsuarioDiscord(accessToken)
                    } else {
                        exibirMensagemErro("Erro: Token de acesso ausente na resposta.")
                    }
                } else {
                    exibirMensagemErro("Erro na solicitação de token: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                exibirMensagemErro("Erro de comunicação com o servidor: ${t.message}")
            }
        })
    }

    private fun obterInformacoesUsuarioDiscord(accessToken: String) {
        val discordRetrofit = Retrofit.Builder()
            .baseUrl(DISCORD_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val discordService = discordRetrofit.create(DiscordApiService::class.java)

        val userCall = discordService.obterInformacoesUsuario("Bearer $accessToken")

        userCall.enqueue(object : Callback<DiscordUserResponse> {
            override fun onResponse(
                call: Call<DiscordUserResponse>,
                response: Response<DiscordUserResponse>
            ) {
                if (response.isSuccessful) {
                    val discordUser = response.body()
                    if (discordUser != null) {
                        val nome = discordUser.username
                        val email = discordUser.email.toString()
                        registrarUsuarioNoSeuServidor(accessToken, nome, email)
                    }
                } else {
                    exibirMensagemErro("Erro ao obter informações do usuário do Discord: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<DiscordUserResponse>, t: Throwable) {
                exibirMensagemErro("Erro de comunicação com o servidor do Discord: ${t.message}")
            }
        })
    }

    private fun registrarUsuarioNoSeuServidor(
        tokenDeAcessoDiscord: String,
        nome: String,
        email: String
    ) {
        val userRegisterRequest = UserRegisterRequest(nome, email, tokenDeAcessoDiscord, "USER")

        Rest.getInstance()
            .create(AutenticationUser::class.java)
            .singUp(userRegisterRequest).enqueue(object : Callback<GsonBuilder> {
                override fun onResponse(call: Call<GsonBuilder>, response: Response<GsonBuilder>) {
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
                        exibirMensagemErro("Erro ao registrar: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<GsonBuilder>, t: Throwable) {
                    exibirMensagemErro("Erro de comunicação com o servidor: ${t.message}")
                }
            })
    }

    private fun cadastrar() {
        val nome = binding.editTextNome.text.toString()
        val email = binding.editTextEmail.text.toString()
        val senha = binding.editTextSenha.text.toString()
        val confirmarSenha = binding.editTextConfirmarSenha.text.toString()

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            exibirMensagemErro("Por favor, preencha todos os campos.")
            return
        }

        if (!isValidEmail(email)) {
            exibirMensagemErro("Email inválido. Verifique o formato do email.")
            return
        }

        if (!isValidPassword(senha)) {
            exibirMensagemErro("Senha inválida. A senha deve conter pelo menos 12 caracteres com pelo menos uma letra maiúscula.")
            return
        }

        if (senha != confirmarSenha) {
            exibirMensagemErro("As senhas não coincidem. Verifique sua senha e confirmação de senha.")
            return
        }

        val userRegisterRequest = UserRegisterRequest(nome, email, senha, "USER")

        Rest.getInstance()
            .create(AutenticationUser::class.java)
            .singUp(userRegisterRequest).enqueue(object : Callback<GsonBuilder> {
                override fun onResponse(call: Call<GsonBuilder>, response: Response<GsonBuilder>) {
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
                        exibirMensagemErro("Erro ao registrar: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<GsonBuilder>, t: Throwable) {
                    exibirMensagemErro("Erro de comunicação com o servidor: ${t.message}")
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
            binding.editTextEmail.error = "Email inválido. Deve conter @ e .com"
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

    private fun exibirMensagemErro(mensagem: String) {
        Toast.makeText(this@SingupActivity, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun iniciarAutenticacaoDiscord() {
        val authUrl = "https://discord.com/api/oauth2/authorize" +
                "?client_id=$CLIENT_ID" +
                "&redirect_uri=$REDIRECT_URI" +
                "&response_type=code" +
                "&scope=identify"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
    }

    private fun processarCodigoDiscord() {
        if (isRedirectedFromDiscord()) {
            val code = extractCodeFromIntent()
            if (code != null) {
                trocarCodigoPorTokenDeAcesso(code)
            } else {
                exibirMensagemErro("Erro de autorização")
            }
        }
    }
}
