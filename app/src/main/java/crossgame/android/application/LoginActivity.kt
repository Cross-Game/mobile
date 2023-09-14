package crossgame.android.application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityLoginBinding

class LoginActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNaoPossuiConta.setOnClickListener { telaCadastro() }
    }

    private fun telaCadastro(){
        startActivity(Intent(baseContext, SingupActivity::class.java))
    }
}