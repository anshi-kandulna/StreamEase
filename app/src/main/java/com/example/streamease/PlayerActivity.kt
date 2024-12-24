package com.example.streamease

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private var playerNotificationManager: PlayerNotificationManager? = null

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

        // Create a MediaItem and prepare the player
        val mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        // Set up the notification
        setupNotification()
    }

    private fun setupNotification() {
        val channelId = "media_playback_channel"

        // Create a notification channel (required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // Create PlayerNotificationManager
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            1, // Notification ID
            channelId
        ).setMediaDescriptionAdapter(object :
            PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return "Playing Video"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                // Create an intent to open the PlayerActivity when the notification is tapped
                val intent = Intent(this@PlayerActivity, PlayerActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                return PendingIntent.getActivity(
                    this@PlayerActivity,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                // Return a description or subtitle for the media content
                return "Playing your video"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                // Provide a large icon for the notification (e.g., a thumbnail of the video)
                val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.s)
                return largeIcon
            }
        }).build()

        // Attach the player to the PlayerNotificationManager
        playerNotificationManager?.setPlayer(player)
    }

    override fun onPause() {
        super.onPause()
        player.release()
        playerNotificationManager?.setPlayer(null)
    }
}
