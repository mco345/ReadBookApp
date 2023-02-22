package com.example.bookreviewapp.model.retrofit

import com.google.gson.annotations.SerializedName

data class Book (
    @SerializedName("itemId") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("cover") val coverSmallUrl: String,
    @SerializedName("author") val author: String,
    @SerializedName("publisher") val publisher: String,
    @SerializedName("isbn") val isbn10: String,
    @SerializedName("isbn13") val isbn13: String,
    @SerializedName("pubDate") val pubDate: String,
    @SerializedName("link") val link: String,
    @SerializedName("subInfo") val subInfo: SubInfo
)