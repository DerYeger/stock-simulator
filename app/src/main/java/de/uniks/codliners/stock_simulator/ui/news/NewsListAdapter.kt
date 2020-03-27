package de.uniks.codliners.stock_simulator.ui.news

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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

/**
 * [ListAdapter](https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter) for [News].
 *
 * @param locale The locale for date formatting.
 *
 * @author Jonas Thelemann
 */
class NewsListAdapter(
    locale: Locale
) : ListAdapter<News, NewsListAdapter.ViewHolder>(DiffCallback) {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", locale)

    /**
     * The [RecyclerView](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView)'s ViewHolder.
     *
     * @property binding The card's binding.
     */
    inner class ViewHolder(private val binding: CardNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Bind a news object to the card.
         *
         * @param news The news object to bind.
         */
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

    /**
     * Companion object that allows comparison of news objects.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<News>() {

        /**
         * Compares two news objects for equality by checking their "url" property.
         *
         * @param oldItem The comparisons left operand.
         * @param newItem The comparisons right operand.
         */
        override fun areItemsTheSame(oldItem: News, newItem: News) =
            oldItem.url == newItem.url

        /**
         * Compares two news objects for equality.
         *
         * @param oldItem The comparisons left operand.
         * @param newItem The comparisons right operand.
         */
        override fun areContentsTheSame(oldItem: News, newItem: News) =
            oldItem == newItem
    }

    /**
     * The "onCreate" callback.
     *
     * @param parent The ViewHolder's parent.
     * @param viewType The ViewHolder's view type.
     * @return The ViewHolder with inflated CardNewsBinding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardNewsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    /**
     * The "onBind" callback.
     *
     * @param holder The ViewHolder.
     * @param position The news object's position.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val news: News = getItem(position)
        holder.bind(news)
    }
}