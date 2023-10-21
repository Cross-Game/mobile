package crossgame.android.application

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import crossgame.android.application.databinding.ActivityLoadingBinding


class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val timer = object : CountDownTimer(1500, 1000) {

            override fun onTick(millisUntilFinished: Long) {
            }
            override fun onFinish() {
                startActivity(Intent(baseContext, HomeScreenActivity::class.java))
            }
        }
        timer.start()
    }
}