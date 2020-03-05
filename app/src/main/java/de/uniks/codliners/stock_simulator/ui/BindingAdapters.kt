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

@BindingAdapter("visible")
fun View.bindVisibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("onRefresh")
fun SwipeRefreshLayout.bindRefreshListener(listener: Runnable) {
    setOnRefreshListener {
        listener.run()
    }
}

@BindingAdapter("symbolList")
fun RecyclerView.bindSymbolList(symbols: List<Symbol>?) {
    val adapter = adapter as SymbolListAdapter
    adapter.submitList(symbols)
}

@BindingAdapter("news")
fun RecyclerView.bindNews(news: List<News>?) {
    val adapter = adapter as NewsAdapter
    adapter.submitList(news)
}

@BindingAdapter("achievements")
fun RecyclerView.bindAchievements(achievements: List<Achievement>?) {
    val adapter = adapter as AchievementsAdapter
    adapter.submitList(achievements)
}

@BindingAdapter("depotQuotes")
fun RecyclerView.bindDepotQuotes(quotePurchases: List<DepotQuote>?) {
    val adapter = adapter as DepotQuoteRecyclerViewAdapter
    adapter.submitList(quotePurchases)
}

@BindingAdapter("depotQuoteText")
fun TextView.bindDepotQuoteText(depotQuotePurchase: DepotQuote?) {
    depotQuotePurchase?.let {
        text = when (depotQuotePurchase.amount.isWholeNumber()) {
            true ->
                String.format(
                    resources.getText(R.string.long_depot_quote_amount_format).toString(),
                    depotQuotePurchase.amount.toLong()
                )
            false ->
                String.format(
                    resources.getText(R.string.double_depot_quote_amount_format).toString(),
                    depotQuotePurchase.amount
                )
        }
    }
}

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

@BindingAdapter("depotQuote")
fun TextView.bindDepotQuote(depotQuotePurchase: DepotQuote?) {
    depotQuotePurchase?.let {
        text = when (depotQuotePurchase.amount.isWholeNumber()) {
            true ->
                String.format(
                    resources.getText(R.string.long_depot_quote_format).toString(),
                    depotQuotePurchase.symbol,
                    depotQuotePurchase.amount.toLong()
                )
            false ->
                String.format(
                    resources.getText(R.string.double_depot_quote_format).toString(),
                    depotQuotePurchase.symbol,
                    depotQuotePurchase.amount
                )
        }
    }
}

@BindingAdapter("transactions")
fun RecyclerView.bindTransactions(transactions: List<Transaction>?) {
    val adapter = adapter as HistoryRecyclerViewAdapter
    adapter.submitList(transactions)
}

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

//@BindingAdapter("transactionResultIcon")
//fun ImageView.bindTransactionResultIcon(performance: Double?) {
//    if (performance == null) {
//        this.visibility = View.INVISIBLE
//        return
//    }
//    when {
//        performance > 0.0 -> {
//            setImageDrawable(resources.getDrawable(R.drawable.ic_trending_up_black_24dp, context.theme))
//            this.setColorFilter(resources.getColor(R.color.colorAccent))
//        }
//        performance == 0.0 -> {
//            setImageDrawable(resources.getDrawable(R.drawable.ic_trending_flat_black_24dp, context.theme))
//            this.setColorFilter(resources.getColor(R.color.trendingFlat))
//        }
//        else -> {
//            setImageDrawable(resources.getDrawable(R.drawable.ic_trending_down_black_24dp, context.theme))
//            this.setColorFilter(resources.getColor(R.color.trendingDown))
//        }
//    }
//}

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

@BindingAdapter("stockbrotQuotes")
fun RecyclerView.bindStockbrotQuotes(quotes: List<StockbrotQuote>?) {
    val adapter = adapter as StockbrotQuoteRecyclerViewAdapter
    adapter.submitList(quotes)
}

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

@BindingAdapter("botAddRemoveQuote")
fun Button.bindBotAddRemoveQuote(enabled: Boolean) {
    text = if (enabled) {
        context.getText(R.string.stockbrot_remove_control_quote)
    } else {
        context.getText(R.string.stockbrot_add_control_quote)
    }
}

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

@BindingAdapter("enableCardView")
fun CardView.bindEnableCardView(enabled: Boolean) {
    val color = when(enabled) {
        true -> R.color.enabledCardBackground
        false -> R.color.disabledCardBackground
    }
    setCardBackgroundColor(resources.getColor(color, context.theme))
}
