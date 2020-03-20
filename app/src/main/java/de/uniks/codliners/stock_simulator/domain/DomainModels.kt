package de.uniks.codliners.stock_simulator.domain

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.domain.Symbol.Type.CRYPTO
import de.uniks.codliners.stock_simulator.domain.Symbol.Type.SHARE
import de.uniks.codliners.stock_simulator.domain.TransactionType.BUY
import de.uniks.codliners.stock_simulator.domain.TransactionType.SELL
import kotlinx.android.parcel.Parcelize

/**
 * Achievement database entity.
 *
 * @property name String resource that holds the name of the achievement.
 * @property description String resource that holds the description of the achievement.
 * @property reached Boolean that marks if the achievement is reached.
 * @property timestamp Millisecond epoch of reached time of the achievement.
 * @property displayed Boolean that marks if the achievement has displayed to the user.
 *
 * @author Lucas Held
 */
@Entity
data class Achievement(
    @PrimaryKey
    @StringRes
    val name: Int,
    @StringRes
    val description: Int,
    val reached: Boolean = false,
    val timestamp: Long? = null,  // null if achievement not reached otherwise the reached timestamp
    val displayed: Boolean = false
)

/**
 * Balance database entity.
 *
 * @property value The value of the balance.
 * @property timestamp The timestamp of the balance.
 *
 * @author Jan Müller
 */
@Entity
data class Balance(
    val value: Double,
    @PrimaryKey
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Depot quote domain model entity used to aggregate all depot quote purchases of one asset.
 *
 * @property id The id of the asset.
 * @property symbol The symbol of the asset.
 * @property type The asset's type.
 * @property amount The amount of the asset in the depot.
 * @property buyingPrice The average price that was paid for the asset.
 *
 * @author Juri Lozowoj
 */
data class DepotQuote(
    val id: String,
    val symbol: String,
    val type: Symbol.Type,
    val amount: Double,
    val buyingPrice: Double
)

/**
 * Depot quote purchase database entity.
 *
 * @property primaryKey An automatically generated primary key.
 * @property id The id of the asset.
 * @property symbol The symbol of the asset.
 * @property type The asset's type.
 * @property amount The amount of this purchase.
 * @property buyingPrice The purchase price that was paid for the asset.
 *
 * @author Juri Lozowoj
 */
@Entity
data class DepotQuotePurchase(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val id: String,
    val symbol: String,
    val type: Symbol.Type,
    val amount: Double,
    val buyingPrice: Double
)

/**
 * Depot value database entity.
 *
 * @property value The value of the depot.
 * @property timestamp The timestamp of the depot value.
 *
 * @author Juri Lozowoj
 */
@Entity
data class DepotValue(
    val value: Double,
    @PrimaryKey
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * The historical price database entity.
 *
 * @property primaryKey An automatically generated primary key.
 * @property id The id of the asset.
 * @property date The date when this share had this price.
 * @property price The price the share had on that date.
 *
 * @author Juri Lozowoj
 */
@Entity
data class HistoricalPrice(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val id: String,
    val date: Long,
    val price: Double
)

/**
 * News database entity and JSON adapter source.
 *
 * @property primaryKey An automatically generated primary key.
 * @property datetime Millisecond epoch of time of article.
 * @property headline The article's headline.
 * @property source Source of the news article. Make sure to always attribute the source.
 * @property url URL to IEX Cloud for associated news image.
 * @property summary The requested news article in short.
 * @property related Comma-delimited list of tickers associated with this news article. Not all tickers are available on the API. Make sure to check against available [ref-data](https://iexcloud.io/docs/api/#symbols).
 * @property image URL to IEX Cloud for associated news image.
 * @property lang Language of the source article.
 * @property hasPaywall Whether the news source has a paywall.
 *
 * @author Jonas Thelemann
 */
@Entity
@JsonClass(generateAdapter = true)
data class News(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val datetime: Long,
    val headline: String,
    val source: String,
    val url: String,
    val summary: String,
    val related: String,
    val image: String,
    val lang: String,
    val hasPaywall: Boolean
)

/**
 * Quote database entity.
 *
 * @property id The id of the asset.
 * @property symbol The symbol of the asset.
 * @property type The asset's type.
 * @property name The name of the asset.
 * @property latestPrice The latest price of the asset.
 * @property change The price change of the asset.
 *
 * @author Jan Müller
 */
@Entity
data class Quote(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val type: Symbol.Type,
    val name: String,
    val latestPrice: Double,
    val change: Double?
)

/**
 * StockbrotQuote database entity.
 *
 * @property id Primary key, that identifies the stockbrotQuote.
 * @property symbol Symbol of the stockbrotQuote.
 * @property type Type [Symbol.Type] of the stockbrotQuote.
 * @property limitedBuying Boolean that indicates if a buy limit is set to the stockbrotQuote.
 * @property buyLimit Value of the buy limit.
 * @property maximumBuyPrice Value of the maximum buy price.
 * @property minimumSellPrice Value of the minimum sell price.
 *
 * @author Lucas Held
 * @author Jan Müller
 */
@Entity
data class StockbrotQuote(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val type: Symbol.Type,
    val limitedBuying: Boolean,
    val buyLimit: Double,
    val maximumBuyPrice: Double,
    val minimumSellPrice: Double
)

/**
 * Symbol database entity.
 *
 * @property id The id of the asset.
 * @property symbol The symbol of the asset.
 * @property name The name of the asset.
 * @property type The asset's type.
 *
 * @author Jan Müller
 */
@Entity
data class Symbol(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val name: String,
    val type: Type
) {
    /**
     * Enumeration class for the [Type] of a [Symbol].
     * The possible types of a transaction are [SHARE] and [CRYPTO].
     *
     * @author Jan Müller
     */
    @Parcelize
    enum class Type : Parcelable {
        /**
         * SHARE indicates that the asset is a share.
         */
        SHARE,

        /**
         * CRYPTO indicates that the asset is a crypto currency.
         */
        CRYPTO
    }

    /**
     * Utility class used for filtering [Symbol]s.
     *
     * @property symbolQuery The query for asset symbols.
     * @property nameQuery The query for asset names.
     * @property type The type of assets.
     *
     * @author Jan Müller
     */
    data class Filter(
        val symbolQuery: String,
        val nameQuery: String,
        val type: Type?
    ) {
        constructor(query: String, type: Type?) : this(
            symbolQuery = "$query%",
            nameQuery = "%$query%",
            type = type
        )
    }
}

/**
 * The transaction database entity used to store purchases and sales of assets.
 *
 * @property primaryKey An automatically generated primary key.
 * @property id The id of the asset.
 * @property symbol The symbol of the asset.
 * @property type The asset's type.
 * @property amount The amount of this purchase or sale.
 * @property date The date when this share had this price.
 * @property price The price at which the asset was bought or sold.
 * @property transactionCosts The possibly paid transaction costs for sales.
 * @property cashflow The total amount of money issued or received.
 * @property transactionType The type of a transaction.
 * @property date The date on which the transaction took place.
 * @property result The profit or loss on a sale.
 *
 * @author Juri Lozowoj
 */
@Entity
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val id: String,
    val symbol: String,
    val type: Symbol.Type,
    val amount: Double,
    val price: Double,
    val transactionCosts: Double,
    val cashflow: Double,
    val transactionType: TransactionType,
    val date: Long,
    val result: Double?
)

/**
 * Enumeration class for the [TransactionType] of a [Transaction].
 * The possible types of a transaction are [BUY] and [SELL].
 *
 * @author Juri Lozowoj
 */
enum class TransactionType {
    /**
     * BUY indicates that the transaction was a purchase.
     */
    BUY,

    /**
     * SELL indicates that the transaction was a sale.
     */
    SELL
}
