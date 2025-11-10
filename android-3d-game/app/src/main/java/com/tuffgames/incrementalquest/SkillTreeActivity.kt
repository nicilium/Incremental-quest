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

class SkillTreeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        refreshUI()
    }

    private fun refreshUI() {
        // Main layout
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(10, 10, 15))
        mainLayout.setPadding(15, 15, 15, 15)

        // ScrollView for content
        val scrollView = ScrollView(this)
        val contentLayout = LinearLayout(this)
        contentLayout.orientation = LinearLayout.VERTICAL
        scrollView.addView(contentLayout)

        // Title
        val titleText = TextView(this)
        titleText.text = "🌳 SKILL-BAUM"
        titleText.textSize = 24f
        titleText.setTextColor(Color.rgb(100, 255, 100))
        titleText.typeface = Typeface.DEFAULT_BOLD
        titleText.gravity = Gravity.CENTER
        titleText.setPadding(0, 0, 0, 10)
        contentLayout.addView(titleText)

        // Available Skill Points
        val skillPointsText = TextView(this)
        val availablePoints = GameState.availableSkillPoints
        val characterLevel = GameState.getCharacterStats()?.level ?: 1
        skillPointsText.text = """
📊 Verfügbare Skill-Points: $availablePoints
🎯 Charakter-Level: $characterLevel/100
        """.trimIndent()
        skillPointsText.textSize = 14f
        skillPointsText.setTextColor(Color.rgb(255, 215, 0))
        skillPointsText.typeface = Typeface.DEFAULT_BOLD
        skillPointsText.gravity = Gravity.CENTER
        skillPointsText.setPadding(0, 10, 0, 10)
        skillPointsText.setBackgroundColor(Color.rgb(30, 30, 40))
        contentLayout.addView(skillPointsText)

        // Info Text
        val infoText = TextView(this)
        infoText.text = """
ℹ️ Jeder Skill kostet 1 Point
💡 1 Point pro Level-Up
🔄 Respec: 50 Divine Essence
        """.trimIndent()
        infoText.textSize = 11f
        infoText.setTextColor(Color.rgb(180, 180, 200))
        infoText.gravity = Gravity.CENTER
        infoText.setPadding(10, 10, 10, 10)
        contentLayout.addView(infoText)

        addDivider(contentLayout)

        // Zeitdilatation Button (Level 100 Active Skill)
        if (GameState.getUnlockedSkills().contains(UniversalSkill.ZEITDILATATION)) {
            val zeitButton = createZeitdilatationButton()
            contentLayout.addView(zeitButton)
            addDivider(contentLayout)
        }

        // Respec Button
        val respecButton = createRespecButton()
        contentLayout.addView(respecButton)

        addDivider(contentLayout)

        // Skill Tiers (5 Tiers, 20 levels each)
        val tiers = listOf(
            1 to "TIER 1 (Level 1-20) - Grundlagen",
            2 to "TIER 2 (Level 21-40) - Fortgeschritten",
            3 to "TIER 3 (Level 41-60) - Experte",
            4 to "TIER 4 (Level 61-80) - Meister",
            5 to "TIER 5 (Level 81-100) - Legende"
        )

        for ((tier, tierName) in tiers) {
            val tierSection = createTierSection(tier, tierName)
            contentLayout.addView(tierSection)
            addDivider(contentLayout)
        }

        // Back button
        val backButton = Button(this)
        backButton.text = "🔙 Zurück zur Taverne"
        backButton.textSize = 16f
        backButton.setBackgroundColor(Color.rgb(60, 60, 80))
        backButton.setTextColor(Color.WHITE)
        backButton.setPadding(20, 15, 20, 15)
        backButton.setOnClickListener {
            finish()
        }
        contentLayout.addView(backButton)

        // Add to main layout
        mainLayout.addView(scrollView)
        setContentView(mainLayout)
    }

    private fun createTierSection(tier: Int, tierName: String): LinearLayout {
        val section = LinearLayout(this)
        section.orientation = LinearLayout.VERTICAL
        section.setBackgroundColor(Color.rgb(20, 20, 30))
        section.setPadding(15, 15, 15, 15)

        // Tier title
        val tierTitle = TextView(this)
        tierTitle.text = tierName
        tierTitle.textSize = 18f
        tierTitle.setTextColor(getTierColor(tier))
        tierTitle.typeface = Typeface.DEFAULT_BOLD
        tierTitle.setPadding(0, 0, 0, 10)
        section.addView(tierTitle)

        // Get skills for this tier
        val skillsInTier = UniversalSkill.values().filter { it.tier == tier }

        // Group skills: Incremental vs Combat
        val incrementalSkills = skillsInTier.filter { it.category == SkillCategory.INCREMENTAL }
        val combatSkills = skillsInTier.filter { it.category == SkillCategory.COMBAT }

        // Incremental Section
        if (incrementalSkills.isNotEmpty()) {
            val incrementalLabel = TextView(this)
            incrementalLabel.text = "⚡ Incremental Skills"
            incrementalLabel.textSize = 14f
            incrementalLabel.setTextColor(Color.rgb(100, 200, 255))
            incrementalLabel.typeface = Typeface.DEFAULT_BOLD
            incrementalLabel.setPadding(0, 10, 0, 5)
            section.addView(incrementalLabel)

            for (skill in incrementalSkills) {
                val skillButton = createSkillButton(skill)
                section.addView(skillButton)
            }
        }

        // Combat Section
        if (combatSkills.isNotEmpty()) {
            val combatLabel = TextView(this)
            combatLabel.text = "⚔️ Combat Skills"
            combatLabel.textSize = 14f
            combatLabel.setTextColor(Color.rgb(255, 100, 100))
            combatLabel.typeface = Typeface.DEFAULT_BOLD
            combatLabel.setPadding(0, 15, 0, 5)
            section.addView(combatLabel)

            for (skill in combatSkills) {
                val skillButton = createSkillButton(skill)
                section.addView(skillButton)
            }
        }

        return section
    }

    private fun createSkillButton(skill: UniversalSkill): Button {
        val button = Button(this)
        val characterLevel = GameState.getCharacterStats()?.level ?: 1
        val isUnlocked = GameState.getUnlockedSkills().contains(skill)
        val canUnlock = GameState.canUnlockSkill(skill)
        val isBlocked = skill.isChoice && skill.choiceGroup != null &&
                UniversalSkill.values().any {
                    it.choiceGroup == skill.choiceGroup && it != skill && GameState.getUnlockedSkills().contains(it)
                }

        // Button text
        val prefix = when {
            isUnlocked -> "✅"
            canUnlock -> "⭐"
            isBlocked -> "❌"
            else -> "🔒"
        }

        val choiceIndicator = if (skill.isChoice) " [A/B]" else ""
        button.text = "$prefix $prefix Lv${skill.levelRequirement}: ${skill.displayName}$choiceIndicator"
        button.textSize = 12f

        // Button color
        button.setBackgroundColor(when {
            isUnlocked -> Color.rgb(50, 150, 50)  // Green
            canUnlock -> Color.rgb(200, 150, 50)  // Yellow/Orange
            isBlocked -> Color.rgb(150, 50, 50)   // Red
            else -> Color.rgb(60, 60, 70)         // Gray
        })

        button.setTextColor(Color.WHITE)
        button.setPadding(15, 12, 15, 12)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 5, 0, 5)
        button.layoutParams = params

        button.setOnClickListener {
            showSkillDialog(skill, isUnlocked, canUnlock, isBlocked)
        }

        return button
    }

    private fun showSkillDialog(skill: UniversalSkill, isUnlocked: Boolean, canUnlock: Boolean, isBlocked: Boolean) {
        val characterLevel = GameState.getCharacterStats()?.level ?: 1

        val message = buildString {
            append("${skill.displayName}\n\n")
            append("📝 ${skill.description}\n\n")
            append("📊 Level-Requirement: ${skill.levelRequirement}\n")
            append("💰 Kosten: 1 Skill Point\n")
            append("📂 Kategorie: ${if (skill.category == SkillCategory.INCREMENTAL) "⚡ Incremental" else "⚔️ Combat"}\n")

            if (skill.isChoice) {
                append("⚠️ Choice-Skill: Nur eine Option pro Gruppe!\n")
            }

            append("\n")
            when {
                isUnlocked -> append("✅ FREIGESCHALTET")
                canUnlock -> append("⭐ VERFÜGBAR ZUM FREISCHALTEN")
                isBlocked -> append("❌ BLOCKIERT (Andere Option gewählt)")
                characterLevel < skill.levelRequirement -> append("🔒 Level ${skill.levelRequirement} erforderlich (aktuell: $characterLevel)")
                GameState.availableSkillPoints < 1 -> append("🔒 Keine Skill-Points verfügbar")
                else -> append("🔒 GESPERRT")
            }
        }

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("🌳 ${skill.displayName}")
        dialog.setMessage(message)

        if (canUnlock && !isUnlocked) {
            dialog.setPositiveButton("✅ Freischalten") { _, _ ->
                if (GameState.unlockSkill(skill)) {
                    showToast("✅ ${skill.displayName} freigeschaltet!")
                    refreshUI()
                } else {
                    showToast("❌ Konnte Skill nicht freischalten")
                }
            }
            dialog.setNegativeButton("❌ Abbrechen", null)
        } else {
            dialog.setPositiveButton("OK", null)
        }

        dialog.show()
    }

    private fun createZeitdilatationButton(): Button {
        val button = Button(this)
        val canUse = GameState.canUseZeitdilatation()
        val isActive = GameState.zeitdilatationActive
        val cooldownSec = GameState.getZeitdilatationCooldown()

        button.text = when {
            isActive -> "⚡ ZEITDILATATION AKTIV! ⚡"
            canUse -> "⚡ Zeitdilatation aktivieren"
            else -> "⚡ Zeitdilatation (Cooldown: ${cooldownSec}s)"
        }
        button.textSize = 14f
        button.setBackgroundColor(when {
            isActive -> Color.rgb(255, 215, 0)  // Gold
            canUse -> Color.rgb(150, 100, 255)  // Purple
            else -> Color.rgb(80, 80, 100)      // Gray
        })
        button.setTextColor(Color.BLACK)
        button.typeface = Typeface.DEFAULT_BOLD
        button.setPadding(15, 15, 15, 15)

        button.setOnClickListener {
            if (isActive) {
                showToast("⚡ Zeitdilatation ist bereits aktiv!")
            } else if (canUse) {
                showZeitdilatationDialog()
            } else {
                showToast("⏳ Cooldown: ${cooldownSec}s verbleibend")
            }
        }

        return button
    }

    private fun showZeitdilatationDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("⚡ Zeitdilatation aktivieren?")
        dialog.setMessage("""
🌟 ZEITDILATATION
Level 100 Ultimate Ability

⏱️ Duration: 60 Sekunden
⚡ 5x Click Power
🤖 2x Auto-Clicker Speed
💰 2x Gold
✨ 2x XP

Cooldown: 10 Minuten

Jetzt aktivieren?
        """.trimIndent())

        dialog.setPositiveButton("⚡ AKTIVIEREN!") { _, _ ->
            if (GameState.activateZeitdilatation()) {
                showToast("⚡ ZEITDILATATION AKTIVIERT!")
                refreshUI()
            } else {
                showToast("❌ Konnte Zeitdilatation nicht aktivieren")
            }
        }
        dialog.setNegativeButton("Abbrechen", null)
        dialog.show()
    }

    private fun createRespecButton(): Button {
        val button = Button(this)
        val cost = GameState.getRespecCost()
        val canRespec = GameState.canRespec()
        val currentDE = GameState.divineEssence

        button.text = if (canRespec) {
            "🔄 Skill-Tree zurücksetzen ($cost DE)"
        } else {
            "🔄 Respec nicht verfügbar (Kosten: $cost DE, Verfügbar: $currentDE DE)"
        }
        button.textSize = 13f
        button.setBackgroundColor(if (canRespec) Color.rgb(200, 100, 50) else Color.rgb(60, 60, 70))
        button.setTextColor(Color.WHITE)
        button.setPadding(15, 12, 15, 12)

        button.setOnClickListener {
            if (canRespec) {
                showRespecDialog()
            } else {
                showToast("❌ Nicht genug Divine Essence (benötigt: $cost DE)")
            }
        }

        return button
    }

    private fun showRespecDialog() {
        val cost = GameState.getRespecCost()
        val unlockedCount = GameState.getUnlockedSkills().size

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("🔄 Skill-Tree zurücksetzen?")
        dialog.setMessage("""
⚠️ WARNUNG: Alle Skills werden zurückgesetzt!

📊 Aktuell freigeschaltete Skills: $unlockedCount
💰 Kosten: $cost Divine Essence
✅ Alle Skill-Points werden zurückerstattet

❌ Alle Stat-Boni werden entfernt:
   - HP, Armor, Mana Boni
   - Click-Power, Gold, XP Boni
   - Zeitdilatation-Cooldown Reset

Wirklich zurücksetzen?
        """.trimIndent())

        dialog.setPositiveButton("✅ Zurücksetzen") { _, _ ->
            if (GameState.respecSkills()) {
                showToast("✅ Skill-Tree zurückgesetzt! $cost DE bezahlt.")
                refreshUI()
            } else {
                showToast("❌ Respec fehlgeschlagen")
            }
        }
        dialog.setNegativeButton("❌ Abbrechen", null)
        dialog.show()
    }

    private fun getTierColor(tier: Int): Int {
        return when (tier) {
            1 -> Color.rgb(200, 200, 200)  // Gray
            2 -> Color.rgb(100, 255, 100)  // Green
            3 -> Color.rgb(100, 200, 255)  // Blue
            4 -> Color.rgb(200, 100, 255)  // Purple
            5 -> Color.rgb(255, 215, 0)    // Gold
            else -> Color.WHITE
        }
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

    override fun onResume() {
        super.onResume()
        // Update Zeitdilatation state
        GameState.updateZeitdilatation()
    }
}
