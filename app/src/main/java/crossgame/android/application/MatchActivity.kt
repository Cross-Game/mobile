package crossgame.android.application

import android.content.Context
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


import crossgame.android.application.databinding.ActivityMatchBinding
import crossgame.android.application.databinding.BsGameListBinding

import crossgame.android.domain.models.users.UserMatch
import crossgame.android.infra.MatchHelper

import crossgame.android.ui.adapters.match.MatchAdapter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchBinding
    private lateinit var listUsers: List<UserMatch>
    private lateinit var recyclerView: RecyclerView
    private lateinit var matchHelper: MatchHelper
    var userId = 1L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchBinding.inflate(layoutInflater)
        recyclerView = binding.listPlayers
        setContentView(binding.root)

        matchHelper = MatchHelper()
        changeSearchViewColor()

        CoroutineScope(Dispatchers.IO).launch {
            listUsers = matchHelper.getUsersForMatch(userId)
            withContext(Dispatchers.Main) {
                setupRecyclerView()
                onListenServices()
            }
        }
        buildingServices()
    }

    private fun buildingServices() {
    }

     private suspend fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(this);
        val sheetBinding : BsGameListBinding = BsGameListBinding.inflate(layoutInflater, null, false);
        val chipGroup = sheetBinding.listOfGames;

        var jogos = matchHelper.getGames();
        jogos.map { jogo ->
            val newChip = Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)

            val isSelected = gamesForFilter.contains(jogo.name)
            newChip.isSelected = isSelected
            newChip.setChipBackgroundColorResource(
                if (newChip.isSelected) R.color.md_theme_dark_onPrimary
                else R.color.md_theme_dark_inverseOnSurface
            )
            newChip.isCloseIconVisible = isSelected
            newChip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(R.color.white)))
            newChip.text = jogo.name
            newChip.tag = jogo.name
            newChip.setOnClickListener { enableOrRemoveChipFilter(newChip) }
            newChip.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.white)))
            chipGroup.addView(newChip)
        }

        dialog.setContentView(sheetBinding.root)
        dialog.show()

        dialog.setOnDismissListener {
            filterUserByNameAndOrderUsersByGame()
        }
    }


     suspend fun onListenServices() {
         binding.buttonSort.setOnClickListener {
             CoroutineScope(Dispatchers.Main).launch {
                 showBottomSheetDialog()
             }
         }
         binding.buttonSearch.setOnClickListener { toggleSearchField(); }
     }

        var nameForFilter : String? = null;
        var gamesForFilter = mutableListOf<String>()


    fun filterUserByName(name: String): List<UserMatch> {
        val filteredUsers = listUsers.filter { it.baseUser.username.contains(name, ignoreCase = true) }
        Log.i("INFO", "Filtered Users by Name: $filteredUsers")
        return filteredUsers
    }

    fun filterUserByGames(selectedGames: List<String>, includeOnlyAllGames: Boolean = false): List<UserMatch> {
        if (selectedGames.isEmpty()) {
            // Se a lista de jogos estiver vazia, retornar a lista completa de usuários
            return listUsers
        }

        val filteredUsers = listUsers.filter { userMatch ->
            val hasAllGames = userMatch.games.map { it.name }.containsAll(selectedGames)
            if (includeOnlyAllGames) {
                hasAllGames
            } else {
                userMatch.games.any { selectedGames.contains(it.name) }
            }
        }
        Log.i("INFO", "Filtered Users by Game: $filteredUsers")
        return filteredUsers
    }


    fun filterUserByNameAndOrderUsersByGame() {
        val filteredUsersByName = nameForFilter?.let { filterUserByName(it) } ?: listUsers
        val filteredUsersByGame = if (gamesForFilter.isNotEmpty()) {
            filterUserByGames(gamesForFilter)
        } else {
            listUsers
        }

        val finalFilteredUsers = when {
            nameForFilter != null && gamesForFilter.isNotEmpty() -> {
                filteredUsersByName.intersect(filteredUsersByGame).toList()
            }
            nameForFilter != null -> {
                filteredUsersByName
            }
            gamesForFilter.isNotEmpty() -> {
                filteredUsersByGame
            }
            else -> {
                listUsers
            }
        }

        Log.i("INFO", "Final Filtered Users: $finalFilteredUsers")

        updateRecyclerView(finalFilteredUsers.sortedByDescending { calculateUserScore(it, gamesForFilter) })
    }


    fun enableOrRemoveChipFilter(view: View) {
        if (view is Chip) {
            val chip = view as Chip
            val gameName = chip.tag as String
            val chipGroup = binding.listForFilter

            if (chip.isSelected) {
                chip.isSelected = false
                chip.isCloseIconVisible = false
                chip.setChipBackgroundColorResource(R.color.md_theme_dark_inverseOnSurface)
                gamesForFilter.remove(gameName)

                val index = findChipIndexInChipGroup(chipGroup, gameName)
                if (index >= 0) {
                    chipGroup.removeViewAt(index)
                }
            } else {
                chip.isSelected = true
                chip.setChipBackgroundColorResource(R.color.md_theme_dark_onPrimary)
                chip.isCloseIconVisible = true
                chip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(R.color.white)))
                Log.i("INFO", "Adicionando jogo " + gameName.toString() + " para filtragem")
                gamesForFilter.add(gameName)

                createChipInFilterView(chip.text.toString())
            }
            filterUserByNameAndOrderUsersByGame()

            val linearLayout = findViewById<LinearLayout>(R.id.layout_filter_tags)

            if (!gamesForFilter.isEmpty() || nameForFilter != null) {
                linearLayout.visibility = View.VISIBLE
            } else {
                linearLayout.visibility = View.GONE
            }
        }
    }

    private fun findChipIndexInChipGroup(chipGroup: ChipGroup, gameName: String): Int {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.tag == gameName) {
                return i
            }
        }
        return -1
    }

    fun createChipInFilterView(name: String, isSelected: Boolean = true) {
        val chipGroup = binding.listForFilter

        val newChip = Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)
        newChip.isSelected = isSelected
        newChip.setChipBackgroundColorResource(
            if (isSelected) R.color.md_theme_dark_onPrimary
            else R.color.md_theme_dark_inverseOnSurface
        )

        newChip.chipStrokeColor = ColorStateList.valueOf(resources.getColor(R.color.md_theme_dark_inverseOnSurface))
        newChip.setChipStrokeColorResource(R.color.md_theme_dark_inverseOnSurface)
        newChip.isCloseIconVisible = true
        newChip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(R.color.white)))
        newChip.text = name
        newChip.tag = name
        newChip.setOnClickListener { enableOrRemoveChipFilter(newChip) }
        newChip.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.white)))
        chipGroup.addView(newChip)
    }

    fun calculateUserScore(user: UserMatch, selectedChips: List<String>): Int {
        val commonGames = user.games.filter { it.name in selectedChips }
        val score = commonGames.size
        Log.i("INFO", "User: ${user.baseUser.username}, Score: $score, Common Games: $commonGames")
        return score
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MatchAdapter(listUsers, this)
    }

    private fun updateRecyclerView(users: List<UserMatch>) {
        recyclerView.adapter = MatchAdapter(users, this)
    }


    private fun toggleSearchField() {
        Log.i("SearchView", "Chamei toggleSearchField")
        val searchView = binding.searchView

        if (searchView.visibility != View.VISIBLE) {
            Log.i("SearchView", "SearchView está iconificado. Expandindo...")
            searchView.visibility = View.VISIBLE
            searchView.translationX = -searchView.width.toFloat()
            searchView.requestFocus()
            showSoftKeyboard()

            searchView.animate()
                .translationX(0f)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(300)
                .withEndAction {
                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            Log.i("SearchView", "onQueryTextSubmit: $query")
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.i("SearchView", "onQueryTextChange: $newText")
                            nameForFilter = newText.orEmpty()
                            filterUserByNameAndOrderUsersByGame()
                            return false
                        }
                    })
                }
                .start()
        } else {
            Log.i("SearchView", "SearchView não está iconificado. Contraindo...")
            searchView.animate()
                .translationX(-searchView.width.toFloat())
                .setInterpolator(AccelerateInterpolator())
                .setDuration(300)
                .withEndAction {
                    searchView.visibility = View.GONE
                    hideSoftKeyboard()
                }
                .start()
        }
    }

    private fun handleSearchAction(query: String, chipGroup: ChipGroup) {
        Log.i("SearchView", "handleSearchAction: $query")
        if (query.isNotBlank()) {
            nameForFilter = query

            val existingChip = findChipByTag(chipGroup, query)
            if (existingChip != null) {
                existingChip.text = query
            } else {
                val newChip = createChip(query)
                chipGroup.addView(newChip)
            }

            filterUserByNameAndOrderUsersByGame()
        }
    }

    private fun showSoftKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun createChip(text: String): Chip {
        val newChip =
            Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated)
        newChip.isSelected = true
        newChip.setChipBackgroundColorResource(
            if (newChip.isSelected) R.color.md_theme_dark_onPrimary
            else R.color.md_theme_dark_inverseOnSurface
        )

        newChip.chipStrokeColor =
            ColorStateList.valueOf(resources.getColor(R.color.md_theme_dark_inverseOnSurface))
        newChip.setChipStrokeColorResource(R.color.md_theme_dark_inverseOnSurface)
        newChip.isCloseIconVisible = true
        newChip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(R.color.white)))
        newChip.text = text
        newChip.tag = text
        newChip.setOnClickListener { enableOrRemoveChipFilter(newChip) }
        newChip.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.white)))

        return newChip
    }

    private fun findChipByTag(chipGroup: ChipGroup, tag: String): Chip? {
        for (index in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(index) as? Chip
            if (chip?.tag == tag) {
                return chip
            }
        }
        return null
    }

    private fun changeSearchViewColor(){
        val searchView = findViewById<SearchView>(R.id.searchView)
        val searchEditText: EditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(resources.getColor(R.color.white))
        searchEditText.setHintTextColor(resources.getColor(R.color.white))
    }
}