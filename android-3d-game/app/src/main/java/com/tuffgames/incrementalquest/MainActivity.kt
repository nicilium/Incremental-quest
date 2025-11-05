package com.tuffgames.incrementalquest

import android.app.Activity
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
    private lateinit var buffIndicatorText: TextView
    private lateinit var upgradeButton: Button
    private lateinit var prestigeButton: Button
    private lateinit var buffButton: Button
    private lateinit var extraDiceButton: Button

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
        buffIndicatorParams.topMargin = 180
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

        layout.addView(buttonContainer)

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

}
