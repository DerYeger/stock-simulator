package de.uniks.codliners.stock_simulator.ui.quote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.Entry
import de.uniks.codliners.stock_simulator.databinding.FragmentQuoteBinding
import de.uniks.codliners.stock_simulator.initLineChart
import de.uniks.codliners.stock_simulator.ui.BaseFragment
import de.uniks.codliners.stock_simulator.ui.news.NewsAdapter
import de.uniks.codliners.stock_simulator.updateLineChart
import java.text.SimpleDateFormat

class QuoteFragment : BaseFragment() {

    private val viewModel: QuoteViewModel by viewModels {
        val args = QuoteFragmentArgs.fromBundle(arguments!!)
        val symbol = args.symbol
        val type = args.type

        QuoteViewModel.Factory(
            application = activity!!.application,
            symbol = symbol,
            type = type
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

                val action = QuoteFragmentDirections.actionNavigationQuoteToNavigationNews(viewModel.quote.value!!.symbol)
                findNavController().navigate(action)
            }
        })

        viewModel.errorAction.observe(viewLifecycleOwner, Observer { errorMessage: String? ->
            errorMessage?.let {
                showErrorToast(errorMessage)
                viewModel.onErrorActionCompleted()
            }
        })

        viewModel.historicalPrices.observe(viewLifecycleOwner, Observer { priceList ->
            run {
                if (priceList.isEmpty()) return@run

                val simpleDateFormat =
                    SimpleDateFormat("yyyy-MM-dd", resources.configuration.locale)
                val referenceTimestamp = simpleDateFormat.parse(priceList[0].date)!!.time
                val entries = priceList.map { price ->
                    val timestamp = simpleDateFormat.parse(price.date)!!.time
                    Entry((timestamp - referenceTimestamp).toFloat(), price.close.toFloat())
                }
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

    private fun showErrorToast(errorMessage: String?) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}
