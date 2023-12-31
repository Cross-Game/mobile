package crossgame.android.application

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
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

    override fun onStart() {
        super.onStart()
        val sharedPreferences = getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        if (username != "") {
            redirectToMatch()
        }
    }

    private fun redirectToMatch() {
        startActivity(Intent(baseContext, ProfileActivity::class.java))
    }

    private fun telaCadastro(){
        startActivity(Intent(baseContext, SingupActivity::class.java))
    }

    private fun telaEntrar() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}