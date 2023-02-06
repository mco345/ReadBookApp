package com.example.bookreviewapp.dao

import androidx.room.*
import com.example.bookreviewapp.model.restful.Book
import com.example.bookreviewapp.model.room.Like
import com.example.bookreviewapp.model.room.Review

@Dao
interface LikeDao {
    @Query("SELECT isLike FROM `like` WHERE id == :id")
    fun getIsLike(id: Long): Boolean?

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun saveLike(like: Like)

    @Query("DELETE FROM `like` WHERE id == :id")
    fun deleteLike(id: Long)

    @Query("SELECT * FROM `like` WHERE isLike == 1")
    fun getAllLikeList(): List<Like>


}