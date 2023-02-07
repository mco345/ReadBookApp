package com.example.bookreviewapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookreviewapp.model.room.Like
import com.example.bookreviewapp.model.room.Rating

@Dao
interface RatingDao {
    @Query("SELECT rating FROM Rating WHERE id == :id")
    fun getRating(id: Long): Float

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun saveRating(rating: Rating)
}