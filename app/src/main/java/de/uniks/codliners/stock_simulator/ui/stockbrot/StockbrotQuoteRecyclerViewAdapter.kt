package de.uniks.codliners.stock_simulator.ui.stockbrot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.StockbrotQuoteViewBinding
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.ui.OnClickListener

class StockbrotQuoteRecyclerViewAdapter(private val onClickListener: OnClickListener<Quote>):
    ListAdapter<Quote, StockbrotQuoteRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: StockbrotQuoteViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(quote: Quote) {
            binding.onClickListener = onClickListener
            binding.quote = quote
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Quote>() {
        override fun areItemsTheSame(oldItem: Quote, newItem: Quote) =
            oldItem.symbol == newItem.symbol

        override fun areContentsTheSame(oldItem: Quote, newItem: Quote) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            StockbrotQuoteViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quote: Quote = getItem(position)
        holder.bind(quote)
    }
}
