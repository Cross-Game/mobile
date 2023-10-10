package crossgame.android.application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityLoginBinding

class LoginActivity: AppCompatActivity() {

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

    private fun redirectToCadastro(){
        startActivity(Intent(baseContext, SingupActivity::class.java))
    }

    private fun redirectToMatch(){
        //Realizar o redirecionamento para a tela pós login
        //startActivity(Intent(baseContext, SingupActivity::class.java))
    }

    private fun register() {

    }

    private fun showError(errorMessage: String) {
        // Implemente a lógica para exibir a mensagem de erro ao usuário (por exemplo, usando um TextView)
    }
}