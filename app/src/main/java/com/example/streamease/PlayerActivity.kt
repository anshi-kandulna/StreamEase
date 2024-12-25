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
import com.google.android.exoplayer2.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private var playerService: PlayerService? = null
    private var exoPlayer: ExoPlayer? = null
    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)

        val videoUrl = intent.getStringExtra("videoUrl")
        val fromNotification = intent.getBooleanExtra("fromNotification", false)

        // If videoUrl is null and not from the notification, show a toast
        if (videoUrl.isNullOrEmpty() && !fromNotification) {
            Toast.makeText(this, "Video URL is missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Start the foreground service
        val serviceIntent = Intent(this, PlayerService::class.java).apply {
            putExtra("videoUrl", videoUrl)
        }
        startService(serviceIntent)
    }
    // Handle new intents to avoid multiple player screens
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent

        // Retrieve the updated video URL and play it
        val videoUrl = intent.getStringExtra("videoUrl")
        if (!videoUrl.isNullOrEmpty()) {
            val serviceIntent = Intent(this, PlayerService::class.java).apply {
                putExtra("videoUrl", videoUrl)
            }
            startService(serviceIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to the service
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
        playerService?.let {
            stopService(Intent(this, PlayerService::class.java))
        }
    }

    // ServiceConnection to bind the service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.PlayerBinder
            playerService = binder.getService()
            exoPlayer = binder.getPlayer()
            playerView.player = exoPlayer
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }
}
