package crossgame.android.application.menu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import crossgame.android.application.databinding.FragmentChatBinding
import crossgame.android.domain.models.friends.Friends
import crossgame.android.ui.adapters.friends.FriendsAdapter
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.user.UserList
import crossgame.android.service.AutenticationUser
import crossgame.android.service.UserFriendService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private var originalFriendList: List<Friends> = mutableListOf()
    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var closeSearchButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        searchIcon = binding.searchIconTextView
        searchEditText = binding.searchEditText

        searchIcon.setOnClickListener {
            toggleSearchBar()
        }

        closeSearchButton = binding.closeSearchButton
        closeSearchButton.setOnClickListener {
            closeSearchBar()
        }

        val recyclerView = binding.recyclerView
        friendsAdapter = FriendsAdapter(requireContext(), mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = friendsAdapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filtra a lista com base no texto digitado
                val filteredList = originalFriendList.filter { friend ->
                    friend.username.contains(s.toString(), ignoreCase = true)
                }
                // Atualiza o adaptador com a nova lista filtrada
                friendsAdapter.updateData(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Chame o método para atualizar a lista de amigos
        updateFriendsList()

        return binding.root
    }

    private fun toggleSearchBar() {
        if (searchIcon.visibility == View.VISIBLE) {
            searchIcon.visibility = View.GONE
            searchEditText.visibility = View.VISIBLE
            closeSearchButton.visibility = View.VISIBLE
            // Salva a lista original antes de aplicar o filtro
            originalFriendList = friendsAdapter.getData()
        } else {
            closeSearchBar()
        }
    }

    private fun closeSearchBar() {
        searchIcon.visibility = View.VISIBLE
        searchEditText.visibility = View.GONE
        closeSearchButton.visibility = View.GONE
        // Restaura a lista para o estado original ao fechar a barra de pesquisa
        friendsAdapter.updateData(originalFriendList)
        searchEditText.text = null
    }

    private fun updateFriendsList() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", 0).toLong()

        val rest = Rest.getInstance(requireActivity())
        val service = rest.create(UserFriendService::class.java)

        service.listarFriend(userId).enqueue(object : Callback<List<UserList>> {
            override fun onResponse(
                call: Call<List<UserList>>,
                response: Response<List<UserList>>
            ) {
                if (response.isSuccessful) {
                    Log.i("GET", "Listagem de amigos realizada com sucesso")
                    val apiResponse = response.body()

                    originalFriendList = apiResponse?.map {
                        Friends(it.friendUserId, it.username, null)
                    } ?: emptyList()

                    // Atualiza o adaptador com a nova lista
                    friendsAdapter.updateData(originalFriendList)

                    // Chama a função para obter a foto de cada amigo
                    originalFriendList.forEach { friend ->
                        getPhotoUser(friend.friendUserId)
                    }
                } else {
                    Log.e("GET", "Falha ao listar amigos")
                }


                val emptyFriendsListTextView = binding.emptyFriendsListTextView
                val emptyFriendsListImageView = binding.imageCenter
                val emptyFriendsListImageView2 = binding.emptyFriendsListTextView2
                if (originalFriendList.isEmpty()) {
                    emptyFriendsListTextView.visibility = View.VISIBLE
                    emptyFriendsListImageView.visibility = View.VISIBLE
                    emptyFriendsListImageView2.visibility = View.VISIBLE

                } else {
                    emptyFriendsListTextView.visibility = View.GONE
                    emptyFriendsListImageView.visibility = View.GONE
                    emptyFriendsListImageView2.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<List<UserList>>, t: Throwable) {
                Log.e("GET", "Falha ao listar amigos", t)

                // Atualiza a visibilidade do TextView caso ocorra falha ao listar amigos
                val emptyFriendsListTextView = binding.emptyFriendsListTextView
                if (originalFriendList.isEmpty()) {
                    emptyFriendsListTextView.visibility = View.VISIBLE
                } else {
                    emptyFriendsListTextView.visibility = View.GONE
                }
            }
        })
    }



    private fun getPhotoUser(friendUserId: Long) {
        val rest = Rest.getInstance(requireActivity())
        rest.create(AutenticationUser::class.java).getPhoto(friendUserId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()?.contentLength()?.toInt() != 0) {
                            val inputStream: InputStream = response.body()!!.byteStream()
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

                            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
                            val decodedByte =
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            friendsAdapter.updateFriendPhoto(friendUserId, bitmap)


                        }
                    } else {
                        Log.i("GET", "Ops, imagem incompatível !")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.i("GET", "Falha ao obter a foto de perfil")
                }
            })
    }
}
