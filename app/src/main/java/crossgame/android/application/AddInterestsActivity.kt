package crossgame.android.application

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityAddInterestsBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.preferences.Preference
import crossgame.android.domain.models.users.UserPreference
import crossgame.android.service.PreferencesService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddInterestsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAddInterestsBinding

    var preferencesService = Rest.getInstance().create(PreferencesService::class.java)

    var userInterests = mutableListOf<String>();

    var id = 1L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInterestsBinding.inflate(layoutInflater)
        buildingServices()
        setContentView(binding.root)
    }

    private fun buildingServices() {
        preferencesService =  Rest.getInstance().create(PreferencesService::class.java)
    }

    fun getAllUserInterestsInDatabase() {
        preferencesService.listar(id).enqueue(object : Callback<UserPreference> {
            override fun onResponse(
                call: Call<UserPreference>,
                response: Response<UserPreference>
            ) {
                if (response.isSuccessful) {
                    val preferences = response.body()?.preference;
                    preferences?.forEach { preference ->
                        userInterests.add(preference.preference);
                    }
                }
            }

            override fun onFailure(call: Call<UserPreference>, t: Throwable) {
                println("legal"); // TODO
            }
        })
    }
    fun getAllInterests() : List<String> { return userInterests }

    fun getAllUserInterests() : List<String> {
        return userInterests;
    }

    fun insertInterestForUser() : Boolean {
        return false;
    }

    fun deleteInterestForUser() : Boolean {
        return false;
    }

    fun isTheSameThanOriginal() : Boolean {
        return userInterests.isEmpty();
    }

    fun insertOrDeleteInterestForUser(){
        var interestsForDelete = 0;
        var interestForInsert = 1;
    }
}