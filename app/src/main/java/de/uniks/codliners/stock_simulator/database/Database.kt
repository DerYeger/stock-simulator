package de.uniks.codliners.stock_simulator.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import de.uniks.codliners.stock_simulator.domain.*
import de.uniks.codliners.stock_simulator.domain.Transaction

@Dao
interface SymbolDao {

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg symbols: Symbol)

    @Query("SELECT * FROM symbol ORDER BY symbol.symbol ASC")
    fun getAll(): LiveData<List<Symbol>>

    @Query("SELECT * FROM symbol WHERE symbol.id == :id")
    fun get(id: String): LiveData<Symbol>
}

@Dao
interface QuoteDao {

    @Insert(onConflict = REPLACE)
    fun insert(quote: Quote)

    @Query("SELECT * FROM quote")
    fun getAll(): LiveData<List<Quote>>

    @Query("SELECT * FROM quote WHERE quote.id == :id")
    fun getQuoteWithId(id: String): LiveData<Quote>

    @Query("SELECT * FROM quote WHERE quote.id == :id")
    fun getQuoteValueById(id: String): Quote

    @Delete
    fun delete(quote: Quote)

    @Query("DELETE FROM quote")
    fun deleteQuotes()
}

@Dao
interface NewsDao {

    @Insert(onConflict = REPLACE)
    fun insert(news: News)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg news: News)

    @Query("SELECT * FROM news")
    fun getAll(): LiveData<List<News>>

    @Delete
    fun delete(news: News)

    @Query("DELETE FROM news")
    fun deleteNews()
}

@Dao
interface TransactionDao {

    @Query("select * from `transaction` where `transaction`.id = :id")
    fun getTransactionsById(id: String): LiveData<List<Transaction>>

    @Query("select * from `transaction` limit :limit")
    fun getTransactionsLimited(limit: Int): LiveData<List<Transaction>>

    @Query("SELECT * FROM `transaction` ORDER BY `transaction`.date DESC")
    fun getTransactions(): LiveData<List<Transaction>>

    @Delete
    fun deleteAll(vararg transactions: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Insert
    fun insert(transaction: Transaction)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg transactions: Transaction)

    @Query("DELETE FROM `transaction`")
    fun deleteTransactions()
}

@Dao
interface AccountDao {

    @Insert(onConflict = REPLACE)
    fun insertBalance(balance: Balance)

    @Query("SELECT * FROM balance ORDER BY balance.timestamp ASC")
    fun getBalances(): LiveData<List<Balance>>

    @Query("SELECT COUNT(*) FROM balance")
    fun getBalanceCount(): Long

    @Query("SELECT * FROM (SELECT * FROM balance ORDER BY balance.timestamp DESC LIMIT :limit) ORDER BY timestamp ASC")
    fun getBalancesLimited(limit: Int): LiveData<List<Balance>>

    @Query("SELECT * FROM balance ORDER BY balance.timestamp DESC LIMIT 1")
    fun getLatestBalance(): LiveData<Balance>

    @Query("DELETE FROM balance")
    fun deleteBalances()

    @Insert(onConflict = REPLACE)
    fun insertDepotQuote(depot: DepotQuote)

    @Query("SELECT * FROM depotquote")
    fun getDepotQuotes(): LiveData<List<DepotQuote>>

    @Query("SELECT * FROM depotquote WHERE id == :id LIMIT 1")
    fun getDepotQuoteWitId(id: String): LiveData<DepotQuote>

    @Query("SELECT * FROM depotquote")
    fun getDepotQuotesValues(): List<DepotQuote>

    @Query("SELECT * FROM depotquote WHERE id == :id LIMIT 1")
    fun getDepotQuoteById(id: String): DepotQuote?

    @Query("DELETE FROM depotquote WHERE id == :id")
    fun deleteDepotQuoteById(id: String)

    @Query("DELETE FROM depotquote")
    fun deleteDepot()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDepotValue(depotValue: DepotValue)

    @Query("select * from depotvalue")
    fun getDepotValues(): LiveData<List<DepotValue>>

    @Query("select * from depotvalue order by depotvalue.timestamp desc limit 1")
    fun getLatestDepotValues(): LiveData<DepotValue>

    @Query("SELECT * FROM (SELECT * FROM depotvalue ORDER BY depotvalue.timestamp DESC LIMIT :limit) ORDER BY timestamp ASC")
    fun getDepotValuesLimited(limit: Int): LiveData<List<DepotValue>>

    @Query("delete from depotvalue")
    fun deleteDepotValues()
}

@Dao
interface StockbrotDao {

    @Insert(onConflict = REPLACE)
    fun insertStockbrotQuote(stockbrotQuote: StockbrotQuote)

    @Query("SELECT * FROM stockbrotquote")
    fun getStockbrotQuotes(): LiveData<List<StockbrotQuote>>

    @Query("SELECT * FROM stockbrotquote WHERE id == :id LIMIT 1")
    fun getStockbrotQuoteWithId(id: String): LiveData<StockbrotQuote>

    @Query("SELECT * FROM stockbrotquote WHERE id == :id LIMIT 1")
    fun getStockbrotQuoteById(id: String): StockbrotQuote?

    @Query("DELETE FROM stockbrotquote WHERE id == :id")
    fun deleteStockbrotQuoteById(id: String)

    @Query("DELETE FROM stockbrotquote")
    fun deleteStockbrotQuotes()
}

@Dao
interface HistoricalPriceDao {

    @Insert(onConflict = REPLACE)
    fun insert(priceFromApi: HistoricalPrice)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg prices: HistoricalPrice)

    @Query("select * from historicalprice where id = :id")
    fun getHistoricalPricesById(id: String): LiveData<List<HistoricalPrice>>

    @Query("delete from historicalprice")
    fun deleteHistoricalPrices()

    @Query("delete from historicalprice where id = :id")
    fun deleteHistoricalPricesById(id: String)
}

@Database(
    entities = [Symbol::class, DepotQuote::class, News::class, Transaction::class, Quote::class, Balance::class, HistoricalPrice::class, StockbrotQuote::class, DepotValue::class],
    version = 24,
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
