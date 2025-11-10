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
 * ChallengesActivity - Challenge System
 *
 * Shows available challenges with special rules and rewards
 * Players can start/complete challenges for permanent bonuses
 */
class ChallengesActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var challengeContainer: LinearLayout

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            GameState.processOfflineClicks()
            updateHandler.postDelayed(this, 100)
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
        title.text = "⚔️ CHALLENGES ⚔️"
        title.textSize = 32f
        title.setTextColor(Color.rgb(255, 100, 100))
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Info text
        val infoText = TextView(this)
        infoText.text = "Complete challenges for permanent bonuses!\nChallenges add special rules and difficulty."
        infoText.textSize = 13f
        infoText.setTextColor(Color.LTGRAY)
        infoText.gravity = Gravity.CENTER
        infoText.setPadding(10, 15, 10, 20)
        mainLayout.addView(infoText)

        // Active challenge display
        val activeChallenge = GameState.getActiveChallenge()
        if (activeChallenge != null) {
            val activeCard = createActiveChallengeCard(activeChallenge)
            mainLayout.addView(activeCard)
        }

        // Divider
        val divider = TextView(this)
        divider.text = "━━━ ALL CHALLENGES ━━━"
        divider.textSize = 18f
        divider.setTextColor(Color.rgb(150, 150, 255))
        divider.gravity = Gravity.CENTER
        divider.setPadding(0, 15, 0, 10)
        mainLayout.addView(divider)

        // ScrollView for challenges
        val scrollView = ScrollView(this)
        challengeContainer = LinearLayout(this)
        challengeContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(challengeContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Populate challenges
        populateChallenges()

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
    }

    private fun createActiveChallengeCard(challengeType: ChallengeType): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(100, 50, 50))
        card.setPadding(15, 15, 15, 15)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 20)
        card.layoutParams = cardParams

        // Active indicator
        val activeText = TextView(this)
        activeText.text = "🔥 ACTIVE CHALLENGE 🔥"
        activeText.textSize = 20f
        activeText.setTextColor(Color.rgb(255, 200, 100))
        activeText.gravity = Gravity.CENTER
        card.addView(activeText)

        // Challenge name
        val nameText = TextView(this)
        nameText.text = challengeType.displayName
        nameText.textSize = 22f
        nameText.setTextColor(Color.WHITE)
        nameText.gravity = Gravity.CENTER
        nameText.setPadding(0, 10, 0, 5)
        card.addView(nameText)

        // Goal
        val goalText = TextView(this)
        goalText.text = "Goal: ${challengeType.goal}"
        goalText.textSize = 14f
        goalText.setTextColor(Color.rgb(150, 255, 150))
        goalText.gravity = Gravity.CENTER
        goalText.setPadding(0, 5, 0, 10)
        card.addView(goalText)

        // Abandon button
        val abandonButton = Button(this)
        abandonButton.text = "Abandon Challenge"
        abandonButton.textSize = 14f
        abandonButton.setBackgroundColor(Color.rgb(150, 50, 50))
        abandonButton.setTextColor(Color.WHITE)
        abandonButton.setOnClickListener {
            GameState.abandonChallenge()
            recreate() // Refresh UI
        }
        card.addView(abandonButton)

        return card
    }

    private fun populateChallenges() {
        challengeContainer.removeAllViews()

        ChallengeType.values().forEach { challengeType ->
            challengeContainer.addView(createChallengeCard(challengeType))
        }
    }

    private fun createChallengeCard(challengeType: ChallengeType): LinearLayout {
        val isCompleted = GameState.isChallengeCompleted(challengeType)
        val isActive = GameState.getActiveChallenge() == challengeType

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(
            when {
                isCompleted -> Color.rgb(20, 60, 20)
                isActive -> Color.rgb(60, 40, 40)
                else -> Color.rgb(30, 30, 60)
            }
        )
        card.setPadding(15, 15, 15, 15)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 10)
        card.layoutParams = cardParams

        // Challenge name
        val nameText = TextView(this)
        nameText.text = challengeType.displayName
        nameText.textSize = 20f
        nameText.setTextColor(Color.WHITE)
        nameText.gravity = Gravity.CENTER
        card.addView(nameText)

        // Description
        val descText = TextView(this)
        descText.text = challengeType.description
        descText.textSize = 14f
        descText.setTextColor(Color.LTGRAY)
        descText.gravity = Gravity.CENTER
        descText.setPadding(0, 5, 0, 5)
        card.addView(descText)

        // Goal
        val goalText = TextView(this)
        goalText.text = "Goal: ${challengeType.goal}"
        goalText.textSize = 14f
        goalText.setTextColor(Color.rgb(255, 200, 100))
        goalText.gravity = Gravity.CENTER
        goalText.setPadding(0, 5, 0, 5)
        card.addView(goalText)

        // Reward
        val rewardText = TextView(this)
        rewardText.text = "Reward: ${challengeType.reward}"
        rewardText.textSize = 14f
        rewardText.setTextColor(Color.rgb(100, 255, 100))
        rewardText.gravity = Gravity.CENTER
        rewardText.setPadding(0, 5, 0, 15)
        card.addView(rewardText)

        if (isCompleted) {
            // Completed indicator
            val completedText = TextView(this)
            completedText.text = "✅ COMPLETED"
            completedText.textSize = 18f
            completedText.setTextColor(Color.rgb(100, 255, 100))
            completedText.gravity = Gravity.CENTER
            completedText.setPadding(0, 10, 0, 10)
            card.addView(completedText)
        } else if (isActive) {
            // Active indicator
            val activeText = TextView(this)
            activeText.text = "🔥 ACTIVE"
            activeText.textSize = 18f
            activeText.setTextColor(Color.rgb(255, 150, 50))
            activeText.gravity = Gravity.CENTER
            activeText.setPadding(0, 10, 0, 10)
            card.addView(activeText)
        } else {
            // Start button
            val hasActiveChallenge = GameState.getActiveChallenge() != null

            val startButton = Button(this)
            startButton.text = if (hasActiveChallenge) "Another Challenge Active" else "START CHALLENGE"
            startButton.textSize = 16f

            if (!hasActiveChallenge) {
                startButton.setBackgroundColor(Color.rgb(150, 50, 50))
                startButton.setTextColor(Color.WHITE)
                startButton.setOnClickListener {
                    GameState.startChallenge(challengeType)
                    recreate() // Refresh UI
                }
            } else {
                startButton.setBackgroundColor(Color.rgb(100, 100, 100))
                startButton.setTextColor(Color.DKGRAY)
                startButton.isEnabled = false
            }

            card.addView(startButton)
        }

        return card
    }

    override fun onResume() {
        super.onResume()
        updateHandler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        updateHandler.removeCallbacks(updateRunnable)
        GameState.markActiveTime()
        GameState.saveState(this)
    }
}
