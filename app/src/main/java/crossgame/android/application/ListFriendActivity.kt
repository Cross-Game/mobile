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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


}
