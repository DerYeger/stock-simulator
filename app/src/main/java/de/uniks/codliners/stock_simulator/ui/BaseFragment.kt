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

/**
 * BaseFragment for most other fragments. Adds the main menu and manages achievements.
 *
 * @author Lucas Held
 * @author Jan Müller
 */
abstract class BaseFragment : Fragment() {

    private val viewModel: BaseViewModel by viewModels {
        BaseViewModel.Factory(requireActivity().application)
    }

    /**
     * Enables the options menu and observes achievements.
     *
     * @param savedInstanceState The saved instance state.
     *
     * @author Lucas Held
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel.achievements.observe(viewLifecycleOwner, Observer { achievements ->
            for (achievement in achievements) {
                if (achievement.reached && achievement.timestamp != null && !achievement.displayed) {
                    Toast
                        .makeText(
                            requireActivity().application,
                            getString(achievement.name),
                            Toast.LENGTH_SHORT
                        )
                        .show()
                    viewModel.markAchievementAsDisplayed(achievement)
                }
            }
        })

        viewModel.balanceChanged.observe(viewLifecycleOwner, Observer {
            Timber.v(it.toString())
        })

        viewModel.depotChanged.observe(viewLifecycleOwner, Observer {
            Timber.v(it.toString())
        })
    }

    /**
     * Inflates the main menu.
     *
     * @param menu The target menu.
     * @param inflater Used for inflating the menu.
     *
     * @author Jan Müller
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Navigates to the settings screen if the settings menu item was selected.
     *
     * @param item The selected item.
     * @return true if the selection was handled and false otherwise.
     *
     * @author Jan Müller
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings_menu_item -> findNavController().navigate(R.id.navigation_settings)
                .let { true }
            else -> false
        }
    }
}
