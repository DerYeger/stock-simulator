package de.uniks.codliners.stock_simulator.ui.achievements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.CardAchievementBinding
import de.uniks.codliners.stock_simulator.domain.Achievement

/**
 * The adapter that inserts [Achievement]s into a [RecyclerView](https://developer.android.com/jetpack/androidx/releases/recyclerview).
 *
 * @author Lucas Held
 */
class AchievementsAdapter : ListAdapter<Achievement, AchievementsAdapter.ViewHolder>(DiffCallback) {

    /**
     * [RecyclerView.ViewHolder](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder) for CardAchievementBinding.
     *
     * @property binding The CardAchievementBinding.
     *
     * @author Lucas Held
     */
    inner class ViewHolder(private val binding: CardAchievementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a [Achievement] to the CardAchievementBinding.
         *
         * @param achievement The [Achievement] that will be bound to the CardAchievementBinding.
         */
        fun bind(achievement: Achievement) {
            binding.achievement = achievement
            binding.expanded = false
            binding.executePendingBindings()
        }
    }

    /**
     * [DiffUtil.ItemCallback](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback) for [Achievement]s.
     *
     * @author Lucas Held
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Achievement>() {

        /**
         * Checks if two [Achievement]s have the same id.
         *
         * @param oldItem The old [Achievement].
         * @param newItem The new [Achievement].
         * @return true if both [Achievement]s have the same id or false otherwise.
         */
        override fun areItemsTheSame(oldItem: Achievement, newItem: Achievement) =
            oldItem.name == newItem.name

        /**
         * Checks if two [Achievement]s are equal.
         *
         * @param oldItem The old [Achievement].
         * @param newItem The new [Achievement].
         * @return true if both [Achievement]s are equal or false otherwise.
         */
        override fun areContentsTheSame(oldItem: Achievement, newItem: Achievement) =
            oldItem == newItem
    }

    /**
     * Creates a [ViewHolder] by inflating a CardAchievementBinding.
     *
     * @param parent The parent of the inflated CardAchievementBinding.
     * @param viewType Ignored.
     * @return The created [ViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardAchievementBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    /**
     * Binds a [Achievement] to a [ViewHolder].
     *
     * @param holder The target [ViewHolder].
     * @param position The position of the [Achievement] in this adapter's list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement: Achievement = getItem(position)
        holder.bind(achievement)
    }
}