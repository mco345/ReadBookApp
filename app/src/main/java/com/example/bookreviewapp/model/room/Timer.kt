package com.example.bookreviewapp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bookreviewapp.model.restful.Book

@Entity
data class Timer(
    @PrimaryKey val id: Long?,
    @ColumnInfo(name = "readingTime") val readingTime: Long?
)