package de.uniks.codliners.stock_simulator.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.DepotShareViewBinding
import de.uniks.codliners.stock_simulator.domain.Share

class DepotShareRecyclerViewAdapter :
    ListAdapter<Share, DepotShareRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: DepotShareViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(share: Share) {
            binding.share = share
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Share>() {
        override fun areItemsTheSame(oldItem: Share, newItem: Share) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Share, newItem: Share) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DepotShareViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val share: Share = getItem(position)
        holder.bind(share)
    }
}