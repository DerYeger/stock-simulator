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

    private val viewModel: StockbrotViewModel by viewModels{
        StockbrotViewModel.Factory(activity!!.application)
    }

    private lateinit var binding: FragmentStockbrotBinding

    private lateinit var stockbrotWorkRequest: StockbrotWorkRequest

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStockbrotBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        stockbrotWorkRequest = StockbrotWorkRequest(context!!)  // TODO: check for null?

        viewModel.enabledAction.observe(this, Observer { enabled: Boolean? ->
            enabled?.let {
                when(enabled) {
                    true -> stockbrotWorkRequest.start()
                    false -> stockbrotWorkRequest.stop()
                }
                viewModel.onEnabledActionCompleted()
            }
        })

        viewModel.thresholdBuy.observe(this, Observer {
            println("changed threshold buy")
            stockbrotWorkRequest.thresholdBuy = try {
                it.toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
        })

        viewModel.thresholdSell.observe(this, Observer {
            println("changed threshold sell")
            stockbrotWorkRequest.thresholdSell = try {
                it.toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
        })

        return binding.root
    }
}
