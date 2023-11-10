package crossgame.android.application.menu

import android.content.Context
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
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.user.UserList
import crossgame.android.service.UserFriendService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private var friendList: List<Friends> = mutableListOf()
    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

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

        val recyclerView = binding.recyclerView
        friendsAdapter = FriendsAdapter(requireContext(), friendList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = friendsAdapter

        // Chame o método para atualizar a lista de amigos
        updateFriendsList()

        return binding.root
    }

    private fun updateFriendsList() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", 0).toLong()

        val rest = Rest.getInstance(requireActivity())
        val service = rest.create(UserFriendService::class.java)

        service.listarFriend(userId).enqueue(object : Callback<List<UserList>> {
            override fun onResponse(call: Call<List<UserList>>, response: Response<List<UserList>>) {
                if (response.isSuccessful) {
                    Log.i("GET", "Listagem de amigos realizada com sucesso")
                    val apiResponse = response.body()

                    // Limpa a lista existente e adiciona novos amigos
                    friendList = apiResponse?.map {
                        Friends(it.friendUserId, it.username)
                    } ?: emptyList()

                    // Notifica o adaptador sobre a mudança nos dados
                    friendsAdapter.updateData(friendList)
                }
            }

            override fun onFailure(call: Call<List<UserList>>, t: Throwable) {
                Log.e("GET", "Falha ao listar amigos", t)
            }
        })
    }
}
