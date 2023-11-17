package crossgame.android.ui.adapters.games

import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewAnimator
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import crossgame.android.application.databinding.ActivityLoadingBinding
import crossgame.android.application.databinding.CardTitleGameMediumBinding
import crossgame.android.domain.models.games.GameResponse

class GamesAdapter(
    private val context: Context,
    private var gameList: List<GameResponse>,
    private val onItemClick: (String, Long) -> Unit
) : RecyclerView.Adapter<GamesAdapter.GamesViewHolder>() {


    class GamesViewHolder(private val binding: CardTitleGameMediumBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val gameImageView = binding.imageGame
        val gameNameTextView = binding.textGame
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamesViewHolder {
        val binding = CardTitleGameMediumBinding.inflate(LayoutInflater.from(context), parent, false)
        return GamesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    override fun onBindViewHolder(holder: GamesViewHolder, position: Int) {
        val currentGame = gameList[position]

        animateItemHeight(holder.itemView)

        Glide.with(context)
            .load(currentGame.imageGame.link)
            .into(holder.gameImageView)
        holder.gameNameTextView.text = currentGame.name
        holder.itemView.setOnClickListener { onItemClick(currentGame.name, currentGame.id) }
    }

    fun updateData(newGameList: List<GameResponse>) {
        gameList = newGameList
        notifyDataSetChanged()
    }
    private fun animateItemHeight(view: View) {
        val animator = ValueAnimator.ofInt(0, 300)
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = animatedValue
            view.layoutParams = layoutParams
        }
        animator.duration = 500 // Defina a duração da animação
        animator.start()
    }
}