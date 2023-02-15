package com.example.bookreviewapp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookreviewapp.dao.*
import com.example.bookreviewapp.model.room.*

@Database(entities = [History::class, Review::class, Like::class, Rating::class, Reading::class, Timer::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
    abstract fun likeDao(): LikeDao
    abstract fun ratingDao(): RatingDao
    abstract fun readingDao(): ReadingDao
    abstract fun timerDao(): TimerDao
}