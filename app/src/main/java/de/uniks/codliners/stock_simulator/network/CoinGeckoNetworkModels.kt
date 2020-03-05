package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.domain.HistoricalPrice
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.Symbol
import java.util.*

@JsonClass(generateAdapter = true)
data class CoinGeckoSymbol(
    val id: String,
    val symbol: String,
    val name: String
)

fun CoinGeckoSymbol.asDomainSymbol() = Symbol(
    id = id,
    symbol = symbol.toUpperCase(Locale.ROOT),
    name = name,
    type = Symbol.Type.CRYPTO
)

fun List<CoinGeckoSymbol>.asDomainSymbols() = map { it.asDomainSymbol() }.toTypedArray()

@JsonClass(generateAdapter = true)
data class CoinGeckoQuote(
    val id: String,
    val symbol: String,
    val name: String,
    @Json(name = "market_data")
    val marketData: CoinGeckoMarketData
)

@JsonClass(generateAdapter = true)
data class CoinGeckoMarketData(
    @Json(name = "current_price")
    val currentPrices: Map<String, Double>
)

fun CoinGeckoQuote.asDomainQuote() = Quote(
    id = id,
    symbol = symbol.toUpperCase(Locale.ROOT),
    type = Symbol.Type.CRYPTO,
    name = name,
    latestPrice = marketData.currentPrices["usd"] ?: 0.0,
    change = 0.0
)

@JsonClass(generateAdapter = true)
data class CoinGeckoMarketChart(
    val prices: List<List<Any>>
)

private fun List<Any>.asHistoricalPrice(id: String) = HistoricalPrice(
    id = id,
    date = (get(0) as Double).toLong(),
    price = get(1) as Double
)

fun CoinGeckoMarketChart.asHistoricalPrices(id: String) =
    prices.map { it.asHistoricalPrice(id) }
