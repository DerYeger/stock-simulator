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

class StockbrotFragment : BaseFragment() {

    private val viewModel: StockbrotViewModel by viewModels{
        StockbrotViewModel.Factory(activity!!.application)
    }

    private lateinit var binding: FragmentStockbrotBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStockbrotBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.stockbrotRecyclerView.adapter = StockbrotQuoteRecyclerViewAdapter(OnClickListener { stockbrotQuote ->
            val action = StockbrotFragmentDirections.actionNavigationStockbrotToNavigationQuote(stockbrotQuote.id, stockbrotQuote.type)
            findNavController().navigate(action)
        })
        binding.lifecycleOwner = this
        return binding.root
    }
}
