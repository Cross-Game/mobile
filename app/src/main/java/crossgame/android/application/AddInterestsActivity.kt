package crossgame.android.application

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import crossgame.android.application.databinding.ActivityAddInterestsBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.users.UserPreference
import crossgame.android.service.PreferencesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddInterestsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddInterestsBinding
    private lateinit var rootView: View

    private lateinit var preferencesService: PreferencesService

    var userInterests = mutableListOf<String>()
    var userInterestsDynamic = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInterestsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rootView = findViewById(android.R.id.content)
        binding.buttonBack.setOnClickListener { onExit(it) }
        getAllUserInterestsInDatabase()
    }


    fun getAllUserInterestsInDatabase() {
        Log.i("GET", "Listando Preferencias")
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        var id = sharedPreferences.getInt("id", 0).toLong()
        preferencesService = Rest.getInstance(this).create(PreferencesService::class.java)
        preferencesService.listar(id).enqueue(object : Callback<UserPreference> {
            override fun onResponse(
                call: Call<UserPreference>,
                response: Response<UserPreference>
            ) {
                if (response.isSuccessful) {
                    Log.i("GET", "Sucesso ao listar Preferencias")
                    val preferences = response.body()?.preferences
                    preferences?.forEach { preferences ->
                        userInterests.add(preferences.preferences)
                        val chipId =
                            resources.getIdentifier(preferences.preferences, "id", packageName)
                        enableOrDisableChip(findViewById<Chip>(chipId))
                        Log.i("CHIP", "Chips Habilitado: " + preferences.preferences)
                    }
                } else {
                    Log.i("ERRO", "Response falhou !")
                }
            }

            override fun onFailure(call: Call<UserPreference>, t: Throwable) {
                Log.e("ERROR", "ERRO AO OBTER PREFERENCIAS: " + t.message.toString()) // TODO
            }
        })
    }

    fun getAllUserInterests(): List<String> {
        return userInterests
    }

    fun insertInterestForUser(preferences: Set<String>, onResult: (Boolean) -> Unit) {
        Log.i("Sucesso", "Inserindo as preferências: $preferences")
        val sharedPreferences =
            this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        var id = sharedPreferences.getInt("id", 0).toLong()
        preferencesService = Rest.getInstance(this).create(PreferencesService::class.java)
        val call = preferencesService.adicionar(id, preferences)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.i("Sucesso", "Sucesso ao adicionar preferências: $preferences")
                    onResult(true)
                } else {
                    Log.e("ERROR", "Erro ao adicionar preferências: ${response.message()}")
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("EXCEPTION", "Erro ao adicionar preferências: $t")
                onResult(false)
            }
        })
    }

    fun deleteInterestForUser(namePreference: String, onResult: (Boolean) -> Unit) {
        Log.i("Information", "Deletando preferência: $namePreference")

        try {
            val sharedPreferences =
                this.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
            var id = sharedPreferences.getInt("id", 0).toLong()
            preferencesService = Rest.getInstance(this).create(PreferencesService::class.java)
            val response = preferencesService.deletar(id, namePreference).execute()

            if (response.isSuccessful) {
                Log.i("Sucesso", "Sucesso ao deletar preferência: $namePreference")
                onResult(true)
            } else {
                Log.e("ERROR", "Erro ao deletar preferência: ${response.message()}")
                onResult(false)
            }
        } catch (e: Exception) {
            Log.e("EXCEPTION", "Exceção durante a exclusão de preferência: $e")
            onResult(false)
        }
    }


    fun isTheSameThanOriginal(): Boolean {
        return userInterests.sorted() == userInterestsDynamic.sorted()
    }

    fun insertOrDeleteInterestForUser() {
        if (isTheSameThanOriginal()) {
            Log.i("Information", "Listas iguais")
            return
        }

        val interestsForInsert = userInterestsDynamic.subtract(userInterests)
        val interestForDelete = userInterests.subtract(userInterestsDynamic)

        runBlocking {
            interestForDelete.forEach { preference ->
                launch(Dispatchers.IO) {
                    deleteInterestForUser(preference) { success ->
                        print("OK")
                    }
                }
            }
        }

        if (interestsForInsert.isNotEmpty()) {
            insertInterestForUser(interestsForInsert) { success ->
                if (success) {
                    Log.i("SUCESS", "SUCESSO AO INSERIR")
                } else {
                    Log.i("ERROR", "ERRO AO INSERIR")
                }
            }
        }

        exibirSnackbar("Lista de interesses atualizada com sucesso!.", true)
    }

    fun enableOrDisableChip(view: View) {
        if (view is Chip) {
            val chip = view as Chip
            val preferenceName = chip.tag as String

            if (chip.isSelected) {
                chip.isSelected = false
                chip.isCloseIconVisible = false
                chip.setChipBackgroundColorResource(R.color.md_theme_dark_inverseOnSurface)
                userInterestsDynamic.remove(preferenceName)
            } else {
                chip.isSelected = true
                chip.setChipBackgroundColorResource(R.color.md_theme_dark_onPrimary)
                chip.isCloseIconVisible = true
                chip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(R.color.white)))
                chip.setCloseIconTintResource(R.color.white)
                userInterestsDynamic.add(preferenceName)
            }
        }
    }

    fun onExit(v: View) {
        Log.i("EXIT", "Saindo da tela")
        insertOrDeleteInterestForUser()
        finish()
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
