package de.uniks.codliners.stock_simulator

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import de.uniks.codliners.stock_simulator.background.StockbrotWorkRequest
import de.uniks.codliners.stock_simulator.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

const val SHARED_PREFERENCES_KEY = "de.uniks.codliners.stock_simulator"
private lateinit var tfLight: Typeface
private lateinit var tfRegular: Typeface

/**
 * Creates a [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) that is updated every time one of the [sources] changes.
 *
 * @param T The type of the [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
 * @param sources The sources of the returned [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
 * @param block Called every time a source changes. Its result is only applied if it differs from the current value.
 * @return The initialized [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
 *
 * @author Jan Müller
 */
fun <T> sourcedLiveData(vararg sources: LiveData<*>, block: () -> T?): LiveData<T> =
    MediatorLiveData<T>().apply {
        sources.forEach { source ->
            addSource(source) {
                val oldValue = value
                val newValue = block()
                if (oldValue != newValue) value = block()
            }
        }
    }

/**
 * Shortcut function for creating [MediatorLiveData](https://developer.android.com/reference/androidx/lifecycle/MediatorLiveData).
 *
 * @param T The type of the [MediatorLiveData](https://developer.android.com/reference/androidx/lifecycle/MediatorLiveData).
 * @param block Applied to the returned [MediatorLiveData](https://developer.android.com/reference/androidx/lifecycle/MediatorLiveData).
 * @return The modified [MediatorLiveData](https://developer.android.com/reference/androidx/lifecycle/MediatorLiveData).
 *
 * @author Jan Müller
 */
inline fun <T> mediatedLiveData(block: MediatorLiveData<T>.() -> Unit): MediatorLiveData<T> =
    MediatorLiveData<T>().apply(block)

/**
 * Getter for this app's shared preferences.
 *
 * @return This app's shared preferences.
 *
 * @author Jan Müller
 */
fun ContextWrapper.sharedPreferences(): SharedPreferences =
    getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE)

/**
 * Resets account information.
 *
 * @author Jan Müller
 */
fun Context.resetAccount() {
    val self = this
    CoroutineScope(Dispatchers.IO).launch {
        AccountRepository(self).resetAccount()
    }
}

/**
 * Deletes all transactions from the database.
 *
 * @author Jonas Thelemann
 */
fun Context.resetHistory() {
    val self = this
    CoroutineScope(Dispatchers.IO).launch {
        HistoryRepository(self).resetHistory()
    }
}

/**
 * Deletes all quotes from the database.
 *
 * @author Jonas Thelemann
 */
fun Context.resetQuotes() {
    val self = this
    CoroutineScope(Dispatchers.IO).launch {
        QuoteRepository(self).resetQuotes()
    }
}

fun Context.resetStockbrot() {
    val self = this
    CoroutineScope(Dispatchers.IO).launch {
        // reset stockbrot database
        StockbrotRepository(self).resetStockbrot()
        // cancel all running workers
        StockbrotWorkRequest(self).cancelAll()
    }
}

/**
 * Deletes all news from the database.
 *
 * @author Jonas Thelemann
 */
fun Context.resetNews() {
    val self = this
    CoroutineScope(Dispatchers.IO).launch {
        NewsRepository(self).resetNews()
    }
}

/**
 * Resets all achievements in the database.
 *
 * @author Lucas Held
 */
fun Context.resetAchievements() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        AchievementsRepository(self).resetAchievements()
    }
}

/**
 * Resets (/creates) the account if no balance is retrieved from the [AccountRepository].
 *
 * @author Jonas Thelemann
 */
fun Context.ensureAccountPresence() {
    val self = this
    CoroutineScope(Dispatchers.IO).launch {
        val accountRepository = AccountRepository(self)

        if (!accountRepository.hasBalance()) {
            accountRepository.resetAccount()
        }
    }
}

/**
 * Checks if all arguments are non-null.
 *
 * @param args The arguments.
 * @return true if all arguments are non-null or false otherwise.
 */
fun noNulls(vararg args: Any?): Boolean = listOfNotNull(*args).size == args.size

/**
 * If possible, extracts a [Double] value from a [String].
 *
 * @receiver The source [String].
 * @return [Double] value of the [String] if it exists or null otherwise.
 *
 * @author Jan Müller
 */
fun String?.toSafeDouble(): Double? =
    try {
        this?.toDouble()
    } catch (_: Throwable) {
        null
    }

/**
 * Checks if a [Double] is a whole number.
 *
 * @receiver The [Double] to be checked.
 * @return true if it is a whole number or false otherwise.
 *
 * @author Jan Müller
 */
fun Double.isWholeNumber(): Boolean = toLong().toDouble() == this

/**
 * Initializes a line chart and its axis.
 *
 * @param chart The chart that is to be set up.
 * @param context The context from which the typeface assets can be loaded.
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

/**
 * Fills a line chart with entries (/data).
 *
 * @param chart The chart that is to be filled with data.
 * @param entryList The entries that are to be shown.
 * @param label The [LineDataSet](https://javadoc.jitpack.io/com/github/PhilJay/MPAndroidChart/v3.1.0/javadoc/com/github/mikephil/charting/data/LineDataSet.html)'s label.
 * @param locale The locale to use for [ValueFormatter](https://javadoc.jitpack.io/com/github/PhilJay/MPAndroidChart/v3.1.0/javadoc/com/github/mikephil/charting/formatter/ValueFormatter.html)s.
 * @param referenceTimestamp The timestamp to subtract as a workaround for the graph library's float usage. Optional.
 * @param xAxisValueFormatter The x axis value formatter to use. Optional.
 * @param axisLeftValueFormatter The left axis value formatter to use. Optional.
 */
fun updateLineChart(
    chart: LineChart,
    entryList: List<Entry>,
    label: String,
    locale: Locale,
    referenceTimestamp: Long = 0,
    xAxisValueFormatter: ValueFormatter = TimestampValueFormatter(
        referenceTimestamp,
        locale
    ),
    axisLeftValueFormatter: ValueFormatter = CurrencyValueFormatter("$")
) {
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

/**
 * A timestamp value formatter that works around the graph library's float usage issue.
 *
 * @property referenceTimestamp The timestamp which has been subtracted.
 * @param locale The locale to use for the [SimpleDateFormat] date formatter.
 *
 * @author Jonas Thelemann
 */
class TimestampValueFormatter(private val referenceTimestamp: Long, locale: Locale) :
    ValueFormatter() {
    private val dateFormatter =
        SimpleDateFormat("dd.MM.yy", locale)

    /**
     * Transforms a float value to a date string.
     *
     * @param value The value that is to be formatted.
     * @return A formatted date.
     */
    override fun getFormattedValue(value: Float): String {
        // "toInt()" required to workaround inaccurate results due to unchangeable float usage
        val recalculatedValue = value.toInt() + referenceTimestamp
        return dateFormatter.format(recalculatedValue)
    }
}

class CurrencyValueFormatter(private val currencySymbol: String) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "%s$currencySymbol".format(value)
    }
}
