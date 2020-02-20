package de.uniks.codliners.stock_simulator.background

import de.uniks.codliners.stock_simulator.domain.Symbol

class Constants {
    companion object {
        const val SYMBOL_KEY = "SYMBOL_KEY"
        const val TYPE_KEY = "TYPE_KEY"
        const val BUY_AMOUNT_KEY = "BUY_AMOUNT_KEY"
        const val THRESHOLD_BUY_KEY = "THRESHOLD_BUY_KEY"
        const val THRESHOLD_SELL_KEY = "THRESHOLD_SELL_KEY"
        const val DOUBLE_DEFAULT = -1.0
        val TYPE_DEFAULT = Symbol.Type.SHARE
    }
}