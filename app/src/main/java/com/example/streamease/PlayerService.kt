package com.example.streamease

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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

class PlayerService : Service() {
    private lateinit var player: ExoPlayer
    private var playerNotificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // Set up the notification
        setupNotification()
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        // Handle when the player is ready
                    }
                    Player.STATE_ENDED -> {
                        // Handle when playback ends
                    }
                }
            }
        })
    }

    private fun createNotification(): Notification {
        val channelId = "media_playback_channel"
        val intent = Intent(this, PlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("StreamEase")
                .setContentText("Playing Video")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("StreamEase")
                .setContentText("Playing Video")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()
        }
    }

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

        startForeground(1, createNotification())

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

        // Attach the player to the PlayerNotificationManager
        playerNotificationManager?.setPlayer(player)
    }

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

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        playerNotificationManager?.setPlayer(null)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
