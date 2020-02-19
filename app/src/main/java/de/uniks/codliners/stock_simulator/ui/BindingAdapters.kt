package de.uniks.codliners.stock_simulator.ui

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.database.DepotQuote
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.SearchResult
import de.uniks.codliners.stock_simulator.repository.SearchRepository
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.ui.account.DepotQuoteRecyclerViewAdapter
import de.uniks.codliners.stock_simulator.ui.search.SearchResultAdapter
import de.uniks.codliners.stock_simulator.ui.history.HistoryRecyclerViewAdapter
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

@BindingAdapter("searchResults")
fun RecyclerView.bindSearchResults(symbols: List<SearchResult>?) {
    val adapter = adapter as SearchResultAdapter
    adapter.submitList(symbols)
}

@BindingAdapter("depotQuotes")
fun RecyclerView.bindDepotQuotes(quotes: List<DepotQuote>?) {
    val adapter = adapter as DepotQuoteRecyclerViewAdapter
    adapter.submitList(quotes)
}

@BindingAdapter("searchState")
fun TextView.bindSearchState(state: SearchRepository.State) {
    when (state) {
        is SearchRepository.State.Empty -> {
            text = context.getText(R.string.no_results)
            visibility = View.VISIBLE
        }
        is SearchRepository.State.Searching -> visibility = View.GONE
        is SearchRepository.State.Done -> visibility = View.GONE
        is SearchRepository.State.Error -> {
            text = state.message
            visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("searchState")
fun ProgressBar.bindSearchRepositoryState(state: SearchRepository.State) {
    visibility = when (state) {
        is SearchRepository.State.Searching -> View.VISIBLE
        else -> View.GONE
    }
}

@BindingAdapter("transactions")
fun RecyclerView.bindTransactions(transactions: List<Transaction>?) {
    val adapter = adapter as HistoryRecyclerViewAdapter
    adapter.submitList(transactions)
}

@BindingAdapter("botEnabled")
fun Button.bindBotEnabled(enabled: Boolean) {
    text = if (enabled) {
        context.getText(R.string.stockbrot_disable_bot)
    } else {
        context.getText(R.string.stockbrot_enable_bot)
    }
}

@BindingAdapter("stockbrotQuotes")
fun RecyclerView.bindStockbrotQuotes(quotes: List<Quote>?) {
    val adapter = adapter as StockbrotQuoteRecyclerViewAdapter
    adapter.submitList(quotes)
}
