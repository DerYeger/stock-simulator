package de.uniks.codliners.stock_simulator.domain

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
    AD, RE, CE, SI, LP, CS, ET, WT, OEF, CEF, PS, UT, STRUCT
}
