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
import crossgame.android.application.LoginActivity
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
    private val CLIENT_ID = "1102730864972009612" // Substitua pelo seu ID de cliente do Discord
    private val CLIENT_SECRET =
        "2OQPvF3bRMrwH5nKzDN9YPycQ7NO6Jkq" // Substitua pelo seu segredo de cliente do Discord
    private val REDIRECT_URI = "http://myawesomeapp.com/oauth"

    private lateinit var binding: ActivitySingupBinding
    private var senhaVisivel = false

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

        val authUrl = "https://discord.com/api/oauth2/authorize" +
                "?client_id=$CLIENT_ID" +
                "&redirect_uri=$REDIRECT_URI" +
                "&response_type=code" +
                "&scope=identify"

        binding.textDiscordSingup.setOnClickListener { iniciarAutenticacaoDiscord(authUrl) }

        val data: Uri? = intent?.data
        if (data != null) {
            val code = data.getQueryParameter("code")

            if (code != null) {
                // Você recebeu o código de autorização. Agora você pode processá-lo.
                processarCodigoAutorizacao(code)
            } else {
                // Não foi recebido um código de autorização no deep link.
            }
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

    private fun iniciarAutenticacaoDiscord(authUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
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

    // Processar o código de autorização e obter o token de acesso e detalhes do usuário
    private fun processarCodigoAutorizacao(codigoAutorizacao: String) {
        // Configuração do Retrofit para fazer a solicitação ao Discord
        val retrofit = Retrofit.Builder()
            .baseUrl("https://discord.com/api/v10/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val discordApiService = retrofit.create(DiscordApiService::class.java)

        val clientId = CLIENT_ID // Usando a variável global
        val clientSecret = CLIENT_SECRET // Usando a variável global
        val redirectUri = REDIRECT_URI // Usando a variável global
        val grantType = "authorization_code"

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
                        startActivity(Intent(this@SingupActivity, SingupActivity::class.java))
                        cadastrarPeloDiscord(codigoAutorizacao)
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

    private fun cadastrarPeloDiscord(codigoAutorizacao: String) {
        // Processar o código de autorização e obter o token de acesso
        processarCodigoAutorizacao(codigoAutorizacao)
    }

    // Função para obter os detalhes do usuário do Discord e preencher o formulário
    private fun obterDadosDoDiscordETokenDeAcesso(tokenDeAcesso: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://discord.com/api/v10/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val discordApiService = retrofit.create(DiscordApiService::class.java)

        val call = discordApiService.getDiscordUser("Bearer $tokenDeAcesso")

        call.enqueue(object : Callback<DiscordUserResponse> {
            override fun onResponse(
                call: Call<DiscordUserResponse>,
                response: Response<DiscordUserResponse>
            ) {
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


}
