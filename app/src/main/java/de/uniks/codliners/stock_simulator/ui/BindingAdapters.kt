package de.uniks.codliners.stock_simulator.ui

import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.domain.*
import de.uniks.codliners.stock_simulator.domain.TransactionType.BUY
import de.uniks.codliners.stock_simulator.domain.TransactionType.SELL
import de.uniks.codliners.stock_simulator.isWholeNumber
import de.uniks.codliners.stock_simulator.ui.account.DepotQuoteRecyclerViewAdapter
import de.uniks.codliners.stock_simulator.ui.achievements.AchievementsAdapter
import de.uniks.codliners.stock_simulator.ui.history.HistoryRecyclerViewAdapter
import de.uniks.codliners.stock_simulator.ui.news.NewsAdapter
import de.uniks.codliners.stock_simulator.ui.search.SymbolListAdapter
import de.uniks.codliners.stock_simulator.ui.stockbrot.StockbrotQuoteRecyclerViewAdapter

/**
 * Sets the visibility of a [View](https://developer.android.com/reference/android/view/View).
 *
 * @receiver The target view.
 * @param visible true for View.VISIBLE and false for View.GONE.
 *
 * @author Jan Müller
 */
@BindingAdapter("visible")
fun View.bindVisibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * Sets the refresh listener of a [SwipeRefreshLayout](https://developer.android.com/reference/android/support/v4/widget/SwipeRefreshLayout).
 *
 * @receiver The target [SwipeRefreshLayout](https://developer.android.com/reference/android/support/v4/widget/SwipeRefreshLayout).
 * @param listener The [Runnable] to be run inside the listener.
 *
 * @author Jan Müller
 */
@BindingAdapter("onRefresh")
fun SwipeRefreshLayout.bindRefreshListener(listener: Runnable) {
    setOnRefreshListener {
        listener.run()
    }
}

/**
 * Submits a [Symbol] [List] to the [SymbolListAdapter] of a [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView).
 *
 * @receiver The target [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView).
 * @param symbols The [List] of [Symbol]s that will be submitted.
 * @param callback Callback, which is executed after the [symbols] have been submitted.
 *
 * @author Jan Müller
 */
@BindingAdapter(value = ["symbols", "callback"], requireAll = true)
fun RecyclerView.bindSymbolList(symbols: List<Symbol>?, callback: Runnable) {
    val adapter = adapter as SymbolListAdapter
    adapter.submitList(symbols, callback)
}

/**
 * Binds news to the [NewsAdapter].
 *
 * @param news The news to display.
 *
 * @author Jonas Thelemann
 */
@BindingAdapter("news")
fun RecyclerView.bindNews(news: List<News>?) {
    val adapter = adapter as NewsAdapter
    adapter.submitList(news)
}

/**
 * Bind achievements to the [AchievementsAdapter]
 *
 * @param achievements The achievements to display.
 *
 * @author Lucas Held
 */
@BindingAdapter("achievements")
fun RecyclerView.bindAchievements(achievements: List<Achievement>?) {
    val adapter = adapter as AchievementsAdapter
    adapter.submitList(achievements)
}

/**
 * Submits a [DepotQuote] [List] to the [DepotQuoteRecyclerViewAdapter] of a [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView).
 *
 * @receiver The target [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView).
 * @param quotePurchases The [List] of [DepotQuote]s that will be submitted.
 *
 * @author Jan Müller
 */
@BindingAdapter("depotQuotes")
fun RecyclerView.bindDepotQuotes(quotePurchases: List<DepotQuote>?) {
    val adapter = adapter as DepotQuoteRecyclerViewAdapter
    adapter.submitList(quotePurchases)
}

/**
 * Sets the text of a [TextView](https://developer.android.com/reference/android/widget/TextView) depending on a [DepotQuote] amount.
 *
 * @receiver The target [TextView](https://developer.android.com/reference/android/widget/TextView).
 * @param depotQuote The [DepotQuote] source.
 *
 * @author Jan Müller
 */
@BindingAdapter("depotQuoteText")
fun TextView.bindDepotQuoteText(depotQuote: DepotQuote?) {
    depotQuote?.let {
        text = when (depotQuote.amount.isWholeNumber()) {
            true ->
                String.format(
                    resources.getText(R.string.long_depot_quote_amount_format).toString(),
                    depotQuote.amount.toLong()
                )
            false ->
                String.format(
                    resources.getText(R.string.double_depot_quote_amount_format).toString(),
                    depotQuote.amount
                )
        }
    }
}

/**
 * Sets text of a [TextView] to the total depot value if quote available in depot.
 *
 * @param depotQuotePurchase [DepotQuote] that can be null
 * @param quote [Quote] that can be null
 *
 * @author Lucas Held
 */
@BindingAdapter(value = ["depotTotalValueDepotQuote", "depotTotalValueQuote"])
fun TextView.bindDepotQuoteTotalValue(depotQuotePurchase: DepotQuote?, quote: Quote?) {
    if (depotQuotePurchase != null && quote != null) {
        val price = depotQuotePurchase.amount * quote.latestPrice
        text = String.format(
            resources.getText(R.string.total_price_format).toString(),
            price
        )
    }
}

/**
 * Sets the text of a [TextView](https://developer.android.com/reference/android/widget/TextView) depending on a [DepotQuote] symbol and amount.
 *
 * @receiver The target [TextView](https://developer.android.com/reference/android/widget/TextView).
 * @param depotQuote The [DepotQuote] source.
 *
 * @author Jan Müller
 */
@BindingAdapter("depotQuote")
fun TextView.bindDepotQuote(depotQuote: DepotQuote?) {
    depotQuote?.let {
        text = when (depotQuote.amount.isWholeNumber()) {
            true ->
                String.format(
                    resources.getText(R.string.long_depot_quote_format).toString(),
                    depotQuote.symbol,
                    depotQuote.amount.toLong()
                )
            false ->
                String.format(
                    resources.getText(R.string.double_depot_quote_format).toString(),
                    depotQuote.symbol,
                    depotQuote.amount
                )
        }
    }
}

@BindingAdapter("transactions")
fun RecyclerView.bindTransactions(transactions: List<Transaction>?) {
    val adapter = adapter as HistoryRecyclerViewAdapter
    adapter.submitList(transactions)
}

/**
 * TODO
 *
 * @param performance TODO
 *
 * @author TODO
 * @author Jonas Thelemann
 */
@BindingAdapter("lossOrWin")
fun TextView.bindPerformanceText(performance: Double) {
    when {
        performance > 0.0 -> {
            text = String.format(
                resources.getText(R.string.performance_format_win).toString(),
                performance
            )
            this.setTextColor(resources.getColor(R.color.colorAccent, context.theme))
        }
        performance == 0.0 -> {
            text = String.format(
                resources.getText(R.string.performance_format_neutral).toString(),
                performance
            )
            this.setTextColor(resources.getColor(R.color.trendingFlat, context.theme))
        }
        else -> {
            text = String.format(
                resources.getText(R.string.performance_format_loss).toString(),
                performance
            )
            this.setTextColor(resources.getColor(R.color.trendingDown, context.theme))
        }
    }
}

/**
 * TODO
 *
 * @param performance TODO
 *
 * @author TODO
 * @author Jonas Thelemann
 */
@BindingAdapter("trendingImage")
fun ImageView.bindPerformanceIcon(performance: Double) {
    when {
        performance > 0.0 -> {
            setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_trending_up_black_24dp,
                    context.theme
                )
            )
            this.setColorFilter(resources.getColor(R.color.colorAccent, context.theme))
        }
        performance == 0.0 -> {
            setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_trending_flat_black_24dp,
                    context.theme
                )
            )
            this.setColorFilter(resources.getColor(R.color.trendingFlat, context.theme))
        }
        else -> {
            setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_trending_down_black_24dp,
                    context.theme
                )
            )
            this.setColorFilter(resources.getColor(R.color.trendingDown, context.theme))
        }
    }
}

/**
 * TODO
 *
 * @param performance TODO
 *
 * @author TODO
 * @author Jonas Thelemann
 */
@BindingAdapter("transactionResultText")
fun TextView.bindTransactionResultText(performance: Double?) {
    if (performance == null) {
        this.visibility = View.INVISIBLE
        return
    }
    when {
        performance > 0.0 -> {
            text = String.format(
                resources.getText(R.string.transaction_format_win).toString(),
                performance
            )
            this.setTextColor(resources.getColor(R.color.colorAccent, context.theme))
        }
        performance == 0.0 -> {
            text = String.format(
                resources.getText(R.string.transaction_format_neutral).toString(),
                performance
            )
            this.setTextColor(resources.getColor(R.color.trendingFlat, context.theme))
        }
        else -> {
            text = String.format(
                resources.getText(R.string.transaction_format_loss).toString(),
                performance
            )
            this.setTextColor(resources.getColor(R.color.trendingDown, context.theme))
        }
    }
}

/**
 * Bind [StockbrotQuote]s to the [StockbrotQuoteRecyclerViewAdapter]
 *
 * @param quotes The [StockbrotQuote]s to display.
 *
 * @author Lucas Held
 */
@BindingAdapter("stockbrotQuotes")
fun RecyclerView.bindStockbrotQuotes(quotes: List<StockbrotQuote>?) {
    val adapter = adapter as StockbrotQuoteRecyclerViewAdapter
    adapter.submitList(quotes)
}

/**
 * Sets the image of an [ImageView](https://developer.android.com/reference/android/widget/ImageView) depending on a [TransactionType].
 *
 * @param transactionType The [TransactionType] source.
 *
 * @author Jan Müller
 * @author Jonas Thelemann
 */
@BindingAdapter("transactionType")
fun ImageView.bindTransactionType(transactionType: TransactionType?) {
    transactionType?.let {
        val drawableId = when (transactionType) {
            BUY -> R.drawable.ic_shopping_cart_24dp
            SELL -> R.drawable.ic_attach_money_24dp
        }
        setImageDrawable(resources.getDrawable(drawableId, context.theme))
    }
}

/**
 * Binds a paywall indicator to the image view, which displays a money symbol.
 *
 * @param paywallType Indicates whether there is a paywall or not.
 *
 * @author Jonas Thelemann
 */
@BindingAdapter("paywallType")
fun ImageView.bindPaywallType(paywallType: Boolean?) {
    paywallType?.let {
        val drawableId = when (paywallType) {
            true -> R.drawable.ic_attach_money_24dp
            false -> R.drawable.ic_money_off_black_24dp
        }
        setImageDrawable(resources.getDrawable(drawableId, context.theme))
    }
}

/**
 * Sets the text of a [TextView](https://developer.android.com/reference/android/widget/TextView) depending on a [Transaction] amount.
 *
 * @receiver The target [TextView](https://developer.android.com/reference/android/widget/TextView).
 * @param transaction The [Transaction] source.
 *
 * @author Jan Müller
 */
@BindingAdapter("transaction")
fun TextView.bindTransaction(transaction: Transaction?) {
    transaction?.let {
        text = if (transaction.amount.isWholeNumber()) {
            val stringId = when (transaction.transactionType) {
                BUY -> R.string.long_buy_amount_format
                SELL -> R.string.long_sell_amount_format
            }
            String.format(resources.getText(stringId).toString(), transaction.amount.toLong())
        } else {
            val stringId = when (transaction.transactionType) {
                BUY -> R.string.double_buy_amount_format
                SELL -> R.string.double_sell_amount_format
            }
            String.format(resources.getText(stringId).toString(), transaction.amount)
        }
    }
}

/**
 * Creates and binds a listener that posts a [Spinner](https://developer.android.com/guide/topics/ui/controls/spinner)'s value to a [MutableLiveData](https://developer.android.com/reference/android/arch/lifecycle/MutableLiveData).
 *
 * @receiver The source [Spinner](https://developer.android.com/guide/topics/ui/controls/spinner).
 * @param selection The target [MutableLiveData](https://developer.android.com/reference/android/arch/lifecycle/MutableLiveData).
 */
@BindingAdapter("observeSelection")
fun Spinner.bindSelection(selection: MutableLiveData<String>) {
    val self = this

    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // ignore, as it's impossible
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val selectedItem = self.getItemAtPosition(position).toString()
            selection.postValue(selectedItem)
        }
    }
}

/**
 * Sets the text of a [Button] depending on a state.
 *
 * @param enabled The state that indicates if a [StockbrotQuote] exists or not
 *
 * @author Lucas Held
 */
@BindingAdapter("botAddRemoveQuote")
fun Button.bindBotAddRemoveQuote(enabled: Boolean) {
    text = if (enabled) {
        context.getText(R.string.stockbrot_remove_control_quote)
    } else {
        context.getText(R.string.stockbrot_add_control_quote)
    }
}

/**
 * Sets the text of a [TextView](https://developer.android.com/reference/android/widget/TextView) depending on a [StockbrotQuote].
 *
 * @receiver The target [TextView](https://developer.android.com/reference/android/widget/TextView).
 * @param stockbrotQuote The [StockbrotQuote] source.
 *
 * @author Jan Müller
 */
@BindingAdapter("stockbrotQuote")
fun TextView.bindStockbrotQuote(stockbrotQuote: StockbrotQuote) {
    text = when (stockbrotQuote.limitedBuying) {
        true -> when (stockbrotQuote.type) {
            Symbol.Type.SHARE -> String.format(
                resources.getText(R.string.stockbrot_quote_buying_amount_format_long).toString(),
                stockbrotQuote.buyLimit.toLong(),
                stockbrotQuote.maximumBuyPrice
            )
            Symbol.Type.CRYPTO -> String.format(
                resources.getText(R.string.stockbrot_quote_buying_amount_format_double).toString(),
                stockbrotQuote.buyLimit,
                stockbrotQuote.maximumBuyPrice
            )
        }
        false ->
            String.format(
                resources.getText(R.string.stockbrot_quote_buying_format).toString(),
                stockbrotQuote.maximumBuyPrice
            )
    }
}

/**
 * Changes the [CardView] background color to create an enable/disable effect.
 *
 * @param enabled If set to true, the [CardView] will be enabled. If set to false, the [CardView] will be disabled.
 *
 * @author Lucas Held
 */
@BindingAdapter("enableCardView")
fun CardView.bindEnableCardView(enabled: Boolean) {
    val color = when(enabled) {
        true -> R.color.enabledCardBackground
        false -> R.color.disabledCardBackground
    }
    setCardBackgroundColor(resources.getColor(color, context.theme))
}

/**
 * Sets the text of a [TextView] and handles null values.
 *
 * @param change The quote change or null if the change is not defined
 *
 * @author Lucas Held
 */
@BindingAdapter("quoteChange")
fun TextView.bindQuoteChange(change: Double?) {
    text = when(change) {
        null -> resources.getText(R.string.not_defined).toString()
        else -> String.format(
            resources.getText(R.string.currency_format).toString(),
            change
        )
    }
}
