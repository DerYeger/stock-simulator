package de.uniks.codliners.stock_simulator.ui

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.domain.Share
import de.uniks.codliners.stock_simulator.ui.account.DepotShareRecyclerViewAdapter

@BindingAdapter("depotShares")
fun RecyclerView.bindDepotShares(shares: List<Share>?) {
    val adapter = adapter as DepotShareRecyclerViewAdapter
    adapter.submitList(shares)
}
