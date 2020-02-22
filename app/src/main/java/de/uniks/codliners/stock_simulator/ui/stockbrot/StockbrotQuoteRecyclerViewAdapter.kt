package de.uniks.codliners.stock_simulator.ui.stockbrot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.StockbrotQuoteCardBinding
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.ui.OnClickListener

class StockbrotQuoteRecyclerViewAdapter(private val onClickListener: OnClickListener<StockbrotQuote>):
    ListAdapter<StockbrotQuote, StockbrotQuoteRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: StockbrotQuoteCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(StockbrotQuote: StockbrotQuote) {
            binding.onClickListener = onClickListener
            binding.stockbrotQuote = StockbrotQuote
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StockbrotQuote>() {
        override fun areItemsTheSame(oldItem: StockbrotQuote, newItem: StockbrotQuote) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: StockbrotQuote, newItem: StockbrotQuote) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            StockbrotQuoteCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stockbrotQuote: StockbrotQuote = getItem(position)
        holder.bind(stockbrotQuote)
    }
}
