package de.uniks.codliners.stock_simulator.ui.stockbrot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.uniks.codliners.stock_simulator.databinding.FragmentStockbrotBinding
import de.uniks.codliners.stock_simulator.ui.BaseFragment
import de.uniks.codliners.stock_simulator.ui.OnClickListener

/**
 * Fragment for the stockbrot ui.
 *
 * @author Lucas Held
 */
class StockbrotFragment : BaseFragment() {

    private val viewModel: StockbrotViewModel by viewModels {
        StockbrotViewModel.Factory(requireActivity().application)
    }

    private lateinit var binding: FragmentStockbrotBinding

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
        binding = FragmentStockbrotBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.stockbrotRecyclerView.adapter =
            StockbrotQuoteListAdapter(OnClickListener { stockbrotQuote ->
                val action = StockbrotFragmentDirections.actionNavigationStockbrotToNavigationQuote(
                    stockbrotQuote.id,
                    stockbrotQuote.type
                )
                findNavController().navigate(action)
            })
        binding.lifecycleOwner = this
        return binding.root
    }
}
