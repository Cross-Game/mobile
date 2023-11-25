package crossgame.android.ui.adapters.match

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import crossgame.android.application.R
import crossgame.android.application.SuggestionPlayerActivity
import crossgame.android.application.databinding.CardUserFilterBinding
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.enums.FriendshipState
import crossgame.android.domain.models.friends.FriendAdd
import crossgame.android.domain.models.notifications.NotificationRequest
import crossgame.android.domain.models.notifications.NotificationState
import crossgame.android.domain.models.notifications.NotificationType
import crossgame.android.domain.models.users.UserMatch
import crossgame.android.service.FriendsService
import crossgame.android.service.NotificationService
import crossgame.android.service.UserFriendService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MatchAdapter(private val context: Context) :
    RecyclerView.Adapter<MatchAdapter.ViewHolder>() {

    private val users = mutableListOf<UserMatch>()

    class ViewHolder(binding: CardUserFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameUser = binding.textNomepessoa
        val imageUser = binding.imagePessoa
        val chipGroup = binding.listOfGames

        val buttonAddFriend = binding.buttonAddFriend
        val buttonLink = binding.buttonLink

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = CardUserFilterBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.nameUser.text = user.baseUser.username
        holder.chipGroup.removeAllViews()


        Glide.with(context)
            .load(if (user.img == "default_image") R.drawable.user_2  else android.util.Base64.decode(user.img, android.util.Base64.DEFAULT))
//            .placeholder()
//            .error()
            .into(holder.imageUser)

        val maxGamesToShow = 3
        val games = user.games

        if (games.size.equals(0)){
            val newChip = Chip(context, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)
            newChip.isSelected = false
            newChip.setChipBackgroundColorResource(
                if (newChip.isSelected) R.color.md_theme_dark_onPrimary
                else R.color.md_theme_dark_inverseOnSurface
            )
            newChip.isCloseIconVisible = false
            newChip.text = "Sem jogos"
            newChip.tag = "Sem jogos"
            newChip.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.chipGroup.addView(newChip)
        }

        for (i in 0 until minOf(maxGamesToShow, games.size)) {
            val newChip = Chip(context, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)
            newChip.isSelected = false
            newChip.setChipBackgroundColorResource(
                if (newChip.isSelected) R.color.md_theme_dark_onPrimary
                else R.color.md_theme_dark_inverseOnSurface
            )
            newChip.isCloseIconVisible = false
            newChip.text = games[i].name
            newChip.tag = games[i].name
            newChip.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.chipGroup.addView(newChip)
        }

        val remainingGames = games.size - maxGamesToShow
        if (remainingGames > 0) {
            val moreGamesChip = Chip(context)

            val marginInPixels = 1000;
            moreGamesChip.setPadding(marginInPixels, 0, 0, 0)

            moreGamesChip.text = "+$remainingGames"
            moreGamesChip.setTextColor(ContextCompat.getColor(context, R.color.white))

            val space = TextView(context)
            space.text = "spa"
            space.setTextColor(ContextCompat.getColor(context, R.color.md_theme_dark_inverseOnSurface))

            holder.chipGroup.addView(space)
            holder.chipGroup.addView(moreGamesChip)
        }

        setAnimation(holder.itemView, position)

        holder.buttonAddFriend.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if (sendRequestFriend(getUserId(), user.baseUser.username, user.baseUser.id)) {
                    Toast.makeText(context, "Solicitação de amizade enviada!", Toast.LENGTH_LONG).show()

                    sendNotificationFriend(getMyName(),user.baseUser.id,)
                    holder.buttonAddFriend.isChecked = true
                    holder.buttonAddFriend.setTextColor(ContextCompat.getColor(context, R.color.button_heart))
                    holder.buttonAddFriend.setBackgroundResource(R.drawable.baseline_favorite_pressed)

                } else {
                    Log.e("ERRO", "ERRO ao adicionar amigo")
                }
            }
        }

        holder.buttonLink.setOnClickListener {
            saveUserPositionToSharedPreferences(position)
            val intent = Intent(context, SuggestionPlayerActivity::class.java)
            context.startActivity(intent)
        }

        holder.buttonAddFriend.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                holder.buttonAddFriend.setBackgroundResource(R.drawable.baseline_favorite_pressed)
                holder.buttonAddFriend.setTextColor(ContextCompat.getColor(context, R.color.button_heart))
            }
        }
    }

    private fun setAnimation(view: View, position: Int) {
        val animation = AnimationUtils.loadAnimation(view.context, com.google.android.material.R.anim.abc_slide_in_bottom)
        view.startAnimation(animation)
    }

    private suspend fun sendRequestFriend(userId: Long, friendUsername: String, friendUserId : Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = Rest.getInstance(context).create(FriendsService::class.java)
                    .addFriendToAnUser(userId, FriendAdd(friendUsername, friendUserId, FriendshipState.SENDED),).execute()

                if (response.isSuccessful) {
                    return@withContext true
                }

                if (response.code() == 429){
                    Toast.makeText(context, "Solicitação de amizade já enviada!", Toast.LENGTH_LONG).show()
                }

                Log.e("ERRO", response.toString())
                Log.e("ERRO", "Corpo da resposta: ${response.errorBody()?.string()}")
            } catch (e: Exception) {
                Log.e("EXCEPTION", e.toString())
            }
            return@withContext false
        }
    }

    private suspend fun sendNotificationFriend(myName: String, friendUserId : Long) : Boolean{
        return withContext(Dispatchers.IO) {
            try {
                val response = Rest.getInstance(context).create(NotificationService::class.java)
                    .createNotificationWithQuery(
                        friendUserId,
                        message = myName + " enviou um pedido de amizade",
                        description = myName.toString(),
                        type = NotificationType.FRIEND_REQUEST,
                        state = NotificationState.AWAITING).execute()

                if (response.isSuccessful) {
                    Toast.makeText(context, "Notificação de amizade enviada!", Toast.LENGTH_LONG).show()
                    return@withContext true
                }

                if (response.code() == 429){
                    Toast.makeText(context, "Notificação de amizade já enviada!", Toast.LENGTH_LONG).show()
                }

                Log.e("ERRO", response.toString())
                Log.e("ERRO", "Corpo da resposta: ${response.errorBody()?.string()}")
            } catch (e: Exception) {
                Log.e("EXCEPTION", e.toString())
            }
            return@withContext false
        }
    }

    private fun getUserId(): Long {
        val sharedPreferences =
            context.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id", 1).toLong()
    }

    private fun getMyName(): String {
        val sharedPreferences =
            context.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences.getString("username", "Meu nome").toString();
    }

    fun updateData(newData: List<UserMatch>) {
        users.clear()
        users.addAll(newData)
        saveUsersToSharedPreferences(newData)
        notifyDataSetChanged()
    }

    private fun saveUserPositionToSharedPreferences(userPosition : Int) {
        val sharedPreferences =
            context.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putInt("MATCH_POSITION", userPosition)
        editor.apply()
    }

    private fun saveUsersToSharedPreferences(users: List<UserMatch>) {
        val jsonUsers = convertListToJson(users)

        val sharedPreferences =
            context.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putString("MATCH_USERS", jsonUsers)
        editor.apply()
    }

    private fun convertListToJson(users: List<UserMatch>): String {
        val gson = Gson()
        return gson.toJson(users)
    }

}
