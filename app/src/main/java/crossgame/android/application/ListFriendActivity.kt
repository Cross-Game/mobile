package crossgame.android.application

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import crossgame.android.application.databinding.ActivityListFriendBinding
import crossgame.android.domain.models.friends.Friends
import crossgame.android.ui.adapters.friends.FriendsAdapter

class ListFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListFriendBinding
    private var friendList: List<Friends> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val searchIcon = findViewById<ImageView>(R.id.searchIconTextView)
//        val searchEditText = findViewById<EditText>(R.id.searchEditText)
//        val cancelButton = findViewById<Button>(R.id.cancelButton)
//
//        searchIcon.setOnClickListener {
//            searchEditText.visibility = View.VISIBLE
//            cancelButton.visibility = View.VISIBLE
//        }
//
//        cancelButton.setOnClickListener {
//            searchEditText.text.clear()
//            searchEditText.visibility = View.GONE
//            cancelButton.visibility = View.GONE
//        }


        initializeFriendList()

        if (friendList.isNotEmpty()) {
            val recyclerView = binding.recyclerView
            val friendsAdapter = FriendsAdapter(this, friendList)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = friendsAdapter
        } else {
            Log.e("ListFriendActivity", "A lista de amigos está vazia.")
        }
    }

    private fun initializeFriendList() {
        friendList = mutableListOf(
            Friends(
                "Junior Patricio",
                "Bora jogatina",
                "https://w7.pngwing.com/pngs/227/283/png-transparent-call-break-card-game-android-banduk-game-youtube-android-game-mobile-phones-profile.png"
            ),
            Friends(
                "China",
                "Partiu Sekiro",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQwudakIrwTEVtZMpYd8zO6NWzaIdpg6hkcJA&usqp=CAU"
            ),
            Friends(
                "Mello",
                "Hoje tem UFC",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRlVjfrsqiO8XxWB_iLOsB9mu81mCOjWh-moQ&usqp=CAU"
            )
        )
        // Log para verificar a lista
        Log.d("ListFriendActivity", "Amigos: $friendList")
    }
}
