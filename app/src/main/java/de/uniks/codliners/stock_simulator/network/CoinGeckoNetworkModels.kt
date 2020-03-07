package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.domain.HistoricalPrice
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.Symbol
import java.util.*

/**
 * Symbol information of a CoinGecko cryptocurrency.
 *
 * @property id The CoinGecko ID of the cryptocurrency.
 * @property symbol The symbol of the cryptocurrency.
 * @property name The name of the cryptocurrency.
 *
 * @author Jan Müller
 */
@JsonClass(generateAdapter = true)
data class CoinGeckoSymbol(
    val id: String,
    val symbol: String,
    val name: String
)

/**
 * Transforms a [CoinGeckoSymbol] to an equivalent [Symbol].
 *
 * @receiver The [CoinGeckoSymbol] that will be transformed.
 * @return The equivalent [Symbol].
 *
 * @author Jan Müller
 */
fun CoinGeckoSymbol.asDomainSymbol() = Symbol(
    id = id,
    symbol = symbol.toUpperCase(Locale.ROOT),
    name = name,
    type = Symbol.Type.CRYPTO
)

/**
 * Transforms a [CoinGeckoSymbol] [List] to an equivalent [Symbol] [Array].
 *
 * @receiver The [CoinGeckoSymbol] [List] that will be transformed.
 * @return The equivalent [Symbol] [Array].
 *
 * @author Jan Müller
 */
fun List<CoinGeckoSymbol>.asDomainSymbols() = map { it.asDomainSymbol() }.toTypedArray()

/**
 * Quote information of a CoinGecko cryptocurrency.
 *
 * @property id The CoinGecko ID of the cryptocurrency.
 * @property symbol The symbol of the cryptocurrency.
 * @property name The name of the cryptocurrency.
 * @property marketData The market data of the cryptocurrency.
 *
 * @author Jan Müller
 */
@JsonClass(generateAdapter = true)
data class CoinGeckoQuote(
    val id: String,
    val symbol: String,
    val name: String,
    @Json(name = "market_data")
    val marketData: CoinGeckoMarketData
)

/**
 * Market data of a CoinGecko cryptocurrency.
 *
 * @property currentPrices A [Map] containing currency names and their respective value.
 *
 * @author Jan Müller
 */
@JsonClass(generateAdapter = true)
data class CoinGeckoMarketData(
    @Json(name = "current_price")
    val currentPrices: Map<String, Double>
)

/**
 * Transforms a [CoinGeckoQuote] to an equivalent [Quote].
 *
 * @receiver The [CoinGeckoQuote] that will be transformed.
 * @return The equivalent [Quote].
 *
 * @author Jan Müller
 */
fun CoinGeckoQuote.asDomainQuote() = Quote(
    id = id,
    symbol = symbol.toUpperCase(Locale.ROOT),
    type = Symbol.Type.CRYPTO,
    name = name,
    latestPrice = marketData.currentPrices["usd"] ?: 0.0,
    change = 0.0
)

/**
 * Timestamp and price data points of a CoinGecko cryptocurrency.
 *
 * @property prices A [List] containing all the data points.
 *
 * @author Jan Müller
 */
@JsonClass(generateAdapter = true)
data class CoinGeckoMarketChart(
    val prices: List<List<Any>>
)

/**
 * Transforms a [CoinGeckoMarketChart] to an equivalent [List] of [HistoricalPrice]s.
 *
 * @receiver The [CoinGeckoMarketChart] that will be transformed.
 * @param id The id of the related CoinGecko cryptocurrency.
 * @return The equivalent [HistoricalPrice] [List].
 *
 * @author Jan Müller
 */
fun CoinGeckoMarketChart.asHistoricalPrices(id: String) =
    prices.map { dataPoint ->
        HistoricalPrice(
            id = id,
            date = (dataPoint[0] as Double).toLong(),
            price = dataPoint[1] as Double
        )
    }
