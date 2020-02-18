package de.uniks.codliners.stock_simulator.ui.stockbrot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import de.uniks.codliners.stock_simulator.background.StockbrotWorkRequest
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

        viewModel.enabledAction.observe(this, Observer { enabled: Boolean? ->
            val stockbrotWorkRequest = StockbrotWorkRequest(context!!)  // TODO: check for null?
            enabled?.let {
                when(enabled) {
                    true -> stockbrotWorkRequest.start()
                    false -> stockbrotWorkRequest.stop()
                }
                viewModel.onEnabledActionCompleted()
            }
        })

        return binding.root
    }
}
