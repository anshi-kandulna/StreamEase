package com.example.streamease

import android.content.Intent
import android.os.Bundle
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

        fetchPopularVideos() // Fetch the videos on startup
    }

    private fun fetchPopularVideos() {
        val apiKey = getString(R.string.api_key) // Retrieve the API key from strings.xml

        // Example API request using Retrofit to fetch popular videos
        apiService.getPopularVideos(apiKey, page = 1, perPage = 80).enqueue(object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val videoResponse = response.body()!!
                    // Pass the list of videos to the adapter and handle item clicks
                    videoAdapter = VideoAdapter(videoResponse.videos) { videoUrl ->
                        // When a card is clicked, launch PlayerActivity with the video URL
                        val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                        intent.putExtra("videoUrl", videoUrl) // Pass the video URL to PlayerActivity
                        startActivity(intent)
                    }
                    recyclerView.adapter = videoAdapter
                } else {
                    // Handle API error
                    Toast.makeText(this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                // Handle failure (e.g., network issue)
                Toast.makeText(this@MainActivity, "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
