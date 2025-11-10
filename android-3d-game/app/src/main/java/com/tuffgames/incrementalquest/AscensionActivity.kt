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
 * AscensionActivity - Second-layer prestige system
 *
 * Allows player to spend Divine Essence to gain Celestial Essence (CE)
 * and purchase powerful permanent upgrades
 */
class AscensionActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var ceText: TextView
    private lateinit var deText: TextView
    private lateinit var ascensionButton: Button
    private lateinit var upgradeContainer: LinearLayout

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            GameState.processOfflineClicks()
            updateUI()
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
        title.text = "🌟 ASCENSION 🌟"
        title.textSize = 32f
        title.setTextColor(Color.rgb(255, 215, 0))
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Celestial Essence display
        ceText = TextView(this)
        ceText.textSize = 24f
        ceText.setTextColor(Color.rgb(100, 255, 255))
        ceText.gravity = Gravity.CENTER
        ceText.setPadding(0, 20, 0, 10)
        mainLayout.addView(ceText)

        // Divine Essence display
        deText = TextView(this)
        deText.textSize = 20f
        deText.setTextColor(Color.rgb(255, 215, 0))
        deText.gravity = Gravity.CENTER
        deText.setPadding(0, 5, 0, 10)
        mainLayout.addView(deText)

        // Ascensions count
        val ascensionsText = TextView(this)
        ascensionsText.text = "Total Ascensions: ${GameState.ascensions}"
        ascensionsText.textSize = 16f
        ascensionsText.setTextColor(Color.LTGRAY)
        ascensionsText.gravity = Gravity.CENTER
        ascensionsText.setPadding(0, 5, 0, 15)
        mainLayout.addView(ascensionsText)

        // Info text
        val explanationText = TextView(this)
        explanationText.text = "Ascension is the ultimate meta-progression!\nSpend Divine Essence to gain Celestial Essence.\nYou'll lose ALL DE, but gain powerful permanent upgrades!"
        explanationText.textSize = 13f
        explanationText.setTextColor(Color.LTGRAY)
        explanationText.gravity = Gravity.CENTER
        explanationText.setPadding(10, 10, 10, 20)
        mainLayout.addView(explanationText)

        // Ascension button
        ascensionButton = Button(this)
        ascensionButton.textSize = 18f
        ascensionButton.setPadding(15, 20, 15, 20)
        ascensionButton.setOnClickListener {
            if (GameState.performAscension()) {
                recreate() // Refresh entire UI after ascension
            }
        }
        mainLayout.addView(ascensionButton)

        // Divider
        val divider = TextView(this)
        divider.text = "━━━━━ UPGRADES ━━━━━"
        divider.textSize = 18f
        divider.setTextColor(Color.rgb(150, 150, 255))
        divider.gravity = Gravity.CENTER
        divider.setPadding(0, 20, 0, 10)
        mainLayout.addView(divider)

        // ScrollView for upgrades
        val scrollView = ScrollView(this)
        upgradeContainer = LinearLayout(this)
        upgradeContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(upgradeContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Populate upgrades
        populateUpgrades()

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
        updateUI()
    }

    private fun populateUpgrades() {
        upgradeContainer.removeAllViews()

        // Add all ascension upgrades
        AscensionUpgrade.values().forEach { upgrade ->
            upgradeContainer.addView(createUpgradeCard(upgrade))
        }
    }

    private fun createUpgradeCard(upgrade: AscensionUpgrade): LinearLayout {
        val currentLevel = GameState.getAscensionUpgradeLevel(upgrade)
        val maxLevel = upgrade.maxLevel
        val isMaxLevel = currentLevel >= maxLevel

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(
            if (isMaxLevel) Color.rgb(20, 60, 20)
            else Color.rgb(30, 30, 60)
        )
        card.setPadding(15, 15, 15, 15)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 15)
        card.layoutParams = cardParams

        // Title
        val titleText = TextView(this)
        titleText.text = upgrade.displayName
        titleText.textSize = 20f
        titleText.setTextColor(Color.WHITE)
        titleText.gravity = Gravity.CENTER
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = upgrade.description
        descText.textSize = 14f
        descText.setTextColor(Color.LTGRAY)
        descText.gravity = Gravity.CENTER
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        // Level info
        val levelText = TextView(this)
        levelText.text = "Level: $currentLevel / $maxLevel"
        levelText.textSize = 16f
        levelText.setTextColor(Color.rgb(150, 255, 150))
        levelText.gravity = Gravity.CENTER
        levelText.setPadding(0, 5, 0, 10)
        card.addView(levelText)

        if (isMaxLevel) {
            // Max level indicator
            val maxText = TextView(this)
            maxText.text = "✨ MAX LEVEL ✨"
            maxText.textSize = 18f
            maxText.setTextColor(Color.rgb(255, 215, 0))
            maxText.gravity = Gravity.CENTER
            maxText.setPadding(0, 10, 0, 10)
            card.addView(maxText)
        } else {
            // Cost and buy button
            val cost = GameState.getAscensionUpgradeCost(upgrade)
            val canAfford = GameState.canAffordAscensionUpgrade(upgrade)

            val costText = TextView(this)
            costText.text = "Cost: $cost CE"
            costText.textSize = 16f
            costText.setTextColor(Color.rgb(100, 255, 255))
            costText.gravity = Gravity.CENTER
            costText.setPadding(0, 5, 0, 10)
            card.addView(costText)

            val buyButton = Button(this)
            buyButton.text = "BUY LEVEL ${currentLevel + 1}"
            buyButton.textSize = 16f
            buyButton.setBackgroundColor(
                if (canAfford) Color.rgb(50, 150, 50)
                else Color.rgb(100, 100, 100)
            )
            buyButton.setTextColor(Color.WHITE)
            buyButton.setOnClickListener {
                if (GameState.buyAscensionUpgrade(upgrade)) {
                    populateUpgrades()
                    updateUI()
                }
            }
            card.addView(buyButton)
        }

        return card
    }

    private fun updateUI() {
        // Update CE display
        ceText.text = "💎 Celestial Essence: ${GameState.celestialEssence}\n(Total earned: ${GameState.totalCelestialEssenceEarned})"

        // Update DE display
        deText.text = "✨ Divine Essence: ${GameState.divineEssence}"

        // Update ascension button
        val cost = GameState.getAscensionCost()
        val canAscend = GameState.canAscend()

        if (canAscend) {
            val ceGain = 1 + (GameState.divineEssence / 1000)
            ascensionButton.text = "🌟 ASCEND 🌟\n(Cost: $cost DE → Gain $ceGain CE)"
            ascensionButton.setBackgroundColor(Color.rgb(200, 100, 200))
            ascensionButton.setTextColor(Color.WHITE)
            ascensionButton.isEnabled = true
        } else {
            ascensionButton.text = "Need $cost Divine Essence to Ascend"
            ascensionButton.setBackgroundColor(Color.rgb(100, 100, 100))
            ascensionButton.setTextColor(Color.DKGRAY)
            ascensionButton.isEnabled = false
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
