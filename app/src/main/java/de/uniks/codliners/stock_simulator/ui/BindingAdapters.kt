package de.uniks.codliners.stock_simulator.ui

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.domain.SearchResult
import de.uniks.codliners.stock_simulator.domain.Share
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.repository.SearchRepository
import de.uniks.codliners.stock_simulator.ui.account.DepotShareRecyclerViewAdapter
import de.uniks.codliners.stock_simulator.ui.search.SearchResultAdapter

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

@BindingAdapter("depotShares")
fun RecyclerView.bindDepotShares(shares: List<Share>?) {
    val adapter = adapter as DepotShareRecyclerViewAdapter
    adapter.submitList(shares)
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
