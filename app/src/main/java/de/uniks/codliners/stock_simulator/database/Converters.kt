package de.uniks.codliners.stock_simulator.database

import androidx.room.TypeConverter
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.domain.TransactionType


class Converters {
    @TypeConverter
    fun fromTransactionTypeToString(type: TransactionType): String {
        return type.toString()
    }

    @TypeConverter
    fun fromStringToTransactionType(typeString: String): TransactionType {
        return when(typeString) {
            "BUY" -> TransactionType.BUY
            else -> TransactionType.SELL
        }
    }

    @TypeConverter
    fun fromSymbolTypeToString(type: Symbol.Type): String {
        return type.toString()
    }

    @TypeConverter
    fun fromStringToSymbolType(typeString: String): Symbol.Type {
        return Symbol.Type.valueOf(typeString)
    }
}