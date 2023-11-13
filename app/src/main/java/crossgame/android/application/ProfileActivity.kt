package crossgame.android.application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import crossgame.android.application.databinding.ActivityProfileBinding
import crossgame.android.application.databinding.BsGameListBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding


    private val navController by lazy {
        (supportFragmentManager
            .findFragmentById(binding.navigationCore.id) as NavHostFragment
                ).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.bottomNavigationView) {
            setupWithNavController(navController)
        }
    }
}