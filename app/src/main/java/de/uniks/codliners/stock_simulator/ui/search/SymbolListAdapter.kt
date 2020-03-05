package de.uniks.codliners.stock_simulator.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.CardSymbolBinding
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.ui.OnClickListener

class SymbolListAdapter(private val onClickListener: OnClickListener<Symbol>) :
    ListAdapter<Symbol, SymbolListAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: CardSymbolBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(symbol: Symbol) {
            binding.symbol = symbol
            binding.onClickListener = onClickListener
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Symbol>() {
        override fun areItemsTheSame(oldItem: Symbol, newItem: Symbol) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Symbol, newItem: Symbol) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardSymbolBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val symbol: Symbol = getItem(position)
        holder.bind(symbol)
    }
}
