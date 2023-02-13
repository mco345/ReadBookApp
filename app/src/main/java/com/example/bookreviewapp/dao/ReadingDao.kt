package com.example.bookreviewapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookreviewapp.model.room.History
import com.example.bookreviewapp.model.room.Reading

@Dao
interface ReadingDao {
    @Query("SELECT * FROM reading WHERE state = :state ORDER BY startTime DESC")
    fun getAll(state: String): List<Reading>

    @Query("SELECT * FROM reading WHERE id == :id")
    fun getAllFromId(id: Long): Reading

    @Query("SELECT state FROM reading WHERE id == :id")
    fun getState(id: Long): String

    @Insert
    fun insertReading(reading: Reading)

    @Query("UPDATE reading SET state = :state, finishTime = :finishTime WHERE id == :id")
    fun updateReadingState(id: Long, state: String, finishTime: Long)

    @Query("UPDATE reading SET readingPage = :readingPage, startDate = :startDate, targetDate = :targetDate WHERE id == :id")
    fun updateReading(id: Long, readingPage: Int?, startDate: String?, targetDate: String?)

    @Query("DELETE FROM reading WHERE id == :id")
    fun delete(id: Long)
}