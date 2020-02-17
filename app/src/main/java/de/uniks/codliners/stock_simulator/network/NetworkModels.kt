package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.domain.IssueType
import de.uniks.codliners.stock_simulator.domain.Symbol

@JsonClass(generateAdapter = true)
data class SymbolsResponse(
    @Json(name = "symbol")
    val symbol: String,
    @Json(name = "exchange")
    val exchange: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "date")
    val date: String,
    @Json(name = "isEnabled")
    val isEnabled: Boolean,
    @Json(name = "type")
    val type: IssueType,
    @Json(name = "region")
    val region: String,
    @Json(name = "currency")
    val currency: String,
    @Json(name = "iexId")
    val iexId: String
)

fun SymbolsResponse.toDomainModel(): Symbol = Symbol(
    symbol = symbol,
    exchange = exchange,
    name = name,
    date = date,
    isEnabled = isEnabled,
    type = type,
    region = region,
    currency = currency,
    iexId = iexId
)
