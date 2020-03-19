package de.uniks.codliners.stock_simulator.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.CardDepotQuoteBinding
import de.uniks.codliners.stock_simulator.domain.DepotQuote
import de.uniks.codliners.stock_simulator.ui.OnClickListener

class DepotQuoteListAdapter(private val onClickListener: OnClickListener<DepotQuote>) :
    ListAdapter<DepotQuote, DepotQuoteListAdapter.ViewHolder>(DiffCallback) {

    /**
     * [RecyclerView.ViewHolder](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder) for CardDepotQuoteBinding.
     *
     * @property binding The CardDepotQuoteBinding.
     *
     * @author Jan Müller
     */
    inner class ViewHolder(private val binding: CardDepotQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a [DepotQuote] and [OnClickListener] to the CardDepotQuoteBinding.
         *
         * @param quote The [DepotQuote] that will be bound to the CardSymbolBinding.
         */
        fun bind(quote: DepotQuote) {
            binding.onClickListener = onClickListener
            binding.quote = quote
            binding.executePendingBindings()
        }
    }

    /**
     * [DiffUtil.ItemCallback](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback) for [DepotQuote]s.
     *
     * @author Jan Müller
     */
    companion object DiffCallback : DiffUtil.ItemCallback<DepotQuote>() {
        /**
         * Checks if two [DepotQuote]s have the same id.
         *
         * @param oldItem The old [DepotQuote].
         * @param newItem The new [DepotQuote].
         * @return true if both [DepotQuote]s have the same id or false otherwise.
         */
        override fun areItemsTheSame(oldItem: DepotQuote, newItem: DepotQuote) =
            oldItem.id == newItem.id

        /**
         * Checks if two [DepotQuote]s are equal.
         *
         * @param oldItem The old [DepotQuote].
         * @param newItem The new [DepotQuote].
         * @return true if both [DepotQuote]s are equal or false otherwise.
         */
        override fun areContentsTheSame(oldItem: DepotQuote, newItem: DepotQuote) =
            oldItem == newItem
    }

    /**
     * Creates a [ViewHolder] by inflating a CardDepotQuoteBinding.
     *
     * @param parent The parent of the inflated CardDepotQuoteBinding.
     * @param viewType Ignored.
     * @return The created [ViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardDepotQuoteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    /**
     * Binds a [DepotQuote] to a [ViewHolder].
     *
     * @param holder The target [ViewHolder].
     * @param position The position of the [DepotQuote] in this adapter's list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quotePurchase: DepotQuote = getItem(position)
        holder.bind(quotePurchase)
    }
}
