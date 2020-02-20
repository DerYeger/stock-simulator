package de.uniks.codliners.stock_simulator.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import de.uniks.codliners.stock_simulator.domain.*

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

    @Delete
    fun delete(quote: Quote)

    @Query("DELETE FROM quote")
    fun deleteQuotes()
}

@Dao
interface TransactionDao {

    @Query("select * from databasetransaction where databasetransaction.id = :id")
    fun getTransactionsById(id: String): LiveData<List<DatabaseTransaction>>

    @Query("select * from databasetransaction limit :limit")
    fun getTransactionsLimited(limit: Int): LiveData<List<DatabaseTransaction>>

    @Query("SELECT * FROM databasetransaction ORDER BY databasetransaction.date DESC")
    fun getTransactions(): LiveData<List<DatabaseTransaction>>

    @Delete
    fun deleteAll(vararg transactions: DatabaseTransaction)

    @Delete
    fun delete(transaction: DatabaseTransaction)

    @Insert
    fun insert(transaction: DatabaseTransaction)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg transactions: DatabaseTransaction)

    @Query("DELETE FROM databasetransaction")
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

    @Query("SELECT * FROM depotquote WHERE id == :id LIMIT 1")
    fun getDepotQuoteById(id: String): DepotQuote?

    @Query("DELETE FROM depotquote WHERE id == :id")
    fun deleteDepotQuoteById(id: String)

    @Query("DELETE FROM depotquote")
    fun deleteDepot()
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
    entities = [Symbol::class, DepotQuote::class, DatabaseTransaction::class, Quote::class, Balance::class, HistoricalPrice::class, StockbrotQuote::class],
    version = 19,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StockAppDatabase: RoomDatabase() {
    abstract val symbolDao: SymbolDao
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
