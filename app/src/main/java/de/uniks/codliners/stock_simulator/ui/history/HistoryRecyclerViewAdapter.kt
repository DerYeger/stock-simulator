package de.uniks.codliners.stock_simulator.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.TransactionViewBinding
import de.uniks.codliners.stock_simulator.domain.Transaction

class HistoryRecyclerViewAdapter: ListAdapter<Transaction,
        HistoryRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: TransactionViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.transaction = transaction
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TransactionViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction: Transaction = getItem(position)
        holder.bind(transaction)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            // refactor
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}