package com.example.bookreviewapp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookreviewapp.dao.HistoryDao
import com.example.bookreviewapp.dao.LikeDao
import com.example.bookreviewapp.dao.RatingDao
import com.example.bookreviewapp.dao.ReviewDao
import com.example.bookreviewapp.model.room.History
import com.example.bookreviewapp.model.room.Like
import com.example.bookreviewapp.model.room.Rating
import com.example.bookreviewapp.model.room.Review

@Database(entities = [History::class, Review::class, Like::class, Rating::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
    abstract fun likeDao(): LikeDao
    abstract fun ratingDao(): RatingDao
}