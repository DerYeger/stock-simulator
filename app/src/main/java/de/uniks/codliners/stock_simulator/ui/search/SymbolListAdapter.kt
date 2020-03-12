package de.uniks.codliners.stock_simulator.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.CardSymbolBinding
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.ui.OnClickListener
import de.uniks.codliners.stock_simulator.ui.search.SymbolListAdapter.ViewHolder

/**
 * [ListAdapter](https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter) for [Symbol]s.
 *
 * @property onClickListener The [OnClickListener] for [ViewHolder]s.
 *
 * @author Jan Müller
 */
class SymbolListAdapter(private val onClickListener: OnClickListener<Symbol>) :
    ListAdapter<Symbol, SymbolListAdapter.ViewHolder>(DiffCallback) {

    /**
     * [RecyclerView.ViewHolder](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder) for CardSymbolBindings.
     *
     * @property binding The CardSymbolBinding.
     *
     * @author Jan Müller
     */
    inner class ViewHolder(private val binding: CardSymbolBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a [Symbol] and [OnClickListener] to the CardSymbolBinding.
         *
         * @param symbol The [Symbol] that will be bound to the CardSymbolBinding.
         */
        fun bind(symbol: Symbol) {
            binding.symbol = symbol
            binding.onClickListener = onClickListener
            binding.executePendingBindings()
        }
    }

    /**
     * [DiffUtil.ItemCallback](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback) for [Symbol]s.
     *
     * @author Jan Müller
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Symbol>() {

        /**
         * Checks if two [Symbol]s have the same id.
         *
         * @param oldItem The old [Symbol].
         * @param newItem The new [Symbol].
         * @return true if both [Symbol]s have the same id or false otherwise.
         */
        override fun areItemsTheSame(oldItem: Symbol, newItem: Symbol): Boolean =
            oldItem.id == newItem.id

        /**
         * Checks if two [Symbol]s are equal.
         *
         * @param oldItem The old [Symbol].
         * @param newItem The new [Symbol].
         * @return true if both [Symbol]s are equal or false otherwise.
         */
        override fun areContentsTheSame(oldItem: Symbol, newItem: Symbol) =
            oldItem == newItem
    }

    /**
     * Creates a [ViewHolder] by inflating a CardSymbolBinding.
     *
     * @param parent The parent of the inflated CardSymbolBinding.
     * @param viewType Ignored.
     * @return The created [ViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            CardSymbolBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    /**
     * Binds a [Symbol] to a [ViewHolder].
     *
     * @param holder The target [ViewHolder].
     * @param position The position of the [Symbol] in this adapter's list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val symbol: Symbol = getItem(position)
        holder.bind(symbol)
    }
}
