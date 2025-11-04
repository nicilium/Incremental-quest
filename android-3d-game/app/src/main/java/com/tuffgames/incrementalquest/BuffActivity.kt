package com.tuffgames.incrementalquest

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class BuffActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if a buff is available
        val buffType = GameState.availableBuffType
        if (buffType == null) {
            finish()
            return
        }

        // Fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Main layout
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(15, 15, 45))
        mainLayout.setPadding(40, 40, 40, 40)
        mainLayout.gravity = Gravity.CENTER

        // Title
        val title = TextView(this)
        title.text = "✨ BOOST AVAILABLE! ✨"
        title.textSize = 28f
        title.setTextColor(Color.rgb(255, 215, 0))
        title.gravity = Gravity.CENTER
        title.setPadding(0, 0, 0, 30)
        mainLayout.addView(title)

        // Buff details based on type
        val (buffTitle, buffDescription, buffColor) = when (buffType) {
            BuffType.DOUBLE_POINTS -> Triple(
                "⭐ Double Points",
                "2x points for 2 minutes!",
                Color.rgb(255, 215, 0)
            )
            BuffType.FASTER_AUTOCLICK -> Triple(
                "⚡ Fast Auto",
                "2x auto-click speed for 2 minutes!",
                Color.rgb(100, 200, 255)
            )
            BuffType.FREE_PAINT_CAN -> Triple(
                "✨ Free Essence",
                "Gain 1 Divine Essence instantly!",
                Color.rgb(150, 50, 150)
            )
            BuffType.RANDOM_UPGRADE -> Triple(
                "🎲 Free Upgrade",
                "Get a free color upgrade!",
                Color.rgb(50, 150, 50)
            )
        }

        // Buff card
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(30, 30, 30, 30)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 40)
        card.layoutParams = cardParams

        // Buff title
        val titleText = TextView(this)
        titleText.text = buffTitle
        titleText.textSize = 32f
        titleText.setTextColor(buffColor)
        titleText.gravity = Gravity.CENTER
        titleText.setPadding(0, 0, 0, 20)
        card.addView(titleText)

        // Buff description
        val descText = TextView(this)
        descText.text = buffDescription
        descText.textSize = 18f
        descText.setTextColor(Color.WHITE)
        descText.gravity = Gravity.CENTER
        descText.setPadding(0, 0, 0, 30)
        card.addView(descText)

        // Button container
        val buttonContainer = LinearLayout(this)
        buttonContainer.orientation = LinearLayout.VERTICAL
        buttonContainer.gravity = Gravity.CENTER

        // Normal button
        val normalButton = Button(this)
        normalButton.text = "✅ Claim"
        normalButton.textSize = 20f
        normalButton.setBackgroundColor(buffColor)
        normalButton.setTextColor(Color.WHITE)
        normalButton.setPadding(40, 20, 40, 20)
        normalButton.setOnClickListener {
            GameState.claimAvailableBuff(watchedAd = false)
            GameState.saveState(this)
            finish()
        }
        buttonContainer.addView(normalButton)

        // Ad button (only for time-based buffs)
        if (buffType == BuffType.DOUBLE_POINTS || buffType == BuffType.FASTER_AUTOCLICK) {
            val adButton = Button(this)
            adButton.text = "📺 Watch ad (4 minutes)"
            adButton.textSize = 16f
            adButton.setBackgroundColor(Color.rgb(200, 150, 0))
            adButton.setTextColor(Color.WHITE)
            adButton.setPadding(30, 15, 30, 15)
            val adParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            adParams.setMargins(0, 20, 0, 0)
            adButton.layoutParams = adParams
            adButton.setOnClickListener {
                // TODO: Implement ad logic later
                // For now: Activate directly with double time
                GameState.claimAvailableBuff(watchedAd = true)
                GameState.saveState(this)
                finish()
            }
            buttonContainer.addView(adButton)
        }

        card.addView(buttonContainer)
        mainLayout.addView(card)

        // Decline button
        val declineButton = Button(this)
        declineButton.text = "❌ Not now"
        declineButton.textSize = 18f
        declineButton.setBackgroundColor(Color.rgb(100, 100, 100))
        declineButton.setTextColor(Color.WHITE)
        declineButton.setPadding(30, 20, 30, 20)
        declineButton.setOnClickListener {
            // Buff stays available - not consumed
            finish()
        }
        mainLayout.addView(declineButton)

        setContentView(mainLayout)
    }
}
