package crossgame.android.application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityAddInterestsBinding

class AddInterestsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAddInterestsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInterestsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    val platforms = listOf("PLATSTATION", "XBOX", "MOBILE", "PC");

    var userInterests = mutableListOf<String>();


    fun getAllInterests() : List<String> { return platforms }

    fun getAllUserInterests(){

    }


    fun insertInterestForUser() : Boolean {
        return false;
    }

    fun deleteInterestForUser() : Boolean {
        return false;
    }

    fun compareListOfInterests() : List<String> {
        return emptyList();
    }
}