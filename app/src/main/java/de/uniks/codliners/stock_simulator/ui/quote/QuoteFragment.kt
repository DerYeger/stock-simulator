package de.uniks.codliners.stock_simulator.ui.quote

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import de.uniks.codliners.stock_simulator.database.HistoricalPrice
import de.uniks.codliners.stock_simulator.databinding.FragmentQuoteBinding
import java.text.SimpleDateFormat

class QuoteFragment : Fragment() {

    private val viewModel: QuoteViewModel by viewModels {
        val symbol = QuoteFragmentArgs.fromBundle(arguments!!).symbol
        QuoteViewModel.Factory(activity!!.application, symbol)
    }

    private lateinit var binding: FragmentQuoteBinding

    private lateinit var tfLight: Typeface
    private lateinit var tfRegular: Typeface
    private lateinit var chart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tfLight = Typeface.createFromAsset(context!!.assets, "OpenSans-Light.ttf")
        tfRegular = Typeface.createFromAsset(context!!.assets, "OpenSans-Regular.ttf")

        binding = FragmentQuoteBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        chart = binding.quoteChart

        viewModel.errorAction.observe(viewLifecycleOwner, Observer { errorMessage: String? ->
            errorMessage?.let {
                showErrorToast(errorMessage)
                viewModel.onErrorActionCompleted()
            }
        })

        viewModel.historicalPrices.observe(viewLifecycleOwner, Observer { priceList ->
            priceList?.let {
                drawGraph(priceList)
            }
        })

        if (viewModel.historicalPrices.value != null) {
            drawGraph(viewModel.historicalPrices.value!!)
        }


        return binding.root
    }

    private fun showErrorToast(errorMessage: String?) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun drawGraph(prices: List<HistoricalPrice>) {
        chart.data = generateLineData(prices)
        styleGraph(chart)

        chart.invalidate()
    }

    private fun styleGraph(chart: LineChart) {
        val xAxis = chart.xAxis
        val yAxis = chart.axisLeft

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.setDrawGridBackground(false)
        chart.setTouchEnabled(false)

        xAxis.axisLineColor = Color.TRANSPARENT
        xAxis.gridColor = Color.DKGRAY
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.typeface = tfLight

        yAxis.axisLineColor = Color.TRANSPARENT
        yAxis.gridColor = Color.DKGRAY
        yAxis.setLabelCount(2, false)
        yAxis.textColor = Color.WHITE
        yAxis.typeface = tfLight
    }

    private fun generateLineData(prices: List<HistoricalPrice>): LineData {

        val lineDataValues = prices.map { price ->
            val date = SimpleDateFormat("yyyy-MM-dd").parse(price.date)
            Entry(date.time.toFloat(), price.close.toFloat())
        }

        val lds = LineDataSet(lineDataValues, "Account Balance")
        lds.color = Color.WHITE
        lds.color = ColorTemplate.VORDIPLOM_COLORS[0]
        lds.cubicIntensity = 0.2f
        lds.fillAlpha = 100
        lds.fillColor = Color.WHITE
        lds.lineWidth = 1.8f
        lds.mode = LineDataSet.Mode.CUBIC_BEZIER
        lds.setDrawCircles(false)
        lds.setDrawFilled(true)
        lds.setDrawHorizontalHighlightIndicator(false)
        val sets: ArrayList<ILineDataSet> = ArrayList()
        sets.add(lds)

        val ld = LineData(sets)
        ld.setDrawValues(false)

        return ld
    }
}
