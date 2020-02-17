package de.uniks.codliners.stock_simulator.ui.stockbrot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.uniks.codliners.stock_simulator.databinding.FragmentStockbrotBinding

class StockbrotFragment : Fragment() {

    private val viewModel: StockbrotViewModel by viewModels()

    private lateinit var binding: FragmentStockbrotBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStockbrotBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
}
