package de.uniks.codliners.stock_simulator.ui.achievements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.CardAchievementBinding
import de.uniks.codliners.stock_simulator.domain.Achievement

class AchievementsAdapter : ListAdapter<Achievement, AchievementsAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: CardAchievementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(achievement: Achievement) {
            binding.achievement = achievement
            binding.expanded = false
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Achievement>() {
        override fun areItemsTheSame(oldItem: Achievement, newItem: Achievement) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Achievement, newItem: Achievement) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardAchievementBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement: Achievement = getItem(position)
        holder.bind(achievement)
    }
}