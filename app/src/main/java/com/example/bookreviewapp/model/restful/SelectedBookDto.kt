package com.example.bookreviewapp.model.restful

import com.google.gson.annotations.SerializedName

data class SelectedBookDto(
    @SerializedName("title") val title: String,
    @SerializedName("item") val item: List<Book>
)
