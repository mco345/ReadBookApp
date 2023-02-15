package com.example.bookreviewapp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Reading(
    @PrimaryKey val id: Long?,   // isbn13
    @ColumnInfo(name="state") val state: String?, // isReading: 읽는 중, isReadDone: 읽음
    @ColumnInfo(name="readingTime") val readingTime: Long?,
    @ColumnInfo(name="readingPage") val readingPage: Int?,
    @ColumnInfo(name="totalPage") val totalPage: Int?,
    @ColumnInfo(name="startTime") val startTime: Long?,
    @ColumnInfo(name="startDate") val startDate: String?,
    @ColumnInfo(name="targetChecked") val targetChecked: Boolean?,
    @ColumnInfo(name="targetDate") val targetDate: String?,
    @ColumnInfo(name="finishTime") val finishTime: Long?,
    @ColumnInfo(name="title") val title: String?,
    @ColumnInfo(name="author") val author: String?,
    @ColumnInfo(name="coverUrl") val coverUrl: String?,
    @ColumnInfo(name="description") val description: String?
)


