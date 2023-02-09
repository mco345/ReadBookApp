package com.example.bookreviewapp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Reading(
    @PrimaryKey val id: Long?,   // isbn13
    @ColumnInfo(name="state") val state: String?, // isReading: 읽는 중, isReadDone: 읽음
    @ColumnInfo(name="readingTime") val readingTime: Int?,
    @ColumnInfo(name="readingPage") val readingPage: Int?,
    @ColumnInfo(name="startDate") val startDate: String?,
    @ColumnInfo(name="targetDate") val targetDate: String?,
    @ColumnInfo(name="title") val title: String?,
    @ColumnInfo(name="author") val author: String?,
    @ColumnInfo(name="coverUrl") val coverUrl: String?,
    @ColumnInfo(name="description") val description: String?
)


