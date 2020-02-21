package de.uniks.codliners.stock_simulator.ui.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import de.uniks.codliners.stock_simulator.databinding.FragmentAchievementsBinding
import de.uniks.codliners.stock_simulator.domain.Achievement
import de.uniks.codliners.stock_simulator.ui.BaseFragment


class AchievementsFragment : BaseFragment() {

    private val viewModel: AchievementsViewModel by viewModels {
        AchievementsViewModel.Factory(activity!!.application)
    }

    private lateinit var binding: FragmentAchievementsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAchievementsBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.achievementsRecyclerView.adapter = AchievementsAdapter()
        binding.lifecycleOwner = this

        viewModel.refresh()

        return binding.root
    }

    fun showAchivement(achievement: Achievement) {
        Snackbar
            .make(this.view!!, achievement.name, Snackbar.LENGTH_SHORT)
            .show()
    }
}
