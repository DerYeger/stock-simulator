package de.uniks.codliners.stock_simulator.ui

import android.widget.Button
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.domain.Share
import de.uniks.codliners.stock_simulator.ui.account.DepotShareRecyclerViewAdapter

@BindingAdapter("depotShares")
fun RecyclerView.bindDepotShares(shares: List<Share>?) {
    val adapter = adapter as DepotShareRecyclerViewAdapter
    adapter.submitList(shares)
}

@BindingAdapter("botEnabled")
fun Button.bindBotEnabled(enabled: Boolean) {
    text = if (enabled) {
        context.getText(R.string.stockbrot_disable_bot)
    } else {
        context.getText(R.string.stockbrot_enable_bot)
    }
}
