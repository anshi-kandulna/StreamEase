package com.example.streamease

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Log

class PlayerActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private var playerService: PlayerService? = null
    private var exoPlayer: ExoPlayer? = null
    private var isBound = false
    private lateinit var videoList: List<Video> // List of Video objects
    private var videoIndex: Int = -1 // The index of the selected video


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)

        // Retrieve the video URL and index from the Intent
        val videoUrl = intent.getStringExtra("videoUrl")
        videoIndex = intent.getIntExtra("videoIndex", -1) // Retrieve the video index

        // Ensure video URL is valid
        if (videoUrl.isNullOrEmpty() && videoIndex == -1) {
            Toast.makeText(this, "Video URL is missing", Toast.LENGTH_SHORT).show()
            return
        }

        videoList = VideoRepository.videoList

        // Start the service with the video URL
        val serviceIntent = Intent(this, PlayerService::class.java).apply {
            putExtra("videoUrl", videoUrl)
        }
        startService(serviceIntent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val videoUrl = intent.getStringExtra("videoUrl")
        
        if (!videoUrl.isNullOrEmpty()) {
            playerService?.playNewVideo(videoUrl)
        }
    }


    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, PlayerService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        playerService?.let {
            stopService(Intent(this, PlayerService::class.java))
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.PlayerBinder
            playerService = binder.getService()
            exoPlayer = binder.getPlayer()
            playerView.player = exoPlayer
            isBound = true

            // Initialize the playlist and start from the selected video index
            initializePlaylist(videoList, videoIndex)

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    private fun initializePlaylist(videoList: List<Video>, startIndex: Int) {
        exoPlayer?.apply {
            clearMediaItems()

            // Loop through the video list starting from the selected index
            videoList.forEachIndexed { index, video ->
                val videoUrl = video.video_files
                    .filter { it.file_type == "video/mp4" }
                    .maxByOrNull { it.width ?: 0 }?.link

                if (!videoUrl.isNullOrEmpty()) {
                    addMediaItem(MediaItem.fromUri(videoUrl))
                } else {
                    Toast.makeText(this@PlayerActivity, "Video URL not found for video ID: ${video.id}", Toast.LENGTH_SHORT).show()
                }
            }

            prepare()
            playWhenReady = true

            // If the selected video index is valid, seek to that position
            if (startIndex >= 0 && startIndex < videoList.size) {
                seekTo(startIndex, 0)  // Start the player at the selected video
            }
        }
    }
}
