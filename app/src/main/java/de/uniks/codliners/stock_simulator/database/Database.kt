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
    fun getShareById(shareId: Int): LiveData<ShareDatabase>

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

    @Insert
    fun insert(shareId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg depotShareIds: String)

    
}



@Database(entities = [ShareDatabase::class, DepotShare::class], version = 1)
abstract class StockAppDatabase: RoomDatabase() {
    abstract val shareDao: ShareDao
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




