package crossgame.android.application.menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import crossgame.android.application.databinding.FragmentChatBinding
import crossgame.android.domain.models.friends.Friends
import crossgame.android.ui.adapters.friends.FriendsAdapter

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private var friendList: List<Friends> = mutableListOf()

    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(
            inflater,
            container,
            false
        )

        initializeFriendList()

        searchIcon = binding.searchIconTextView
        searchEditText = binding.searchEditText

        searchIcon.setOnClickListener {
            if (searchIcon.visibility == View.VISIBLE) {
                searchIcon.visibility = View.GONE
                searchEditText.visibility = View.VISIBLE
            } else {
                searchIcon.visibility = View.VISIBLE
                searchEditText.visibility = View.GONE
            }
        }

        if (friendList.isNotEmpty()) {
            val recyclerView = binding.recyclerView
            val friendsAdapter = FriendsAdapter(requireContext(), friendList)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = friendsAdapter
        } else {
            Log.e("ListFriendActivity", "A lista de amigos está vazia.")
        }

        return binding.root
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
            ), Friends(
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
            ),
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
            ), Friends(
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