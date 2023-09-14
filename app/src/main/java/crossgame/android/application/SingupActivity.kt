package crossgame.android.application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivitySingupBinding

class SingupActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySingupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPossuiConta.setOnClickListener { telaEntrar() }
    }

    private fun telaEntrar() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}