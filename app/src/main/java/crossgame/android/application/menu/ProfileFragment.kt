package crossgame.android.application.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import crossgame.android.application.AddGamesActivity
import crossgame.android.application.AddInterestsActivity
import crossgame.android.application.FeedbacksActivity
import crossgame.android.application.PlatformsActivity
//import crossgame.android.application.databinding.ActivityBsEditProfileBinding
import crossgame.android.application.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(
            inflater,
            container,
            false
        )
        binding.btnSettingProfile.setOnClickListener { showBottomSheet() }
        return binding.root
    }

    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(binding.root.context)

//        val sheetBinding: ActivityBsEditProfileBinding =
//            ActivityBsEditProfileBinding.inflate(layoutInflater, null, false)

//        dialog.setContentView(sheetBinding.root)
//        dialog.show()
//        sheetBinding.textVerFeedbacks.setOnClickListener { navigateToFeedbacks() }
//        sheetBinding.textEditarFoto.setOnClickListener { navigateToEditPhoto() }
//        sheetBinding.textEditarInteresse.setOnClickListener { navigateToEditInterests() }
//        sheetBinding.textEditarJogo.setOnClickListener { navigateToEditGame() }
//        sheetBinding.textEditarPlataforma.setOnClickListener { navigateToEditPlatforms() }
    }

    private fun navigateToFeedbacks() {
        startActivity(Intent(binding.root.context, FeedbacksActivity::class.java))
    }
    private fun navigateToEditPhoto() {
        startActivity(Intent(binding.root.context, FeedbacksActivity::class.java))
    }
    private fun navigateToEditInterests() {
        startActivity(Intent(binding.root.context, AddInterestsActivity::class.java))
    }
    private fun navigateToEditGame() {
        startActivity(Intent(binding.root.context, AddGamesActivity::class.java))
    }
    private fun navigateToEditPlatforms() {
        startActivity(Intent(binding.root.context, PlatformsActivity::class.java))
    }
}