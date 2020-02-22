package de.uniks.codliners.stock_simulator.ui.news

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.uniks.codliners.stock_simulator.databinding.NewsCardBinding
import de.uniks.codliners.stock_simulator.domain.News
import java.text.SimpleDateFormat
import java.util.*


class NewsAdapter(
    locale: Locale
) : ListAdapter<News, NewsAdapter.ViewHolder>(DiffCallback) {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy hh:mm", locale)

    inner class ViewHolder(private val binding: NewsCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(news: News) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = news.datetime
            binding.news = news
            Picasso.get().load(news.image).into(binding.newsImage)
            binding.dateString = dateFormatter.format(calendar.time)
            binding.newsOpenArticle.setOnClickListener { view ->
                kotlin.run {
                    startActivity(
                        view.context,
                        Intent(Intent.ACTION_VIEW, Uri.parse(news.url)),
                        null
                    )
                }
            }
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