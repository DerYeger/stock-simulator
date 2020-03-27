package de.uniks.codliners.stock_simulator.network

import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.domain.HistoricalPrice
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.Symbol
import java.text.SimpleDateFormat
import java.util.*

/**
 * Symbol information of an IEX share.
 *
 * @property symbol The symbol of the share.
 * @property name The name of the share.
 * @property type The type of the share.
 *
 * @author Jan Müller
 */
@JsonClass(generateAdapter = true)
data class IEXSymbol(
    val symbol: String,
    val name: String,
    val type: String
)

/**
 * Transforms an [IEXSymbol] to an equivalent [Symbol].
 *
 * @receiver The [IEXSymbol] that will be transformed.
 * @return The equivalent [Symbol].
 *
 * @author Jan Müller
 */
fun IEXSymbol.asDomainSymbol() = Symbol(
    id = symbol.toUpperCase(Locale.ROOT),
    symbol = symbol,
    name = name,
    type = Symbol.Type.SHARE
)

/**
 * Transforms an [IEXSymbol] [List] to an equivalent [Symbol] [Array].
 *
 * @receiver The [IEXSymbol] [List] that will be transformed.
 * @return The equivalent [Symbol] [Array].
 *
 * @author Jan Müller
 */
fun List<IEXSymbol>.asDomainSymbols() = map { it.asDomainSymbol() }.toTypedArray()

/**
 * Quote information of an IEX share.
 *
 * @property symbol The symbol of the share.
 * @property companyName The name of the company behind the share.
 * @property latestPrice The latest price of the share.
 * @property change The current price change of the share.
 *
 * @author Jan Müller
 */
@JsonClass(generateAdapter = true)
data class IEXQuote(
    val symbol: String,
    val companyName: String,
    val latestPrice: Double,
    val change: Double?
)

/**
 * Transforms an [IEXQuote] to an equivalent [Quote].
 *
 * @receiver The [IEXQuote] that will be transformed.
 * @return The equivalent [Quote].
 *
 * @author Jan Müller
 */
fun IEXQuote.asDomainQuote() = Quote(
    id = symbol,
    symbol = symbol,
    type = Symbol.Type.SHARE,
    name = companyName,
    latestPrice = latestPrice,
    change = change
)

/**
 * Price data point for an IEX share.
 *
 * @property date The timestamp of the data point.
 * @property close The close value of the data point.
 * @property changeOverTime The change over time of the data point.
 * @property change The price change of the data point.
 * @property changePercent The change percentage of the data point.
 *
 * @author Juri Lozowoj
 */
@JsonClass(generateAdapter = true)
data class IEXHistoricalPrice(
    @PrimaryKey
    val date: String,
    val close: Double,
    val changeOverTime: Double,
    val change: Double,
    val changePercent: Double
)

/**
 * Transforms an [IEXHistoricalPrice] to an equivalent [HistoricalPrice].
 *
 * @receiver The [CoinGeckoMarketChart] that will be transformed.
 * @param id The id of the related CoinGecko cryptocurrency.
 * @return The equivalent [HistoricalPrice] [List].
 *
 * @author Juri Lozowoj
 */
fun IEXHistoricalPrice.asDomainHistoricalPrice(id: String) = HistoricalPrice(
    id = id,
    date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(date)!!.time,
    price = this.close
)

/**
 * Transforms an [IEXHistoricalPrice] [List] to an equivalent [HistoricalPrice] [List].
 *
 * @receiver The [IEXHistoricalPrice] [List] that will be transformed.
 * @return The equivalent [HistoricalPrice] [List].
 *
 * @author Juri Lozowoj
 */
fun List<IEXHistoricalPrice>.asDomainHistoricalPrices(id: String) =
    map { it.asDomainHistoricalPrice(id) }
