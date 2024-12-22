package com.example.streamease

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.media.AudioManager
import android.content.Context

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var audioManager: AudioManager
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        enableEdgeToEdge()

        // Initialize AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playerView = findViewById(R.id.playerView)

        // Retrieve the video URL from the intent
        val videoUrl = intent.getStringExtra("videoUrl")
        if (videoUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Video URL is missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize Media3 ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Create a MediaItem and prepare the player
        val mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()

        // Start playing the video
        player.play()

        // Request audio focus
        requestAudioFocus()
    }

    private fun requestAudioFocus() {
        // Define an AudioFocusChangeListener
        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (!player.isPlaying) {
                        player.play() // resume playback if audio focus is gained
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    player.pause() // pause playback when audio focus is lost
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    player.pause() // pause playback when audio focus is temporarily lost (e.g., incoming call)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    player.volume = 0.1f // reduce volume when audio focus is lost but can still play at lower volume
                }
            }
        }

        // Request audio focus for both Android 8.0 and below
        // For Android 8.0 and above, we can still use the older method if we avoid AudioFocusRequest
        val result = audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Successfully gained audio focus
        } else {
            Toast.makeText(this, "Audio focus not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        // Keep playing the audio when the activity is paused
        if (player.isPlaying) {
            player.play()
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure the player is resumed if necessary
        if (!player.isPlaying) {
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        // Abandon audio focus when the activity is destroyed
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }
}
