package de.uniks.codliners.stock_simulator.database

import androidx.room.TypeConverter
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
}