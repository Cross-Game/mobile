package crossgame.android.application

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import crossgame.android.application.databinding.ActivitySuggestionPlayerBinding
import crossgame.android.domain.models.feedbacks.MediaFeedback
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.games.ImageGame
import crossgame.android.domain.models.users.BaseUser
import crossgame.android.domain.models.users.UserMatch
import crossgame.android.infra.OnBackButtonClickListener
import crossgame.android.ui.adapters.match.SuggestionPlayerAdapter
import java.util.Timer
import java.util.TimerTask

class SuggestionPlayerActivity : AppCompatActivity(), OnBackButtonClickListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var progressBar: ProgressBar
    private lateinit var userAdapter: SuggestionPlayerAdapter
    private lateinit var handler: Handler
    private lateinit var sharedPreferences: SharedPreferences
    private var currentPage = 0
    private val duration = 10000L // 10s
    private var timer: Timer? = null
    private val updateInterval = 100L // 100ms
    private val maxProgress = 100
    private var currentProgress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        currentPage = getPositionFromSharedPrefereces()
        setContentView(R.layout.activity_suggestion_player)


        Log.i("TESTE", "Current page: " + currentPage)

        viewPager = findViewById(R.id.viewPager)
        progressBar = findViewById(R.id.progressBar)

        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        // userAdapter = SuggestionPlayerAdapter(context = this, userList = getUsers(), this)
        userAdapter = SuggestionPlayerAdapter(context = this, userList = getUsersFromSharedPreferences(), this)
        viewPager.adapter = userAdapter
        viewPager.post { viewPager.setCurrentItem(getPositionFromSharedPrefereces(), false) }

        handler = Handler(Looper.getMainLooper())
        startTimer()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                resetProgress()
            }
        })

        viewPager.setOnTouchListener { _, _ ->
            pauseTimer()
            true
        }
    }

    private fun getUsers(): List<UserMatch> {
        return listOf(
            UserMatch(
                baseUser = BaseUser(1, "user1", "user1@example.com", "user", true),
                games = listOf(
                    GameResponse(
                        id = 123,
                        platformsType = listOf("PC", "PlayStation"),
                        imageGame = ImageGame(
                            1,
                            "cover",
                            "https://example.com/game1_cover.jpg",
                            "abc123"
                        ),
                        gameGenres = listOf("Action", "Adventure"),
                        name = "Game 1",
                        platforms = listOf("Windows", "PlayStation 4"),
                        cover = 456,
                        genres = listOf(1, 2, 3)
                    )
                ),
                img = "default_image",
                feedback = MediaFeedback(1, 2, 1),
                preference = listOf("Arroz", "Feijão"),
                platforms = listOf("PC", "PLAYSTATION", "MOBILE", "XBOX"),
                qtdFriends = 1
            ),

            UserMatch(
                baseUser = BaseUser(2, "user2", "user2@example.com", "user", true),
                games = listOf(
                    GameResponse(
                        id = 456,
                        platformsType = listOf("PC", "Xbox"),
                        imageGame = ImageGame(
                            2,
                            "cover",
                            "https://example.com/game2_cover.jpg",
                            "def456"
                        ),
                        gameGenres = listOf("RPG", "Simulation"),
                        name = "Game 2",
                        platforms = listOf("Windows", "Xbox Series X"),
                        cover = 789,
                        genres = listOf(4, 5, 6)
                    )
                ),
                img = "default_image",
                feedback = MediaFeedback(1, 2, 1),
                preference = listOf("Arroz", "Feijão"),
                platforms = listOf("XBOX", "PLAYSTATION"),
                qtdFriends = 55
            ),
            UserMatch(
                baseUser = BaseUser(3, "user3", "user3@example.com", "user", true),
                games = listOf(
                    GameResponse(
                        id = 789,
                        platformsType = listOf("PlayStation", "Switch"),
                        imageGame = ImageGame(
                            3,
                            "cover",
                            "https://example.com/game3_cover.jpg",
                            "ghi789"
                        ),
                        gameGenres = listOf("Sports", "Racing"),
                        name = "Game 3",
                        platforms = listOf("PlayStation 5", "Nintendo Switch"),
                        cover = 1011,
                        genres = listOf(7, 8, 9)
                    )
                ),
                img = "default_image",
                feedback = MediaFeedback(1, 2, 1),
                preference = listOf("Arroz", "Feijão"),
                platforms = listOf("XBOX", "MOBILE"),
                qtdFriends = 10
            )
        )
    }

    private fun getUsersFromSharedPreferences(): List<UserMatch> {
        val sharedPreferences = getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val jsonUsers = sharedPreferences.getString("MATCH_USERS", "")

        Log.i("getUsersFromSharedPreferences", jsonUsers!!)
        if (jsonUsers.isNullOrEmpty()) {
            return emptyList()
        }

        val gson = Gson()
        val type = object : TypeToken<List<UserMatch>>() {}.type
        return gson.fromJson(jsonUsers, type)
    }

    private fun getPositionFromSharedPrefereces(): Int {
        var position = sharedPreferences.getInt("MATCH_POSITION", 0)
        Log.i("getPositionFromSharedPrefereces", "Posição Salva: " + position)
        return position
    }

    private fun startTimer() {
        timer = Timer()
        val updateTask = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    updateProgressBar()
                }
            }
        }

        timer?.scheduleAtFixedRate(updateTask, 0, updateInterval)
    }

    private fun pauseTimer() {
        timer?.cancel()
    }

    private fun updateProgressBar() {
        currentProgress += 1
        progressBar.progress = currentProgress

        if (currentProgress == maxProgress) {
            goToNextUser()
        }
    }

    private fun resetProgress() {
        currentProgress = 0
        progressBar.progress = currentProgress
    }

    private fun goToNextUser() {
        if (currentPage < userAdapter.itemCount - 1) {
            currentPage++
            viewPager.currentItem = currentPage
        } else {
            currentPage = 0
            viewPager.currentItem = currentPage
        }
        resetProgress()
    }

    override fun onBackButtonClick() {
        super.onBackPressed()
    }
}
