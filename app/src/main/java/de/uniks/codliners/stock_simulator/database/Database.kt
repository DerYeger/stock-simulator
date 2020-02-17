package de.uniks.codliners.stock_simulator.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface ShareDao {

    @Insert
    fun insert(share: DatabaseShare)

    @Insert
    fun insertAll(vararg share: DatabaseShare)
}