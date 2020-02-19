package de.uniks.codliners.stock_simulator.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.database.DepotQuote
import de.uniks.codliners.stock_simulator.databinding.DepotQuoteCardBinding
import de.uniks.codliners.stock_simulator.ui.OnClickListener

class DepotQuoteRecyclerViewAdapter(private val onClickListener: OnClickListener<DepotQuote>):
    ListAdapter<DepotQuote, DepotQuoteRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: DepotQuoteCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(quote: DepotQuote) {
            binding.onClickListener = onClickListener
            binding.quote = quote
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DepotQuote>() {
        override fun areItemsTheSame(oldItem: DepotQuote, newItem: DepotQuote) =
            oldItem.symbol == newItem.symbol

        override fun areContentsTheSame(oldItem: DepotQuote, newItem: DepotQuote) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DepotQuoteCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quote: DepotQuote = getItem(position)
        holder.bind(quote)
    }
}
