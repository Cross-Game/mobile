package crossgame.android.application

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.google.gson.GsonBuilder
import crossgame.android.application.databinding.ActivitySingupBinding
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
    private val CLIENT_ID = "1102730864972009612"
    private val REDIRECT_URI = "https://myawesomeapp.com/oauth"
    private val CLIENT_SECRET = "Tq5jUUh9AwcRXV1RfTPRCkRUOpUPE6IC"

    private lateinit var binding: ActivitySingupBinding
    private var senhaVisivel = false
    private lateinit var webView: WebView
    private var isDiscordLoginProcess = false

    private val DISCORD_API_URL = "https://discord.com/api/v10/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageMostraSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            mostrarOcultarSenha(binding.editTextSenha, senhaVisivel)
        }

        binding.editTextConfirmarSenha.addTextChangedListener(createTextWatcher { validarSenha() })
        binding.editTextEmail.addTextChangedListener(createTextWatcher { validarEmail(it) })
        binding.editTextSenha.addTextChangedListener(createTextWatcher { validarSenhaEmTempoReal(it) })

        binding.btnRegistrar.setOnClickListener {
            cadastrar()
        }

        binding.btnPossuiConta.setOnClickListener {
            telaEntrar()
        }

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        // Defina um WebViewClient para controlar o fluxo de autorização
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.startsWith(REDIRECT_URI) && isDiscordLoginProcess) {
                    // O usuário autorizou o acesso
                    val code = Uri.parse(url).getQueryParameter("code")
                    if (code != null) {
                        processarCodigoAutorizacao(code)
                    }
                    isDiscordLoginProcess = false
                    return true
                } else {
                    // Abra links externos no aplicativo do Discord
                    view?.loadUrl(url)
                }
                return true
            }
        }

        binding.textDiscordSingup.setOnClickListener {
            iniciarAutenticacaoDiscord()
        }
    }


    // Função para iniciar a autenticação no aplicativo do Discord
    private fun iniciarAutenticacaoDiscord() {
        isDiscordLoginProcess = true

        val authUrl = "https://discord.com/api/oauth2/authorize" +
                "?client_id=$CLIENT_ID" +
                "&redirect_uri=$REDIRECT_URI" +
                "&response_type=code" +
                "&scope=identify"

        webView.loadUrl(authUrl)
    }

    // Adicione a função processarCodigoAutorizacao
    private fun processarCodigoAutorizacao(codigoAutorizacao: String) {
        val clientId = CLIENT_ID // Usando a variável global
        val clientSecret = CLIENT_SECRET // Seu segredo de cliente do Discord
        val redirectUri = REDIRECT_URI // Usando a variável global
        val grantType = "authorization_code"

        val retrofit = Retrofit.Builder()
            .baseUrl(DISCORD_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val discordApiService = retrofit.create(DiscordApiService::class.java)

        val call = discordApiService.exchangeCodeForToken(
            clientId,
            clientSecret,
            redirectUri,
            codigoAutorizacao,
            grantType
        )

        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        val tokenDeAcesso = tokenResponse.access_token

                        // Aqui você tem o token de acesso, que pode ser usado para acessar recursos do Discord

                        // Agora você pode obter os detalhes do usuário
                        obterDadosDoDiscordETokenDeAcesso(tokenDeAcesso)
                    } else {
                        exibirMensagemErro("Erro ao obter o token de acesso do Discord")
                    }
                } else {
                    // Trate erros na resposta do Discord
                    exibirMensagemErro("Erro ao trocar código por token no Discord")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                // Trate erros na comunicação com o servidor
                exibirMensagemErro("Erro na comunicação com o servidor do Discord: ${t.message}")
            }
        })
    }

    // Função para obter os detalhes do usuário do Discord e preencher o formulário
    private fun obterDadosDoDiscordETokenDeAcesso(tokenDeAcesso: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(DISCORD_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val discordApiService = retrofit.create(DiscordApiService::class.java)

        val call = discordApiService.getDiscordUser("Bearer $tokenDeAcesso")

        call.enqueue(object : Callback<DiscordUserResponse> {
            override fun onResponse(call: Call<DiscordUserResponse>, response: Response<DiscordUserResponse>) {
                if (response.isSuccessful) {
                    val discordUser = response.body()
                    if (discordUser != null) {
                        val nomeDeUsuario = discordUser.username
                        val email = discordUser.email.toString()

                        // Agora você tem o nome de usuário (username) e o e-mail do usuário

                        // Realize o cadastro do usuário usando as informações obtidas do Discord
                        val nome = nomeDeUsuario
                        val senha = tokenDeAcesso // Use o token de acesso como senha
                        val confirmarSenha = tokenDeAcesso // Confirme com o token de acesso
                        val userRegisterRequest = UserRegisterRequest(nome, email, senha, "USER")

                        // Faça a chamada para cadastrar o usuário
                        cadastrarUsuarioNoSeuSistema(userRegisterRequest)
                    } else {
                        exibirMensagemErro("Erro ao obter dados do usuário do Discord")
                    }
                } else {
                    // Trate erros na resposta do Discord
                    exibirMensagemErro("Erro ao obter dados do usuário do Discord")
                }
            }

            override fun onFailure(call: Call<DiscordUserResponse>, t: Throwable) {
                // Trate erros na comunicação com o servidor
                exibirMensagemErro("Erro na comunicação com o servidor do Discord: ${t.message}")
            }
        })
    }

    // Função para cadastrar o usuário no seu sistema
    private fun cadastrarUsuarioNoSeuSistema(userRegisterRequest: UserRegisterRequest) {
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

    private fun createTextWatcher(action: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            action(s.toString())
        }

        override fun afterTextChanged(s: Editable?) {}
    }
}

