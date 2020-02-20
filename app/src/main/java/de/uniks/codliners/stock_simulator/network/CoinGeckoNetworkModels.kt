package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.database.HistoricalPrice
import de.uniks.codliners.stock_simulator.domain.Symbol
import java.util.*

@JsonClass(generateAdapter = true)
data class CryptoNetworkSymbol(
    val id: String,
    val symbol: String,
    val name: String
)

fun CryptoNetworkSymbol.asDomainSymbol() = Symbol(
    symbol = symbol.toUpperCase(Locale.ROOT),
    name = id,
    type = Symbol.Type.CRYPTO
)

fun List<CryptoNetworkSymbol>.asDomainSymbols() = map { it.asDomainSymbol() }.toTypedArray()

@JsonClass(generateAdapter = true)
data class CoinGeckoMarketChart(
    val prices: List<Pair<Long, Double>>
)

private fun Pair<Long, Double>.asHistoricalPrice(symbol: String) = HistoricalPrice(
    symbol = symbol,
    date = first,
    price = second
)

fun CoinGeckoMarketChart.toHistoricalPrices(symbol: String) = prices.map { it.asHistoricalPrice(symbol) }
