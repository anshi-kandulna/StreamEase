package com.example.streamease

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView

class Splash : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setContentView(R.layout.activity_splash)

        // Get the LottieAnimationView by ID
        val lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        lottieAnimationView.setAnimation(R.raw.curve) // Use your actual file name
        lottieAnimationView.playAnimation()

        // Listener to detect when the animation ends
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // No action needed when animation starts
            }

            override fun onAnimationEnd(animation: Animator) {
                // Navigate to MainActivity when the animation ends
                val intent = Intent(this@Splash, LoginActivity::class.java)
                startActivity(intent)
                finish() // Finish Splash Activity to remove it from the back stack
            }

            override fun onAnimationCancel(animation: Animator) {
                // Optional: Handle animation cancel event
            }

            override fun onAnimationRepeat(animation: Animator) {
                // Optional: Handle animation repeat event
            }
        })
    }
}
