package crossgame.android.application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityHomeScreenBinding

class HomeScreenActivity: AppCompatActivity() {

    private lateinit var binding: ActivityHomeScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHomeCadastrar.setOnClickListener { telaCadastro() }
        binding.btnEntrar.setOnClickListener { telaEntrar() }
    }

    private fun telaCadastro(){
        startActivity(Intent(baseContext, SingupActivity::class.java))
    }

    private fun telaEntrar() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}