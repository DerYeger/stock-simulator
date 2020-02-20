package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.Symbol
import java.util.*

@JsonClass(generateAdapter = true)
data class NetworkSymbol(
    val symbol: String,
    val name: String,
    val type: String,
    val currency: String
)

fun NetworkSymbol.asDomainSymbol() = Symbol(
    id = symbol.toUpperCase(Locale.ROOT),
    symbol = symbol,
    name = name,
    type = if (type == "crypto") Symbol.Type.CRYPTO else Symbol.Type.SHARE
)

fun List<NetworkSymbol>.asDomainSymbols() = map { it.asDomainSymbol() }.toTypedArray()

@JsonClass(generateAdapter = true)
data class NetworkQuote(
    val symbol: String,
    val companyName: String,
    val latestPrice: Double,
    val change: Double?
)

fun NetworkQuote.asDomainQuote() = Quote(
    id = symbol,
    symbol = symbol,
    type = Symbol.Type.SHARE,
    name = companyName,
    latestPrice = latestPrice,
    change = change
)
