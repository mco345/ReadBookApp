package com.example.bookreviewapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookreviewapp.model.room.History
import com.example.bookreviewapp.model.room.Reading

@Dao
interface ReadingDao {
    @Query("SELECT * FROM reading ORDER BY startDate DESC")
    fun getAll(): List<Reading>

    @Query("SELECT state FROM reading WHERE id == :id")
    fun getState(id: Long): String

    @Insert
    fun insertReading(reading: Reading)

    @Query("UPDATE reading SET state = :state WHERE id == :id")
    fun updateReading(id: Long, state: String)

    @Query("DELETE FROM reading WHERE id == :id")
    fun delete(id: Long)
}