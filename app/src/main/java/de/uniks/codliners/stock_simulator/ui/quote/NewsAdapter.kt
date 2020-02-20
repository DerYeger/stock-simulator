package de.uniks.codliners.stock_simulator.ui.quote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.uniks.codliners.stock_simulator.databinding.NewsCardBinding
import de.uniks.codliners.stock_simulator.domain.News
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(
    locale: Locale
) : ListAdapter<News, NewsAdapter.ViewHolder>(
    DiffCallback
) {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy hh:mm", locale)

    inner class ViewHolder(private val binding: NewsCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(news: News) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = news.datetime
            binding.news = news
            binding.dateString = dateFormatter.format(calendar.time)
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News) =
            oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: News, newItem: News) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            NewsCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val news: News = getItem(position)
        holder.bind(news)
    }
}