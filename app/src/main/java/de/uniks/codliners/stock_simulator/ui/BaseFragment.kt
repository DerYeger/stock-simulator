package de.uniks.codliners.stock_simulator.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.uniks.codliners.stock_simulator.R
import timber.log.Timber

abstract class BaseFragment : Fragment() {

    private val viewModel: BaseViewModel by viewModels {
        BaseViewModel.Factory(activity!!.application)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel.latestAchievement.observe(this, Observer { achievement ->
            println("latest achievement $achievement - " + getString(achievement.name))
            if (achievement?.reached == true && !achievement.displayed) {
                println("achievement $achievement reached")
                Snackbar
                    .make(this.view!!, getString(achievement.name), Snackbar.LENGTH_SHORT)
                    .show()
                viewModel.markAchievementAsDisplayed(achievement)
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
