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
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView

/**
 * MilestonesActivity - Lifetime Achievement System
 *
 * Shows progress toward lifetime point milestones
 * Milestones provide permanent bonuses
 */
class MilestonesActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var lifetimeText: TextView
    private lateinit var milestoneContainer: LinearLayout

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            GameState.processOfflineClicks()
            updateLifetimeScore()
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
        title.text = "🏆 MILESTONES 🏆"
        title.textSize = 32f
        title.setTextColor(Color.rgb(255, 215, 0))
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Lifetime score display
        lifetimeText = TextView(this)
        lifetimeText.textSize = 20f
        lifetimeText.setTextColor(Color.rgb(150, 255, 150))
        lifetimeText.gravity = Gravity.CENTER
        lifetimeText.setPadding(0, 15, 0, 10)
        mainLayout.addView(lifetimeText)

        // Info text
        val infoText = TextView(this)
        infoText.text = "Reach lifetime point goals for permanent bonuses!\nMilestones never reset."
        infoText.textSize = 13f
        infoText.setTextColor(Color.LTGRAY)
        infoText.gravity = Gravity.CENTER
        infoText.setPadding(10, 10, 10, 20)
        mainLayout.addView(infoText)

        // ScrollView for milestones
        val scrollView = ScrollView(this)
        milestoneContainer = LinearLayout(this)
        milestoneContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(milestoneContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Populate milestones
        populateMilestones()

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
        updateLifetimeScore()
    }

    private fun populateMilestones() {
        milestoneContainer.removeAllViews()

        LIFETIME_MILESTONES.forEach { milestone ->
            milestoneContainer.addView(createMilestoneCard(milestone))
        }
    }

    private fun createMilestoneCard(milestone: Milestone): LinearLayout {
        val isClaimed = GameState.isMilestoneClaimed(milestone)
        val currentLifetime = GameState.lifetimeScore.toLong()
        val progress = (currentLifetime.toFloat() / milestone.lifetimePoints.toFloat()).coerceAtMost(1f)

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(
            if (isClaimed) Color.rgb(20, 60, 20)
            else Color.rgb(30, 30, 60)
        )
        card.setPadding(15, 15, 15, 15)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 15)
        card.layoutParams = cardParams

        // Milestone name
        val nameText = TextView(this)
        nameText.text = milestone.displayName
        nameText.textSize = 22f
        nameText.setTextColor(Color.rgb(255, 215, 0))
        nameText.gravity = Gravity.CENTER
        card.addView(nameText)

        // Goal
        val goalText = TextView(this)
        goalText.text = "Goal: ${formatNumber(milestone.lifetimePoints)} lifetime points"
        goalText.textSize = 16f
        goalText.setTextColor(Color.LTGRAY)
        goalText.gravity = Gravity.CENTER
        goalText.setPadding(0, 5, 0, 10)
        card.addView(goalText)

        // Progress bar
        if (!isClaimed) {
            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            progressBar.max = 100
            progressBar.progress = (progress * 100).toInt()
            progressBar.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                30
            )
            card.addView(progressBar)

            // Progress text
            val progressText = TextView(this)
            progressText.text = String.format("%.1f%% Complete", progress * 100)
            progressText.textSize = 14f
            progressText.setTextColor(Color.rgb(150, 255, 150))
            progressText.gravity = Gravity.CENTER
            progressText.setPadding(0, 5, 0, 10)
            card.addView(progressText)
        }

        // Reward
        val rewardText = TextView(this)
        rewardText.text = "Reward: ${milestone.reward}"
        rewardText.textSize = 14f
        rewardText.setTextColor(Color.rgb(100, 255, 100))
        rewardText.gravity = Gravity.CENTER
        rewardText.setPadding(0, 10, 0, 10)
        card.addView(rewardText)

        if (isClaimed) {
            // Claimed indicator
            val claimedText = TextView(this)
            claimedText.text = "✅ CLAIMED"
            claimedText.textSize = 18f
            claimedText.setTextColor(Color.rgb(100, 255, 100))
            claimedText.gravity = Gravity.CENTER
            claimedText.setPadding(0, 10, 0, 10)
            card.addView(claimedText)
        } else if (currentLifetime >= milestone.lifetimePoints) {
            // Claim button
            val claimButton = Button(this)
            claimButton.text = "🎉 CLAIM REWARD 🎉"
            claimButton.textSize = 16f
            claimButton.setBackgroundColor(Color.rgb(200, 150, 50))
            claimButton.setTextColor(Color.WHITE)
            claimButton.setOnClickListener {
                GameState.claimMilestone(milestone)
                populateMilestones() // Refresh
            }
            card.addView(claimButton)
        } else {
            // Not yet reached
            val remainingText = TextView(this)
            val remaining = milestone.lifetimePoints - currentLifetime
            remainingText.text = "${formatNumber(remaining)} more points needed"
            remainingText.textSize = 14f
            remainingText.setTextColor(Color.rgb(255, 200, 100))
            remainingText.gravity = Gravity.CENTER
            remainingText.setPadding(0, 10, 0, 10)
            card.addView(remainingText)
        }

        return card
    }

    private fun updateLifetimeScore() {
        val lifetime = GameState.lifetimeScore
        lifetimeText.text = "⭐ Lifetime Score: ${formatNumber(lifetime.toLong())}"
    }

    private fun formatNumber(value: Long): String {
        return when {
            value >= 1_000_000_000 -> String.format("%.2fB", value / 1_000_000_000.0)
            value >= 1_000_000 -> String.format("%.2fM", value / 1_000_000.0)
            value >= 1_000 -> String.format("%.2fK", value / 1_000.0)
            else -> value.toString()
        }
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
