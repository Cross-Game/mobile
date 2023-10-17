package crossgame.android.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import crossgame.android.application.databinding.ActivityPlatformsBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.service.PlatformsService
import crossgame.android.service.PreferencesService

class PlatformsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlatformsBinding
    private val selectedPlatforms = mutableSetOf<ImageView>()
    var platformsService = Rest.getInstance().create(PlatformsService::class.java)


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
        val selectedPlatformNames = selectedPlatforms.map { getPlatformName(it) }
        if (selectedPlatformNames.isNotEmpty()) {
        //platformsService.updateGamePlatformsForUserById()
        }
    }

    private fun getPlatformName(imageView: ImageView): String {
        return when (imageView) {
            binding.computerImage -> "PC"
            binding.xboxImage -> "XBOX"
            binding.mobileImage -> "MOBILE"
            binding.playstationImage -> "PLAYSTATION"
            else -> ""
        }
    }

    private fun showSelectedPlatformsDialog(platformNames: List<String>) {
        val platforms = platformNames.joinToString(", ")
        val dialog = AlertDialog.Builder(this)
            .setTitle("Plataformas Selecionadas")
            .setMessage("Você selecionou as seguintes plataformas: $platforms")
            .setPositiveButton("OK", null)
            .create()

        dialog.show()
    }
}