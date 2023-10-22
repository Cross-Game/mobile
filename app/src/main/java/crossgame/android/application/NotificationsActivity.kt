package crossgame.android.application

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView2.setOnClickListener { testTranferDataFromSharedPreference() }
    }

    private fun testTranferDataFromSharedPreference() {
        val sharedPreferences = getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val username = sharedPreferences.getInt("id", 0)
        Toast.makeText(
            baseContext,
            username.toString(),
            Toast.LENGTH_SHORT
        ).show()
    }
}