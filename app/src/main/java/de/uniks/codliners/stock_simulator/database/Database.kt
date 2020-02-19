package de.uniks.codliners.stock_simulator.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.domain.HistoricalPriceFromApi
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote

@Dao
interface QuoteDao {

    @Insert(onConflict = REPLACE)
    fun insert(quote: Quote)

    @Query("SELECT * FROM quote")
    fun getAll(): LiveData<List<Quote>>

    @Query("SELECT * FROM quote WHERE quote.symbol == :symbol")
    fun getQuoteWithSymbol(symbol: String): LiveData<Quote>

    @Delete
    fun delete(quote: Quote)

    @Query("DELETE FROM quote")
    fun deleteQuotes()
}

@Dao
interface TransactionDao {

    @Query("select * from transactiondatabase where symbol = :shareName")
    fun getTransactionsByShareName(shareName: String): LiveData<List<TransactionDatabase>>

    @Query("select * from transactiondatabase limit :limit")
    fun getTransactionsLimited(limit: Int): LiveData<List<TransactionDatabase>>

    @Query("SELECT * FROM transactiondatabase ORDER BY transactiondatabase.date DESC")
    fun getTransactions(): LiveData<List<TransactionDatabase>>

    @Delete
    fun deleteAll(vararg transactions: TransactionDatabase)

    @Delete
    fun delete(transaction: TransactionDatabase)

    @Insert
    fun insert(transaction: TransactionDatabase)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg transactions: TransactionDatabase)

    @Query("DELETE FROM transactiondatabase")
    fun deleteTransactions()
}

@Dao
interface AccountDao {

    @Insert(onConflict = REPLACE)
    fun insertBalance(balance: Balance)

    @Query("SELECT * FROM balance ORDER BY balance.timestamp ASC")
    fun getBalances(): LiveData<List<Balance>>

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

    @Query("SELECT * FROM depotquote WHERE symbol == :symbol LIMIT 1")
    fun getDepotQuoteWithSymbol(symbol: String): LiveData<DepotQuote>

    @Query("SELECT * FROM depotquote WHERE symbol == :symbol LIMIT 1")
    fun getDepotQuoteBySymbol(symbol: String): DepotQuote?

    @Query("DELETE FROM depotquote WHERE symbol == :symbol")
    fun deleteDepotQuoteBySymbol(symbol: String)

    @Query("DELETE FROM depotquote")
    fun deleteDepot()
}

@Dao
interface StockbrotDao {

    @Insert(onConflict = REPLACE)
    fun insertStockbrotQuote(stockbrotQuote: StockbrotQuote)

    @Query("SELECT * FROM stockbrotquote")
    fun getStockbrotQuotes(): LiveData<List<StockbrotQuote>>

    @Query("SELECT * FROM stockbrotquote WHERE symbol == :symbol LIMIT 1")
    fun getStockbrotQuoteWithSymbol(symbol: String): LiveData<StockbrotQuote>

    @Query("SELECT * FROM stockbrotquote WHERE symbol == :symbol LIMIT 1")
    fun getStockbrotQuoteBySymbol(symbol: String): StockbrotQuote?

    @Query("DELETE FROM stockbrotquote WHERE symbol == :symbol")
    fun deleteStockbrotQuoteBySymbol(symbol: String)

    @Query("DELETE FROM stockbrotquote")
    fun deleteStockbrote()
}

@Dao
interface HistoricalPriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(priceFromApi: HistoricalPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg prices: HistoricalPrice)

    @Query("select * from historicalprice where symbol = :symbol")
    fun getHistoricalPricesBySymbol(symbol: String): LiveData<List<HistoricalPrice>>

    @Query("delete from historicalprice")
    fun deleteHistoricalPrices()

    @Query("delete from historicalprice where symbol = :symbol")
    fun deleteHistoricalPricesBySymbol(symbol: String)
}

@Database(
    entities = [DepotQuote::class, TransactionDatabase::class, Quote::class, Balance::class, HistoricalPrice::class, StockbrotQuote::class],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StockAppDatabase: RoomDatabase() {
    abstract val quoteDao: QuoteDao
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
