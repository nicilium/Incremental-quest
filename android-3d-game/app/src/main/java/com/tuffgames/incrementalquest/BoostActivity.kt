package com.tuffgames.incrementalquest

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * BoostActivity - Ad-Based Boost Station
 *
 * Shows all available boosts, their status, and allows activating them via ads
 */
class BoostActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var boostContainer: LinearLayout

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Update UI
            updateBoostCards()

            // Next update in 1 second
            updateHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Main layout
        mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(15, 15, 45))
        mainLayout.setPadding(20, 20, 20, 20)

        // Title
        val title = TextView(this)
        title.text = "⚡ BOOST STATION"
        title.textSize = 32f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Subtitle
        val subtitle = TextView(this)
        subtitle.text = "Watch ads for powerful temporary boosts!"
        subtitle.textSize = 14f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 10, 0, 20)
        mainLayout.addView(subtitle)

        // Scroll view for boost cards
        val scrollView = ScrollView(this)
        boostContainer = LinearLayout(this)
        boostContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(boostContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Back button
        val backButton = Button(this)
        backButton.text = "← BACK"
        backButton.setBackgroundColor(Color.rgb(80, 80, 80))
        backButton.setTextColor(Color.WHITE)
        backButton.setOnClickListener {
            finish()
        }
        mainLayout.addView(backButton)

        setContentView(mainLayout)

        // Start update loop
        updateHandler.post(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun updateBoostCards() {
        boostContainer.removeAllViews()

        // Add card for each boost type
        BoostType.values().forEach { boostType ->
            val card = createBoostCard(boostType)
            boostContainer.addView(card)
        }
    }

    private fun createBoostCard(boostType: BoostType): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(20, 20, 20, 20)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 15)
        card.layoutParams = cardParams

        // Header with emoji and name
        val header = TextView(this)
        header.text = "${boostType.emoji} ${boostType.displayName}"
        header.textSize = 24f
        header.setTextColor(Color.WHITE)
        header.gravity = Gravity.CENTER
        card.addView(header)

        // Description
        val description = TextView(this)
        description.text = boostType.description
        description.textSize = 14f
        description.setTextColor(Color.LTGRAY)
        description.gravity = Gravity.CENTER
        description.setPadding(0, 5, 0, 10)
        card.addView(description)

        // Duration info
        val durationText = TextView(this)
        durationText.text = "⏱️ Duration: ${boostType.durationMinutes} min | Cooldown: ${boostType.cooldownMinutes / 60}h"
        durationText.textSize = 12f
        durationText.setTextColor(Color.YELLOW)
        durationText.gravity = Gravity.CENTER
        durationText.setPadding(0, 0, 0, 15)
        card.addView(durationText)

        // Status and button
        val activeBoost = GameState.getActiveBoost(boostType)
        val cooldown = GameState.getBoostCooldown(boostType)

        if (activeBoost != null && activeBoost.isActive()) {
            // Boost is active
            val statusText = TextView(this)
            val remainingMin = activeBoost.getRemainingMinutes()
            val remainingSec = activeBoost.getRemainingSeconds() % 60
            statusText.text = "✅ ACTIVE\nRemaining: ${remainingMin}m ${remainingSec}s"
            statusText.textSize = 18f
            statusText.setTextColor(Color.rgb(50, 255, 50))
            statusText.gravity = Gravity.CENTER
            statusText.setPadding(0, 10, 0, 10)
            card.addView(statusText)

        } else if (cooldown != null && cooldown.isOnCooldown()) {
            // On cooldown
            val statusText = TextView(this)
            val cooldownMin = cooldown.getRemainingCooldownMinutes()
            val cooldownSec = cooldown.getRemainingCooldownSeconds() % 60
            statusText.text = "⏳ ON COOLDOWN\nAvailable in: ${cooldownMin}m ${cooldownSec}s"
            statusText.textSize = 16f
            statusText.setTextColor(Color.rgb(255, 150, 50))
            statusText.gravity = Gravity.CENTER
            statusText.setPadding(0, 10, 0, 10)
            card.addView(statusText)

        } else {
            // Available - show button
            val activateButton = Button(this)
            activateButton.text = "🎬 Watch Ad to Activate"
            activateButton.textSize = 16f
            activateButton.setBackgroundColor(Color.rgb(50, 150, 50))
            activateButton.setTextColor(Color.WHITE)
            activateButton.setPadding(20, 20, 20, 20)
            activateButton.setOnClickListener {
                // Show ad
                AdManager.showRewardedAd(
                    activity = this,
                    onRewardEarned = {
                        // Activate boost
                        val success = GameState.activateBoost(boostType)
                        if (success) {
                            // Update UI immediately
                            updateBoostCards()

                            // Save state
                            GameState.saveState(this)
                        }
                    },
                    onAdFailed = { error ->
                        // Could show error toast here
                    }
                )
            }
            card.addView(activateButton)
        }

        return card
    }
}
