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
import java.util.concurrent.TimeUnit

/**
 * QuestsActivity - Daily and Weekly Quest System
 *
 * Shows active daily and weekly quests with progress
 * Rewards include Gold, DE, Research Points, and Artifacts
 */
class QuestsActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var timerText: TextView
    private lateinit var dailyContainer: LinearLayout
    private lateinit var weeklyContainer: LinearLayout

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            GameState.processOfflineClicks()
            updateTimer()
            updateHandler.postDelayed(this, 1000) // Update every second
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
        title.text = "📜 QUESTS 📜"
        title.textSize = 32f
        title.setTextColor(Color.rgb(100, 200, 255))
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Timer text
        timerText = TextView(this)
        timerText.textSize = 16f
        timerText.setTextColor(Color.rgb(255, 200, 100))
        timerText.gravity = Gravity.CENTER
        timerText.setPadding(0, 15, 0, 10)
        mainLayout.addView(timerText)

        // Info text
        val infoText = TextView(this)
        infoText.text = "Complete daily and weekly quests for rewards!"
        infoText.textSize = 13f
        infoText.setTextColor(Color.LTGRAY)
        infoText.gravity = Gravity.CENTER
        infoText.setPadding(10, 10, 10, 20)
        mainLayout.addView(infoText)

        // Daily quests section
        val dailyHeader = TextView(this)
        dailyHeader.text = "━━━ DAILY QUESTS ━━━"
        dailyHeader.textSize = 18f
        dailyHeader.setTextColor(Color.rgb(150, 255, 150))
        dailyHeader.gravity = Gravity.CENTER
        dailyHeader.setPadding(0, 10, 0, 10)
        mainLayout.addView(dailyHeader)

        // Daily container
        dailyContainer = LinearLayout(this)
        dailyContainer.orientation = LinearLayout.VERTICAL
        mainLayout.addView(dailyContainer)

        // Weekly quests section
        val weeklyHeader = TextView(this)
        weeklyHeader.text = "━━━ WEEKLY QUEST ━━━"
        weeklyHeader.textSize = 18f
        weeklyHeader.setTextColor(Color.rgb(255, 215, 0))
        weeklyHeader.gravity = Gravity.CENTER
        weeklyHeader.setPadding(0, 20, 0, 10)
        mainLayout.addView(weeklyHeader)

        // ScrollView for weekly
        val scrollView = ScrollView(this)
        weeklyContainer = LinearLayout(this)
        weeklyContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(weeklyContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Populate quests
        populateQuests()

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
        updateTimer()
    }

    private fun populateQuests() {
        // Clear containers
        dailyContainer.removeAllViews()
        weeklyContainer.removeAllViews()

        // Get quests
        val dailyQuests = GameState.getDailyQuests()
        val weeklyQuest = GameState.getWeeklyQuest()

        // Add daily quests
        dailyQuests.forEach { quest ->
            dailyContainer.addView(createQuestCard(quest))
        }

        // Add weekly quest
        if (weeklyQuest != null) {
            weeklyContainer.addView(createQuestCard(weeklyQuest))
        }
    }

    private fun createQuestCard(quest: Quest): LinearLayout {
        val isCompleted = quest.completed
        val progress = quest.progress.toFloat() / quest.goal.toFloat()

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(
            if (isCompleted) Color.rgb(20, 60, 20)
            else Color.rgb(30, 30, 60)
        )
        card.setPadding(15, 15, 15, 15)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 10)
        card.layoutParams = cardParams

        // Quest name
        val nameText = TextView(this)
        nameText.text = quest.type.displayName
        nameText.textSize = 20f
        nameText.setTextColor(Color.WHITE)
        nameText.gravity = Gravity.CENTER
        card.addView(nameText)

        // Description
        val descText = TextView(this)
        descText.text = quest.type.description
        descText.textSize = 14f
        descText.setTextColor(Color.LTGRAY)
        descText.gravity = Gravity.CENTER
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        // Progress
        if (!isCompleted) {
            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            progressBar.max = 100
            progressBar.progress = (progress * 100).toInt().coerceAtMost(100)
            progressBar.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                30
            )
            card.addView(progressBar)

            val progressText = TextView(this)
            progressText.text = "${quest.progress} / ${quest.goal}"
            progressText.textSize = 14f
            progressText.setTextColor(Color.rgb(150, 255, 150))
            progressText.gravity = Gravity.CENTER
            progressText.setPadding(0, 5, 0, 10)
            card.addView(progressText)
        }

        // Reward
        val reward = quest.type.getReward()
        val rewardText = TextView(this)
        val rewardParts = mutableListOf<String>()
        if (reward.gold > 0) rewardParts.add("${reward.gold} Gold")
        if (reward.de > 0) rewardParts.add("${reward.de} DE")
        if (reward.researchPoints > 0) rewardParts.add("${reward.researchPoints.toInt()} RP")
        if (reward.artifact) {
            val rarity = reward.artifactRarity?.displayName ?: "Random"
            rewardParts.add("$rarity Artifact")
        }
        rewardText.text = "Reward: ${rewardParts.joinToString(", ")}"
        rewardText.textSize = 14f
        rewardText.setTextColor(Color.rgb(100, 255, 100))
        rewardText.gravity = Gravity.CENTER
        rewardText.setPadding(0, 10, 0, 10)
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
        } else if (quest.progress >= quest.goal) {
            // Claim button
            val claimButton = Button(this)
            claimButton.text = "🎉 CLAIM REWARD 🎉"
            claimButton.textSize = 16f
            claimButton.setBackgroundColor(Color.rgb(200, 150, 50))
            claimButton.setTextColor(Color.WHITE)
            claimButton.setOnClickListener {
                GameState.claimQuestReward(quest)
                populateQuests() // Refresh
            }
            card.addView(claimButton)
        }

        return card
    }

    private fun updateTimer() {
        // Calculate time until daily reset
        val timeUntilDailyReset = GameState.getTimeUntilDailyReset()
        val hours = TimeUnit.MILLISECONDS.toHours(timeUntilDailyReset)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeUntilDailyReset) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeUntilDailyReset) % 60

        timerText.text = String.format("⏰ Daily Reset In: %02d:%02d:%02d", hours, minutes, seconds)
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
