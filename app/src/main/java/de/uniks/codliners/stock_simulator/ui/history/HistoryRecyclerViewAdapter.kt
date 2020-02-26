package de.uniks.codliners.stock_simulator.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.CardTransactionBinding
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.ui.OnClickListener
import java.text.SimpleDateFormat
import java.util.*

class HistoryRecyclerViewAdapter(
    private val onClickListener: OnClickListener<Transaction>,
    locale: Locale
) : ListAdapter<Transaction,
        HistoryRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", locale)

    inner class ViewHolder(private val binding: CardTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = transaction.date
            binding.transaction = transaction
            binding.dateString = dateFormatter.format(calendar.time)
            binding.onClickListener = onClickListener
            binding.expanded = false
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardTransactionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction: Transaction = getItem(position)
        holder.bind(transaction)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}