package de.uniks.codliners.stock_simulator.domain

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResult(
    val symbol: String,
    val securityName: String,
    val securityType: String,
    val exchange: String
)

@JsonClass(generateAdapter = true)
data class Symbol(
    val symbol: String,
    val exchange: String,
    val name: String,
    val date: String,
    val isEnabled: Boolean,
    val type: IssueType,
    val region: String,
    val currency: String,
    val iexId: String
)

enum class IssueType{
    ad, re, ce, si, lp, cs, et, wt, rt, oef, cef, ps, ut, struct, temp
}
