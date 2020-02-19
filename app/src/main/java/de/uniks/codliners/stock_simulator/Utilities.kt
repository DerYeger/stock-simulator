package de.uniks.codliners.stock_simulator

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.HistoryRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


const val SHARED_PREFERENCES_KEY = "de.uniks.codliners.stock_simulator"
private lateinit var tfLight: Typeface
private lateinit var tfRegular: Typeface

fun ContextWrapper.sharedPreferences(): SharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE)

fun Context.resetAccount() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        AccountRepository(self).resetAccount()
    }
}

fun Context.resetHistory() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        HistoryRepository(self).resetHistory()
    }
}

fun Context.resetQuotes() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        QuoteRepository(self).resetQuotes()
    }
}

fun Context.ensureAccountPresence(lifecycleOwner: LifecycleOwner) {
    val accountRepository = AccountRepository(this)
    accountRepository.latestBalance.observe(lifecycleOwner, Observer { t ->
        run {
            CoroutineScope(Dispatchers.Main).launch {
                if (t == null) {
                    Timber.i("No balance detected. Resetting account")
                    accountRepository.resetAccount()
                }
            }
        }
    })
    accountRepository.balances.observe(lifecycleOwner, Observer { t ->
        run {
            Timber.i("Created new account with balances: $t")
        }
    })
}

fun noNulls(vararg args: Any?): Boolean {
    return listOfNotNull(*args).size == args.size
}

fun String?.toSafeLong(): Long? {
    return try {
        this?.toLong()
    } catch (_: Throwable) {
        null
    }
}

fun initLineChart(chart: LineChart, context: Context, locale: Locale) {
    tfLight = Typeface.createFromAsset(context.assets, "OpenSans-Light.ttf")
    tfRegular = Typeface.createFromAsset(context.assets, "OpenSans-Regular.ttf")

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
            SimpleDateFormat("dd.MM.YYYY hh:mm:ss", locale)

        override fun getFormattedValue(value: Float): String {
            return dateFormatter.format(value)
        }
    }

    yAxis.gridColor = Color.GRAY
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

fun updateLineChart(chart: LineChart, entryList: List<Entry>, label: String) {
    if (entryList.isEmpty()) {
        return
    }

    val lds = LineDataSet(entryList, label)
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