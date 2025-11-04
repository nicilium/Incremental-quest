package com.tuffgames.incrementalquest

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class SettingsActivity : Activity() {

    private var resetWarningStage = 0  // 0 = no warning, 1-3 = warning stages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Main layout
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(20, 20, 40))
        mainLayout.setPadding(40, 40, 40, 40)

        // Title
        val title = TextView(this)
        title.text = "⚙️ SETTINGS"
        title.textSize = 32f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.setPadding(0, 0, 0, 40)
        mainLayout.addView(title)

        // Sound volume section
        val soundSection = createSoundSection()
        mainLayout.addView(soundSection)

        // Spacer
        val spacer1 = TextView(this)
        spacer1.text = ""
        spacer1.setPadding(0, 30, 0, 30)
        mainLayout.addView(spacer1)

        // Reset section
        val resetSection = createResetSection()
        mainLayout.addView(resetSection)

        // Spacer
        val spacer2 = TextView(this)
        spacer2.text = ""
        spacer2.setPadding(0, 30, 0, 30)
        mainLayout.addView(spacer2)

        // Back button
        val backButton = Button(this)
        backButton.text = "← Back"
        backButton.textSize = 18f
        backButton.setBackgroundColor(Color.rgb(70, 70, 90))
        backButton.setTextColor(Color.WHITE)
        backButton.setPadding(40, 20, 40, 20)
        backButton.setOnClickListener {
            finish()
        }
        val backParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        backParams.gravity = Gravity.CENTER
        backButton.layoutParams = backParams
        mainLayout.addView(backButton)

        setContentView(mainLayout)
    }

    private fun createSoundSection(): LinearLayout {
        val section = LinearLayout(this)
        section.orientation = LinearLayout.VERTICAL
        section.setBackgroundColor(Color.rgb(30, 30, 60))
        section.setPadding(30, 30, 30, 30)

        // Title
        val titleText = TextView(this)
        titleText.text = "🔊 Sound Volume"
        titleText.textSize = 24f
        titleText.setTextColor(Color.rgb(100, 200, 255))
        titleText.gravity = Gravity.CENTER
        section.addView(titleText)

        // Description
        val descText = TextView(this)
        descText.text = "(Not functional yet - for future sounds)"
        descText.textSize = 12f
        descText.setTextColor(Color.LTGRAY)
        descText.gravity = Gravity.CENTER
        descText.setPadding(0, 5, 0, 20)
        section.addView(descText)

        // Volume value display
        val volumeText = TextView(this)
        volumeText.text = "50%"
        volumeText.textSize = 20f
        volumeText.setTextColor(Color.WHITE)
        volumeText.gravity = Gravity.CENTER
        volumeText.setPadding(0, 0, 0, 10)
        section.addView(volumeText)

        // Slider
        val seekBar = SeekBar(this)
        seekBar.max = 100
        seekBar.progress = 50
        seekBar.setPadding(20, 10, 20, 10)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeText.text = "$progress%"
                // TODO: Set sound volume later
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        section.addView(seekBar)

        return section
    }

    private fun createResetSection(): LinearLayout {
        val section = LinearLayout(this)
        section.orientation = LinearLayout.VERTICAL
        section.setBackgroundColor(Color.rgb(30, 30, 60))
        section.setPadding(30, 30, 30, 30)

        // Title
        val titleText = TextView(this)
        titleText.text = "🗑️ Reset Game"
        titleText.textSize = 24f
        titleText.setTextColor(Color.rgb(255, 100, 100))
        titleText.gravity = Gravity.CENTER
        section.addView(titleText)

        // Warning text
        val warningText = TextView(this)
        warningText.text = "This deletes ALL your progress!"
        warningText.textSize = 14f
        warningText.setTextColor(Color.LTGRAY)
        warningText.gravity = Gravity.CENTER
        warningText.setPadding(0, 10, 0, 20)
        section.addView(warningText)

        // Reset button
        val resetButton = Button(this)
        resetButton.text = "Reset Game"
        resetButton.textSize = 16f
        resetButton.setBackgroundColor(Color.rgb(150, 50, 50))
        resetButton.setTextColor(Color.WHITE)
        resetButton.setPadding(30, 20, 30, 20)
        resetButton.setOnClickListener {
            handleResetClick(resetButton, warningText)
        }
        val resetParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        resetParams.gravity = Gravity.CENTER
        resetButton.layoutParams = resetParams
        section.addView(resetButton)

        return section
    }

    private fun handleResetClick(button: Button, warningText: TextView) {
        resetWarningStage++

        when (resetWarningStage) {
            1 -> {
                // First warning
                button.text = "Are you SURE? (Click 1/3)"
                button.setBackgroundColor(Color.rgb(180, 60, 60))
                warningText.text = "⚠️ All points, upgrades and essence will be lost!"
                warningText.textSize = 16f
            }
            2 -> {
                // Second warning
                button.text = "Delete EVERYTHING? (Click 2/3)"
                button.setBackgroundColor(Color.rgb(210, 70, 70))
                warningText.text = "⚠️⚠️ Lifetime, prestige, EVERYTHING will be deleted!"
                warningText.textSize = 18f
            }
            3 -> {
                // Third warning
                button.text = "FINAL WARNING! (Click 3/3)"
                button.setBackgroundColor(Color.rgb(255, 0, 0))
                warningText.text = "⚠️⚠️⚠️ CANNOT BE UNDONE!"
                warningText.textSize = 20f
                warningText.setTextColor(Color.rgb(255, 100, 100))
            }
            4 -> {
                // Final reset
                GameState.reset()
                GameState.saveState(this)

                button.text = "✓ Game reset!"
                button.setBackgroundColor(Color.rgb(50, 150, 50))
                warningText.text = "Restart app recommended."
                warningText.textSize = 16f
                warningText.setTextColor(Color.rgb(100, 255, 100))

                button.isEnabled = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Reset warning stage when activity is left
        resetWarningStage = 0
    }
}
