package de.uniks.codliners.stock_simulator

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import androidx.lifecycle.LifecycleOwner
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import de.uniks.codliners.stock_simulator.background.StockbrotWorkRequest
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.HistoryRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.repository.StockbrotRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

fun Context.resetStockbrot() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        // reset stockbrot database
        StockbrotRepository(self).resetStockbrot()
        // cancel all running workers
        StockbrotWorkRequest(self).cancelAll()
    }
}

fun Context.ensureAccountPresence(lifecycleOwner: LifecycleOwner) {
    val accountRepository = AccountRepository(this)
    CoroutineScope(Dispatchers.Main).launch {
        if (!accountRepository.hasBalance()) {
            accountRepository.resetAccount()
        }
    }
}

fun noNulls(vararg args: Any?): Boolean {
    return listOfNotNull(*args).size == args.size
}

fun String?.toSafeDouble(): Double? {
    return try {
        this?.toDouble()
    } catch (_: Throwable) {
        null
    }
}

fun String.toType(): Symbol.Type? {
    return when(this) {
        "SHARE" -> Symbol.Type.SHARE
        "CRYPTO" -> Symbol.Type.CRYPTO
        else -> null
    }
}

fun Double.isWholeNumber() = toLong().toDouble() == this

/**
 * Initializes a line chart and its axis.
 */
fun initLineChart(chart: LineChart, context: Context) {
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

    yAxis.gridColor = Color.GRAY
    yAxis.setDrawAxisLine(false)
    yAxis.setLabelCount(2, false)
    yAxis.textColor = Color.WHITE
    yAxis.typeface = tfLight

    chart.invalidate()
}

fun updateLineChart(chart: LineChart, entryList: List<Entry>, label: String, locale: Locale, referenceTimestamp: Long = 0, xAxisValueFormatter: ValueFormatter = TimestampValueFormatter(referenceTimestamp, locale), axisLeftValueFormatter: ValueFormatter = CurrencyValueFormatter("€")) {
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

    chart.xAxis.valueFormatter = xAxisValueFormatter
    chart.axisLeft.valueFormatter = axisLeftValueFormatter
    chart.data = ld
    chart.invalidate()
}

class TimestampValueFormatter(private val referenceTimestamp: Long, locale: Locale) : ValueFormatter() {
    private val dateFormatter =
        SimpleDateFormat("dd.MM.yyyy hh:mm:ss", locale)

    override fun getFormattedValue(value: Float): String {
        // "toInt()" required to workaround inaccurate results due to unchangeable float usage
        val tmp = value.toInt() + referenceTimestamp
        return dateFormatter.format(tmp)
    }
}

class CurrencyValueFormatter(private val currencySymbol: String) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "%.2f$currencySymbol".format(value)
    }
}