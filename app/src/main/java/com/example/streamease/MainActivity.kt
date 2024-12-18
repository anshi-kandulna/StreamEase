package com.example.streamease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private lateinit var searchView: SearchView

    private val videoList = mutableListOf<Video>()
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private var currentQuery: String? = null // Track current search query

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        apiService = ApiClient.getClient().create(PexelsApiService::class.java)

        videoAdapter = VideoAdapter(videoList) { videoUrl ->
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("videoUrl", videoUrl)
            startActivity(intent)
        }
        recyclerView.adapter = videoAdapter

        setupScrollListener()
        setupSearchView()
        fetchVideos(currentPage) // Fetch random videos initially
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    currentQuery = query
                    resetVideos()
                    fetchVideos(currentPage, query) // Fetch videos with search query
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Implement live search here
                return false
            }
        })

        searchView.setOnCloseListener {
            currentQuery = null
            resetVideos()
            fetchVideos(currentPage) // Fetch random videos
            false
        }
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                // Hide the search bar when scrolling down
                if (dy > 0) searchView.clearFocus()

                if (!isLoading && !isLastPage) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        currentPage++
                        fetchVideos(currentPage, currentQuery) // Fetch more results
                    }
                }
            }
        })
    }

    private fun fetchVideos(page: Int, query: String? = null) {
        val apiKey = getString(R.string.api_key)
        isLoading = true
        videoAdapter.setLoading(true)

        val call = if (query.isNullOrEmpty()) {
            // Fetch random videos
            apiService.getVideos(apiKey, page, perPage = 80)
        } else {
            // Fetch videos based on search query
            apiService.searchVideos(apiKey, query, page, perPage = 80)
        }

        call.enqueue(object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                isLoading = false
                videoAdapter.setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val videoResponse = response.body()!!
                    videoList.addAll(videoResponse.videos)
                    videoAdapter.addVideos(videoResponse.videos)
                    isLastPage = videoResponse.videos.isEmpty()
                } else {
                    Toast.makeText(this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                isLoading = false
                videoAdapter.setLoading(false)
                Toast.makeText(this@MainActivity, "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resetVideos() {
        currentPage = 1
        videoList.clear()
        videoAdapter.notifyDataSetChanged()
        isLastPage = false
    }
}
