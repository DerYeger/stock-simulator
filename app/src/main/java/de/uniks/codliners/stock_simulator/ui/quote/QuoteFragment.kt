package de.uniks.codliners.stock_simulator.ui.quote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.Entry
import de.uniks.codliners.stock_simulator.databinding.FragmentQuoteBinding
import de.uniks.codliners.stock_simulator.initLineChart
import de.uniks.codliners.stock_simulator.updateLineChart
import java.text.SimpleDateFormat

class QuoteFragment : Fragment() {

    private val viewModel: QuoteViewModel by viewModels {
        val symbol = QuoteFragmentArgs.fromBundle(arguments!!).symbol
        QuoteViewModel.Factory(activity!!.application, symbol)
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

        viewModel.errorAction.observe(viewLifecycleOwner, Observer { errorMessage: String? ->
            errorMessage?.let {
                showErrorToast(errorMessage)
                viewModel.onErrorActionCompleted()
            }
        })

        viewModel.historicalPrices.observe(viewLifecycleOwner, Observer { priceList ->
            run {
                val simpleDateFormat =
                    SimpleDateFormat("yyyy-MM-dd", resources.configuration.locale)
                val entries = priceList.map { price ->
                    val timestamp = simpleDateFormat.parse(price.date)!!.time
                    Entry(timestamp.toFloat(), price.close.toFloat())
                }
                updateLineChart(binding.quoteChart, entries, "Historical Prices")
            }
        })

        initLineChart(binding.quoteChart, context!!, resources.configuration.locale)

        return binding.root
    }

    private fun showErrorToast(errorMessage: String?) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}
