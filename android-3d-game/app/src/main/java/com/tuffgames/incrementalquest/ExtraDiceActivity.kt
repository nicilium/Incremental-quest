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

class ExtraDiceActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var essenceText: TextView
    private lateinit var diceContainer: LinearLayout

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateUI()
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

        // Titel
        val title = TextView(this)
        title.text = "EXTRA DICE"
        title.textSize = 32f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Divine Essence display
        essenceText = TextView(this)
        essenceText.textSize = 20f
        essenceText.setTextColor(Color.rgb(255, 215, 0))
        essenceText.gravity = Gravity.CENTER
        essenceText.setPadding(0, 20, 0, 20)
        mainLayout.addView(essenceText)

        // Info text
        val infoText = TextView(this)
        infoText.text = "Each extra die rolls on every click!\nBuy dice, upgrade them, and unlock buffs."
        infoText.textSize = 14f
        infoText.setTextColor(Color.LTGRAY)
        infoText.gravity = Gravity.CENTER
        infoText.setPadding(10, 0, 10, 20)
        mainLayout.addView(infoText)

        // ScrollView für Würfel
        val scrollView = ScrollView(this)
        diceContainer = LinearLayout(this)
        diceContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(diceContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Back button
        val backButton = Button(this)
        backButton.text = "← BACK"
        backButton.textSize = 18f
        backButton.setBackgroundColor(Color.rgb(80, 80, 80))
        backButton.setTextColor(Color.WHITE)
        backButton.setOnClickListener { finish() }
        mainLayout.addView(backButton)

        setContentView(mainLayout)

        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateHandler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun updateUI() {
        // Update essence display
        essenceText.text = "Divine Essence: ${GameState.divineEssence}"

        // Rebuild dice list
        diceContainer.removeAllViews()

        // Show existing dice
        val dice = GameState.getExtraDice()
        dice.forEachIndexed { index, die ->
            diceContainer.addView(createDiceCard(index, die))
        }

        // Show "Buy Next Dice" button if not at max
        if (GameState.getExtraDiceCount() < 5) {
            diceContainer.addView(createBuyDiceCard())
        } else {
            val maxText = TextView(this)
            maxText.text = "⭐ Maximum dice reached! (5/5)"
            maxText.textSize = 16f
            maxText.setTextColor(Color.rgb(255, 215, 0))
            maxText.gravity = Gravity.CENTER
            maxText.setPadding(20, 40, 20, 20)
            diceContainer.addView(maxText)
        }
    }

    private fun createDiceCard(index: Int, die: ExtraDice): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(20, 20, 20, 20)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 20)
        card.layoutParams = params

        // Title
        val title = TextView(this)
        title.text = "Dice #${index + 1} - ${die.diceLevel.name}"
        title.textSize = 22f
        title.setTextColor(Color.rgb(100, 200, 255))
        title.gravity = Gravity.CENTER
        card.addView(title)

        // Upgrade button
        if (die.canUpgrade()) {
            val upgradeButton = Button(this)
            val nextLevel = die.diceLevel.next()
            val cost = die.getUpgradeCost()
            upgradeButton.text = "Upgrade to ${nextLevel?.name} (${cost} DE)"
            upgradeButton.textSize = 16f
            upgradeButton.setBackgroundColor(Color.rgb(50, 100, 200))
            upgradeButton.setTextColor(Color.WHITE)
            upgradeButton.setPadding(15, 15, 15, 15)

            val canAfford = GameState.canUpgradeExtraDice(index)
            upgradeButton.isEnabled = canAfford
            if (!canAfford) {
                upgradeButton.setBackgroundColor(Color.rgb(100, 100, 100))
            }

            upgradeButton.setOnClickListener {
                if (GameState.upgradeExtraDice(index)) {
                    GameState.saveState(this)
                    updateUI()
                }
            }
            val buttonParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            buttonParams.setMargins(0, 10, 0, 10)
            upgradeButton.layoutParams = buttonParams
            card.addView(upgradeButton)
        } else {
            val maxText = TextView(this)
            maxText.text = "⭐ MAX LEVEL"
            maxText.textSize = 14f
            maxText.setTextColor(Color.rgb(255, 215, 0))
            maxText.gravity = Gravity.CENTER
            maxText.setPadding(0, 10, 0, 10)
            card.addView(maxText)
        }

        // Buff slots
        val slotsTitle = TextView(this)
        slotsTitle.text = "Buff Slots:"
        slotsTitle.textSize = 18f
        slotsTitle.setTextColor(Color.rgb(200, 200, 200))
        slotsTitle.setPadding(0, 10, 0, 10)
        card.addView(slotsTitle)

        BuffSlotType.values().forEach { slotType ->
            card.addView(createBuffSlot(index, die, slotType))
        }

        return card
    }

    private fun createBuffSlot(diceIndex: Int, die: ExtraDice, slotType: BuffSlotType): LinearLayout {
        val slot = die.buffSlots.find { it.type == slotType } ?: return LinearLayout(this)

        val slotLayout = LinearLayout(this)
        slotLayout.orientation = LinearLayout.HORIZONTAL
        slotLayout.setPadding(10, 5, 10, 5)

        val isUnlocked = slot.isUnlocked(die.diceLevel)
        val isPurchased = slot.isPurchased()

        // Slot info
        val infoText = TextView(this)
        val slotName = when (slotType) {
            BuffSlotType.FLAT_SCORE -> "⚡ Flat Score (+10/lv)"
            BuffSlotType.CRIT_CHANCE -> "💥 Crit (5s, +50%/lv)"
            BuffSlotType.PASSIVE_POINTS -> "⏱️ Passive (10s, 5pts/s/lv)"
            BuffSlotType.ESSENCE_MULT -> "✨ Essence (10s, +25%/lv)"
            BuffSlotType.BUFF_DURATION -> "⏳ Duration (+1s/lv)"
            BuffSlotType.MEGA_CRIT -> "💫 Burst (100-1000/lv)"
        }

        val statusText = when {
            !isUnlocked -> " [LOCKED - Need ${slotType.unlockedAt.name}]"
            !isPurchased -> " [NOT BOUGHT]"
            else -> " [Lv ${slot.level}]"
        }

        infoText.text = "$slotName $statusText"
        infoText.textSize = 14f
        infoText.setTextColor(when {
            !isUnlocked -> Color.rgb(100, 100, 100)
            !isPurchased -> Color.rgb(200, 200, 100)
            else -> Color.rgb(100, 255, 100)
        })
        infoText.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        slotLayout.addView(infoText)

        // Buy/Upgrade button
        if (isUnlocked) {
            val buyButton = Button(this)
            val cost = slot.getUpgradeCost()
            buyButton.text = if (isPurchased) "↑ $cost" else "Buy $cost"
            buyButton.textSize = 12f
            buyButton.setBackgroundColor(Color.rgb(50, 150, 50))
            buyButton.setTextColor(Color.WHITE)
            buyButton.setPadding(10, 5, 10, 5)

            val canAfford = GameState.canUpgradeBuffSlot(diceIndex, slotType)
            buyButton.isEnabled = canAfford
            if (!canAfford) {
                buyButton.setBackgroundColor(Color.rgb(80, 80, 80))
            }

            buyButton.setOnClickListener {
                if (GameState.upgradeBuffSlot(diceIndex, slotType)) {
                    GameState.saveState(this)
                    updateUI()
                }
            }
            slotLayout.addView(buyButton)
        }

        return slotLayout
    }

    private fun createBuyDiceCard(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(40, 60, 40))
        card.setPadding(20, 20, 20, 20)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 20)
        card.layoutParams = params

        val title = TextView(this)
        title.text = "🎲 Buy Extra Dice ${GameState.getExtraDiceCount() + 1}/5"
        title.textSize = 20f
        title.setTextColor(Color.rgb(150, 255, 150))
        title.gravity = Gravity.CENTER
        card.addView(title)

        val buyButton = Button(this)
        val cost = GameState.getNextExtraDieCost()
        buyButton.text = "Buy New Die (${cost} DE)"
        buyButton.textSize = 18f
        buyButton.setBackgroundColor(Color.rgb(50, 150, 50))
        buyButton.setTextColor(Color.WHITE)
        buyButton.setPadding(20, 20, 20, 20)

        val canAfford = GameState.canAffordNextExtraDice()
        buyButton.isEnabled = canAfford
        if (!canAfford) {
            buyButton.setBackgroundColor(Color.rgb(100, 100, 100))
        }

        buyButton.setOnClickListener {
            if (GameState.buyExtraDice()) {
                GameState.saveState(this)
                updateUI()
            }
        }
        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.setMargins(0, 10, 0, 0)
        buyButton.layoutParams = buttonParams
        card.addView(buyButton)

        return card
    }
}
