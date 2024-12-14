package com.example.streamease

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                val intent = Intent(this@Splash, MainActivity::class.java)
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
