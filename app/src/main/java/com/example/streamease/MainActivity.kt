package com.example.streamease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var apiService: PexelsApiService

    private val videoList = mutableListOf<Video>() // List to store videos
    private var currentPage = 1                   // Current page number
    private var isLoading = false                 // Loading state
    private var isLastPage = false                // Last page state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the API service
        apiService = ApiClient.getClient().create(PexelsApiService::class.java)

        // Adjusting padding for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        videoAdapter = VideoAdapter(videoList) { videoUrl ->
            // Launch PlayerActivity with the video URL
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("videoUrl", videoUrl)
            startActivity(intent)
        }
        recyclerView.adapter = videoAdapter

        setupScrollListener() // Set up infinite scrolling
        fetchVideos(currentPage) // Fetch the first page of videos
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                // Check if the user has scrolled to the bottom and not loading
                if (!isLoading && !isLastPage) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        currentPage++ // Increment page
                        fetchVideos(currentPage)
                    }
                }
            }
        })
    }

    private fun fetchVideos(page: Int) {
        val apiKey = getString(R.string.api_key) // Retrieve the API key from strings.xml
        isLoading = true
        videoAdapter.setLoading(true) // Show the loading spinner while making the request

        // Example API request using Retrofit to fetch popular videos
        apiService.getVideos(apiKey, page, perPage = 80).enqueue(object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                isLoading = false
                videoAdapter.setLoading(false) // Hide the loading spinner once the response is received

                if (response.isSuccessful && response.body() != null) {
                    val videoResponse = response.body()!!
                    videoList.addAll(videoResponse.videos) // Append new videos to the list

                    // Call addVideos to add new items to the adapter
                    videoAdapter.addVideos(videoResponse.videos)

                    // Check if it's the last page
                    isLastPage = videoResponse.videos.isEmpty()
                } else {
                    Toast.makeText(this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                isLoading = false
                videoAdapter.setLoading(false) // Hide the loading spinner in case of failure

                Toast.makeText(this@MainActivity, "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
