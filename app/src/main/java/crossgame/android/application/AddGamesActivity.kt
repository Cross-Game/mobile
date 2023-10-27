package crossgame.android.application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityAddGamesBinding

class AddGamesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAddGamesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGamesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener { backScreen() }
    }

    private fun backScreen() {
        finish()
    }

    private var games = 10;
    private var userGames = 10;

    fun getAllGames(){
        
    }

    fun getAllUserGames(){

    }

    fun insertGameForUser(){

    }

    fun deleteGameForUser(){

    }

    fun compareListOfGames(){

    }
}

