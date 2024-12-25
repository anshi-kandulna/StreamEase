package com.example.streamease

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import android.app.Service
import android.os.Binder

class PlayerService : Service() {
    private lateinit var player: ExoPlayer
    private var playerNotificationManager: PlayerNotificationManager? = null

    // Binder for communication with the activity
    private val binder = PlayerBinder()

    override fun onCreate() {
        super.onCreate()

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // Set up the notification
        setupNotification()
    }

    // Set up the notification for media playback
    @SuppressLint("ForegroundServiceType")
    private fun setupNotification() {
        val channelId = "media_playback_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        startForeground(1, createNotification(channelId))

        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            1,
            channelId
        ).setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return "Playing Video"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = Intent(this@PlayerService, PlayerActivity::class.java)
                return PendingIntent.getActivity(
                    this@PlayerService,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return "Playing your video"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.s)
                return largeIcon
            }
        }).build()

        playerNotificationManager?.setPlayer(player)
    }

    // Create the notification for the foreground service
    private fun createNotification(channelId: String): Notification {
        val intent = Intent(this, PlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("StreamEase")
            .setContentText("Playing video in background...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    // When a video URL is passed, prepare and start playback
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val videoUrl = intent?.getStringExtra("videoUrl")
        if (!videoUrl.isNullOrEmpty()) {
            val mediaItem = MediaItem.fromUri(videoUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    // Binder for accessing player in the activity
    inner class PlayerBinder : Binder() {
        // Get the ExoPlayer instance
        fun getPlayer(): ExoPlayer = player

        // Get the PlayerService instance
        fun getService(): PlayerService = this@PlayerService
    }


    override fun onDestroy() {
        super.onDestroy()
        player.release()
        playerNotificationManager?.setPlayer(null)
    }
}
