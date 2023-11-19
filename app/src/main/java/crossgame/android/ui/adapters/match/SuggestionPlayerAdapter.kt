package crossgame.android.ui.adapters.match

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView

import android.widget.LinearLayout

import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


import crossgame.android.domain.models.users.UserMatch
import crossgame.android.application.R
import crossgame.android.domain.httpClient.Rest
import crossgame.android.domain.models.enums.FriendshipState
import crossgame.android.domain.models.friends.FriendAdd
import crossgame.android.domain.models.notifications.NotificationRequest
import crossgame.android.domain.models.notifications.NotificationState
import crossgame.android.domain.models.notifications.NotificationType
import crossgame.android.domain.models.platforms.GameplayPlatformType
import crossgame.android.infra.OnBackButtonClickListener
import crossgame.android.service.FriendsService
import crossgame.android.service.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SuggestionPlayerAdapter(
    private val context: Context,
    private val userList: List<UserMatch>,
    private val onBackButtonClickListener: OnBackButtonClickListener,
) : RecyclerView.Adapter<SuggestionPlayerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.titleUsername)
        val imageUser : ImageView = itemView.findViewById(R.id.imagemUser)

        val chipGroupJogos : ChipGroup = itemView.findViewById(R.id.listGames)
        val chipGroupInteresses : ChipGroup = itemView.findViewById(R.id.listInteresses)

        val ratingBarHabilidade : RatingBar = itemView.findViewById(R.id.ratingBar_Habilidade)
        val ratingBarComportamento: RatingBar = itemView.findViewById(R.id.ratingBar_Comportamento)


        var imagePlaystation = itemView.findViewById<ImageView>(R.id.playstation_image);
        var imageMobile = itemView.findViewById<ImageView>(R.id.mobile_image);
        var imagePC = itemView.findViewById<ImageView>(R.id.computer_image);
        var imageXbox = itemView.findViewById<ImageView>(R.id.xbox_image);

        val imageMedal = itemView.findViewById<ImageView>(R.id.medal);

        val buttonAddFriend = itemView.findViewById<ToggleButton>(R.id.button_add_friend)
        val buttonBack = itemView.findViewById<Button>(R.id.button_voltar)


        val containerPlataforms = itemView.findViewById<LinearLayout>(R.id.listPlataforms)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.card_suggestion_player, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        holder.username.text = user.baseUser.username

        holder.ratingBarHabilidade.rating = user.feedback.mediaHabilidade.toFloat();
        holder.ratingBarComportamento.rating = user.feedback.mediaComportamento.toFloat();

        holder.itemView.setOnClickListener {

        }


        holder.chipGroupJogos.removeAllViews()
        holder.containerPlataforms.removeAllViews()
        holder.chipGroupInteresses.removeAllViews()

        if (user.games.isEmpty()) {
            val genericChip = Chip(context, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)
            genericChip.isSelected = false
            genericChip.setChipBackgroundColorResource(R.color.md_theme_dark_inverseOnSurface)
            genericChip.setChipStrokeColorResource(R.color.md_theme_dark_inverseOnSurface)
            genericChip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))
            genericChip.text = "Sem jogos"
            genericChip.tag = "Sem jogos"
            holder.chipGroupJogos.addView(genericChip)
        } else { for (game in user.games) {
                val newChip = Chip(context, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)
                newChip.isSelected = false
                newChip.setChipBackgroundColorResource(R.color.md_theme_dark_inverseOnSurface)
                newChip.setChipStrokeColorResource(R.color.md_theme_dark_inverseOnSurface)
                newChip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))
                newChip.text = game.name
                newChip.tag = game.name
                holder.chipGroupJogos.addView(newChip)
            } }

        Glide.with(context)
            .load(if (user.img == "default_image"  || user.img == null) R.drawable.user_2 else android.util.Base64.decode(user.img, android.util.Base64.DEFAULT))
//            .placeholder()
//            .error()
            .into(holder.imageUser)



        if (user.preference.isEmpty()) {
            val genericChip = Chip(context, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)
            genericChip.isSelected = false
            genericChip.setChipBackgroundColorResource(R.color.md_theme_dark_inverseOnSurface)
            genericChip.setChipStrokeColorResource(R.color.md_theme_dark_inverseOnSurface)
            genericChip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))
            genericChip.text = "Sem interesses"
            genericChip.tag = "Sem interesses"
            holder.chipGroupInteresses.addView(genericChip)
        } else {
            for (interesse in user.preference) {
                val newChip = Chip(context, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)
                newChip.isSelected = false
                newChip.setChipBackgroundColorResource(R.color.md_theme_dark_inverseOnSurface)
                newChip.setChipStrokeColorResource(R.color.md_theme_dark_inverseOnSurface)
                newChip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))
                newChip.text = interesse
                newChip.tag = interesse
                holder.chipGroupInteresses.addView(newChip)
            }
        }

        if (user.platforms.isEmpty()) {

            val space = TextView(context)
            space.text = "spa"
            space.setTextColor(ContextCompat.getColor(context, R.color.md_theme_dark_inverseOnSurface))

            holder.containerPlataforms.addView(space)

            val genericChip = Chip(context, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)
            genericChip.isSelected = false
            genericChip.setChipBackgroundColorResource(R.color.md_theme_dark_inverseOnSurface)
            genericChip.setChipStrokeColorResource(R.color.md_theme_dark_inverseOnSurface)
            genericChip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))
            genericChip.text = "Sem plataformas"
            genericChip.tag = "Sem plataformas"
            holder.containerPlataforms.addView(genericChip)
        } else { for (platform in user.platforms){ when (platform) {
                    "PC" -> holder.imagePC.visibility = View.VISIBLE
                    "XBOX"-> holder.imageXbox.visibility = View.VISIBLE
                    "MOBILE" -> holder.imageMobile.visibility = View.VISIBLE
                    "PLAYSTATION" -> holder.imagePlaystation.visibility = View.VISIBLE
                } } }

        when (user.qtdFriends) {
            in 0..5 -> holder.imageMedal.setImageResource(R.drawable.image_medalha_prata)
            in 6..12 -> holder.imageMedal.setImageResource(R.drawable.image_medalha_ouro)
            in 13..21 -> holder.imageMedal.setImageResource(R.drawable.image_medalha_diamante)
            else -> holder.imageMedal.setImageResource(R.drawable.image_medalha_mestre)
        }

        holder.buttonAddFriend.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if (sendRequestFriend(getUserId(), user.baseUser.username, user.baseUser.id)) {
                    Toast.makeText(context, "Solicitação de amizade enviada!", Toast.LENGTH_LONG).show()

                    sendNotificationFriend(user.baseUser.username,user.baseUser.id,)

                    holder.buttonAddFriend.isChecked = true
                    holder.buttonAddFriend.setTextColor(ContextCompat.getColor(context, R.color.button_heart))
                    holder.buttonAddFriend.setBackgroundResource(R.drawable.baseline_favorite_pressed)

                } else {
                    Log.e("ERRO", "ERRO ao adicionar amigo")
                }
            }
        }

        holder.buttonBack.setOnClickListener {
            exit()
            resetUserPositionInSharedPrefereces()
        }



        // Regras quando o usuário não tiver cadastrado algo



    }

    override fun getItemCount(): Int {
        return userList.size
    }

    private suspend fun sendRequestFriend(userId: Long, friendUsername: String, friendUserId : Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = Rest.getInstance().create(FriendsService::class.java)
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
    private suspend fun sendNotificationFriend(friendUsername: String, friendUserId : Long) : Boolean{
        return withContext(Dispatchers.IO) {
            try {
                val response = Rest.getInstance().create(NotificationService::class.java)
                    .createNotification(friendUserId, notification = NotificationRequest(message = friendUsername + " enviou um pedido de amizade", description = friendUsername.toString(), NotificationType.FRIEND_REQUEST, NotificationState.AWAITING )).execute()

                if (response.isSuccessful) {
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

    private fun exit(){
        onBackButtonClickListener.onBackButtonClick()
    }

    private fun resetUserPositionInSharedPrefereces(){
        val sharedPreferences =
            context.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.remove("MATCH_POSITION")
        editor.apply()
    }
}
