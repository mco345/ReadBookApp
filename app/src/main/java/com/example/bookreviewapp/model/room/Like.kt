package com.example.bookreviewapp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Like(
    @PrimaryKey val id: Long?,
    @ColumnInfo(name = "isLike") val isLike: Boolean?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "coverUrl") val coverUrl: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "likeCurrentDate") val likeCurrentDate: Long
)