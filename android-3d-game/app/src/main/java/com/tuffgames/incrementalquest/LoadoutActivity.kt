package com.tuffgames.incrementalquest

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class LoadoutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Vollbild
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        refreshUI()
    }

    private fun refreshUI() {
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(15, 15, 20))
        mainLayout.setPadding(15, 15, 15, 15)

        val scrollView = ScrollView(this)
        val contentLayout = LinearLayout(this)
        contentLayout.orientation = LinearLayout.VERTICAL
        scrollView.addView(contentLayout)

        // Title
        val titleText = TextView(this)
        titleText.text = "⚡ LOADOUT KONFIGURATION"
        titleText.textSize = 22f
        titleText.setTextColor(Color.rgb(255, 215, 0))
        titleText.typeface = Typeface.DEFAULT_BOLD
        titleText.gravity = Gravity.CENTER
        titleText.setPadding(0, 0, 0, 15)
        contentLayout.addView(titleText)

        val stats = GameState.getCharacterStats()
        val loadout = GameState.getCharacterLoadout()
        val playerClass = GameState.selectedClass

        if (stats == null || playerClass == null) {
            val errorText = TextView(this)
            errorText.text = "❌ Keine Klasse gewählt!"
            errorText.textSize = 16f
            errorText.setTextColor(Color.RED)
            errorText.gravity = Gravity.CENTER
            contentLayout.addView(errorText)

            val backButton = Button(this)
            backButton.text = "🔙 Zurück"
            backButton.setOnClickListener { finish() }
            contentLayout.addView(backButton)

            mainLayout.addView(scrollView)
            setContentView(mainLayout)
            return
        }

        // Info
        val infoText = TextView(this)
        infoText.text = """
📊 Level: ${stats.level}
⚔️ Klasse: ${playerClass.displayName}

Wähle deine aktiven Fähigkeiten für den Kampf!
        """.trimIndent()
        infoText.textSize = 13f
        infoText.setTextColor(Color.rgb(200, 200, 220))
        infoText.gravity = Gravity.CENTER
        infoText.setPadding(10, 5, 10, 15)
        contentLayout.addView(infoText)

        addDivider(contentLayout)

        // Normal Skills Section
        val normalHeader = TextView(this)
        normalHeader.text = "⚔️ NORMALE FÄHIGKEITEN (${loadout.getMaxNormalSlots(stats.level)} Slots)"
        normalHeader.textSize = 18f
        normalHeader.setTextColor(Color.rgb(100, 200, 255))
        normalHeader.typeface = Typeface.DEFAULT_BOLD
        normalHeader.setPadding(0, 10, 0, 10)
        contentLayout.addView(normalHeader)

        // Normal Skill Slots
        for (i in 0 until loadout.getMaxNormalSlots(stats.level)) {
            val slotButton = createSkillSlotButton(i, false, stats.level)
            contentLayout.addView(slotButton)
        }

        addDivider(contentLayout)

        // Ultimate Skill Section
        if (loadout.hasUltimateSlot(stats.level)) {
            val ultHeader = TextView(this)
            ultHeader.text = "💥 ULTIMATE FÄHIGKEIT (1 Slot)"
            ultHeader.textSize = 18f
            ultHeader.setTextColor(Color.rgb(255, 100, 100))
            ultHeader.typeface = Typeface.DEFAULT_BOLD
            ultHeader.setPadding(0, 10, 0, 10)
            contentLayout.addView(ultHeader)

            val ultButton = createSkillSlotButton(0, true, stats.level)
            contentLayout.addView(ultButton)

            addDivider(contentLayout)
        }

        // Passive Abilities Section
        val passiveHeader = TextView(this)
        passiveHeader.text = "✨ PASSIVE FÄHIGKEITEN (Automatisch)"
        passiveHeader.textSize = 18f
        passiveHeader.setTextColor(Color.rgb(150, 255, 150))
        passiveHeader.typeface = Typeface.DEFAULT_BOLD
        passiveHeader.setPadding(0, 10, 0, 10)
        contentLayout.addView(passiveHeader)

        // Get passives based on player class
        val passiveList: List<Any> = when(playerClass) {
            PlayerClass.PALADIN -> PaladinPassive.values().filter { it.isUnlockedAt(stats.level) }
            PlayerClass.BARBAR -> BarbarPassive.values().filter { it.isUnlockedAt(stats.level) }
            else -> emptyList()
        }

        if (passiveList.isEmpty()) {
            val noPassives = TextView(this)
            noPassives.text = "Keine Passives freigeschaltet"
            noPassives.textSize = 12f
            noPassives.setTextColor(Color.GRAY)
            noPassives.setPadding(10, 5, 10, 5)
            contentLayout.addView(noPassives)
        } else {
            for (passive in passiveList) {
                val passiveText = TextView(this)

                // Handle different passive types
                val (displayName, levelReq) = when(passive) {
                    is PaladinPassive -> Pair(passive.displayName, passive.levelRequirement)
                    is BarbarPassive -> Pair(passive.displayName, passive.levelRequirement)
                    else -> Pair("Unknown", 0)
                }

                passiveText.text = "✅ Lv$levelReq: $displayName"
                passiveText.textSize = 13f
                passiveText.setTextColor(Color.rgb(150, 255, 150))
                passiveText.setPadding(10, 8, 10, 8)
                passiveText.setBackgroundColor(Color.rgb(30, 50, 30))
                passiveText.setOnClickListener {
                    showPassiveDialog(passive, stats.level)
                }
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 5, 0, 5)
                passiveText.layoutParams = params
                contentLayout.addView(passiveText)
            }
        }

        addDivider(contentLayout)

        // Back Button
        val backButton = Button(this)
        backButton.text = "💾 Speichern & Zurück"
        backButton.textSize = 16f
        backButton.setBackgroundColor(Color.rgb(50, 150, 50))
        backButton.setTextColor(Color.WHITE)
        backButton.typeface = Typeface.DEFAULT_BOLD
        backButton.setPadding(20, 15, 20, 15)
        backButton.setOnClickListener {
            showToast("✅ Loadout gespeichert!")
            finish()
        }
        contentLayout.addView(backButton)

        mainLayout.addView(scrollView)
        setContentView(mainLayout)
    }

    private fun createSkillSlotButton(slotIndex: Int, isUltimate: Boolean, characterLevel: Int): Button {
        val button = Button(this)
        val loadout = GameState.getCharacterLoadout()

        val currentSkill = if (isUltimate) {
            loadout.ultimateAbility
        } else {
            loadout.normalAbilities.getOrNull(slotIndex)
        }

        val skillName = if (currentSkill != null) {
            when(currentSkill) {
                is PaladinAbility -> currentSkill.displayName
                is BarbarAbility -> currentSkill.displayName
                else -> "Unknown"
            }
        } else {
            "[Leer]"
        }

        button.text = "${if (isUltimate) "💥" else "⚔️"} Slot ${slotIndex + 1}: $skillName"

        button.textSize = 14f
        button.setBackgroundColor(if (currentSkill != null) Color.rgb(50, 100, 150) else Color.rgb(60, 60, 70))
        button.setTextColor(Color.WHITE)
        button.setPadding(15, 12, 15, 12)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 5, 0, 5)
        button.layoutParams = params

        button.setOnClickListener {
            showSkillSelectionDialog(slotIndex, isUltimate, characterLevel)
        }

        return button
    }

    private fun showSkillSelectionDialog(slotIndex: Int, isUltimate: Boolean, characterLevel: Int) {
        val playerClass = GameState.selectedClass ?: return

        // Get available skills (filter based on player class)
        val availableSkills: List<Any> = when(playerClass) {
            PlayerClass.PALADIN -> {
                if (isUltimate) {
                    (playerClass.getAvailableUltimates() as List<PaladinAbility>).filter { it.isUnlockedAt(characterLevel) }
                } else {
                    (playerClass.getAvailableAbilities() as List<PaladinAbility>).filter { it.isUnlockedAt(characterLevel) }
                }
            }
            PlayerClass.BARBAR -> {
                if (isUltimate) {
                    (playerClass.getAvailableUltimates() as List<BarbarAbility>).filter { it.isUnlockedAt(characterLevel) }
                } else {
                    (playerClass.getAvailableAbilities() as List<BarbarAbility>).filter { it.isUnlockedAt(characterLevel) }
                }
            }
            else -> emptyList()
        }

        if (availableSkills.isEmpty()) {
            showToast("Keine Fähigkeiten verfügbar!")
            return
        }

        // Extract display names (works for both Paladin and Barbar)
        val skillNames = availableSkills.map { ability ->
            when(ability) {
                is PaladinAbility -> ability.displayName
                is BarbarAbility -> ability.displayName
                else -> "Unknown"
            }
        }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isUltimate) "💥 Ultimate wählen" else "⚔️ Skill wählen (Slot ${slotIndex + 1})")

        builder.setItems(skillNames) { _, which ->
            val selectedSkill = availableSkills[which]
            val loadout = GameState.getCharacterLoadout()

            if (isUltimate) {
                loadout.ultimateAbility = selectedSkill
            } else {
                loadout.setNormalAbility(slotIndex, selectedSkill, characterLevel)
            }

            val skillName = when(selectedSkill) {
                is PaladinAbility -> selectedSkill.displayName
                is BarbarAbility -> selectedSkill.displayName
                else -> "Unknown"
            }

            showToast("✅ $skillName ausgewählt!")
            refreshUI()
        }

        builder.setNegativeButton("❌ Entfernen") { _, _ ->
            val loadout = GameState.getCharacterLoadout()
            if (isUltimate) {
                loadout.ultimateAbility = null
            } else {
                loadout.setNormalAbility(slotIndex, null, characterLevel)
            }
            showToast("🗑️ Slot geleert")
            refreshUI()
        }

        builder.setNeutralButton("Abbrechen", null)
        builder.show()
    }

    private fun showPassiveDialog(passive: Any, characterLevel: Int) {
        val dialog = AlertDialog.Builder(this)

        val (displayName, levelReq, scaledDescription) = when(passive) {
            is PaladinPassive -> Triple(
                passive.displayName,
                passive.levelRequirement,
                passive.getScaledDescription(characterLevel)
            )
            is BarbarPassive -> Triple(
                passive.displayName,
                passive.levelRequirement,
                passive.getScaledDescription(characterLevel)
            )
            else -> Triple("Unknown", 0, "Unknown passive")
        }

        dialog.setTitle("✨ $displayName")
        dialog.setMessage("""
Level $levelReq Passive

$scaledDescription

Diese Fähigkeit ist automatisch aktiv!
        """.trimIndent())
        dialog.setPositiveButton("OK", null)
        dialog.show()
    }

    private fun addDivider(layout: LinearLayout) {
        val divider = TextView(this)
        divider.text = "─".repeat(40)
        divider.textSize = 10f
        divider.setTextColor(Color.rgb(60, 60, 80))
        divider.gravity = Gravity.CENTER
        divider.setPadding(0, 10, 0, 10)
        layout.addView(divider)
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
