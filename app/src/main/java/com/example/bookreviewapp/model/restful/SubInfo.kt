package com.example.bookreviewapp.model.restful

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SubInfo(
    @SerializedName("itemPage") val itemPage: String
)
