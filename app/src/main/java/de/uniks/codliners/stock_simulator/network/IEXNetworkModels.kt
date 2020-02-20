package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.Symbol

@JsonClass(generateAdapter = true)
data class NetworkSymbol(
    val symbol: String,
    val name: String,
    val type: String,
    val currency: String
)

fun NetworkSymbol.asDomainSymbol() = Symbol(
    symbol = symbol,
    name = name,
    type = if (type == "crypto") Symbol.Type.CRYPTO else Symbol.Type.SHARE
)

fun List<NetworkSymbol>.asDomainSymbols() = map { it.asDomainSymbol() }.toTypedArray()

@JsonClass(generateAdapter = true)
data class NetworkQuote(
    val symbol: String,
    val companyName: String?,
    val sector: String?,
    val latestPrice: Double,
    val change: Double?
)

fun NetworkQuote.asDomainQuote(type: Symbol.Type) = Quote(
    symbol = symbol,
    type = type,
    companyName = companyName,
    sector = sector,
    latestPrice = latestPrice,
    change = change
)
