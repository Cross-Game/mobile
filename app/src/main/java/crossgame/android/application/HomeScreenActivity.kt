package crossgame.android.application

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityHomeScreenBinding
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import crossgame.android.infra.BiometricHelper
import java.util.concurrent.Executor

class HomeScreenActivity: AppCompatActivity() {

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var rootView: View
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
        if (BiometricHelper.isBiometricAvailable(this)) {

            val executor: Executor = ContextCompat.getMainExecutor(this)

            val biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (username != "") {
                            redirectToMatch()
                        } else {
                            val snackbar = Snackbar.make(rootView, "Usuario não logado !", Snackbar.LENGTH_SHORT)
                        }
                        super.onAuthenticationSucceeded(result)
                    }
                })

            // Informações apresentadas no momento da autenticação
            val info: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("CrossGame")
                .setDescription("Desbloqueie seu celular")
                .setNegativeButtonText("Cancelar")
                .build()

            biometricPrompt.authenticate(info)
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