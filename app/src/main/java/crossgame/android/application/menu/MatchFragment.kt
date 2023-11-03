package crossgame.android.application.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import crossgame.android.application.databinding.FragmentMatchBinding


class MatchFragment : Fragment() {


    private lateinit var binding: FragmentMatchBinding

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
        return binding.root
    }
}