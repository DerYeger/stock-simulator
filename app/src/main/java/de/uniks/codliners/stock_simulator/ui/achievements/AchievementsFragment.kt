package de.uniks.codliners.stock_simulator.ui.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import de.uniks.codliners.stock_simulator.databinding.FragmentAchievementsBinding
import de.uniks.codliners.stock_simulator.ui.BaseFragment


/**
 * [Fragment](https://developer.android.com/jetpack/androidx/releases/fragment) for the achievement ui.
 *
 * @author Lucas Held
 */
class AchievementsFragment : BaseFragment() {

    private val viewModel: AchievementsViewModel by viewModels {
        AchievementsViewModel.Factory(requireActivity().application)
    }

    private lateinit var binding: FragmentAchievementsBinding

    /**
     * Sets up the fragment view.
     *
     * The layout inflater.
     * @param container The view group.
     * @param savedInstanceState The saved instance state.
     * @return The set up fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAchievementsBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.achievementsRecyclerView.adapter = AchievementsAdapter()
        binding.lifecycleOwner = this
        return binding.root
    }
}
