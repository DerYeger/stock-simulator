package de.uniks.codliners.stock_simulator.ui.account

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import de.uniks.codliners.stock_simulator.databinding.FragmentAccountBinding
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.ui.OnClickListener
import java.text.SimpleDateFormat


class AccountFragment : Fragment() {

    private val viewModel: AccountViewModel by viewModels {
        AccountViewModel.Factory(activity!!.application)
    }

    private lateinit var binding: FragmentAccountBinding

    private lateinit var chart: LineChart

    private lateinit var tfLight: Typeface
    private lateinit var tfRegular: Typeface

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tfLight = Typeface.createFromAsset(context!!.assets, "OpenSans-Light.ttf")
        tfRegular = Typeface.createFromAsset(context!!.assets, "OpenSans-Regular.ttf")

        binding = FragmentAccountBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.depotRecyclerView.adapter =
            DepotQuoteRecyclerViewAdapter(OnClickListener { symbol ->
                val action =
                    AccountFragmentDirections.actionNavigationAccountToShareFragment(symbol.symbol)
                findNavController().navigate(action)
            })
        binding.lifecycleOwner = this

        chart = binding.accountChart

        initBalanceChart()

        viewModel.balancesLimited.observe(viewLifecycleOwner, Observer { balances ->
            run {
                updateBalanceChart(balances)
            }
        })

        return binding.root
    }

    private fun initBalanceChart() {
        val xAxis = chart.xAxis
        val yAxis = chart.axisLeft

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.setDrawGridBackground(false)
        chart.setTouchEnabled(false)

        xAxis.gridColor = Color.GRAY
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.setLabelCount(2, false)
        xAxis.textColor = Color.WHITE
        xAxis.typeface = tfLight
        xAxis.valueFormatter = object : ValueFormatter() {
            private val dateFormatter =
                SimpleDateFormat("dd.MM.YYYY hh:mm:ss", resources.configuration.locale)

            override fun getFormattedValue(value: Float): String {
                return dateFormatter.format(value)
            }
        }

        yAxis.gridColor = Color.DKGRAY
        yAxis.setDrawAxisLine(false)
        yAxis.setLabelCount(2, false)
        yAxis.textColor = Color.WHITE
        yAxis.typeface = tfLight
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "%.2f€".format(value)
            }
        }

        chart.invalidate()
    }

    private fun updateBalanceChart(balancesList: List<Balance>) {
        if (balancesList.isEmpty()) {
            return
        }

        val firstBalanceTimestamp = balancesList[0].timestamp
        val entries = balancesList.map { balance ->
            Entry(
                (balance.timestamp - firstBalanceTimestamp).toFloat(),
                balance.value.toFloat()
            )
        }
        val lds = LineDataSet(entries, "Account Balance")
        lds.color = Color.WHITE
        lds.color = ColorTemplate.VORDIPLOM_COLORS[0]
        lds.fillAlpha = 100
        lds.fillColor = Color.WHITE
        lds.lineWidth = 1.8f
        lds.mode = LineDataSet.Mode.STEPPED
        lds.setDrawCircles(false)
        lds.setDrawFilled(true)
        lds.setDrawHorizontalHighlightIndicator(false)

        val sets: ArrayList<ILineDataSet> = ArrayList()
        sets.add(lds)

        val ld = LineData(sets)
        ld.setDrawValues(false)

        chart.data = ld
        chart.invalidate()
    }
}
