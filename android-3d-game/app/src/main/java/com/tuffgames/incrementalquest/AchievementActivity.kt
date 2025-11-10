package com.tuffgames.incrementalquest

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class AchievementActivity : Activity() {

    private lateinit var mainLayout: LinearLayout

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
        title.text = "🏆 ACHIEVEMENTS"
        title.textSize = 32f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Stats summary
        val unlockedCount = GameState.getUnlockedAchievements().size
        val totalCount = AchievementType.values().size
        val statsText = TextView(this)
        statsText.text = "Unlocked: $unlockedCount / $totalCount"
        statsText.textSize = 18f
        statsText.setTextColor(Color.rgb(255, 215, 0))
        statsText.gravity = Gravity.CENTER
        statsText.setPadding(0, 10, 0, 10)
        mainLayout.addView(statsText)

        // Total bonus display
        val clickBonus = GameState.getAchievementClickBonus()
        val passiveBonus = GameState.getAchievementPassiveBonus()
        val bonusText = TextView(this)
        bonusText.text = String.format("Total Bonuses: +%.0f%% Clicks, +%.0f%% Passive",
            clickBonus * 100, passiveBonus * 100)
        bonusText.textSize = 16f
        bonusText.setTextColor(Color.rgb(100, 255, 100))
        bonusText.gravity = Gravity.CENTER
        bonusText.setPadding(0, 5, 0, 20)
        mainLayout.addView(bonusText)

        // ScrollView for achievements
        val scrollView = ScrollView(this)
        val achievementContainer = LinearLayout(this)
        achievementContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(achievementContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Add achievement cards (unlocked first, then locked)
        val unlocked = GameState.getUnlockedAchievements()
        val locked = GameState.getLockedAchievements()

        // Unlocked achievements
        if (unlocked.isNotEmpty()) {
            val unlockedHeader = createHeaderText("✅ UNLOCKED")
            achievementContainer.addView(unlockedHeader)
            unlocked.forEach { achievement ->
                achievementContainer.addView(createAchievementCard(achievement, true))
            }
        }

        // Locked achievements
        if (locked.isNotEmpty()) {
            val lockedHeader = createHeaderText("🔒 LOCKED")
            achievementContainer.addView(lockedHeader)
            locked.forEach { achievement ->
                achievementContainer.addView(createAchievementCard(achievement, false))
            }
        }

        // Back button
        val backButton = Button(this)
        backButton.text = "← BACK"
        backButton.textSize = 18f
        backButton.setBackgroundColor(Color.rgb(80, 80, 80))
        backButton.setTextColor(Color.WHITE)
        backButton.setOnClickListener { finish() }
        mainLayout.addView(backButton)

        setContentView(mainLayout)
    }

    private fun createHeaderText(text: String): TextView {
        val header = TextView(this)
        header.text = text
        header.textSize = 22f
        header.setTextColor(Color.rgb(255, 215, 0))
        header.gravity = Gravity.CENTER
        header.setPadding(0, 20, 0, 15)
        return header
    }

    private fun createAchievementCard(achievement: AchievementType, isUnlocked: Boolean): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(if (isUnlocked) Color.rgb(30, 60, 30) else Color.rgb(30, 30, 30))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 15)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        val icon = if (isUnlocked) "✅" else "🔒"
        titleText.text = "$icon ${achievement.displayName}"
        titleText.textSize = 20f
        titleText.setTextColor(if (isUnlocked) Color.rgb(100, 255, 100) else Color.rgb(150, 150, 150))
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = achievement.description
        descText.textSize = 14f
        descText.setTextColor(if (isUnlocked) Color.WHITE else Color.rgb(120, 120, 120))
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        // Rewards
        val rewardParts = mutableListOf<String>()
        if (achievement.clickBonus > 0) {
            rewardParts.add(String.format("+%.0f%% Clicks", achievement.clickBonus * 100))
        }
        if (achievement.passiveBonus > 0) {
            rewardParts.add(String.format("+%.0f%% Passive", achievement.passiveBonus * 100))
        }
        if (rewardParts.isNotEmpty()) {
            val rewardText = TextView(this)
            rewardText.text = "Reward: ${rewardParts.joinToString(", ")}"
            rewardText.textSize = 14f
            rewardText.setTextColor(Color.rgb(255, 215, 0))
            card.addView(rewardText)
        }

        // Progress hint for locked achievements
        if (!isUnlocked) {
            val progressText = getProgressText(achievement)
            if (progressText != null) {
                val progressView = TextView(this)
                progressView.text = progressText
                progressView.textSize = 12f
                progressView.setTextColor(Color.rgb(100, 200, 255))
                progressView.setPadding(0, 5, 0, 0)
                card.addView(progressView)
            }
        }

        return card
    }

    private fun getProgressText(achievement: AchievementType): String? {
        return when (achievement) {
            AchievementType.FIRST_CLICK -> null
            AchievementType.CLICKER_100 -> "Progress: ${GameState.totalClicksAllTime} / 100"
            AchievementType.CLICKER_1000 -> "Progress: ${GameState.totalClicksAllTime} / 1,000"
            AchievementType.CLICKER_10000 -> "Progress: ${GameState.totalClicksAllTime} / 10,000"
            AchievementType.CLICKER_100000 -> "Progress: ${GameState.totalClicksAllTime} / 100,000"
            AchievementType.FIRST_PRESTIGE -> null
            AchievementType.PRESTIGE_10 -> "Progress: ${GameState.prestigesClaimed} / 10"
            AchievementType.PRESTIGE_50 -> "Progress: ${GameState.prestigesClaimed} / 50"
            AchievementType.UNLOCK_D20 -> if (GameState.d20Active) null else "Unlock D20 in Prestige menu"
            AchievementType.ALL_COLORS_LEVEL_10 -> {
                val colors = GameState.getAvailableColors()
                val at10 = colors.count { GameState.getUpgradeLevel(it) >= 10 }
                "Progress: $at10 / ${colors.size} colors at Level 10+"
            }
            AchievementType.LIFETIME_100K -> "Progress: ${GameState.lifetimeScore.toLong()} / 100,000"
            AchievementType.LIFETIME_1M -> "Progress: ${GameState.lifetimeScore.toLong()} / 1,000,000"
            AchievementType.LIFETIME_10M -> "Progress: ${GameState.lifetimeScore.toLong()} / 10,000,000"
            AchievementType.FIRST_COMBAT -> null
            AchievementType.COMBAT_10 -> {
                val total = GameState.storyCompletedCount + GameState.auftragCompletedCount
                "Progress: $total / 10 combats won"
            }
            AchievementType.COMBO_10 -> "Progress: ${GameState.maxComboReached} / 10"
            AchievementType.COMBO_50 -> "Progress: ${GameState.maxComboReached} / 50"
            AchievementType.COMBO_100 -> "Progress: ${GameState.maxComboReached} / 100"
            AchievementType.UNLOCK_SYNERGIES -> {
                val at5 = GameState.getAvailableColors().count { GameState.getUpgradeLevel(it) >= 5 }
                "Progress: $at5 / 3 colors at Level 5+"
            }
        }
    }
}
