package de.uniks.codliners.stock_simulator.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.CardTransactionBinding
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.ui.OnClickListener
import de.uniks.codliners.stock_simulator.ui.history.TransactionListAdapter.ViewHolder
import java.text.SimpleDateFormat
import java.util.*

/**
 * [ListAdapter](https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter) for [Transaction]s.
 *
 * @property onClickListener The [OnClickListener] for [ViewHolder]s.
 *
 * @author Juri Lozowoj
 * @author Jan Müller
 */
class TransactionListAdapter(
    private val onClickListener: OnClickListener<Transaction>,
    locale: Locale
) : ListAdapter<Transaction, TransactionListAdapter.ViewHolder>(DiffCallback) {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", locale)

    /**
     * [RecyclerView.ViewHolder](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder) for CardTransactionBinding.
     *
     * @property binding The CardTransactionBinding.
     *
     * @author Juri Lozowoj
     */
    inner class ViewHolder(private val binding: CardTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a [Transaction], dateString and [OnClickListener] to the CardTransactionBinding.
         *
         * @param transaction The [Transaction] that will be bound to the CardTransactionBinding.
         */
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

    /**
     * Creates a [ViewHolder] by inflating a CardTransactionBinding.
     *
     * @param parent The parent of the inflated CardTransactionBinding.
     * @param viewType Ignored.
     * @return The created [ViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardTransactionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    /**
     * Binds a [Transaction] to a [ViewHolder].
     *
     * @param holder The target [ViewHolder].
     * @param position The position of the [Transaction] in this adapter's list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction: Transaction = getItem(position)
        holder.bind(transaction)
    }

    /**
     * [DiffUtil.ItemCallback](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback) for [Transaction]s.
     *
     * @author Juri Lozowoj
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {

        /**
         * Checks if two [Transaction]s have the same id.
         *
         * @param oldItem The old [Transaction].
         * @param newItem The new [Transaction].
         * @return true if both [Transaction]s have the same id or false otherwise.
         */
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }

        /**
         * Checks if two [Transaction]s are equal.
         *
         * @param oldItem The old [Transaction].
         * @param newItem The new [Transaction].
         * @return true if both [Transaction]s are equal or false otherwise.
         */
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}