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
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import de.uniks.codliners.stock_simulator.database.HistoricalPrice
import de.uniks.codliners.stock_simulator.databinding.FragmentQuoteBinding

class QuoteFragment : Fragment() {

    private val viewModel: QuoteViewModel by viewModels {
        val symbol = QuoteFragmentArgs.fromBundle(arguments!!).symbol
        QuoteViewModel.Factory(activity!!.application, symbol)
    }

    private lateinit var binding: FragmentQuoteBinding

    private lateinit var tfLight: Typeface
    private lateinit var tfRegular: Typeface

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

        viewModel.errorAction.observe(viewLifecycleOwner, Observer { errorMessage: String? ->
            errorMessage?.let {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                viewModel.onErrorActionCompleted()
            }
        })

        viewModel.historicalPrices.observe(viewLifecycleOwner, Observer { priceList -> })

        initQuoteChart()

        return binding.root
    }

    private fun initQuoteChart() {
        val chart = binding.quoteChart

        chart.data = generateLineData(10, 5f)
        styleGraph(chart)
//        yAxis.setDrawGridLines(false)

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
        //        xAxis.setDrawGridLines(false)

        yAxis.axisLineColor = Color.TRANSPARENT
        yAxis.gridColor = Color.DKGRAY
        yAxis.setLabelCount(2, false)
        yAxis.textColor = Color.WHITE
        yAxis.typeface = tfLight
    }

    private fun generateLineData(count: Int, range: Float): LineData {
        val values: ArrayList<Entry> = ArrayList()

        for (i in 0 until count) {
            val value = (Math.random() * (range + 1)).toFloat() + 20
            values.add(Entry(i.toFloat(), value))
        }

        val dummy = listOf<HistoricalPrice>()

        val entries = dummy.map { price ->
            Entry(price.date.toFloat(), price.close.toFloat())
        }

        val lds = LineDataSet(values, "Account Balance")
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
//        ds1.circleRadius = 4f
//        ds1.highLightColor = Color.rgb(244, 117, 117)
//        ds1.setCircleColor(Color.WHITE)

        val sets: ArrayList<ILineDataSet> = ArrayList()
        sets.add(lds)

        val ld = LineData(sets)
        ld.setDrawValues(false)
//        lineData.setValueTextColor(Color.WHITE)
//        lineData.setValueTypeface(tfRegular)
        return ld
    }
}
