package crossgame.android.application.menu

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import crossgame.android.application.databinding.BsGameListBinding
import crossgame.android.application.databinding.FragmentMatchBinding
import crossgame.android.domain.models.users.UserMatch
import crossgame.android.infra.MatchHelper
import crossgame.android.ui.adapters.match.MatchAdapter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MatchFragment : Fragment() {


    private lateinit var binding: FragmentMatchBinding
    private lateinit var rootView: View
    private lateinit var listUsers: List<UserMatch>
    private lateinit var recyclerView: RecyclerView
    private lateinit var matchHelper: MatchHelper

    private var userId: Long = 1L
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMatchBinding.inflate(
            inflater,
            container,
            false
        )

        binding.layoutAnimation.visibility = View.VISIBLE
        // Initial visibility setup
        binding.apply {

            userIconCenter.visibility = View.VISIBLE
            circleMuitoPequeno.visibility = View.VISIBLE
            circlePequeno.visibility = View.VISIBLE
            userIconTopEnd.visibility = View.VISIBLE
            userIconEndStart.visibility = View.VISIBLE
            circleMedio.visibility = View.VISIBLE
            userIconTopStart.visibility = View.VISIBLE
            userIconEndEnd.visibility = View.VISIBLE
            circleGrande.visibility = View.VISIBLE
        }

        // Step 1: Fade in userIconCenter
        fadeInView(binding.userIconCenter)

        // Set the initial radius for the ripple effect
        val initialRadius = 0

        // Define a slower duration for each step
        val duration = 2000L

        // Step 2: Create a ripple effect and fade in circle_muitoPequeno
        createRippleEffect(binding.circleMuitoPequeno, initialRadius, duration) {
            fadeInView(it)
        }

        // Step 3: Create a ripple effect and fade in circle_pequeno
        createRippleEffect(binding.circlePequeno, initialRadius, 3000L) {
            fadeInView(it)
        }

        // Step 4: Fade in userIconTopEnd and userIconEndStart, then add border
        fadeInViewWithBorder(binding.userIconTopEnd)
        fadeInViewWithBorder(binding.userIconEndStart)

        // Step 5: Create a ripple effect and fade in circle_medio
        createRippleEffect(binding.circleMedio, initialRadius, 5000L) {
            fadeInView(it)
        }

        // Step 6: Fade in userIconTopStart and userIconEndEnd, then add border
        fadeInViewWithBorder(binding.userIconTopStart)
        fadeInViewWithBorder(binding.userIconEndEnd)

        // Step 7: Create a ripple effect and fade in circle_grande
        createRippleEffect(binding.circleGrande, initialRadius, 10000L) {
            fadeInView(it)
        }

        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("id", 1).toLong()
        rootView = binding.root
        recyclerView = binding.listPlayers
        matchHelper = MatchHelper(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            listUsers = matchHelper.getUsersForMatch(userId)
            withContext(Dispatchers.Main) {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = MatchAdapter(requireContext())
                (recyclerView.adapter as MatchAdapter).updateData(listUsers)
                onListenServices()

                binding.layoutAnimation.visibility = View.GONE
                binding.layoutContent.visibility = View.VISIBLE
            }
        }
        buildingServices()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeSearchViewColor()
    }

    private fun buildingServices() {
    }

    private suspend fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding : BsGameListBinding = BsGameListBinding.inflate(layoutInflater, null, false);
        val chipGroup = sheetBinding.listOfGames;

        var jogos = matchHelper.getGames();
        jogos.map { jogo ->
            val newChip = Chip(requireContext(), null, R.style.Widget_Material3_Chip_Assist_Elevated)

            val isSelected = gamesForFilter.contains(jogo.name)
            newChip.isSelected = isSelected
            newChip.setChipBackgroundColorResource(
                if (newChip.isSelected) crossgame.android.application.R.color.md_theme_dark_onPrimary
                else crossgame.android.application.R.color.md_theme_dark_inverseOnSurface
            )
            newChip.isCloseIconVisible = isSelected
            newChip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(crossgame.android.application.R.color.white)))
            newChip.text = jogo.name
            newChip.tag = jogo.name
            newChip.setOnClickListener { enableOrRemoveChipFilter(newChip) }
            newChip.setTextColor(ColorStateList.valueOf(resources.getColor(crossgame.android.application.R.color.white)))
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
                chip.setChipBackgroundColorResource(crossgame.android.application.R.color.md_theme_dark_inverseOnSurface)
                gamesForFilter.remove(gameName)

                val index = findChipIndexInChipGroup(chipGroup, gameName)
                if (index >= 0) {
                    chipGroup.removeViewAt(index)
                }
            } else {
                chip.isSelected = true
                chip.setChipBackgroundColorResource(crossgame.android.application.R.color.md_theme_dark_onPrimary)
                chip.isCloseIconVisible = true
                chip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(crossgame.android.application.R.color.white)))
                Log.i("INFO", "Adicionando jogo " + gameName.toString() + " para filtragem")
                gamesForFilter.add(gameName)

                createChipInFilterView(chip.text.toString())
            }
            filterUserByNameAndOrderUsersByGame()

            val linearLayout = requireView().findViewById<LinearLayout>(crossgame.android.application.R.id.layout_filter_tags)


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

        val newChip = Chip(requireContext(), null, R.style.Widget_Material3_Chip_Assist_Elevated)
        newChip.isSelected = isSelected
        newChip.setChipBackgroundColorResource(
            if (isSelected) crossgame.android.application.R.color.md_theme_dark_onPrimary
            else crossgame.android.application.R.color.md_theme_dark_inverseOnSurface
        )

        newChip.chipStrokeColor = ColorStateList.valueOf(resources.getColor(crossgame.android.application.R.color.md_theme_dark_inverseOnSurface))
        newChip.setChipStrokeColorResource(crossgame.android.application.R.color.md_theme_dark_inverseOnSurface)
        newChip.isCloseIconVisible = true
        newChip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(crossgame.android.application.R.color.white)))
        newChip.text = name
        newChip.tag = name
        newChip.setOnClickListener { enableOrRemoveChipFilter(newChip) }
        newChip.setTextColor(ColorStateList.valueOf(resources.getColor(crossgame.android.application.R.color.white)))
        chipGroup.addView(newChip)
    }

    fun calculateUserScore(user: UserMatch, selectedChips: List<String>): Int {
        val commonGames = user.games.filter { it.name in selectedChips }
        val score = commonGames.size
        Log.i("INFO", "User: ${user.baseUser.username}, Score: $score, Common Games: $commonGames")
        return score
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MatchAdapter(requireContext())
    }

    private fun updateRecyclerView(users: List<UserMatch>) {
        (recyclerView.adapter as MatchAdapter).updateData(users)
        saveUsersToSharedPreferences(users)


        when(users.size){
            0 -> {
                binding.listPlayers.visibility = View.GONE
                binding.emptyList.visibility = View.VISIBLE
            }
            else -> {
                binding.listPlayers.visibility = View.VISIBLE
                binding.emptyList.visibility = View.GONE
            }
        }


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
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun createChip(text: String): Chip {
        val newChip =
            Chip(requireContext(), null, R.style.Widget_Material3_Chip_Assist_Elevated)
        newChip.isSelected = true
        newChip.setChipBackgroundColorResource(
            if (newChip.isSelected) crossgame.android.application.R.color.md_theme_dark_onPrimary
            else crossgame.android.application.R.color.md_theme_dark_inverseOnSurface
        )

        newChip.chipStrokeColor =
            ColorStateList.valueOf(resources.getColor(crossgame.android.application.R.color.md_theme_dark_inverseOnSurface))
        newChip.setChipStrokeColorResource(crossgame.android.application.R.color.md_theme_dark_inverseOnSurface)
        newChip.isCloseIconVisible = true
        newChip.setCloseIconTint(ColorStateList.valueOf(resources.getColor(crossgame.android.application.R.color.white)))
        newChip.text = text
        newChip.tag = text
        newChip.setOnClickListener { enableOrRemoveChipFilter(newChip) }
        newChip.setTextColor(ColorStateList.valueOf(resources.getColor(crossgame.android.application.R.color.white)))

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
        val searchView = requireView().findViewById<SearchView>(crossgame.android.application.R.id.searchView)
        val searchEditText: EditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(resources.getColor(crossgame.android.application.R.color.white))
        searchEditText.setHintTextColor(resources.getColor(crossgame.android.application.R.color.white))
    }

    private fun saveUsersToSharedPreferences(users: List<UserMatch>) {
        val jsonUsers = convertListToJson(users)

        val sharedPreferences =
            requireActivity().getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putString("MATCH_USERS", jsonUsers)
        editor.apply()
    }

    fun convertListToJson(users: List<UserMatch>): String {
        val gson = Gson()
        return gson.toJson(users)
    }

    private fun exibirSnackbar(mensagem: String, isSucess : Boolean = true) {
        val snackbar = Snackbar.make(rootView, mensagem, Snackbar.LENGTH_SHORT)

        if (isSucess) {
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), crossgame.android.application.R.color.sucess))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), crossgame.android.application.R.color.white))
        }
        else {
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), crossgame.android.application.R.color.error))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), crossgame.android.application.R.color.white))
        }

        snackbar.show()
    }

    private fun fadeInView(view: View) {
        ViewCompat.animate(view)
            .alpha(1f)
            .setDuration(2000)
            .start()
    }

    private fun fadeInViewWithBorder(circleImageView: CircleImageView) {
        fadeInView(circleImageView)
        circleImageView.borderColor = ContextCompat.getColor(requireContext(), crossgame.android.application.R.color.seed)
        circleImageView.borderWidth = 2.dpToPx()
    }

    private fun createRippleEffect(circleImageView: CircleImageView, initialRadius: Int, duration: Long, onAnimationEnd: (CircleImageView) -> Unit) {
        circleImageView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                circleImageView.viewTreeObserver.removeOnPreDrawListener(this)
                val endRadius = circleImageView.width.coerceAtLeast(circleImageView.height) / 2
                val anim = ValueAnimator.ofInt(initialRadius, endRadius)
                anim.addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Int
                    circleImageView.layoutParams.width = value * 2
                    circleImageView.layoutParams.height = value * 2
                    circleImageView.requestLayout()
                }
                anim.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        onAnimationEnd(circleImageView)
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })
                anim.duration = duration
                anim.start()
                return true
            }
        })
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}