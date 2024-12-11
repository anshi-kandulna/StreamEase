package com.example.streamease

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.Call

interface PexelsApiService {

    @GET("videos/popular")
    fun getPopularVideos(
        @Header("Authorization") apiKey: String, // API key for authentication
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Call<VideoResponse>
}
