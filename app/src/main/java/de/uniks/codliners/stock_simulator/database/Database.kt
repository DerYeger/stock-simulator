package de.uniks.codliners.stock_simulator.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Database
import androidx.room.OnConflictStrategy.REPLACE
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

    @Delete
    fun deleteAll(vararg shares: DepotShare)

    @Delete
    fun delete(share: DepotShare)

    @Insert
    fun insert(share: DepotShare)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg depotShares: DepotShare)
}

@Dao
interface QuoteDao {
@Dao
interface TransactionDao {

    @Insert(onConflict = REPLACE)
    fun insert(quote: Quote)
    @Query("select * from transactiondatabase where shareName = :shareName")
    fun getTransactionsByShareName(shareName: String): LiveData<List<TransactionDatabase>>

    @Query("SELECT * FROM quote")
    fun getAll(): LiveData<List<Quote>>

    @Query("SELECT * FROM quote WHERE quote.symbol == :symbol")
    fun getQuoteWithSymbol(symbol: String): LiveData<Quote>

    @Delete
    fun delete(quote: Quote)
}


@Database(
    entities = [ShareDatabase::class, DepotShare::class, Quote::class],
    version = 7,
    exportSchema = false
)
abstract class StockAppDatabase : RoomDatabase() {
    @Query("select * from transactiondatabase")
    fun getTransactions(): LiveData<List<TransactionDatabase>>

    @Delete
    fun deletaAll(vararg transactions: TransactionDatabase)

    @Delete
    fun delete(transaction: TransactionDatabase)

    @Insert
    fun insert(transaction: TransactionDatabase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg transactions: TransactionDatabase)
}



@Database(entities = [ShareDatabase::class, DepotShare::class, TransactionDatabase::class], version = 1, exportSchema = false)
abstract class StockAppDatabase: RoomDatabase() {
    abstract val shareDao: ShareDao
    abstract val depotDao: DepotDao
    abstract val quoteDao: QuoteDao
    abstract val transactionDao: TransactionDao
}

private lateinit var INSTANCE: StockAppDatabase

fun getDatabase(context: Context): StockAppDatabase {
    synchronized(StockAppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room
                .databaseBuilder(
                    context.applicationContext,
                    StockAppDatabase::class.java,
                    "stock"
                )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}




