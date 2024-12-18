package com.example.streamease

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.Call

interface PexelsApiService {
    @GET("videos/popular")
    fun getVideos(
        @Header("Authorization") apiKey: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Call<VideoResponse>

    @GET("videos/search")
    fun searchVideos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Call<VideoResponse>
}

