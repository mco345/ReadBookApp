package com.example.bookreviewapp.dao

import androidx.room.*
import com.example.bookreviewapp.model.room.Timer

@Dao
interface TimerDao {
    @Query("SELECT readingTime FROM timer WHERE id == :id")
    fun getTimer(id: Long): Long?

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun saveTimer(timer: Timer)

    @Query("DELETE FROM timer WHERE id == :id")
    fun deleteTimer(id: Long)


}