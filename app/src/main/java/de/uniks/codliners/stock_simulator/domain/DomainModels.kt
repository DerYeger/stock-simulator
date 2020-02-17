package de.uniks.codliners.stock_simulator.domain

data class Share(
    val id: String,
    val name: String,
    val price: Double,
    val gap: Double,
    val gapPercent: Double
)
