package de.uniks.codliners.stock_simulator.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Database
import androidx.room.OnConflictStrategy.REPLACE
import de.uniks.codliners.stock_simulator.domain.Account
import de.uniks.codliners.stock_simulator.domain.Quote

@Dao
interface ShareDao {

    @Query("select * from sharedatabase where name = :shareName")
    fun getShareByName(shareName: String): LiveData<ShareDatabase>

    @Query("select * from sharedatabase where id = :shareId")
    fun getShareById(shareId: String): LiveData<ShareDatabase>

    @Query("select * from sharedatabase")
    fun getShares(): LiveData<List<ShareDatabase>>

    @Delete
    fun delete(share: ShareDatabase)

    @Delete
    fun deleteAll(vararg shares: ShareDatabase)

    @Insert
    fun insert(share: ShareDatabase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg shares: ShareDatabase)
}

@Dao
interface DepotDao {

    @Query("select sharedatabase.* from depotshare inner join sharedatabase where depotshare.id = sharedatabase.id = :shareId")
    fun getShareById(shareId: String): LiveData<ShareDatabase>

    @Query("select sharedatabase.* from depotshare inner join sharedatabase where depotshare.id = sharedatabase.id")
    fun getShares(): LiveData<List<ShareDatabase>>

    @Query("DELETE FROM depotshare")
    fun deleteAll()

    @Delete
    fun delete(share: DepotShare)

    @Insert
    fun insert(share: DepotShare)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg depotShares: DepotShare)
}

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
}

@Dao
interface TransactionDao {

    @Query("select * from transactiondatabase where shareName = :shareName")
    fun getTransactionsByShareName(shareName: String): LiveData<List<TransactionDatabase>>

    @Query("select * from transactiondatabase")
    fun getTransactions(): LiveData<List<TransactionDatabase>>

    @Delete
    fun deleteAll(vararg transactions: TransactionDatabase)

    @Delete
    fun delete(transaction: TransactionDatabase)

    @Insert
    fun insert(transaction: TransactionDatabase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg transactions: TransactionDatabase)
}

@Dao
interface AccountDao {

    @Insert(onConflict = REPLACE)
    fun insert(account: Account)

    @Update
    fun update(account: Account)

    @Query("SELECT * FROM account ORDER BY account.id ASC LIMIT 1")
    fun getAccount(): LiveData<Account>
}

@Database(
    entities = [ShareDatabase::class, DepotShare::class, TransactionDatabase::class, Quote::class, Account::class],
    version = 1,
    exportSchema = false
)
abstract class StockAppDatabase: RoomDatabase() {
    abstract val shareDao: ShareDao
    abstract val depotDao: DepotDao
    abstract val quoteDao: QuoteDao
    abstract val transactionDao: TransactionDao
    abstract val accountDao: AccountDao
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
