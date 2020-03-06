package de.uniks.codliners.stock_simulator.ui.news

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.databinding.CardNewsBinding
import de.uniks.codliners.stock_simulator.domain.News
import java.text.SimpleDateFormat
import java.util.*


class NewsAdapter(
    locale: Locale
) : ListAdapter<News, NewsAdapter.ViewHolder>(DiffCallback) {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", locale)

    inner class ViewHolder(private val binding: CardNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(news: News) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = news.datetime
            binding.news = news
            Picasso.get().load(news.image).into(binding.newsImage)
            binding.dateString = dateFormatter.format(calendar.time)
            binding.newsOpenArticle.setOnClickListener { view ->
                kotlin.run {
                    try {
                        startActivity(
                            view.context,
                            Intent(Intent.ACTION_VIEW, Uri.parse(news.url)),
                            null
                        )
                    } catch (e: ActivityNotFoundException) {
                        Snackbar.make(
                            view,
                            R.string.invalid_news_url,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            binding.expanded = false
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
            CardNewsBinding.inflate(
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