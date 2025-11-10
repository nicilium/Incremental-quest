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

class PrestigeActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var scoreText: TextView
    private lateinit var lifetimeText: TextView
    private lateinit var progressText: TextView
    private lateinit var paintCanText: TextView
    private lateinit var prestigeButton: Button
    private lateinit var infoContainer: LinearLayout

    private var showingUpgrades = false

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Offline-Klicks verarbeiten
            GameState.processOfflineClicks()

            // UI aktualisieren (aber nur wenn im Prestige-View, nicht im Upgrade-View)
            if (!showingUpgrades) {
                updateScore()
                updateLifetimeScore()
                updateProgress()
                updatePrestigeButton()
                createColorInfo()
            }

            // Nächstes Update in 100ms
            updateHandler.postDelayed(this, 100)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Vollbild-Modus
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Haupt-Layout
        mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(15, 15, 45))
        mainLayout.setPadding(20, 20, 20, 20)

        setContentView(mainLayout)
        showPrestigeView()
    }

    private fun showPrestigeView() {
        showingUpgrades = false
        mainLayout.removeAllViews()

        // Titel
        val title = TextView(this)
        title.text = "PRESTIGE"
        title.textSize = 32f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Divine Essence display
        paintCanText = TextView(this)
        paintCanText.textSize = 24f
        paintCanText.setTextColor(Color.rgb(255, 215, 0))
        paintCanText.gravity = Gravity.CENTER
        paintCanText.setPadding(0, 20, 0, 10)
        updatePaintCanText()
        mainLayout.addView(paintCanText)

        // Score-Anzeige
        scoreText = TextView(this)
        scoreText.textSize = 18f
        scoreText.setTextColor(Color.WHITE)
        scoreText.gravity = Gravity.CENTER
        scoreText.setPadding(0, 10, 0, 5)
        updateScore()
        mainLayout.addView(scoreText)

        // Lifetime Score-Anzeige
        lifetimeText = TextView(this)
        lifetimeText.textSize = 18f
        lifetimeText.setTextColor(Color.rgb(150, 255, 150))
        lifetimeText.gravity = Gravity.CENTER
        lifetimeText.setPadding(0, 5, 0, 5)
        updateLifetimeScore()
        mainLayout.addView(lifetimeText)

        // Fortschritt bis zum nächsten Prestige
        progressText = TextView(this)
        progressText.textSize = 16f
        progressText.setTextColor(Color.rgb(255, 200, 100))
        progressText.gravity = Gravity.CENTER
        progressText.setPadding(0, 5, 0, 20)
        updateProgress()
        mainLayout.addView(progressText)

        // Info text
        val explanationText = TextView(this)
        explanationText.text = "Earn 1 Divine Essence per 1000 lifetime points.\nPrestige resets your progress.\nEssence provides permanent bonuses!"
        explanationText.textSize = 13f
        explanationText.setTextColor(Color.LTGRAY)
        explanationText.gravity = Gravity.CENTER
        explanationText.setPadding(10, 10, 10, 20)
        mainLayout.addView(explanationText)

        // ScrollView für Info
        val scrollView = ScrollView(this)
        infoContainer = LinearLayout(this)
        infoContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(infoContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Prestige button
        prestigeButton = Button(this)
        prestigeButton.textSize = 20f
        prestigeButton.setPadding(20, 20, 20, 20)
        updatePrestigeButton()
        prestigeButton.setOnClickListener {
            if (GameState.performPrestige()) {
                updateAll()
            }
        }
        mainLayout.addView(prestigeButton)

        // Upgrades button
        val upgradeButton = Button(this)
        upgradeButton.text = "⭐ UPGRADES"
        upgradeButton.setBackgroundColor(Color.rgb(100, 50, 150))
        upgradeButton.setTextColor(Color.WHITE)
        upgradeButton.setOnClickListener {
            showUpgradeView()
        }
        mainLayout.addView(upgradeButton)

        // Back button
        val backButton = Button(this)
        backButton.text = "← Back"
        backButton.setOnClickListener { finish() }
        mainLayout.addView(backButton)

        createColorInfo()
    }

    private fun showUpgradeView() {
        showingUpgrades = true
        mainLayout.removeAllViews()

        // Titel
        val title = TextView(this)
        title.text = "PRESTIGE UPGRADES"
        title.textSize = 28f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Divine Essence display
        val essenceText = TextView(this)
        essenceText.text = "✨ Divine Essence: ${GameState.divineEssence} / ${GameState.totalDivineEssenceEarned}\n(Available / Total earned)"
        essenceText.textSize = 20f
        essenceText.setTextColor(Color.rgb(255, 215, 0))
        essenceText.gravity = Gravity.CENTER
        essenceText.setPadding(0, 20, 0, 20)
        mainLayout.addView(essenceText)

        // ScrollView für Upgrades
        val scrollView = ScrollView(this)
        val upgradeContainer = LinearLayout(this)
        upgradeContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(upgradeContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // D6-Upgrade (always shown, unless already active)
        if (!GameState.d6Active) {
            val d6Card = createD6Card()
            upgradeContainer.addView(d6Card)
        }

        // D8-Upgrade (nur wenn D6 aktiv)
        if (GameState.d6Active && !GameState.d8Active) {
            val d8Card = createD8Card()
            upgradeContainer.addView(d8Card)
        }

        // D10-Upgrade (nur wenn D8 aktiv)
        if (GameState.d8Active && !GameState.d10Active) {
            val d10Card = createD10Card()
            upgradeContainer.addView(d10Card)
        }

        // D12-Upgrade (nur wenn D10 aktiv)
        if (GameState.d10Active && !GameState.d12Active) {
            val d12Card = createD12Card()
            upgradeContainer.addView(d12Card)
        }

        // D20-Upgrade (nur wenn D12 aktiv)
        if (GameState.d12Active && !GameState.d20Active) {
            val d20Card = createD20Card()
            upgradeContainer.addView(d20Card)
        }

        // Show current die info if all are purchased
        val currentDieInfo = TextView(this)
        val currentDie = when {
            GameState.d20Active -> "D20"
            GameState.d12Active -> "D12"
            GameState.d10Active -> "D10"
            GameState.d8Active -> "D8"
            GameState.d6Active -> "D6"
            else -> "D4"
        }
        currentDieInfo.text = "🎲 Current Die: $currentDie"
        currentDieInfo.textSize = 18f
        currentDieInfo.setTextColor(Color.rgb(255, 215, 0))
        currentDieInfo.gravity = Gravity.CENTER
        currentDieInfo.setPadding(0, 20, 0, 10)
        upgradeContainer.addView(currentDieInfo)

        // Autoklicker-Geschwindigkeit Upgrade (nur wenn Autoklicker aktiv)
        if (GameState.autoClickerActive) {
            val speedCard = createAutoClickerSpeedCard()
            upgradeContainer.addView(speedCard)
        }

        // NEW: Permanent Click Multiplier (Gold-based)
        val multiplierCard = createPermanentMultiplierCard()
        upgradeContainer.addView(multiplierCard)

        // Permanente Farb-Upgrades (bleiben beim Prestige!)
        val permanentUpgradesCard = createPermanentColorUpgradesCard()
        upgradeContainer.addView(permanentUpgradesCard)

        // Back button
        val backButton = Button(this)
        backButton.text = "← Back"
        backButton.setOnClickListener {
            showPrestigeView()
        }
        mainLayout.addView(backButton)
    }

    private fun createD10Card(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        titleText.text = "🎲 D10 Die"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(255, 100, 255))
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "Unlocks 4 new colors"
        descText.textSize = 16f
        descText.setTextColor(Color.LTGRAY)
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        if (GameState.d10Active) {
            // Already purchased
            val activeText = TextView(this)
            activeText.text = "✅ UNLOCKED"
            activeText.textSize = 18f
            activeText.setTextColor(Color.rgb(100, 255, 100))
            activeText.gravity = Gravity.CENTER
            activeText.setPadding(0, 10, 0, 10)
            card.addView(activeText)
        } else {
            // Buy button
            val buyButton = Button(this)
            buyButton.text = "Buy (25 Divine Essence)"
            buyButton.textSize = 16f

            if (GameState.canAffordD10()) {
                buyButton.setBackgroundColor(Color.rgb(50, 150, 50))
            } else {
                buyButton.setBackgroundColor(Color.rgb(100, 100, 100))
            }

            buyButton.setOnClickListener {
                if (GameState.buyD10()) {
                    showUpgradeView()  // Refresh
                }
            }
            card.addView(buyButton)
        }

        return card
    }

    private fun createD12Card(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        titleText.text = "🎲 D12 Die"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(150, 255, 150))
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "Unlocks 2 new colors"
        descText.textSize = 16f
        descText.setTextColor(Color.LTGRAY)
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        if (GameState.d12Active) {
            // Already purchased
            val activeText = TextView(this)
            activeText.text = "✅ UNLOCKED"
            activeText.textSize = 18f
            activeText.setTextColor(Color.rgb(100, 255, 100))
            activeText.gravity = Gravity.CENTER
            activeText.setPadding(0, 10, 0, 10)
            card.addView(activeText)
        } else {
            // Buy button
            val buyButton = Button(this)
            buyButton.text = "Buy (125 Divine Essence)"
            buyButton.textSize = 16f

            if (GameState.canAffordD12()) {
                buyButton.setBackgroundColor(Color.rgb(50, 150, 50))
            } else {
                buyButton.setBackgroundColor(Color.rgb(100, 100, 100))
            }

            buyButton.setOnClickListener {
                if (GameState.buyD12()) {
                    showUpgradeView()  // Refresh
                }
            }
            card.addView(buyButton)
        }

        return card
    }

    private fun createD20Card(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        titleText.text = "🎲 D20 Die"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(255, 215, 0))
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "Unlocks 8 new colors"
        descText.textSize = 16f
        descText.setTextColor(Color.LTGRAY)
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        if (GameState.d20Active) {
            // Already purchased
            val activeText = TextView(this)
            activeText.text = "✅ UNLOCKED"
            activeText.textSize = 18f
            activeText.setTextColor(Color.rgb(100, 255, 100))
            activeText.gravity = Gravity.CENTER
            activeText.setPadding(0, 10, 0, 10)
            card.addView(activeText)
        } else {
            // Buy button
            val buyButton = Button(this)
            buyButton.text = "Buy (625 Divine Essence)"
            buyButton.textSize = 16f

            if (GameState.canAffordD20()) {
                buyButton.setBackgroundColor(Color.rgb(50, 150, 50))
            } else {
                buyButton.setBackgroundColor(Color.rgb(100, 100, 100))
            }

            buyButton.setOnClickListener {
                if (GameState.buyD20()) {
                    showUpgradeView()  // Refresh
                }
            }
            card.addView(buyButton)
        }

        return card
    }

    private fun createD6Card(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        titleText.text = "🎲 D6 Die"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(100, 200, 255))
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "Unlocks 2 new colors (Magenta, Cyan)"
        descText.textSize = 16f
        descText.setTextColor(Color.LTGRAY)
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        if (GameState.d6Active) {
            // Already purchased
            val activeText = TextView(this)
            activeText.text = "✅ UNLOCKED"
            activeText.textSize = 18f
            activeText.setTextColor(Color.rgb(100, 255, 100))
            activeText.gravity = Gravity.CENTER
            activeText.setPadding(0, 10, 0, 10)
            card.addView(activeText)
        } else {
            // Buy button
            val buyButton = Button(this)
            buyButton.text = "Buy (1 Divine Essence)"
            buyButton.textSize = 16f

            if (GameState.canAffordD6()) {
                buyButton.setBackgroundColor(Color.rgb(50, 150, 50))
            } else {
                buyButton.setBackgroundColor(Color.rgb(100, 100, 100))
            }

            buyButton.setOnClickListener {
                if (GameState.buyD6()) {
                    showUpgradeView()  // Refresh
                }
            }
            card.addView(buyButton)
        }

        return card
    }

    private fun createD8Card(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        titleText.text = "🎲 D8 Die"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(255, 150, 100))
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "Unlocks 2 new colors (Orange, Pink)"
        descText.textSize = 16f
        descText.setTextColor(Color.LTGRAY)
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        if (GameState.d8Active) {
            // Already purchased
            val activeText = TextView(this)
            activeText.text = "✅ UNLOCKED"
            activeText.textSize = 18f
            activeText.setTextColor(Color.rgb(100, 255, 100))
            activeText.gravity = Gravity.CENTER
            activeText.setPadding(0, 10, 0, 10)
            card.addView(activeText)
        } else {
            // Buy button
            val buyButton = Button(this)
            buyButton.text = "Buy (5 Divine Essence)"
            buyButton.textSize = 16f

            if (GameState.canAffordD8()) {
                buyButton.setBackgroundColor(Color.rgb(50, 150, 50))
            } else {
                buyButton.setBackgroundColor(Color.rgb(100, 100, 100))
            }

            buyButton.setOnClickListener {
                if (GameState.buyD8()) {
                    showUpgradeView()  // Refresh
                }
            }
            card.addView(buyButton)
        }

        return card
    }

    private fun createAutoClickerSpeedCard(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        titleText.text = "⚡ Auto Speed"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(255, 200, 100))
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "Faster auto-clicking"
        descText.textSize = 16f
        descText.setTextColor(Color.LTGRAY)
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        // Level info
        val levelText = TextView(this)
        val level = GameState.autoClickerSpeedLevel
        val maxLevel = 100
        levelText.text = "Level: $level / $maxLevel"
        levelText.textSize = 18f
        levelText.setTextColor(Color.WHITE)
        card.addView(levelText)

        // Effect info
        val effectText = TextView(this)
        val currentInterval = GameState.getAutoClickerInterval()
        val currentSeconds = currentInterval / 1000.0
        if (level < maxLevel) {
            val nextInterval = (1000L - ((level + 1) * 10L)).coerceAtLeast(10L)
            val nextSeconds = nextInterval / 1000.0
            effectText.text = String.format("Current: %.2fs → Next: %.2fs",
                currentSeconds, nextSeconds)
        } else {
            effectText.text = String.format("MAX: %.2fs", currentSeconds)
        }
        effectText.textSize = 14f
        effectText.setTextColor(Color.rgb(150, 255, 150))
        effectText.setPadding(0, 5, 0, 10)
        card.addView(effectText)

        // Upgrade button
        if (level < maxLevel) {
            val cost = GameState.getAutoClickerSpeedCost()
            val upgradeButton = Button(this)
            upgradeButton.text = "Upgrade ($cost Gold)"
            upgradeButton.textSize = 16f

            if (GameState.canAffordAutoClickerSpeed()) {
                upgradeButton.setBackgroundColor(Color.rgb(50, 150, 50))
            } else {
                upgradeButton.setBackgroundColor(Color.rgb(100, 100, 100))
            }

            upgradeButton.setOnClickListener {
                if (GameState.buyAutoClickerSpeed()) {
                    showUpgradeView()  // Refresh
                }
            }
            card.addView(upgradeButton)
        } else {
            val maxText = TextView(this)
            maxText.text = "✨ MAX LEVEL ✨"
            maxText.textSize = 18f
            maxText.setTextColor(Color.rgb(255, 215, 0))
            maxText.gravity = Gravity.CENTER
            maxText.setPadding(0, 10, 0, 10)
            card.addView(maxText)
        }

        return card
    }

    private fun createPermanentMultiplierCard(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        titleText.text = "💰 Permanent Click Multiplier"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(255, 215, 0))
        card.addView(titleText)

        // Current Level
        val levelText = TextView(this)
        levelText.text = "Current Level: ${GameState.permanentMultiplierLevel}"
        levelText.textSize = 20f
        levelText.setTextColor(Color.rgb(100, 255, 100))
        levelText.gravity = Gravity.CENTER
        levelText.setPadding(0, 10, 0, 5)
        card.addView(levelText)

        // Current Click Multiplier
        val multiplier = GameState.getPermanentClickMultiplier()
        val multiplierText = TextView(this)
        multiplierText.text = String.format("Click Multiplier: ×%.2f", multiplier)
        multiplierText.textSize = 18f
        multiplierText.setTextColor(Color.rgb(255, 215, 0))
        multiplierText.gravity = Gravity.CENTER
        multiplierText.setPadding(0, 5, 0, 5)
        card.addView(multiplierText)

        // Multiplier explanation
        val explainText = TextView(this)
        if (GameState.permanentMultiplierLevel == 0) {
            explainText.text = "Spend Gold to permanently increase all clicks!"
        } else {
            val percentBonus = ((multiplier - 1.0) * 100).toInt()
            explainText.text = "+$percentBonus% bonus on all clicks!"
        }
        explainText.textSize = 14f
        explainText.setTextColor(Color.rgb(200, 255, 200))
        explainText.gravity = Gravity.CENTER
        explainText.setPadding(0, 5, 0, 15)
        card.addView(explainText)

        // Upgrade Button
        val cost = GameState.getPermanentMultiplierCost()
        val upgradeButton = Button(this)
        upgradeButton.text = "Upgrade (+5%)\nCost: $cost Gold"
        upgradeButton.textSize = 16f
        upgradeButton.setPadding(20, 20, 20, 20)
        upgradeButton.setBackgroundColor(if (GameState.canAffordPermanentMultiplier()) Color.rgb(50, 150, 50) else Color.rgb(100, 100, 100))
        upgradeButton.setTextColor(Color.WHITE)
        upgradeButton.setOnClickListener {
            if (GameState.buyPermanentMultiplier()) {
                showUpgradeView()  // Refresh
            }
        }
        card.addView(upgradeButton)

        // Description
        val descText = TextView(this)
        descText.text = "Permanent upgrade with Gold! +5% click power per level.\nSurvives prestige!"
        descText.textSize = 12f
        descText.setTextColor(Color.LTGRAY)
        descText.setPadding(0, 15, 0, 0)
        card.addView(descText)

        return card
    }

    private fun createPermanentColorUpgradesCard(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        titleText.text = "🎨 Permanent Color Upgrades"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(255, 100, 255))
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "Spend POINTS for passive income per color (Max Lv 10)\nLevel 10 = 10x base points/sec | Survives prestige!"
        descText.textSize = 14f
        descText.setTextColor(Color.LTGRAY)
        descText.setPadding(0, 5, 0, 15)
        card.addView(descText)

        // Total passive points/sec display
        val totalPassiveText = TextView(this)
        val totalPassive = GameState.getTotalPassivePointsPerSecond()
        totalPassiveText.text = String.format("⏱️ Total Passive: %.2f pts/sec", totalPassive)
        totalPassiveText.textSize = 16f
        totalPassiveText.setTextColor(Color.rgb(100, 255, 100))
        totalPassiveText.gravity = Gravity.CENTER
        totalPassiveText.setPadding(0, 5, 0, 15)
        card.addView(totalPassiveText)

        // List of all colors
        val colors = mutableListOf(
            CubeColor.RED to "🔴 Red",
            CubeColor.GREEN to "🟢 Green",
            CubeColor.BLUE to "🔵 Blue",
            CubeColor.YELLOW to "🟡 Yellow",
            CubeColor.MAGENTA to "🟣 Magenta",
            CubeColor.CYAN to "🩵 Cyan"
        )

        if (GameState.d10Active) {
            colors.add(CubeColor.ORANGE to "🟠 Orange")
            colors.add(CubeColor.PINK to "🩷 Pink")
            colors.add(CubeColor.PURPLE to "🟪 Purple")
            colors.add(CubeColor.TURQUOISE to "🟦 Turquoise")
        }

        if (GameState.d12Active) {
            colors.add(CubeColor.LIME to "🟩 Lime")
            colors.add(CubeColor.BROWN to "🟫 Brown")
        }

        if (GameState.d20Active) {
            colors.add(CubeColor.GOLD to "🟨 Gold")
            colors.add(CubeColor.SILVER to "⬜ Silver")
            colors.add(CubeColor.BRONZE to "🟧 Bronze")
            colors.add(CubeColor.NAVY to "🔷 Navy")
            colors.add(CubeColor.MAROON to "🔶 Maroon")
            colors.add(CubeColor.OLIVE to "🫒 Olive")
            colors.add(CubeColor.TEAL to "🔹 Teal")
            colors.add(CubeColor.CORAL to "🪸 Coral")
        }

        colors.forEach { (color, name) ->
            val colorCard = createPermanentColorUpgradeButton(color, name)
            card.addView(colorCard)
        }

        return card
    }

    private fun createPermanentColorUpgradeButton(color: CubeColor, name: String): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 5, 0, 5)
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Color name, level, and passive pts/sec
        val infoText = TextView(this)
        val level = GameState.getPermanentColorUpgradeLevel(color)
        val passivePts = GameState.getPassivePointsPerSecond(color)
        val maxLevel = 10

        if (level >= maxLevel) {
            infoText.text = String.format("%s (MAX) [%.1f pts/s]", name, passivePts)
        } else {
            infoText.text = String.format("%s (Lv %d/%d) [%.1f pts/s]", name, level, maxLevel, passivePts)
        }

        infoText.textSize = 16f
        infoText.setTextColor(if (level >= maxLevel) Color.rgb(255, 215, 0) else Color.WHITE)
        infoText.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        row.addView(infoText)

        // Upgrade button
        val button = Button(this)
        val cost = GameState.getPermanentColorUpgradeCost(color)
        button.text = "Upgrade ($cost)"
        button.textSize = 14f

        if (GameState.canAffordPermanentColorUpgrade(color)) {
            button.setBackgroundColor(Color.rgb(50, 150, 50))
            button.setTextColor(Color.WHITE)
        } else {
            button.setBackgroundColor(Color.rgb(100, 100, 100))
            button.setTextColor(Color.DKGRAY)
        }

        button.setOnClickListener {
            if (GameState.buyPermanentColorUpgrade(color)) {
                showUpgradeView()  // Refresh
            }
        }

        row.addView(button)

        return row
    }

    private fun createColorInfo() {
        infoContainer.removeAllViews()

        // Header
        val header = TextView(this)
        header.text = "Points per click:"
        header.textSize = 18f
        header.setTextColor(Color.WHITE)
        header.gravity = Gravity.CENTER
        header.setPadding(0, 0, 0, 20)
        infoContainer.addView(header)

        val colors = mutableListOf(
            CubeColor.RED to "🔴 Red",
            CubeColor.GREEN to "🟢 Green",
            CubeColor.BLUE to "🔵 Blue",
            CubeColor.YELLOW to "🟡 Yellow",
            CubeColor.MAGENTA to "🟣 Magenta",
            CubeColor.CYAN to "🩵 Cyan"
        )

        // D10 colors
        if (GameState.d10Active) {
            colors.add(CubeColor.ORANGE to "🟠 Orange")
            colors.add(CubeColor.PINK to "🩷 Pink")
            colors.add(CubeColor.PURPLE to "🟪 Purple")
            colors.add(CubeColor.TURQUOISE to "🟦 Turquoise")
        }

        // D12 colors
        if (GameState.d12Active) {
            colors.add(CubeColor.LIME to "🟩 Lime")
            colors.add(CubeColor.BROWN to "🟫 Brown")
        }

        // D20 colors
        if (GameState.d20Active) {
            colors.add(CubeColor.GOLD to "🟨 Gold")
            colors.add(CubeColor.SILVER to "⬜ Silver")
            colors.add(CubeColor.BRONZE to "🟧 Bronze")
            colors.add(CubeColor.NAVY to "🔷 Navy")
            colors.add(CubeColor.MAROON to "🔶 Maroon")
            colors.add(CubeColor.OLIVE to "🫒 Olive")
            colors.add(CubeColor.TEAL to "🔹 Teal")
            colors.add(CubeColor.CORAL to "🪸 Coral")
        }

        colors.forEach { (color, name) ->
            val card = createColorCard(color, name)
            infoContainer.addView(card)
        }
    }

    private fun createColorCard(color: CubeColor, name: String): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.HORIZONTAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        card.gravity = Gravity.CENTER_VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 5, 0, 5)
        card.layoutParams = params

        // Color name
        val nameText = TextView(this)
        nameText.text = name
        nameText.textSize = 18f
        nameText.setTextColor(Color.WHITE)
        nameText.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        card.addView(nameText)

        // Current points for this color
        val currentPoints = GameState.getCurrentPoints(color)

        val pointsText = TextView(this)
        pointsText.text = "$currentPoints points"
        pointsText.textSize = 18f
        pointsText.setTextColor(Color.rgb(100, 255, 100))
        pointsText.gravity = Gravity.END
        card.addView(pointsText)

        return card
    }

    private fun updateAll() {
        updatePaintCanText()
        updateScore()
        updateLifetimeScore()
        updateProgress()
        updatePrestigeButton()
        createColorInfo()
    }

    private fun updatePaintCanText() {
        paintCanText.text = "✨ Divine Essence: ${GameState.divineEssence} / ${GameState.totalDivineEssenceEarned}\n(Available / Total earned)"
    }

    private fun updateScore() {
        scoreText.text = "Current: ${String.format("%.2f", GameState.totalScore)}"
    }

    private fun updateLifetimeScore() {
        lifetimeText.text = "Lifetime: ${String.format("%.2f", GameState.lifetimeScore)}"
    }

    private fun updateProgress() {
        val pointsToNext = GameState.getPointsToNextPrestige()
        if (pointsToNext > 0) {
            progressText.text = "⏳ ${String.format("%.2f", pointsToNext)} more to prestige"
        } else {
            val available = GameState.getAvailablePrestigeRewards()
            progressText.text = "✨ $available essence available!"
        }
    }

    private fun updatePrestigeButton() {
        val available = GameState.getAvailablePrestigeRewards()
        if (available > 0) {
            prestigeButton.text = "✨ PRESTIGE ✨\n(Gain $available essence)"
            prestigeButton.setBackgroundColor(Color.rgb(200, 100, 200))
            prestigeButton.setTextColor(Color.WHITE)
            prestigeButton.isEnabled = true
        } else {
            val needed = GameState.getPointsToNextPrestige()
            prestigeButton.text = "${String.format("%.2f", needed)} more to prestige"
            prestigeButton.setBackgroundColor(Color.rgb(100, 100, 100))
            prestigeButton.setTextColor(Color.DKGRAY)
            prestigeButton.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        // Starte regelmäßige Updates
        updateHandler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Stoppe Updates wenn Activity pausiert wird
        updateHandler.removeCallbacks(updateRunnable)
        GameState.markActiveTime()

        // Spielstand speichern
        GameState.saveState(this)
    }
}
