package crossgame.android.application

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
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
    private lateinit var rootView: View
    private val selectedPlatforms = mutableSetOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlatformsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rootView = findViewById(android.R.id.content)


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
        onCadastrarButtonClick()
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
                exibirSnackbar("Minhas plataformas atualizada com sucesso!", true)
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {

                exibirSnackbar("Ops! Ocorreu um erro ao atualizar plataformas. Tente novamente", false)
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

    private fun exibirSnackbar(mensagem: String, isSucess : Boolean = true) {
        val snackbar = Snackbar.make(rootView, mensagem, Snackbar.LENGTH_SHORT)

        if (isSucess) {
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.sucess))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        else {
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.error))
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        snackbar.show()
    }

}