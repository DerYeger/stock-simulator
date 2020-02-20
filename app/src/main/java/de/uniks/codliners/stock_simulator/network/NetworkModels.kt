package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.Symbol

@JsonClass(generateAdapter = true)
data class NetworkSymbol(
    val symbol: String,
    val name: String,
    val date: String,
    val isEnabled: Boolean,
    val type: String,
    val region: String,
    val currency: String
)

fun NetworkSymbol.asDomainSymbol() = Symbol(
    symbol = symbol,
    name = name,
    date = date,
    isEnabled = isEnabled,
    type = if (type == "crypto") Symbol.Type.CRYPTO else Symbol.Type.SHARE,
    region = region,
    currency = currency
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

fun NetworkQuote.asDomainQuote() = Quote(
    symbol = symbol,
    type = if (sector == "cryptocurrency") Symbol.Type.CRYPTO else Symbol.Type.SHARE,
    companyName = companyName,
    sector = sector,
    latestPrice = latestPrice,
    change = change
)
