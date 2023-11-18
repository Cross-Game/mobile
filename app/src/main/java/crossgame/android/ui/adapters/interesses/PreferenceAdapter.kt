package crossgame.android.ui.adapters.interesses

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import crossgame.android.application.databinding.CardInteresseBinding
import crossgame.android.domain.models.games.GameResponse
import crossgame.android.domain.models.preferences.Preference

class PreferenceAdapter(
    private val context: Context,
    private var interesseList: List<Preference>
) : RecyclerView.Adapter<PreferenceAdapter.InteresseViewHolder>() {


    class InteresseViewHolder(private val binding: CardInteresseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val preferenceNameTextView = binding.inputMessage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InteresseViewHolder {
        val binding = CardInteresseBinding.inflate(LayoutInflater.from(context), parent, false)
        return PreferenceAdapter.InteresseViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return interesseList.size
    }

    override fun onBindViewHolder(holder: InteresseViewHolder, position: Int) {
        val currentPreference = interesseList[position]

        holder.preferenceNameTextView.text = currentPreference.preferences
    }

    fun updateData(newGameList: List<Preference>) {
        interesseList = newGameList
        notifyDataSetChanged()
    }
}