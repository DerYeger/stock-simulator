package de.uniks.codliners.stock_simulator.database

import androidx.room.TypeConverter
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.domain.TransactionType

/**
 * Converter functions for object types that are not natively supported by SQLite
 *
 * @author Jan Müller
 * @author Juri Lozowoj
 * @author Lucas Held
 *
 */
class Converters {

    /**
     * Converts a [Boolean] value into an [Int] value
     *
     * @param enabled input [Boolean] value
     * @return output [Int] value
     *
     * @author Lucas Held
     */
    @TypeConverter
    fun fromBooleanToInt(enabled: Boolean): Int {
        return when (enabled) {
            true -> 1
            false -> 0
        }
    }

    /**
     * Converts a [Int] value into an [Boolean] value
     *
     * @param booleanInt input [Int] value
     * @return output [Boolean] value
     *
     * @author Lucas Held
     */
    @TypeConverter
    fun fromIntToBoolean(booleanInt: Int): Boolean {
        return when (booleanInt) {
            0 -> false
            else -> true
        }
    }

    /**
     * Converts [String] to [Symbol.Type].
     *
     * @param typeString The input [String].
     * @return The resulting [Symbol.Type].
     *
     * @author Jan Müller
     */
    @TypeConverter
    fun fromStringToSymbolType(typeString: String): Symbol.Type = Symbol.Type.valueOf(typeString)

    /**
     * Converts [Symbol.Type] to [String].
     *
     * @param type The input [Symbol.Type].
     * @return The resulting [String].
     *
     * @author Jan Müller
     */
    @TypeConverter
    fun fromSymbolTypeToString(type: Symbol.Type): String = type.toString()


    @TypeConverter
    fun fromStringToTransactionType(typeString: String): TransactionType {
        return when (typeString) {
            "BUY" -> TransactionType.BUY
            else -> TransactionType.SELL
        }
    }

    @TypeConverter
    fun fromTransactionTypeToString(type: TransactionType): String {
        return type.toString()
    }
}
