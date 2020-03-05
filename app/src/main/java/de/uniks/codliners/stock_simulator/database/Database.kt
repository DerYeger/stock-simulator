package de.uniks.codliners.stock_simulator.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import de.uniks.codliners.stock_simulator.domain.*
import de.uniks.codliners.stock_simulator.domain.Transaction

@Database(
    entities = [Symbol::class, DepotQuotePurchase::class, News::class, Transaction::class, Quote::class, Balance::class, HistoricalPrice::class, StockbrotQuote::class, DepotValue::class, Achievement::class],
    version = 27,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StockAppDatabase : RoomDatabase() {
    abstract val symbolDao: SymbolDao
    abstract val quoteDao: QuoteDao
    abstract val newsDao: NewsDao
    abstract val transactionDao: TransactionDao
    abstract val accountDao: AccountDao
    abstract val stockbrotDao: StockbrotDao
    abstract val historicalDao: HistoricalPriceDao
    abstract val achievementDao: AchievementsDao
}

private lateinit var INSTANCE: StockAppDatabase

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

@Dao
interface AccountDao {

    @Query("DELETE FROM balance")
    fun deleteBalances()

    @Query("DELETE FROM depotquotepurchase")
    fun deleteDepot()

    @Delete
    fun deleteDepotQuotes(vararg depotPurchase: DepotQuotePurchase)

    @Query("DELETE FROM depotquotepurchase WHERE id == :id")
    fun deleteDepotQuoteById(id: String)

    @Query("DELETE FROM depotvalue")
    fun deleteDepotValues()

    @Query("SELECT COUNT(*) FROM balance")
    fun getBalanceCount(): Long

    @Query("SELECT * FROM (SELECT * FROM balance ORDER BY balance.timestamp DESC LIMIT :limit) ORDER BY timestamp ASC")
    fun getBalancesLimited(limit: Int): LiveData<List<Balance>>

    @Query("SELECT id, symbol, type, SUM(amount) as amount, AVG(buyingPrice) as buyingPrice FROM depotquotepurchase GROUP BY id, symbol, type")
    fun getDepotQuotes(): LiveData<List<DepotQuote>>

    @Query("SELECT * FROM depotquotepurchase WHERE id == :id LIMIT 1")
    fun getDepotQuoteById(id: String): DepotQuotePurchase?

    @Query("SELECT id, symbol, type, SUM(amount) as amount, AVG(buyingPrice) as buyingPrice FROM depotquotepurchase WHERE id = :id GROUP BY id, symbol, type")
    fun getDepotQuoteWithId(id: String): LiveData<DepotQuote>

    @Query("SELECT * FROM depotquotepurchase WHERE id = :id ORDER BY depotquotepurchase.buyingPrice ASC")
    fun getDepotQuotePurchasesByIdOrderedByPrice(id: String): List<DepotQuotePurchase>

    @Query("SELECT * FROM depotquotepurchase ORDER BY depotquotepurchase.buyingPrice ASC")
    fun getDepotQuotePurchasesValuesOrderedByPrice(): List<DepotQuotePurchase>

    @Query("SELECT * FROM (SELECT * FROM depotvalue ORDER BY depotvalue.timestamp DESC LIMIT :limit) ORDER BY timestamp ASC")
    fun getDepotValuesLimited(limit: Int): LiveData<List<DepotValue>>

    @Query("SELECT * FROM balance ORDER BY balance.timestamp DESC LIMIT 1")
    fun getLatestBalance(): LiveData<Balance>

    @Query("SELECT * FROM balance ORDER BY balance.timestamp DESC LIMIT 1")
    fun getLatestBalanceValue(): Balance

    @Query("SELECT * FROM depotvalue ORDER BY depotvalue.timestamp DESC LIMIT 1")
    fun getLatestDepotValues(): LiveData<DepotValue>

    @Insert(onConflict = REPLACE)
    fun insertBalance(balance: Balance)

    @Insert(onConflict = REPLACE)
    fun insertDepotQuote(depotPurchase: DepotQuotePurchase)

    @Insert(onConflict = REPLACE)
    fun insertDepotValue(depotValue: DepotValue)
}

@Dao
interface AchievementsDao {

    @Query("delete from achievement where name = :name")
    fun deleteAchievementById(name: Int)

    @Query("delete from achievement")
    fun deleteAchievements()

    @Query("select * from achievement where name = :name")
    fun getAchievementByName(name: Int): Achievement?

    @Query("SELECT * FROM achievement ORDER BY timestamp DESC")
    fun getAchievements(): LiveData<List<Achievement>>

    @Query("select * from achievement where name = :name")
    fun getAchievementWithName(name: Int): LiveData<Achievement>

    @Insert(onConflict = REPLACE)
    fun insert(achievement: Achievement)

}

@Dao
interface HistoricalPriceDao {

    @Query("DELETE FROM historicalprice WHERE id = :id")
    fun deleteHistoricalPricesById(id: String)

    @Query("SELECT * FROM historicalprice WHERE id = :id")
    fun getHistoricalPricesById(id: String): LiveData<List<HistoricalPrice>>

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg prices: HistoricalPrice)
}

@Dao
interface NewsDao {

    @Query("DELETE FROM news")
    fun deleteNews()

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg news: News)
}

@Dao
interface QuoteDao {

    @Query("DELETE FROM quote")
    fun deleteQuotes()

    @Query("SELECT * FROM quote WHERE quote.id == :id")
    fun getQuoteValueById(id: String): Quote

    @Query("SELECT * FROM quote WHERE quote.id == :id")
    fun getQuoteWithId(id: String): LiveData<Quote>

    @Insert(onConflict = REPLACE)
    fun insert(quote: Quote)
}

@Dao
interface StockbrotDao {

    @Query("DELETE FROM stockbrotquote WHERE id == :id")
    fun deleteStockbrotQuoteById(id: String)

    @Query("DELETE FROM stockbrotquote")
    fun deleteStockbrotQuotes()

    @Query("SELECT * FROM stockbrotquote WHERE id == :id LIMIT 1")
    fun getStockbrotQuoteById(id: String): StockbrotQuote?

    @Query("SELECT * FROM stockbrotquote")
    fun getStockbrotQuotes(): LiveData<List<StockbrotQuote>>

    @Query("SELECT * FROM stockbrotquote WHERE id == :id LIMIT 1")
    fun getStockbrotQuoteWithId(id: String): LiveData<StockbrotQuote>

    @Insert(onConflict = REPLACE)
    fun insertStockbrotQuote(stockbrotQuote: StockbrotQuote)
}

@Dao
interface SymbolDao {

    @Query("SELECT * FROM symbol WHERE symbol.id == :id")
    fun get(id: String): LiveData<Symbol>

    @Query("SELECT * FROM symbol ORDER BY symbol.symbol ASC")
    fun getAll(): LiveData<List<Symbol>>

    @Query("SELECT * FROM symbol WHERE symbol.type == :type AND (symbol.symbol LIKE :symbolQuery OR symbol.name LIKE :nameQuery) ORDER BY symbol.symbol ASC")
    fun getAllFiltered(symbolQuery: String, nameQuery: String, type: Symbol.Type): LiveData<List<Symbol>>

    @Query("SELECT * FROM symbol WHERE symbol.symbol LIKE :symbolQuery OR symbol.name LIKE :nameQuery ORDER BY symbol.symbol ASC")
    fun getAllFiltered(symbolQuery: String, nameQuery: String): LiveData<List<Symbol>>

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg symbols: Symbol)
}

@Dao
interface TransactionDao {

    @Query("DELETE FROM `transaction`")
    fun deleteTransactions()

    @Query("SELECT * FROM `transaction` ORDER BY `transaction`.date DESC")
    fun getTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * from `transaction` WHERE `transaction`.id = :id")
    fun getTransactionsById(id: String): LiveData<List<Transaction>>

    @Query("SELECT * from `transaction` LIMIT :limit")
    fun getTransactionsLimited(limit: Int): LiveData<List<Transaction>>

    @Insert
    fun insert(transaction: Transaction)
}
