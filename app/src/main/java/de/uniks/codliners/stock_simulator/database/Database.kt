package de.uniks.codliners.stock_simulator.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Database

@Dao
interface ShareDao {

    @Query("select * from databaseshare where name = :shareName")
    fun getShareByName(shareName: String): LiveData<DatabaseShare>

    @Query("select * from databaseshare where id = :shareId")
    fun getShareById(shareId: Int): LiveData<DatabaseShare>

    @Query("select * from databaseshare")
    fun getShares(): LiveData<List<DatabaseShare>>

    @Delete
    fun delete(share: DatabaseShare)

    @Delete
    fun deleteAll(vararg shares: DatabaseShare)

    @Insert
    fun insert(share: DatabaseShare)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg shares: DatabaseShare)
}

@Database(entities = [DatabaseShare::class], version = 1)
abstract class StockDatabase: RoomDatabase() {
    abstract val shareDao: ShareDao
}

private lateinit var INSTANCE: StockDatabase

fun getDatabase(context: Context): StockDatabase {
    synchronized(StockDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                StockDatabase::class.java,
                "stock").build()
        }
    }
    return INSTANCE
}

