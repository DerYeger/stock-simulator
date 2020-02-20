package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.JsonClass
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
