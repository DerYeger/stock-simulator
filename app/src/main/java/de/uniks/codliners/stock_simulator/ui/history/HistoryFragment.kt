package de.uniks.codliners.stock_simulator.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.uniks.codliners.stock_simulator.databinding.FragmentHistoryBinding
import de.uniks.codliners.stock_simulator.ui.BaseFragment
import de.uniks.codliners.stock_simulator.ui.OnClickListener

/**
 * [Fragment](https://developer.android.com/jetpack/androidx/releases/fragment) for the history ui.
 *
 * @author Jan Müller
 */
class HistoryFragment : BaseFragment() {

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(requireActivity().application)
    }

    private lateinit var binding: FragmentHistoryBinding

    /**
     * Sets up the fragment view.
     *
     * @param inflater The layout inflater.
     * @param container The view group.
     * @param savedInstanceState The saved instance state.
     * @return The set up fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater)
        binding.viewModel = viewModel

        // resources.configuration.locales[0] requires API level 24
        @Suppress("DEPRECATION")
        binding.historyRecyclerView.adapter = TransactionListAdapter(
            onClickListener = OnClickListener { transaction ->
                val action = HistoryFragmentDirections.actionNavigationHistoryToShareFragment(
                    transaction.id,
                    transaction.type
                )
                findNavController().navigate(action)
            },
            locale = resources.configuration.locale
        )
        binding.lifecycleOwner = this
        return binding.root
    }
}