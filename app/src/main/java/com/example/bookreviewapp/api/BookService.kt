package com.example.bookreviewapp.api

import com.example.bookreviewapp.model.restful.BestSellerDto
import com.example.bookreviewapp.model.restful.SearchBookDto
import com.example.bookreviewapp.model.restful.SelectedBookDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookService {

    @GET("ItemSearch.aspx?output=js&SearchTarget=Book&Version=20131101&Cover=Big")
    fun getBooksByName(
        @Query("ttbkey") apiKey: String,
        @Query("query") keyword: String,
        @Query("start") start: Int
    ): Call<SearchBookDto>

    @GET("ItemList.aspx?output=js&QueryType=Bestseller&MaxResults=50&SearchTarget=Book&Version=20131101&Cover=Big")
    fun getBestSellerBooks(
        @Query("ttbkey") apiKey: String,
    ): Call<BestSellerDto>

    @GET("ItemLookUp.aspx?output=js&Version=20131101&itemIdType=ISBN&Cover=Big")
    fun getSelectedBook(
        @Query("ttbkey") apiKey: String,
        @Query("ItemId") isbn: String
    ): Call<SelectedBookDto>
}