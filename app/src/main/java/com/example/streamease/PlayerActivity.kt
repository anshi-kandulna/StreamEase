package com.example.streamease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

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

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Start the service for background playback
        val serviceIntent = Intent(this, PlayerService::class.java).apply {
            putExtra("videoUrl", videoUrl)
        }
        startService(serviceIntent)

        // Create a MediaItem and prepare the player
        val mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun onPause() {
        super.onPause()
        // You don't need to release the player here, as it's handled by the service
    }
}
