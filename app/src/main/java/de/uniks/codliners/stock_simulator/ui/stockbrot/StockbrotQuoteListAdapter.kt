package de.uniks.codliners.stock_simulator.ui.stockbrot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.CardStockbrotQuoteBinding
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.ui.OnClickListener

/**
 * The adapter that inserts [StockbrotQuote]s into a [RecyclerView](https://developer.android.com/jetpack/androidx/releases/recyclerview).
 *
 * @author Lucas Held
 */
class StockbrotQuoteListAdapter(private val onClickListener: OnClickListener<StockbrotQuote>) :
    ListAdapter<StockbrotQuote, StockbrotQuoteListAdapter.ViewHolder>(DiffCallback) {

    /**
     * [RecyclerView.ViewHolder](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder) for CardStockbrotQuoteBinding.
     *
     * @property binding The CardStockbrotQuoteBinding.
     *
     * @author Lucas Held
     */
    inner class ViewHolder(private val binding: CardStockbrotQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a [StockbrotQuote] and [OnClickListener] to the CardStockbrotQuoteBinding.
         *
         * @param StockbrotQuote The [StockbrotQuote] that will be bound to the CardStockbrotQuoteBinding.
         */
        fun bind(StockbrotQuote: StockbrotQuote) {
            binding.onClickListener = onClickListener
            binding.stockbrotQuote = StockbrotQuote
            binding.executePendingBindings()
        }
    }

    /**
     * [DiffUtil.ItemCallback](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback) for [StockbrotQuote]s.
     *
     * @author Lucas Held
     */
    companion object DiffCallback : DiffUtil.ItemCallback<StockbrotQuote>() {

        /**
         * Checks if two [StockbrotQuote]s have the same id.
         *
         * @param oldItem The old [StockbrotQuote].
         * @param newItem The new [StockbrotQuote].
         * @return true if both [StockbrotQuote]s have the same id or false otherwise.
         */
        override fun areItemsTheSame(oldItem: StockbrotQuote, newItem: StockbrotQuote) =
            oldItem.id == newItem.id

        /**
         * Checks if two [StockbrotQuote]s are equal.
         *
         * @param oldItem The old [StockbrotQuote].
         * @param newItem The new [StockbrotQuote].
         * @return true if both [StockbrotQuote]s are equal or false otherwise.
         */
        override fun areContentsTheSame(oldItem: StockbrotQuote, newItem: StockbrotQuote) =
            oldItem == newItem
    }

    /**
     * Creates a [ViewHolder] by inflating a CardStockbrotQuoteBinding.
     *
     * @param parent The parent of the inflated CardStockbrotQuoteBinding.
     * @param viewType Ignored.
     * @return The created [ViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardStockbrotQuoteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    /**
     * Binds a [StockbrotQuote] to a [ViewHolder].
     *
     * @param holder The target [ViewHolder].
     * @param position The position of the [StockbrotQuote] in this adapter's list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stockbrotQuote: StockbrotQuote = getItem(position)
        holder.bind(stockbrotQuote)
    }
}
