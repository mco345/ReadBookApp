package com.example.bookreviewapp.model.restful

import com.google.gson.annotations.SerializedName

data class SearchBookDto(
    @SerializedName("title") val title: String,
    @SerializedName("item") val books: List<Book>,
    @SerializedName("totalResults") val totalResults: Int,
    @SerializedName("startIndex") val startIndex: Int
)
