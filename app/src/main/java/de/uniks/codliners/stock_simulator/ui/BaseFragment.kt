package de.uniks.codliners.stock_simulator.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.uniks.codliners.stock_simulator.R
import timber.log.Timber

abstract class BaseFragment : Fragment() {

    private val viewModel: BaseViewModel by viewModels {
        BaseViewModel.Factory(requireActivity().application)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel.achievements.observe(viewLifecycleOwner, Observer { achievements ->
            for (achievement in achievements) {
                if (achievement.reached && achievement.timestamp != null && !achievement.displayed) {
                    println("achievement $achievement reached - " + getString(achievement.name))
                    Toast
                        .makeText(requireActivity().application, getString(achievement.name), Toast.LENGTH_SHORT)
                        .show()
                    viewModel.markAchievementAsDisplayed(achievement)
                }
            }
        })

        viewModel.balanceChanged.observe(viewLifecycleOwner, Observer {
            Timber.i(it.toString())
        })

        viewModel.depotChanged.observe(viewLifecycleOwner, Observer {
            Timber.i(it.toString())
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings_menu_item -> findNavController().navigate(R.id.navigation_settings).let { true }
            else -> false
        }
    }
}
