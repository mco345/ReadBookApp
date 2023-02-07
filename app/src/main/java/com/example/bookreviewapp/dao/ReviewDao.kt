package com.example.bookreviewapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookreviewapp.model.room.Review

@Dao
interface ReviewDao {

    @Query("SELECT * FROM review WHERE id == :id ORDER BY currentDate DESC")
    fun getReview(id: Long): List<Review>

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun saveReview(review: Review)

    @Query("DELETE FROM review WHERE currentDate == :currentDate")
    fun deleteReview(currentDate: Long)
}