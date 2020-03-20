package de.uniks.codliners.stock_simulator.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import de.uniks.codliners.stock_simulator.domain.*
import de.uniks.codliners.stock_simulator.domain.Transaction

/**
 * The [Room](https://developer.android.com/jetpack/androidx/releases/room) database with all its [Dao](https://developer.android.com/reference/androidx/room/Dao)s.
 */
@Database(
    entities = [Symbol::class, DepotQuotePurchase::class, News::class, Transaction::class, Quote::class, Balance::class, HistoricalPrice::class, StockbrotQuote::class, DepotValue::class, Achievement::class],
    version = 27,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StockAppDatabase : RoomDatabase() {
    /**
     * The database's [Symbol] [Dao](https://developer.android.com/reference/androidx/room/Dao).
     */
    abstract val symbolDao: SymbolDao

    /**
     * The database's [Quote] [Dao](https://developer.android.com/reference/androidx/room/Dao).
     */
    abstract val quoteDao: QuoteDao

    /**
     * The database's [News] [Dao](https://developer.android.com/reference/androidx/room/Dao).
     */
    abstract val newsDao: NewsDao

    /**
     * The database's [Transaction] [Dao](https://developer.android.com/reference/androidx/room/Dao).
     */
    abstract val transactionDao: TransactionDao

    /**
     * The database's [Balance], [DepotQuotePurchase] and [DepotValue]  [Dao](https://developer.android.com/reference/androidx/room/Dao).
     */
    abstract val accountDao: AccountDao

    /**
     * The database's [StockbrotQuote] [Dao](https://developer.android.com/reference/androidx/room/Dao).
     */
    abstract val stockbrotDao: StockbrotDao

    /**
     * The database's [HistoricalPrice] [Dao](https://developer.android.com/reference/androidx/room/Dao).
     */
    abstract val historicalDao: HistoricalPriceDao

    /**
     * The database's [Achievement] [Dao](https://developer.android.com/reference/androidx/room/Dao).
     */
    abstract val achievementDao: AchievementsDao
}

private lateinit var INSTANCE: StockAppDatabase

/**
 * Creates or returns this app's [StockAppDatabase].
 *
 * @param context The context of the app.
 * @return The [StockAppDatabase].
 *
 * @author Jan Müller
 */
fun getDatabase(context: Context): StockAppDatabase {
    synchronized(StockAppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room
                .databaseBuilder(
                    context.applicationContext,
                    StockAppDatabase::class.java,
                    "StockAppDatabase"
                )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}

/**
 * [Dao](https://developer.android.com/reference/androidx/room/Dao) that manages account entities in the database.
 *
 * @author TODO
 * @author Juri Lozowoj
 * @author Jan Müller
 */
@Dao
interface AccountDao {

    /**
     * Deletes all [Balance]s from the database.
     *
     */
    @Query("DELETE FROM balance")
    fun deleteBalances()

    /**
     * Deletes all [DepotQuotePurchase]s from the database.
     *
     */
    @Query("DELETE FROM depotquotepurchase")
    fun deleteDepot()

    /**
     * Deletes the given [DepotQuotePurchase]s from the database.
     *
     * @param depotPurchase The array of [DepotQuotePurchase]s to delete from the database.
     */
    @Delete
    fun deleteDepotQuotes(vararg depotPurchase: DepotQuotePurchase)

    /**
     * Deletes the [DepotQuotePurchase] with matching id from the database.
     *
     * @param id The [DepotQuotePurchase] id used in this query.
     */
    @Query("DELETE FROM depotquotepurchase WHERE id == :id")
    fun deleteDepotQuoteById(id: String)

    /**
     * Deletes all [DepotValue]s from the database.
     *
     */
    @Query("DELETE FROM depotvalue")
    fun deleteDepotValues()

    /**
     * Returns the amount [Long] of stored [Balance]s.
     *
     * @return The amount [Long] of stored [Balance]s.
     */
    @Query("SELECT COUNT(*) FROM balance")
    fun getBalanceCount(): Long

    /**
     * Returns a limited amount of stored [Balance]s.
     *
     * @param limit The [Balance] limit.
     * @return The limited amount of stored [Balance]s.
     */
    @Query("SELECT * FROM (SELECT * FROM balance ORDER BY balance.timestamp DESC LIMIT :limit) ORDER BY timestamp ASC")
    fun getBalancesLimited(limit: Int): LiveData<List<Balance>>

    /**
     * Returns all [DepotQuote]s as a [List], wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     * Conflates all [DepotQuotePurchase]s with the same id [String], symbol [String], type [Symbol.Type] and returns them as [DepotQuote]s,
     * whereby their amount [Double] is added up and the mean buyingPrice [Double] is bid from the buyingPrices [Double].
     *
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing all [DepotQuote]s as a [List].
     */
    @Query("SELECT id, symbol, type, SUM(amount) as amount, AVG(buyingPrice) as buyingPrice FROM depotquotepurchase GROUP BY id, symbol, type")
    fun getDepotQuotes(): LiveData<List<DepotQuote>>

    /**
     * Returns the [DepotQuotePurchase] with the matching id.
     *
     * @param id The [DepotQuotePurchase] id used in this query.
     * @return The [DepotQuotePurchase] with this id or null if no such quote exists.
     */
    @Query("SELECT * FROM depotquotepurchase WHERE id == :id LIMIT 1")
    fun getDepotQuoteById(id: String): DepotQuotePurchase?

    /**
     * Returns the [DepotQuote] with the matching id, wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     * Conflates all [DepotQuotePurchase]s with the matching id and returns them as one [DepotQuote],
     * whereby the amount [Double] is added up and the mean buyingPrice [Double] is bid from the buyingPrices [Double].
     *
     * @param id The [DepotQuotePurchase] id used in this query.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing the [DepotQuote].
     */
    @Query("SELECT id, symbol, type, SUM(amount) as amount, AVG(buyingPrice) as buyingPrice FROM depotquotepurchase WHERE id = :id GROUP BY id, symbol, type")
    fun getDepotQuoteWithId(id: String): LiveData<DepotQuote>

    /**
     * Returns the [DepotQuotePurchase]s with the matching id as a [List],
     * in ascending order of their buying price.
     *
     * @param id The [DepotQuotePurchase] id used in this query.
     * @return A [List] containing the [DepotQuotePurchase]s with the matching id, in ascending order of their buying price.
     */
    @Query("SELECT * FROM depotquotepurchase WHERE id = :id ORDER BY depotquotepurchase.buyingPrice ASC")
    fun getDepotQuotePurchasesByIdOrderedByPrice(id: String): List<DepotQuotePurchase>

    /**
     * Returns all [DepotQuotePurchase]s as a [List], in ascending order of their buying price.
     *
     * @return A [List] containing all [DepotQuotePurchase]s in ascending order of their buying price
     */
    @Query("SELECT * FROM depotquotepurchase ORDER BY depotquotepurchase.buyingPrice ASC")
    fun getDepotQuotePurchasesValuesOrderedByPrice(): List<DepotQuotePurchase>

    /**
     * Returns the last {limit} [DepotValue]s as a [List], wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @param limit The limit [Int] restricts the query to last {limit} [DepotValue]s.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of the last {limit} [DepotValue]s in ascending order.
     */
    @Query("SELECT * FROM (SELECT * FROM depotvalue ORDER BY depotvalue.timestamp DESC LIMIT :limit) ORDER BY timestamp ASC")
    fun getDepotValuesLimited(limit: Int): LiveData<List<DepotValue>>

    /**
     * Returns the latest [Balance], wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing the latest [Balance].
     */
    @Query("SELECT * FROM balance ORDER BY balance.timestamp DESC LIMIT 1")
    fun getLatestBalance(): LiveData<Balance>

    /**
     * Returns the latest [Balance].
     *
     * @return The latest [Balance].
     */
    @Query("SELECT * FROM balance ORDER BY balance.timestamp DESC LIMIT 1")
    fun getLatestBalanceValue(): Balance

    /**
     * Returns the latest [DepotValue].
     *
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing the latest [DepotValue].
     */
    @Query("SELECT * FROM depotvalue ORDER BY depotvalue.timestamp DESC LIMIT 1")
    fun getLatestDepotValues(): LiveData<DepotValue>

    /**
     * Inserts a [Balance] into the database.
     *
     * @param balance The [Balance] to be inserted.
     */
    @Insert(onConflict = REPLACE)
    fun insertBalance(balance: Balance)

    /**
     * Inserts a [DepotQuotePurchase] into the database.
     *
     * @param depotPurchase The [DepotQuotePurchase] to be inserted.
     */
    @Insert(onConflict = REPLACE)
    fun insertDepotQuote(depotPurchase: DepotQuotePurchase)

    /**
     * Inserts a [DepotValue] into the database.
     *
     * @param depotValue The [DepotValue] to be inserted.
     */
    @Insert(onConflict = REPLACE)
    fun insertDepotValue(depotValue: DepotValue)
}

/**
 * [Dao](https://developer.android.com/reference/androidx/room/Dao) that manages [Achievement]s in the database
 *
 * @author Jan Müller
 * @author Lucas Held
 */
@Dao
interface AchievementsDao {

    /**
     * Deletes a [Achievement] with the matching id from the database.
     *
     * @param name The [Achievement] name used in this query.
     */
    @Query("delete from achievement where name = :name")
    fun deleteAchievementById(name: Int)

    /**
     * Deletes all [Achievement]s from the database.
     *
     */
    @Query("delete from achievement")
    fun deleteAchievements()

    /**
     * Returns the [Achievement] with the matching name.
     *
     * @param name The [Achievement] name used in this query.
     * @return The [Achievement] with this id or null if no such quote exists.
     */
    @Query("select * from achievement where name = :name")
    fun getAchievementByName(name: Int): Achievement?

    /**
     * Returns a [List] of all [Achievement], wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of all [Achievement]s.
     */
    @Query("SELECT * FROM achievement ORDER BY timestamp DESC")
    fun getAchievements(): LiveData<List<Achievement>>

    /**
     * Returns the [Achievement] with the matching name, wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @param name The [Achievement] name used in this query.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing the [Achievement] that matches the query parameters.
     */
    @Query("select * from achievement where name = :name")
    fun getAchievementWithName(name: Int): LiveData<Achievement>

    /**
     * Inserts a [Achievement] into the database.
     *
     * @param achievement The [Achievement] to be inserted.
     */
    @Insert(onConflict = REPLACE)
    fun insert(achievement: Achievement)

}

@Dao
interface HistoricalPriceDao {

    /**
     * Deletes the [HistoricalPrice] with matching id from the database.
     *
     * @param id The [HistoricalPrice] id used in this query.
     */
    @Query("DELETE FROM historicalprice WHERE id = :id")
    fun deleteHistoricalPricesById(id: String)

    /**
     * Returns the [HistoricalPrice]s with the matching id as a [List], wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @param id The [HistoricalPrice] id used in this query.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of all [HistoricalPrice]s matching the id.
     */
    @Query("SELECT * FROM historicalprice WHERE id = :id")
    fun getHistoricalPricesById(id: String): LiveData<List<HistoricalPrice>>

    /**
     * Inserts [HistoricalPrice]s into the database.
     *
     * @param prices The [HistoricalPrice]s to be inserted.
     */
    @Insert(onConflict = REPLACE)
    fun insertAll(vararg prices: HistoricalPrice)
}

/**
 * [Dao](https://developer.android.com/reference/androidx/room/Dao) that manages [News] in the database.
 *
 * @author Jonas Thelemann
 */
@Dao
interface NewsDao {

    /**
     * Deletes everything from the "news" table.
     */
    @Query("DELETE FROM news")
    fun deleteNews()

    /**
     * Inserts all given news articles into the "news" table.
     */
    @Insert(onConflict = REPLACE)
    fun insertAll(vararg news: News)
}

/**
 * [Dao](https://developer.android.com/reference/androidx/room/Dao) that manages [Symbol]s in the database.
 *
 * @author Jan Müller
 */
@Dao
interface QuoteDao {

    /**
     * Deletes all [Quote]s from the database.
     *
     */
    @Query("DELETE FROM quote")
    fun deleteQuotes()

    /**
     * Returns the [Quote] with the matching id.
     *
     * @param id The [Quote] id used in this query.
     * @return The [Quote] with this id or null if no such quote exists.
     */
    @Query("SELECT * FROM quote WHERE quote.id == :id")
    fun getQuoteValueById(id: String): Quote?

    /**
     * Returns the [Quote] with the matching id, wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @param id The [Quote] id used in this query.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing the [Quote] that matches the query parameters.
     */
    @Query("SELECT * FROM quote WHERE quote.id == :id")
    fun getQuoteWithId(id: String): LiveData<Quote>

    /**
     * Inserts a [Quote] into the database.
     *
     * @param quote The quote to be inserted into the database.
     */
    @Insert(onConflict = REPLACE)
    fun insert(quote: Quote)
}

/**
 * [Dao](https://developer.android.com/reference/androidx/room/Dao) that manages [StockbrotQuote]s in the database
 *
 * @author Lucas Held
 */
@Dao
interface StockbrotDao {

    /**
     * Deletes a [StockbrotQuote] with the matching id from the database.
     *
     * @param id The [StockbrotQuote] id used in this query.
     */
    @Query("DELETE FROM stockbrotquote WHERE id == :id")
    fun deleteStockbrotQuoteById(id: String)

    /**
     * Deletes all [StockbrotQuote]s from the database.
     *
     */
    @Query("DELETE FROM stockbrotquote")
    fun deleteStockbrotQuotes()

    /**
     * Returns the [StockbrotQuote] with the matching id.
     *
     * @param id The [StockbrotQuote] id used in this query.
     * @return The [StockbrotQuote] with this id or null if no such quote exists.
     */
    @Query("SELECT * FROM stockbrotquote WHERE id == :id LIMIT 1")
    fun getStockbrotQuoteById(id: String): StockbrotQuote?

    /**
     * Returns a [List] of all [StockbrotQuote], wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of all [StockbrotQuote]s.
     */
    @Query("SELECT * FROM stockbrotquote")
    fun getStockbrotQuotes(): LiveData<List<StockbrotQuote>>

    /**
     * Returns the [StockbrotQuote] with the matching id, wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @param id The [StockbrotQuote] id used in this query.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing the [StockbrotQuote] that matches the query parameters.
     */
    @Query("SELECT * FROM stockbrotquote WHERE id == :id LIMIT 1")
    fun getStockbrotQuoteWithId(id: String): LiveData<StockbrotQuote>

    /**
     * Inserts a [StockbrotQuote] into the database.
     *
     * @param stockbrotQuote The [StockbrotQuote] to be inserted.
     */
    @Insert(onConflict = REPLACE)
    fun insertStockbrotQuote(stockbrotQuote: StockbrotQuote)
}

/**
 * [Dao](https://developer.android.com/reference/androidx/room/Dao) that manages [Symbol]s in the database.
 *
 * @author Jan Müller
 */
@Dao
interface SymbolDao {

    /**
     * Returns all [Symbol]s, wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of all [Symbol]s.
     */
    @Query("SELECT * FROM symbol ORDER BY symbol.symbol ASC")
    fun getAll(): LiveData<List<Symbol>>

    /**
     * Returns all [Symbol]s matching the query parameters, wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @param symbolQuery The symbol fragment used in this query.
     * @param nameQuery The name fragment used in this query.
     * @param type The [Symbol.Type] used in this query.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of all [Symbol]s matching the query parameters.
     */
    @Query("SELECT * FROM symbol WHERE symbol.type == :type AND (symbol.symbol LIKE :symbolQuery OR symbol.name LIKE :nameQuery) ORDER BY symbol.symbol ASC")
    fun getAllFiltered(
        symbolQuery: String,
        nameQuery: String,
        type: Symbol.Type
    ): LiveData<List<Symbol>>

    /**
     * Returns all [Symbol]s matching the query parameters, wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @param symbolQuery The symbol fragment used in this query.
     * @param nameQuery The name fragment used in this query.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of all [Symbol]s matching the query parameters.
     */
    @Query("SELECT * FROM symbol WHERE symbol.symbol LIKE :symbolQuery OR symbol.name LIKE :nameQuery ORDER BY symbol.symbol ASC")
    fun getAllFiltered(symbolQuery: String, nameQuery: String): LiveData<List<Symbol>>

    /**
     * Inserts all [Symbol]s into the [StockAppDatabase].
     *
     * @param symbols The [Symbol]s to be inserted.
     */
    @Insert(onConflict = REPLACE)
    fun insertAll(vararg symbols: Symbol)
}

/**
 * [Dao](https://developer.android.com/reference/androidx/room/Dao) that manages [Transaction]s in the database.
 *
 * @author Juri Lozowoj
 */
@Dao
interface TransactionDao {

    /**
     * Deletes all [Transaction]s from the database.
     *
     */
    @Query("DELETE FROM `transaction`")
    fun deleteTransactions()

    /**
     * Returns all [Transaction]s as a [List], wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData),
     * ordered be their date [Long], in descending order.
     *
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of all [Transaction]s.
     */
    @Query("SELECT * FROM `transaction` ORDER BY `transaction`.date DESC")
    fun getTransactions(): LiveData<List<Transaction>>

    /**
     * Returns all [Transaction]s matching the id as a [List], wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @param id The id of the [Transaction] is used in this query.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of all [Transaction]s matching the id.
     */
    @Query("SELECT * from `transaction` WHERE `transaction`.id = :id")
    fun getTransactionsById(id: String): LiveData<List<Transaction>>

    /**
     * Returns the last {limit} [Transaction]s as a [List], wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     *
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a [List] of the last {limit} [Transaction]s.
     */
    @Query("SELECT * from `transaction` LIMIT :limit")
    fun getTransactionsLimited(limit: Int): LiveData<List<Transaction>>

    /**
     * Inserts the given [Transaction] into the database.
     *
     * @param transaction The [Transaction] to be inserted.
     */
    @Insert
    fun insert(transaction: Transaction)
}
