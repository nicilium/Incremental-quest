package com.tuffgames.incrementalquest

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var glView: GLSurfaceView
    private lateinit var renderer: GameRenderer
    private lateinit var scoreText: TextView
    private lateinit var clickValueText: TextView
    private lateinit var comboText: TextView
    private lateinit var buffIndicatorText: TextView
    private lateinit var upgradeButton: Button
    private lateinit var prestigeButton: Button
    private lateinit var buffButton: Button
    private lateinit var extraDiceButton: Button
    private lateinit var tavernButton: Button
    private lateinit var achievementButton: Button
    private lateinit var boostButton: Button
    private lateinit var shopButton: Button

    private val buffCheckHandler = Handler(Looper.getMainLooper())
    private val buffCheckRunnable = object : Runnable {
        override fun run() {
            // Prüfe ob neuer Buff angeboten werden soll
            if (GameState.shouldOfferNewBuff()) {
                GameState.selectRandomBuff()
            }

            // Buff-Button sichtbarkeit aktualisieren
            updateBuffButton()

            // Buff-Indikator aktualisieren
            updateBuffIndicator()

            // Nächstes Check in 1 Sekunde
            buffCheckHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Spielstand laden
        GameState.loadState(this)

        // Vollbild-Modus
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // OpenGL ES 2.0 Context erstellen
        glView = GLSurfaceView(this)
        glView.setEGLContextClientVersion(2)

        // Renderer mit GameState setzen
        renderer = GameRenderer(GameState) { updateScore() }
        glView.setRenderer(renderer)

        // Touch-Events verarbeiten
        glView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                renderer.onTouch(event.x, event.y, glView.width, glView.height)
                return@setOnTouchListener true
            }
            false
        }

        // UI Layout erstellen
        val layout = FrameLayout(this)
        layout.addView(glView)

        // Einstellungs-Button (Zahnrad) rechts oben
        val settingsButton = Button(this)
        settingsButton.text = "⚙️"
        settingsButton.textSize = 24f
        settingsButton.setBackgroundColor(Color.TRANSPARENT)
        settingsButton.setTextColor(Color.WHITE)
        val settingsParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        settingsParams.gravity = Gravity.TOP or Gravity.END
        settingsParams.topMargin = 20
        settingsParams.rightMargin = 20
        settingsButton.layoutParams = settingsParams
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        layout.addView(settingsButton)

        // Points display
        scoreText = TextView(this)
        scoreText.text = "Points: 0"
        scoreText.textSize = 24f
        scoreText.setTextColor(Color.WHITE)
        scoreText.setShadowLayer(4f, 2f, 2f, Color.BLACK)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.topMargin = 30
        scoreText.layoutParams = params
        layout.addView(scoreText)

        // Click value display
        clickValueText = TextView(this)
        clickValueText.text = "Next Click: 1"
        clickValueText.textSize = 18f
        clickValueText.setTextColor(Color.rgb(100, 255, 100))
        clickValueText.setShadowLayer(4f, 2f, 2f, Color.BLACK)
        val clickValueParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        clickValueParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        clickValueParams.topMargin = 110
        clickValueText.layoutParams = clickValueParams
        layout.addView(clickValueText)

        // Combo display
        comboText = TextView(this)
        comboText.text = ""
        comboText.textSize = 20f
        comboText.setTextColor(Color.rgb(255, 150, 50))
        comboText.setShadowLayer(4f, 2f, 2f, Color.BLACK)
        comboText.visibility = View.GONE
        val comboParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        comboParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        comboParams.topMargin = 150
        comboText.layoutParams = comboParams
        layout.addView(comboText)

        // Buff-Indikator erstellen
        buffIndicatorText = TextView(this)
        buffIndicatorText.text = ""
        buffIndicatorText.textSize = 16f
        buffIndicatorText.setTextColor(Color.rgb(255, 215, 0))
        buffIndicatorText.setShadowLayer(4f, 2f, 2f, Color.BLACK)
        buffIndicatorText.visibility = View.GONE
        val buffIndicatorParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        buffIndicatorParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        buffIndicatorParams.topMargin = 210
        buffIndicatorText.layoutParams = buffIndicatorParams
        layout.addView(buffIndicatorText)

        // Button-Container für Upgrades und Prestige
        val buttonContainer = LinearLayout(this)
        buttonContainer.orientation = LinearLayout.HORIZONTAL
        buttonContainer.gravity = Gravity.CENTER
        val containerParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        containerParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        containerParams.bottomMargin = 40
        buttonContainer.layoutParams = containerParams

        // Upgrades button
        upgradeButton = Button(this)
        upgradeButton.text = "⬆️ UPGRADES"
        upgradeButton.textSize = 16f
        upgradeButton.setBackgroundColor(Color.rgb(200, 100, 0))
        upgradeButton.setTextColor(Color.WHITE)
        val upgradeParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        upgradeParams.setMargins(5, 0, 5, 0)
        upgradeButton.layoutParams = upgradeParams
        upgradeButton.visibility = View.GONE  // Anfangs unsichtbar
        upgradeButton.setOnClickListener {
            val intent = Intent(this, UpgradeActivity::class.java)
            startActivity(intent)
        }
        buttonContainer.addView(upgradeButton)

        // Prestige button
        prestigeButton = Button(this)
        prestigeButton.text = "✨ PRESTIGE"
        prestigeButton.textSize = 16f
        prestigeButton.setBackgroundColor(Color.rgb(150, 50, 150))
        prestigeButton.setTextColor(Color.WHITE)
        val prestigeParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        prestigeParams.setMargins(5, 0, 5, 0)
        prestigeButton.layoutParams = prestigeParams
        prestigeButton.visibility = View.GONE  // Anfangs unsichtbar
        prestigeButton.setOnClickListener {
            val intent = Intent(this, PrestigeActivity::class.java)
            startActivity(intent)
        }
        buttonContainer.addView(prestigeButton)

        // Buff button
        buffButton = Button(this)
        buffButton.text = "⚡ BOOST"
        buffButton.textSize = 16f
        buffButton.setBackgroundColor(Color.rgb(255, 215, 0))
        buffButton.setTextColor(Color.BLACK)
        val buffParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buffParams.setMargins(5, 0, 5, 0)
        buffButton.layoutParams = buffParams
        buffButton.visibility = View.GONE  // Anfangs unsichtbar
        buffButton.setOnClickListener {
            val intent = Intent(this, BuffActivity::class.java)
            startActivity(intent)
        }
        buttonContainer.addView(buffButton)

        // Extra Dice button (only visible after D20)
        extraDiceButton = Button(this)
        extraDiceButton.text = "🎲 EXTRA"
        extraDiceButton.textSize = 16f
        extraDiceButton.setBackgroundColor(Color.rgb(100, 50, 200))
        extraDiceButton.setTextColor(Color.WHITE)
        val extraDiceParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        extraDiceParams.setMargins(5, 0, 5, 0)
        extraDiceButton.layoutParams = extraDiceParams
        extraDiceButton.visibility = View.GONE  // Anfangs unsichtbar
        extraDiceButton.setOnClickListener {
            val intent = Intent(this, ExtraDiceActivity::class.java)
            startActivity(intent)
        }
        buttonContainer.addView(extraDiceButton)

        // Achievement button
        achievementButton = Button(this)
        achievementButton.text = "🏆 ACH"
        achievementButton.textSize = 16f
        achievementButton.setBackgroundColor(Color.rgb(200, 150, 50))
        achievementButton.setTextColor(Color.WHITE)
        val achievementParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        achievementParams.setMargins(5, 0, 5, 0)
        achievementButton.layoutParams = achievementParams
        achievementButton.setOnClickListener {
            val intent = Intent(this, AchievementActivity::class.java)
            startActivity(intent)
        }
        buttonContainer.addView(achievementButton)

        // Boost Station button
        boostButton = Button(this)
        boostButton.text = "⚡"
        boostButton.textSize = 20f
        boostButton.setBackgroundColor(Color.rgb(255, 100, 0))
        boostButton.setTextColor(Color.WHITE)
        val boostParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        boostParams.setMargins(5, 0, 5, 0)
        boostButton.layoutParams = boostParams
        boostButton.setOnClickListener {
            val intent = Intent(this, BoostActivity::class.java)
            startActivity(intent)
        }
        buttonContainer.addView(boostButton)

        // Shop button
        shopButton = Button(this)
        shopButton.text = "💰"
        shopButton.textSize = 20f
        shopButton.setBackgroundColor(Color.rgb(0, 150, 255))
        shopButton.setTextColor(Color.WHITE)
        val shopParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        shopParams.setMargins(5, 0, 5, 0)
        shopButton.layoutParams = shopParams
        shopButton.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
        }
        buttonContainer.addView(shopButton)

        // Tavern "?" button (locked until paid, visible after D20)
        tavernButton = Button(this)
        tavernButton.textSize = 16f
        tavernButton.setTextColor(Color.WHITE)
        val tavernParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tavernParams.setMargins(5, 0, 5, 0)
        tavernButton.layoutParams = tavernParams
        tavernButton.visibility = View.GONE  // Anfangs unsichtbar

        // Update button based on unlock status
        updateTavernButton()

        tavernButton.setOnClickListener {
            if (GameState.tavernUnlocked) {
                // Already unlocked - open tavern
                val intent = Intent(this, TavernActivity::class.java)
                startActivity(intent)
            } else if (GameState.canAffordTavernUnlock()) {
                // Can afford - show confirmation
                showTavernUnlockDialog()
            } else {
                // Can't afford - show cost
                showTavernCostDialog()
            }
        }
        buttonContainer.addView(tavernButton)

        layout.addView(buttonContainer)

        // === IDLE SYSTEMS BUTTON ROW ===
        val idleSystemsContainer = LinearLayout(this)
        idleSystemsContainer.orientation = LinearLayout.HORIZONTAL
        idleSystemsContainer.gravity = Gravity.CENTER
        val idleContainerParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        idleContainerParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        idleContainerParams.bottomMargin = 10
        idleSystemsContainer.layoutParams = idleContainerParams

        // Ascension button
        val ascensionButton = Button(this)
        ascensionButton.text = "🌟"
        ascensionButton.textSize = 20f
        ascensionButton.setBackgroundColor(Color.rgb(100, 0, 150))
        ascensionButton.setTextColor(Color.WHITE)
        val ascensionParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        ascensionParams.setMargins(5, 0, 5, 0)
        ascensionButton.layoutParams = ascensionParams
        ascensionButton.setOnClickListener {
            val intent = Intent(this, AscensionActivity::class.java)
            startActivity(intent)
        }
        idleSystemsContainer.addView(ascensionButton)

        // Artifacts button
        val artifactsButton = Button(this)
        artifactsButton.text = "🏺"
        artifactsButton.textSize = 20f
        artifactsButton.setBackgroundColor(Color.rgb(150, 100, 0))
        artifactsButton.setTextColor(Color.WHITE)
        val artifactsParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        artifactsParams.setMargins(5, 0, 5, 0)
        artifactsButton.layoutParams = artifactsParams
        artifactsButton.setOnClickListener {
            val intent = Intent(this, ArtifactsActivity::class.java)
            startActivity(intent)
        }
        idleSystemsContainer.addView(artifactsButton)

        // Research button
        val researchButton = Button(this)
        researchButton.text = "📖"
        researchButton.textSize = 20f
        researchButton.setBackgroundColor(Color.rgb(0, 100, 150))
        researchButton.setTextColor(Color.WHITE)
        val researchParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        researchParams.setMargins(5, 0, 5, 0)
        researchButton.layoutParams = researchParams
        researchButton.setOnClickListener {
            val intent = Intent(this, ResearchActivity::class.java)
            startActivity(intent)
        }
        idleSystemsContainer.addView(researchButton)

        // Challenges button
        val challengesButton = Button(this)
        challengesButton.text = "⚔️"
        challengesButton.textSize = 20f
        challengesButton.setBackgroundColor(Color.rgb(150, 0, 0))
        challengesButton.setTextColor(Color.WHITE)
        val challengesParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        challengesParams.setMargins(5, 0, 5, 0)
        challengesButton.layoutParams = challengesParams
        challengesButton.setOnClickListener {
            val intent = Intent(this, ChallengesActivity::class.java)
            startActivity(intent)
        }
        idleSystemsContainer.addView(challengesButton)

        // Milestones button
        val milestonesButton = Button(this)
        milestonesButton.text = "🏆"
        milestonesButton.textSize = 20f
        milestonesButton.setBackgroundColor(Color.rgb(200, 150, 0))
        milestonesButton.setTextColor(Color.WHITE)
        val milestonesParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        milestonesParams.setMargins(5, 0, 5, 0)
        milestonesButton.layoutParams = milestonesParams
        milestonesButton.setOnClickListener {
            val intent = Intent(this, MilestonesActivity::class.java)
            startActivity(intent)
        }
        idleSystemsContainer.addView(milestonesButton)

        // Quests button
        val questsButton = Button(this)
        questsButton.text = "📜"
        questsButton.textSize = 20f
        questsButton.setBackgroundColor(Color.rgb(0, 150, 100))
        questsButton.setTextColor(Color.WHITE)
        val questsParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        questsParams.setMargins(5, 0, 5, 0)
        questsButton.layoutParams = questsParams
        questsButton.setOnClickListener {
            val intent = Intent(this, QuestsActivity::class.java)
            startActivity(intent)
        }
        idleSystemsContainer.addView(questsButton)

        layout.addView(idleSystemsContainer)

        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()

        // Offline-Klicks verarbeiten
        val offlineClicks = GameState.processOfflineClicks()

        updateScore() // Score aktualisieren wenn wir vom Upgrade-Menü zurückkehren

        // Buff-Check starten
        buffCheckHandler.post(buffCheckRunnable)
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()

        // Zeit markieren wenn Activity pausiert wird
        GameState.markActiveTime()

        // Spielstand speichern
        GameState.saveState(this)

        // Buff-Check stoppen
        buffCheckHandler.removeCallbacks(buffCheckRunnable)
    }

    private fun updateScore() {
        runOnUiThread {
            scoreText.text = "Points: ${String.format("%.2f", GameState.totalScore)}"

            // Show current click value
            val currentValue = GameState.getCurrentPoints(renderer.currentColor)
            clickValueText.text = "Next: $currentValue"

            // Update combo display
            val combo = GameState.getCurrentCombo()
            if (combo > 1) {
                comboText.text = "🔥 ${combo}x COMBO!"
                comboText.visibility = View.VISIBLE
            } else {
                comboText.visibility = View.GONE
            }

            // Show upgrade button when unlocked
            if (GameState.upgradesUnlocked) {
                upgradeButton.visibility = View.VISIBLE
            }

            // Show prestige button at 1000 lifetime points
            if (GameState.lifetimeScore >= 1000) {
                prestigeButton.visibility = View.VISIBLE
            }

            // Show extra dice button when D20 is unlocked
            if (GameState.d20Active) {
                extraDiceButton.visibility = View.VISIBLE
            }

            // Show tavern button when D20 is unlocked
            if (GameState.d20Active) {
                tavernButton.visibility = View.VISIBLE
                updateTavernButton()
            }
        }
    }

    private fun updateBuffButton() {
        runOnUiThread {
            if (GameState.hasAvailableBuff()) {
                buffButton.visibility = View.VISIBLE
            } else {
                buffButton.visibility = View.GONE
            }
        }
    }

    private fun updateBuffIndicator() {
        runOnUiThread {
            if (GameState.isBuffActive()) {
                val remainingSeconds = (GameState.getRemainingBuffTime() / 1000).toInt()
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60

                val buffName = when (GameState.activeBuffType) {
                    BuffType.DOUBLE_POINTS -> "⭐ 2x Points"
                    BuffType.FASTER_AUTOCLICK -> "⚡ Fast Auto"
                    else -> ""
                }

                buffIndicatorText.text = "$buffName ${minutes}:${seconds.toString().padStart(2, '0')}"
                buffIndicatorText.visibility = View.VISIBLE
            } else {
                buffIndicatorText.visibility = View.GONE
            }
        }
    }

    private fun updateTavernButton() {
        runOnUiThread {
            if (GameState.tavernUnlocked) {
                // Unlocked - show as gold/accessible
                tavernButton.text = "❓"
                tavernButton.setBackgroundColor(Color.rgb(180, 120, 20))
            } else {
                // Locked - show as red mystery
                tavernButton.text = "🔒 ?"
                tavernButton.setBackgroundColor(Color.rgb(150, 0, 0))
            }
        }
    }

    private fun showTavernUnlockDialog() {
        val (deCost, scoreCost) = GameState.getTavernUnlockCost()

        AlertDialog.Builder(this)
            .setTitle("🍺 Unlock the Tavern?")
            .setMessage(
                "Patrick's Tavern awaits!\n\n" +
                "Cost: ${deCost} Divine Essence\n" +
                "       ${String.format("%.0f", scoreCost)} Points\n\n" +
                "A new adventure begins..."
            )
            .setPositiveButton("UNLOCK!") { _, _ ->
                if (GameState.unlockTavern()) {
                    GameState.saveState(this)
                    updateTavernButton()
                    updateScore()

                    // Welcome message
                    AlertDialog.Builder(this)
                        .setTitle("🍺 Tavern Unlocked!")
                        .setMessage("Patrick grins at you from behind the bar.\nTap the ❓ button to enter!")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTavernCostDialog() {
        val (deCost, scoreCost) = GameState.getTavernUnlockCost()
        val currentDE = GameState.divineEssence
        val currentScore = GameState.totalScore

        AlertDialog.Builder(this)
            .setTitle("🔒 Tavern Locked")
            .setMessage(
                "You need:\n\n" +
                "Divine Essence: ${currentDE} / ${deCost}\n" +
                "Points: ${String.format("%.0f", currentScore)} / ${String.format("%.0f", scoreCost)}\n\n" +
                "Keep clicking and prestiging!"
            )
            .setPositiveButton("OK", null)
            .show()
    }

}
