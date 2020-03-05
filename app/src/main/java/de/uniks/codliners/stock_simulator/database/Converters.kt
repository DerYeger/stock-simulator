package de.uniks.codliners.stock_simulator.database

import androidx.room.TypeConverter
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.domain.TransactionType

class Converters {

    @TypeConverter
    fun fromBooleanToInt(enabled: Boolean): Int {
        return when(enabled) {
            true -> 1
            false -> 0
        }
    }

    @TypeConverter
    fun fromIntToBoolean(booleanInt: Int): Boolean {
        return when(booleanInt) {
            0 -> false
            else -> true
        }
    }

    @TypeConverter
    fun fromStringToSymbolType(typeString: String): Symbol.Type {
        return Symbol.Type.valueOf(typeString)
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
    fun fromTransactionTypeToString(type: TransactionType): String {
        return type.toString()
    }
}