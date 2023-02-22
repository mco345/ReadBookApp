package com.example.bookreviewapp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Timer(
    @PrimaryKey val id: Long?,
    @ColumnInfo(name = "readingTime") val readingTime: Long?
)