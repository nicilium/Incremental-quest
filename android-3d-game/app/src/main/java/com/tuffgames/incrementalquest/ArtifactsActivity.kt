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
 * ArtifactsActivity - Artifact/Relic Management System
 *
 * Displays equipped artifacts and inventory
 * Allows equipping/unequipping artifacts
 */
class ArtifactsActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var equippedContainer: LinearLayout
    private lateinit var inventoryContainer: LinearLayout

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            GameState.processOfflineClicks()
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
        title.text = "🏺 ARTIFACTS 🏺"
        title.textSize = 32f
        title.setTextColor(Color.rgb(255, 215, 0))
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Info text
        val infoText = TextView(this)
        infoText.text = "Equip artifacts to gain powerful bonuses!\n10% drop chance on prestige | Max 5 equipped"
        infoText.textSize = 13f
        infoText.setTextColor(Color.LTGRAY)
        infoText.gravity = Gravity.CENTER
        infoText.setPadding(10, 15, 10, 20)
        mainLayout.addView(infoText)

        // Equipped artifacts section
        val equippedHeader = TextView(this)
        equippedHeader.text = "━━━ EQUIPPED ARTIFACTS ━━━"
        equippedHeader.textSize = 18f
        equippedHeader.setTextColor(Color.rgb(150, 255, 150))
        equippedHeader.gravity = Gravity.CENTER
        equippedHeader.setPadding(0, 10, 0, 10)
        mainLayout.addView(equippedHeader)

        // Equipped container
        equippedContainer = LinearLayout(this)
        equippedContainer.orientation = LinearLayout.VERTICAL
        mainLayout.addView(equippedContainer)

        // Divider
        val divider = TextView(this)
        divider.text = "━━━ INVENTORY ━━━"
        divider.textSize = 18f
        divider.setTextColor(Color.rgb(150, 150, 255))
        divider.gravity = Gravity.CENTER
        divider.setPadding(0, 20, 0, 10)
        mainLayout.addView(divider)

        // ScrollView for inventory
        val scrollView = ScrollView(this)
        inventoryContainer = LinearLayout(this)
        inventoryContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(inventoryContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Populate UI
        populateEquipped()
        populateInventory()

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
    }

    private fun populateEquipped() {
        equippedContainer.removeAllViews()

        for (slot in 0 until 5) {
            val artifact = GameState.getEquippedArtifact(slot)

            if (artifact != null) {
                // Show equipped artifact
                val card = createEquippedArtifactCard(artifact, slot)
                equippedContainer.addView(card)
            } else {
                // Show empty slot
                val emptyCard = createEmptySlotCard(slot)
                equippedContainer.addView(emptyCard)
            }
        }
    }

    private fun createEquippedArtifactCard(artifact: Artifact, slot: Int): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(20, 60, 20))
        card.setPadding(15, 15, 15, 15)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 10)
        card.layoutParams = cardParams

        // Slot number
        val slotText = TextView(this)
        slotText.text = "Slot ${slot + 1}"
        slotText.textSize = 12f
        slotText.setTextColor(Color.LTGRAY)
        card.addView(slotText)

        // Artifact name with emoji
        val nameText = TextView(this)
        nameText.text = "${artifact.type.emoji} ${artifact.getDisplayName()}"
        nameText.textSize = 18f
        nameText.setTextColor(artifact.rarity.color)
        nameText.gravity = Gravity.CENTER
        nameText.setPadding(0, 5, 0, 5)
        card.addView(nameText)

        // Artifact description
        val descText = TextView(this)
        descText.text = artifact.getDescription()
        descText.textSize = 14f
        descText.setTextColor(Color.rgb(150, 255, 150))
        descText.gravity = Gravity.CENTER
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        // Unequip button
        val unequipButton = Button(this)
        unequipButton.text = "Unequip"
        unequipButton.textSize = 14f
        unequipButton.setBackgroundColor(Color.rgb(150, 50, 50))
        unequipButton.setTextColor(Color.WHITE)
        unequipButton.setOnClickListener {
            if (GameState.unequipArtifact(slot)) {
                refresh()
            }
        }
        card.addView(unequipButton)

        return card
    }

    private fun createEmptySlotCard(slot: Int): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 10)
        card.layoutParams = cardParams

        // Slot number
        val slotText = TextView(this)
        slotText.text = "Slot ${slot + 1}"
        slotText.textSize = 12f
        slotText.setTextColor(Color.LTGRAY)
        card.addView(slotText)

        // Empty indicator
        val emptyText = TextView(this)
        emptyText.text = "[ Empty Slot ]"
        emptyText.textSize = 16f
        emptyText.setTextColor(Color.DKGRAY)
        emptyText.gravity = Gravity.CENTER
        emptyText.setPadding(0, 10, 0, 10)
        card.addView(emptyText)

        return card
    }

    private fun populateInventory() {
        inventoryContainer.removeAllViews()

        val unequippedArtifacts = GameState.getUnequippedArtifacts()

        if (unequippedArtifacts.isEmpty()) {
            // No artifacts in inventory
            val emptyText = TextView(this)
            emptyText.text = "No artifacts in inventory\n\nPrestige to get a 10% chance for an artifact drop!"
            emptyText.textSize = 14f
            emptyText.setTextColor(Color.LTGRAY)
            emptyText.gravity = Gravity.CENTER
            emptyText.setPadding(10, 30, 10, 30)
            inventoryContainer.addView(emptyText)
        } else {
            // Show unequipped artifacts
            unequippedArtifacts.forEach { artifact ->
                inventoryContainer.addView(createInventoryArtifactCard(artifact))
            }
        }
    }

    private fun createInventoryArtifactCard(artifact: Artifact): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(30, 30, 60))
        card.setPadding(15, 15, 15, 15)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 10)
        card.layoutParams = cardParams

        // Artifact name with emoji
        val nameText = TextView(this)
        nameText.text = "${artifact.type.emoji} ${artifact.getDisplayName()}"
        nameText.textSize = 18f
        nameText.setTextColor(artifact.rarity.color)
        nameText.gravity = Gravity.CENTER
        card.addView(nameText)

        // Type description
        val typeDescText = TextView(this)
        typeDescText.text = artifact.type.description
        typeDescText.textSize = 12f
        typeDescText.setTextColor(Color.LTGRAY)
        typeDescText.gravity = Gravity.CENTER
        typeDescText.setPadding(0, 5, 0, 5)
        card.addView(typeDescText)

        // Artifact bonus
        val bonusText = TextView(this)
        bonusText.text = artifact.getDescription()
        bonusText.textSize = 14f
        bonusText.setTextColor(Color.rgb(150, 255, 150))
        bonusText.gravity = Gravity.CENTER
        bonusText.setPadding(0, 5, 0, 10)
        card.addView(bonusText)

        // Equip button (or indicator if all slots full)
        val availableSlot = GameState.getFirstAvailableArtifactSlot()
        if (availableSlot != -1) {
            val equipButton = Button(this)
            equipButton.text = "Equip to Slot ${availableSlot + 1}"
            equipButton.textSize = 14f
            equipButton.setBackgroundColor(Color.rgb(50, 150, 50))
            equipButton.setTextColor(Color.WHITE)
            equipButton.setOnClickListener {
                if (GameState.equipArtifact(artifact, availableSlot)) {
                    refresh()
                }
            }
            card.addView(equipButton)
        } else {
            val fullText = TextView(this)
            fullText.text = "All slots full! Unequip an artifact first."
            fullText.textSize = 12f
            fullText.setTextColor(Color.rgb(255, 100, 100))
            fullText.gravity = Gravity.CENTER
            fullText.setPadding(0, 10, 0, 0)
            card.addView(fullText)
        }

        return card
    }

    private fun refresh() {
        populateEquipped()
        populateInventory()
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
