package com.tuffgames.incrementalquest

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class UpgradeActivity : Activity() {

    private lateinit var scoreText: TextView
    private lateinit var upgradeContainer: LinearLayout

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Offline-Klicks verarbeiten
            GameState.processOfflineClicks()
            updateScore()
            updateButtonStates()  // Nur Button-Farben aktualisieren, nicht neu erstellen

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
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(15, 15, 45))
        mainLayout.setPadding(20, 20, 20, 20)

        // Title
        val title = TextView(this)
        title.text = "UPGRADES"
        title.textSize = 32f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Score display
        scoreText = TextView(this)
        scoreText.textSize = 24f
        scoreText.setTextColor(Color.YELLOW)
        scoreText.gravity = Gravity.CENTER
        scoreText.setPadding(0, 20, 0, 20)
        updateScore()
        mainLayout.addView(scoreText)

        // ScrollView für Upgrades
        val scrollView = ScrollView(this)
        upgradeContainer = LinearLayout(this)
        upgradeContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(upgradeContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Back button
        val backButton = Button(this)
        backButton.text = "← Back"
        backButton.setOnClickListener { finish() }
        mainLayout.addView(backButton)

        setContentView(mainLayout)

        createUpgradeButtons()
    }

    private fun createUpgradeButtons() {
        upgradeContainer.removeAllViews()

        // Synergy info card (if unlocked and active)
        if (GameState.areSynergiesUnlocked()) {
            val synergyCard = createSynergyInfoCard()
            upgradeContainer.addView(synergyCard)
        }

        // Autoklicker-Card hinzufügen
        val autoClickerCard = createAutoClickerCard()
        upgradeContainer.addView(autoClickerCard)

        // Get available colors from GameState (respects current die)
        val availableColors = GameState.getAvailableColors()

        // Color names mapping
        val colorNames = mapOf(
            CubeColor.RED to "🔴 Red",
            CubeColor.GREEN to "🟢 Green",
            CubeColor.BLUE to "🔵 Blue",
            CubeColor.YELLOW to "🟡 Yellow",
            CubeColor.MAGENTA to "🟣 Magenta",
            CubeColor.CYAN to "🩵 Cyan",
            CubeColor.ORANGE to "🟠 Orange",
            CubeColor.PINK to "🩷 Pink",
            CubeColor.PURPLE to "🟪 Purple",
            CubeColor.TURQUOISE to "🟦 Turquoise",
            CubeColor.LIME to "🟩 Lime",
            CubeColor.BROWN to "🟫 Brown",
            CubeColor.GOLD to "🟨 Gold",
            CubeColor.SILVER to "⬜ Silver",
            CubeColor.BRONZE to "🟧 Bronze",
            CubeColor.NAVY to "🔷 Navy",
            CubeColor.MAROON to "🔶 Maroon",
            CubeColor.OLIVE to "🫒 Olive",
            CubeColor.TEAL to "🔹 Teal",
            CubeColor.CORAL to "🪸 Coral"
        )

        availableColors.forEach { color ->
            val name = colorNames[color] ?: color.name
            val upgradeCard = createUpgradeCard(color, name)
            upgradeContainer.addView(upgradeCard)
        }
    }

    private fun createSynergyInfoCard(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(40, 20, 60))
        card.setPadding(15, 15, 15, 15)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 15)
        card.layoutParams = params

        // Title
        val titleText = TextView(this)
        titleText.text = "🌈 COLOR SYNERGIES"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(255, 100, 255))
        titleText.gravity = Gravity.CENTER
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "Unlock bonuses by leveling color combinations!"
        descText.textSize = 14f
        descText.setTextColor(Color.rgb(200, 200, 200))
        descText.gravity = Gravity.CENTER
        descText.setPadding(0, 5, 0, 15)
        card.addView(descText)

        // Active synergies
        val activeSynergies = GameState.getActiveSynergies()
        if (activeSynergies.isNotEmpty()) {
            val activeTitle = TextView(this)
            activeTitle.text = "✅ ACTIVE:"
            activeTitle.textSize = 16f
            activeTitle.setTextColor(Color.rgb(100, 255, 100))
            activeTitle.setPadding(0, 0, 0, 10)
            card.addView(activeTitle)

            activeSynergies.forEach { synergy ->
                val synergyText = TextView(this)
                synergyText.text = String.format("• %s: +%.0f%%", synergy.name, synergy.bonus * 100)
                synergyText.textSize = 14f
                synergyText.setTextColor(Color.rgb(150, 255, 150))
                synergyText.setPadding(20, 2, 0, 2)
                card.addView(synergyText)
            }
        }

        // Inactive synergies (show first 3)
        val allSynergies = COLOR_SYNERGIES
        val inactiveSynergies = allSynergies.filter { !activeSynergies.contains(it) }.take(3)
        if (inactiveSynergies.isNotEmpty()) {
            val inactiveTitle = TextView(this)
            inactiveTitle.text = if (activeSynergies.isNotEmpty()) "\n🔒 LOCKED:" else "🔒 AVAILABLE:"
            inactiveTitle.textSize = 16f
            inactiveTitle.setTextColor(Color.rgb(150, 150, 150))
            inactiveTitle.setPadding(0, 10, 0, 10)
            card.addView(inactiveTitle)

            inactiveSynergies.forEach { synergy ->
                val synergyText = TextView(this)
                synergyText.text = "• ${synergy.description}"
                synergyText.textSize = 13f
                synergyText.setTextColor(Color.rgb(120, 120, 120))
                synergyText.setPadding(20, 2, 0, 2)
                card.addView(synergyText)
            }
        }

        return card
    }

    private fun createAutoClickerCard(): LinearLayout {
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
        titleText.text = "⏱️ Auto Clicker"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(100, 200, 255))
        card.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "Clicks the die automatically"
        descText.textSize = 16f
        descText.setTextColor(Color.LTGRAY)
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        if (GameState.autoClickerActive) {
            // Already bought
            val activeText = TextView(this)
            activeText.text = "✅ ACTIVE"
            activeText.textSize = 20f
            activeText.setTextColor(Color.rgb(100, 255, 100))
            activeText.gravity = Gravity.CENTER
            activeText.setPadding(0, 10, 0, 10)
            card.addView(activeText)
        } else {
            // Buy button
            val cost = GameState.getAutoClickerCost()
            val buyButton = Button(this)
            buyButton.tag = "AUTOKLICKER"  // Tag for updates
            buyButton.text = "Buy ($cost)"
            buyButton.textSize = 16f

            if (GameState.canAffordAutoClicker()) {
                buyButton.setBackgroundColor(Color.rgb(50, 150, 50))
            } else {
                buyButton.setBackgroundColor(Color.rgb(100, 100, 100))
            }

            buyButton.setOnClickListener {
                if (GameState.buyAutoClicker()) {
                    updateScore()
                    createUpgradeButtons()  // Refresh
                }
            }
            card.addView(buyButton)
        }

        return card
    }

    private fun createUpgradeCard(color: CubeColor, name: String): View {
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
        titleText.text = "$name"
        titleText.textSize = 20f
        titleText.setTextColor(Color.WHITE)
        card.addView(titleText)

        // Info
        val infoText = TextView(this)
        val currentPoints = GameState.getCurrentPoints(color)
        val level = GameState.getUpgradeLevel(color)
        infoText.text = "Value: $currentPoints | Level: $level"
        infoText.textSize = 16f
        infoText.setTextColor(Color.LTGRAY)
        card.addView(infoText)

        // Cost and upgrade button
        val buttonLayout = LinearLayout(this)
        buttonLayout.orientation = LinearLayout.HORIZONTAL
        buttonLayout.gravity = Gravity.CENTER_VERTICAL

        val cost = GameState.getUpgradeCost(color)
        val upgradeButton = Button(this)
        upgradeButton.tag = color  // Tag for updates
        upgradeButton.text = "Upgrade (${String.format("%.2f", cost)})"
        upgradeButton.textSize = 16f

        if (GameState.canAffordUpgrade(color)) {
            upgradeButton.setBackgroundColor(Color.rgb(50, 150, 50))
        } else {
            upgradeButton.setBackgroundColor(Color.rgb(100, 100, 100))
        }

        upgradeButton.setOnClickListener {
            if (GameState.buyUpgrade(color)) {
                updateScore()
                createUpgradeButtons() // Refresh alle Buttons
            }
        }

        buttonLayout.addView(upgradeButton)
        card.addView(buttonLayout)

        return card
    }

    private fun updateScore() {
        scoreText.text = "Available: ${String.format("%.2f", GameState.totalScore)}"
    }

    private fun updateButtonStates() {
        // Loop through all cards and update button colors
        for (i in 0 until upgradeContainer.childCount) {
            val card = upgradeContainer.getChildAt(i) as? LinearLayout ?: continue

            // First card is auto clicker (if not bought yet)
            if (i == 0 && !GameState.autoClickerActive) {
                val button = card.getChildAt(card.childCount - 1) as? Button
                if (button?.tag == "AUTOKLICKER") {
                    // Update auto clicker button
                    if (GameState.canAffordAutoClicker()) {
                        button.setBackgroundColor(Color.rgb(50, 150, 50))
                    } else {
                        button.setBackgroundColor(Color.rgb(100, 100, 100))
                    }
                }
                continue
            }

            // Find button in card (last child is buttonLayout)
            val buttonLayout = card.getChildAt(card.childCount - 1) as? LinearLayout ?: continue
            val button = buttonLayout.getChildAt(0) as? Button ?: continue

            // Also find info text (second child) to update point values
            if (card.childCount >= 2) {
                val infoText = card.getChildAt(1) as? TextView
                val color = button.tag as? CubeColor

                if (color != null) {
                    // Update info text
                    val currentPoints = GameState.getCurrentPoints(color)
                    val level = GameState.getUpgradeLevel(color)
                    infoText?.text = "Value: $currentPoints | Level: $level"

                    // Update button text
                    val cost = GameState.getUpgradeCost(color)
                    button.text = "Upgrade (${String.format("%.2f", cost)})"

                    // Update button color
                    if (GameState.canAffordUpgrade(color)) {
                        button.setBackgroundColor(Color.rgb(50, 150, 50))
                    } else {
                        button.setBackgroundColor(Color.rgb(100, 100, 100))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Start regular updates
        updateHandler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Stop updates when activity is paused
        updateHandler.removeCallbacks(updateRunnable)
        GameState.markActiveTime()

        // Save state
        GameState.saveState(this)
    }
}
