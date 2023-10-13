package crossgame.android.application

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivitySingupBinding

class SingupActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySingupBinding
    private var senhaVisivel = false
    private var confirmarSenhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
            }

            override fun afterTextChanged(s: Editable?) {
                validarSenha()
            }
        })



        binding.btnRegistrar.setOnClickListener {
            val nome = binding.editTextNome.text.toString()
            val email = binding.editTextEmail.text.toString()
            val senha = binding.editTextSenha.text.toString()
            val confirmarSenha = binding.editTextConfirmarSenha.text.toString()

            if (isValidEmail(email) && isValidPassword(senha) && senha == confirmarSenha) {
                // Mostre um Toast para indicar o registro bem-sucedido
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                // Dados válidos, continue com o registro
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Exiba uma mensagem de erro se os dados forem inválidos
                Toast.makeText(this, "Dados inválidos. Verifique seu email e senha.", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnPossuiConta.setOnClickListener {
            telaEntrar()
        }
    }

    private fun telaEntrar() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".com")
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
}