package crossgame.android.application

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import crossgame.android.domain.httpClient.Rest
import crossgame.android.application.databinding.ActivityPlatformsBinding
import crossgame.android.domain.models.platforms.GameplayPlatformType
import crossgame.android.service.PlatformsService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlatformsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlatformsBinding
    private val selectedPlatforms = mutableSetOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlatformsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val computerImage = binding.computerImage
        val xboxImage = binding.xboxImage
        val mobileImage = binding.mobileImage
        val playstationImage = binding.playstationImage
        val images = listOf(computerImage, xboxImage, mobileImage, playstationImage)

        images.forEach { image ->
            image.setOnClickListener {
                togglePlatform(image)
            }
        }

        val cadastrarButton = binding.btnCadastrarPlataforma
        cadastrarButton.setOnClickListener { onCadastrarButtonClick() }
        binding.btnVoltar.setOnClickListener {
            toGoBack() }
    }

    private fun toGoBack() {
        finish()
    }


    private fun togglePlatform(imageView: ImageView) {
        if (selectedPlatforms.contains(imageView)) {
            selectedPlatforms.remove(imageView)
            imageView.isSelected = false
        } else {
            selectedPlatforms.add(imageView)
            imageView.isSelected = true
        }
    }

    private fun onCadastrarButtonClick() {
        val selectedPlatformNames = selectedPlatforms.map { getPlatformName(it).toString() }
        if (selectedPlatformNames.isNotEmpty()) {
            val sharedPreferences = getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
            val id = sharedPreferences.getInt("id", 0)
            var platformsService = Rest.getInstance(this).create(PlatformsService::class.java)
            platformsService.updateGamePlatformsForUserById(id?.toLong() ?: 0, selectedPlatformNames).enqueue(object :Callback<List<String>>{
            override fun onResponse(
                call: Call<List<String>>,
                response: Response<List<String>>
            ) {
                val rootView = findViewById<View>(android.R.id.content)
                val mensagem = "Plataformas adicionadas com sucesso!"
                val duracao = Snackbar.LENGTH_SHORT

                val snackbar = Snackbar.make(rootView, mensagem, duracao)
                snackbar.setBackgroundTint(Color.parseColor("#68f273"))
                snackbar.setTextColor(Color.parseColor("#212121"))
                snackbar.show()
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {

                val rootView = findViewById<View>(android.R.id.content)
                val mensagem = "Erro ao adicionar plataformas!"
                val duracao = Snackbar.LENGTH_SHORT

                val snackbar = Snackbar.make(rootView, mensagem, duracao)
                snackbar.setBackgroundTint(Color.parseColor("#F44336"))
                snackbar.setTextColor(Color.parseColor("#FFFFFF"))
                snackbar.show()
            }

        })
        }
    }


    private fun getPlatformName(imageView: ImageView): GameplayPlatformType {
        return when (imageView) {
            binding.computerImage -> GameplayPlatformType.PC
            binding.xboxImage -> GameplayPlatformType.XBOX
            binding.mobileImage -> GameplayPlatformType.MOBILE
            binding.playstationImage -> GameplayPlatformType.PLAYSTATION
            else -> GameplayPlatformType.PC
        }
    }

}