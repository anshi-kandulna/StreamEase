package com.example.streamease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)

        // Retrieve the video URL from the intent
        val videoUrl = intent.getStringExtra("videoUrl")
        if (videoUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Video URL is missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize ExoPlayer for video playback
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Prepare the video to be played
        val mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()

        // Start the service with the video URL
        val serviceIntent = Intent(this, PlayerService::class.java).apply {
            putExtra("videoUrl", videoUrl)
        }
        startService(serviceIntent)

        // Start playback automatically (optional)
        player.play()
    }

    override fun onPause() {
        super.onPause()
        // Stop the service when the activity is paused
        stopService(Intent(this, PlayerService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the ExoPlayer when the activity is destroyed
        player.release()
    }
}
