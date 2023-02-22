package com.example.bookreviewapp.dao

import androidx.room.*
import com.example.bookreviewapp.model.room.Like

@Dao
interface LikeDao {
    @Query("SELECT isLike FROM `like` WHERE id == :id")
    fun getIsLike(id: Long): Boolean?

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun saveLike(like: Like)

    @Query("DELETE FROM `like` WHERE id == :id")
    fun deleteLike(id: Long)

    @Query("SELECT * FROM `like` WHERE isLike == 1 ORDER BY likeCurrentDate DESC")
    fun getAllLikeList(): List<Like>


}