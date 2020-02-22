package de.uniks.codliners.stock_simulator.ui.quote

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.Entry
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.databinding.FragmentQuoteBinding
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.initLineChart
import de.uniks.codliners.stock_simulator.ui.BaseFragment
import de.uniks.codliners.stock_simulator.updateLineChart

class QuoteFragment : BaseFragment() {

    private val viewModel: QuoteViewModel by viewModels {
        val args = QuoteFragmentArgs.fromBundle(arguments!!)

        QuoteViewModel.Factory(
            application = activity!!.application,
            id = args.id,
            type = args.type
        )
    }

    private lateinit var binding: FragmentQuoteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuoteBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // React to reset button clicks.
        viewModel.clickNewsStatus.observe(viewLifecycleOwner, Observer { status ->
            status?.let {

                // Reset click indicator.
                viewModel.clickNewsStatus.value = null

                val action =
                    QuoteFragmentDirections.actionNavigationQuoteToNavigationNews(viewModel.quote.value!!.symbol)
                findNavController().navigate(action)
            }
        })

        viewModel.errorAction.observe(viewLifecycleOwner, Observer { errorMessage: String? ->
            errorMessage?.let {
                showErrorToast(errorMessage)
                viewModel.onErrorActionCompleted()
            }
        })

        viewModel.buyAction.observe(viewLifecycleOwner, Observer { status: Boolean? ->
            status?.let {
                showTransactionDialog {
                    viewModel.buy()
                }
                viewModel.onBuyActionCompleted()
            }
        })

        viewModel.sellAction.observe(viewLifecycleOwner, Observer { status: Boolean? ->
            status?.let {
                showTransactionDialog {
                    viewModel.sell()
                }
                viewModel.onSellActionCompleted()
            }
        })

        viewModel.stockbrotQuoteAction.observe(this, Observer { stockbrotQuote: StockbrotQuote? ->
            stockbrotQuote?.let {
                viewModel.autoBuyAmount.value = stockbrotQuote.buyAmount.toString()
                viewModel.thresholdBuy.value = stockbrotQuote.thresholdBuy.toString()
                viewModel.thresholdSell.value = stockbrotQuote.thresholdSell.toString()
                viewModel.onThresholdBuyActionCompleted()
            }
        })

        viewModel.historicalPrices.observe(viewLifecycleOwner, Observer { priceList ->
            run {
                if (priceList.isEmpty()) return@run
                val referenceTimestamp = priceList[0].date
                val entries = priceList.map { price ->
                    Entry((price.date - referenceTimestamp).toFloat(), price.price.toFloat())
                }

                // resources.configuration.locales[0] requires API level 24
                @Suppress("DEPRECATION")
                updateLineChart(
                    binding.quoteChart,
                    entries,
                    "Historical Prices",
                    resources.configuration.locale,
                    referenceTimestamp
                )
            }
        })

        initLineChart(binding.quoteChart, context!!)

        return binding.root
    }

    private fun showTransactionDialog(onConfirmation: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(R.string.dialog_confirm_transaction)
            .setPositiveButton(R.string.yes) { dialog, id ->
                onConfirmation()
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                Toast
                    .makeText(context, "Transaction canceled", Toast.LENGTH_SHORT)
                    .show()
            }
            .create()
            .show()
    }

    private fun showErrorToast(errorMessage: String?) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}
