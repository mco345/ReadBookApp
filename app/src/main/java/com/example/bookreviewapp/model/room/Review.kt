package com.example.bookreviewapp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Review(
    @PrimaryKey val currentDate: Long?,
    @ColumnInfo(name = "id") val id: Long?,
    @ColumnInfo(name = "review") val review: String?,
    @ColumnInfo(name = "page") val page: String?

)
