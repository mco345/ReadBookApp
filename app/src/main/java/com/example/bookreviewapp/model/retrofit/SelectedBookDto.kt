package com.example.bookreviewapp.model.retrofit

import com.google.gson.annotations.SerializedName

data class SelectedBookDto(
    @SerializedName("title") val title: String,
    @SerializedName("item") val item: List<Book>
)
