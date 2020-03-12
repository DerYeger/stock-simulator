package de.uniks.codliners.stock_simulator.ui.quote

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.Entry
import com.google.android.material.snackbar.Snackbar
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.databinding.DialogTransactionBinding
import de.uniks.codliners.stock_simulator.databinding.FragmentQuoteBinding
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.extractErrorMessageResource
import de.uniks.codliners.stock_simulator.initLineChart
import de.uniks.codliners.stock_simulator.ui.BaseFragment
import de.uniks.codliners.stock_simulator.updateLineChart
import java.net.UnknownHostException

/**
 * [Fragment](https://developer.android.com/jetpack/androidx/releases/fragment) for viewing, buying and selling assets.
 *
 * @author TODO
 * @author Jan Müller
 * @author Jonas Thelemann
 */
class QuoteFragment : BaseFragment() {

    private val viewModel: QuoteViewModel by viewModels {
        val args = QuoteFragmentArgs.fromBundle(requireArguments())

        QuoteViewModel.Factory(
            application = requireActivity().application,
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

        viewModel.errorAction.observe(viewLifecycleOwner, Observer { exception: Exception? ->
            exception?.let {
                showErrorAndNavigateUp(exception)
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

        viewModel.sellAllAction.observe(viewLifecycleOwner, Observer { status: Boolean? ->
            status?.let {
                viewModel.onSellAllActionStarted()
                showTransactionDialog(R.string.dialog_title_confirm_sell_transaction) {
                    viewModel.sellAll()
                }
                viewModel.onSellAllActionCompleted()
            }
        })

        viewModel.stockbrotQuoteAction.observe(
            viewLifecycleOwner,
            Observer { stockbrotQuote: StockbrotQuote? ->
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

        initLineChart(binding.quoteChart, requireContext())

        return binding.root
    }

    private fun showTransactionDialog(message: Int, onConfirmation: () -> Unit) {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.dialog_transaction, null, false)
        val binding = DialogTransactionBinding.bind(view)
        binding.viewModel = viewModel

        AlertDialog.Builder(context)
            .setMessage(message)
            .setView(view)
            .setPositiveButton(R.string.yes) { _, _ ->
                onConfirmation()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                Snackbar
                    .make(requireView(), "Transaction canceled", Snackbar.LENGTH_SHORT)
                    .show()
            }
            .create()
            .show()
    }

    private fun showErrorAndNavigateUp(exception: Exception) {
        Snackbar.make(
            requireView(),
            exception.extractErrorMessageResource<UnknownHostException>(R.string.no_connection) {
                R.string.unable_to_fetch_quote_information
            },
            Snackbar.LENGTH_SHORT
        ).show()
        findNavController().navigateUp()
    }
}
