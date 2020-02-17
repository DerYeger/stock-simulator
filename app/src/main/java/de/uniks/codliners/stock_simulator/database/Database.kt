package de.uniks.codliners.stock_simulator.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Database

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



@Database(entities = [ShareDatabase::class, DepotShare::class], version = 1, exportSchema = false)
abstract class StockAppDatabase: RoomDatabase() {
    abstract val shareDao: ShareDao
    abstract val depotDao: DepotDao
}

private lateinit var INSTANCE: StockAppDatabase

fun getDatabase(context: Context): StockAppDatabase {
    synchronized(StockAppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                StockAppDatabase::class.java,
                "stock").build()
        }
    }
    return INSTANCE
}




