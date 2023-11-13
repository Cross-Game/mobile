package crossgame.android.ui.adapters.match

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import crossgame.android.application.R
import crossgame.android.application.databinding.CardUserFilterBinding
import crossgame.android.domain.models.users.UserMatch


class MatchAdapter(private val users: List<UserMatch>, private val context: Context) :
    RecyclerView.Adapter<MatchAdapter.ViewHolder>() {

    class ViewHolder(binding: CardUserFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameUser = binding.textNomepessoa
        val imageUser = binding.imagePessoa
        val chipGroup = binding.listOfGames
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
//        holder.imageUser.setBackgroundColor(user.img)

        holder.chipGroup.removeAllViews()

        val maxGamesToShow = 3
        val games = user.games
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
    }

    private fun setAnimation(view: View, position: Int) {
        val animation = AnimationUtils.loadAnimation(view.context, com.google.android.material.R.anim.abc_slide_in_bottom)
        view.startAnimation(animation)
    }
}
