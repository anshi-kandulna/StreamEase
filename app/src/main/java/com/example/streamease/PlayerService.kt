package com.example.streamease

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class PlayerService : Service() {

    private lateinit var player: ExoPlayer
    private var isPlaying = false

    override fun onCreate() {
        super.onCreate()
        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        createNotificationChannel() // Create the notification channel (API 26+)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val videoUrl = intent?.getStringExtra("videoUrl") ?: return START_NOT_STICKY

        // Prepare and play video
        val mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()

        val action = intent?.getStringExtra("action")
        if (action == "play") {
            isPlaying = true
            player.play()
        } else if (action == "pause") {
            isPlaying = false
            player.pause()
        }

        showNotification()  // Show notification after starting or pausing playback

        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun showNotification() {
        val playPauseAction = if (isPlaying) {
            createPauseAction()
        } else {
            createPlayAction()
        }

        val notification = NotificationCompat.Builder(this, "player_service_channel")
            .setContentTitle("Video Player")
            .setContentText("Playing video")
            .setSmallIcon(R.drawable.s) // Ensure this is a valid icon
            .addAction(playPauseAction)
            .setOngoing(true)
            .build()

        startForeground(1, notification) // Start foreground service with notification
    }

    private fun createPlayAction(): NotificationCompat.Action {
        val playIntent = Intent(this, PlayerService::class.java).apply {
            putExtra("action", "play")
        }
        val playPendingIntent: PendingIntent = PendingIntent.getService(
            this,
            0,
            playIntent,
            PendingIntent.FLAG_MUTABLE // Use FLAG_MUTABLE if you need to modify it later
        )

        return NotificationCompat.Action(R.drawable.play_arrow_24px, "Play", playPendingIntent)
    }

    private fun createPauseAction(): NotificationCompat.Action {
        val pauseIntent = Intent(this, PlayerService::class.java).apply {
            putExtra("action", "pause")
        }
        val pausePendingIntent: PendingIntent = PendingIntent.getService(
            this,
            0,
            pauseIntent,
            PendingIntent.FLAG_MUTABLE // Use FLAG_MUTABLE if you need to modify it later
        )

        return NotificationCompat.Action(R.drawable.pause_24px, "Pause", pausePendingIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "player_service_channel"
            val channelName = "Player Service Channel"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Channel for Player Service notifications"
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
