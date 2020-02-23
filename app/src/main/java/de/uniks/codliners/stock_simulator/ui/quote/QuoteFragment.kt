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
import de.uniks.codliners.stock_simulator.databinding.DialogTransactionBinding
import de.uniks.codliners.stock_simulator.databinding.FragmentQuoteBinding
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.initLineChart
import de.uniks.codliners.stock_simulator.ui.BaseFragment
import de.uniks.codliners.stock_simulator.updateLineChart
import timber.log.Timber

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
                viewModel.onBuyActionStarted()
                showTransactionDialog(R.string.dialog_title_confirm_buy_transaction) {
                    viewModel.buy()
                }
                viewModel.onBuyActionCompleted()
            }
        })

        viewModel.sellAction.observe(viewLifecycleOwner, Observer { status: Boolean? ->
            status?.let {
                viewModel.onSellActionStarted()
                showTransactionDialog(R.string.dialog_title_confirm_sell_transaction) {
                    viewModel.sell()
                }
                viewModel.onSellActionCompleted()
            }
        })

        viewModel.amount.observe(viewLifecycleOwner, Observer {
            Timber.i(it.toString())
        })

        viewModel.cashflowBuy.observe(viewLifecycleOwner, Observer {
            Timber.i(it.toString())
        })

        viewModel.cashflowSell.observe(viewLifecycleOwner, Observer {
            Timber.i(it.toString())
        })

        viewModel.stockbrotQuoteAction.observe(this, Observer { stockbrotQuote: StockbrotQuote? ->
            stockbrotQuote?.let {
                viewModel.autoBuyAmount.value = stockbrotQuote.buyLimit.toString()
                viewModel.thresholdBuy.value = stockbrotQuote.maximumBuyPrice.toString()
                viewModel.thresholdSell.value = stockbrotQuote.minimumSellPrice.toString()
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

    private fun showTransactionDialog(message: Int, onConfirmation: () -> Unit) {
        val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_transaction, null, false)
        val binding = DialogTransactionBinding.bind(view)
        binding.viewModel = viewModel

        AlertDialog.Builder(context)
            .setMessage(message)
            .setView(view)
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
