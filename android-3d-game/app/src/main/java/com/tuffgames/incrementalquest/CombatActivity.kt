package com.tuffgames.incrementalquest

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
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

class CombatActivity : Activity() {

    private lateinit var enemyContainer: LinearLayout
    private lateinit var playerHPBar: TextView
    private lateinit var playerManaBar: TextView
    private lateinit var statusEffectsView: TextView
    private lateinit var combatLogView: TextView
    private lateinit var skillButtonsContainer: LinearLayout
    private lateinit var basicAttackButton: Button
    private lateinit var turnIndicator: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var selectedTargetIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Vollbild-Modus
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Check if combat exists
        val combat = GameState.getActiveCombat()
        if (combat == null) {
            finish()
            return
        }

        // Haupt-Layout
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(15, 10, 10))  // Dunkel-Rot (Kampf-Atmosphäre)
        mainLayout.setPadding(15, 15, 15, 15)

        // ScrollView für Content
        val scrollView = ScrollView(this)
        val contentLayout = LinearLayout(this)
        contentLayout.orientation = LinearLayout.VERTICAL
        scrollView.addView(contentLayout)

        // Title
        val titleText = TextView(this)
        titleText.text = "⚔️ KAMPF ⚔️"
        titleText.textSize = 24f
        titleText.setTextColor(Color.RED)
        titleText.typeface = Typeface.DEFAULT_BOLD
        titleText.gravity = Gravity.CENTER
        titleText.setPadding(0, 0, 0, 20)
        contentLayout.addView(titleText)

        // Tutorial Info (if first combat)
        if (combat.isTutorial) {
            val tutorialText = TextView(this)
            tutorialText.text = """
📖 TUTORIAL - Dein erster Kampf!

🎯 So funktioniert der Kampf:
• Reihenfolge nach Initiative (DEX-Wert)
• Wähle Gegner mit ◀ ▶ Buttons
• Nutze ANGRIFF oder FÄHIGKEITEN
• ⚡ COMBAT Skills: Cooldown in Runden
• 💙 SPELL Skills: Kosten Mana
• Mana: +1 pro Runde, +50% nach Kampf

Viel Erfolg! 🗡️
            """.trimIndent()
            tutorialText.textSize = 11f
            tutorialText.setTextColor(Color.rgb(100, 200, 255))
            tutorialText.typeface = Typeface.MONOSPACE
            tutorialText.setBackgroundColor(Color.rgb(20, 20, 40))
            tutorialText.setPadding(15, 15, 15, 15)
            contentLayout.addView(tutorialText)
        }

        // Divider
        addDivider(contentLayout)

        // Enemy Section
        val enemyLabel = TextView(this)
        enemyLabel.text = "🛡️ GEGNER"
        enemyLabel.textSize = 16f
        enemyLabel.setTextColor(Color.rgb(255, 100, 100))
        enemyLabel.typeface = Typeface.DEFAULT_BOLD
        enemyLabel.gravity = Gravity.CENTER
        contentLayout.addView(enemyLabel)

        // Enemy container (HP bars for all enemies)
        enemyContainer = LinearLayout(this)
        enemyContainer.orientation = LinearLayout.VERTICAL
        enemyContainer.setPadding(0, 10, 0, 10)
        contentLayout.addView(enemyContainer)

        // Enemy target selector
        val targetSelectorLayout = LinearLayout(this)
        targetSelectorLayout.orientation = LinearLayout.HORIZONTAL
        targetSelectorLayout.gravity = Gravity.CENTER
        targetSelectorLayout.setPadding(0, 5, 0, 15)

        val prevTargetButton = Button(this)
        prevTargetButton.text = "◀"
        prevTargetButton.setOnClickListener {
            selectPreviousTarget()
        }
        targetSelectorLayout.addView(prevTargetButton)

        val targetLabel = TextView(this)
        targetLabel.text = "Ziel auswählen"
        targetLabel.textSize = 14f
        targetLabel.setTextColor(Color.YELLOW)
        targetLabel.setPadding(20, 0, 20, 0)
        targetSelectorLayout.addView(targetLabel)

        val nextTargetButton = Button(this)
        nextTargetButton.text = "▶"
        nextTargetButton.setOnClickListener {
            selectNextTarget()
        }
        targetSelectorLayout.addView(nextTargetButton)

        contentLayout.addView(targetSelectorLayout)

        // Divider
        addDivider(contentLayout)

        // Player Section
        val playerLabel = TextView(this)
        playerLabel.text = "🗡️ DEIN STATUS"
        playerLabel.textSize = 16f
        playerLabel.setTextColor(Color.rgb(100, 255, 100))
        playerLabel.typeface = Typeface.DEFAULT_BOLD
        playerLabel.gravity = Gravity.CENTER
        contentLayout.addView(playerLabel)

        // Player HP Bar
        playerHPBar = TextView(this)
        playerHPBar.textSize = 12f
        playerHPBar.setTextColor(Color.WHITE)
        playerHPBar.typeface = Typeface.MONOSPACE
        playerHPBar.setPadding(10, 5, 10, 5)
        contentLayout.addView(playerHPBar)

        // Player Mana Bar
        playerManaBar = TextView(this)
        playerManaBar.textSize = 12f
        playerManaBar.setTextColor(Color.CYAN)
        playerManaBar.typeface = Typeface.MONOSPACE
        playerManaBar.setPadding(10, 5, 10, 5)
        contentLayout.addView(playerManaBar)

        // Status Effects Display
        statusEffectsView = TextView(this)
        statusEffectsView.textSize = 10f
        statusEffectsView.setTextColor(Color.rgb(255, 215, 100))
        statusEffectsView.typeface = Typeface.MONOSPACE
        statusEffectsView.setPadding(10, 5, 10, 15)
        statusEffectsView.setBackgroundColor(Color.rgb(30, 25, 20))
        contentLayout.addView(statusEffectsView)

        // Turn Indicator
        turnIndicator = TextView(this)
        turnIndicator.textSize = 14f
        turnIndicator.typeface = Typeface.DEFAULT_BOLD
        turnIndicator.gravity = Gravity.CENTER
        turnIndicator.setPadding(10, 10, 10, 10)
        contentLayout.addView(turnIndicator)

        // Divider
        addDivider(contentLayout)

        // Actions Section
        val actionsLabel = TextView(this)
        actionsLabel.text = "⚡ AKTIONEN"
        actionsLabel.textSize = 16f
        actionsLabel.setTextColor(Color.rgb(255, 200, 50))
        actionsLabel.typeface = Typeface.DEFAULT_BOLD
        actionsLabel.gravity = Gravity.CENTER
        contentLayout.addView(actionsLabel)

        // Basic Attack Button
        basicAttackButton = Button(this)
        basicAttackButton.text = "⚔️ ANGRIFF"
        basicAttackButton.textSize = 16f
        basicAttackButton.setOnClickListener {
            executeBasicAttack()
        }
        contentLayout.addView(basicAttackButton)

        // Passive Ability Buttons (Lay on Hands, Cleansing Touch)
        val passiveButtonsLayout = LinearLayout(this)
        passiveButtonsLayout.orientation = LinearLayout.HORIZONTAL
        passiveButtonsLayout.gravity = Gravity.CENTER
        passiveButtonsLayout.setPadding(0, 5, 0, 5)

        // Lay on Hands Button
        val layOnHandsButton = Button(this)
        layOnHandsButton.text = "🙏 Lay on Hands"
        layOnHandsButton.textSize = 12f
        layOnHandsButton.setBackgroundColor(Color.rgb(100, 150, 100))
        layOnHandsButton.setOnClickListener {
            useLayOnHands()
        }
        passiveButtonsLayout.addView(layOnHandsButton)

        // Cleansing Touch Button
        val cleansingTouchButton = Button(this)
        cleansingTouchButton.text = "✋ Cleansing Touch"
        cleansingTouchButton.textSize = 12f
        cleansingTouchButton.setBackgroundColor(Color.rgb(150, 100, 150))
        cleansingTouchButton.setOnClickListener {
            useCleansingTouch()
        }
        passiveButtonsLayout.addView(cleansingTouchButton)

        contentLayout.addView(passiveButtonsLayout)

        // Skill Buttons Container
        skillButtonsContainer = LinearLayout(this)
        skillButtonsContainer.orientation = LinearLayout.VERTICAL
        skillButtonsContainer.setPadding(0, 10, 0, 10)
        contentLayout.addView(skillButtonsContainer)

        // Divider
        addDivider(contentLayout)

        // Combat Log
        val logLabel = TextView(this)
        logLabel.text = "📜 KAMPF-LOG"
        logLabel.textSize = 14f
        logLabel.setTextColor(Color.rgb(200, 200, 200))
        logLabel.typeface = Typeface.DEFAULT_BOLD
        logLabel.gravity = Gravity.CENTER
        contentLayout.addView(logLabel)

        combatLogView = TextView(this)
        combatLogView.textSize = 10f
        combatLogView.setTextColor(Color.rgb(180, 180, 180))
        combatLogView.typeface = Typeface.MONOSPACE
        combatLogView.setPadding(10, 10, 10, 10)
        combatLogView.setBackgroundColor(Color.rgb(10, 10, 10))
        contentLayout.addView(combatLogView)

        // Back Button
        val backButton = Button(this)
        backButton.text = "🚪 Taverne"
        backButton.setOnClickListener {
            finish()
        }
        contentLayout.addView(backButton)

        mainLayout.addView(scrollView)
        setContentView(mainLayout)

        // Initial UI update
        updateUI()
    }

    private fun addDivider(layout: LinearLayout) {
        val divider = TextView(this)
        divider.text = "═══════════════════════════════"
        divider.textSize = 10f
        divider.setTextColor(Color.GRAY)
        divider.typeface = Typeface.MONOSPACE
        divider.gravity = Gravity.CENTER
        divider.setPadding(0, 5, 0, 5)
        layout.addView(divider)
    }

    private fun selectPreviousTarget() {
        val combat = GameState.getActiveCombat() ?: return
        val aliveEnemies = combat.getAliveEnemies()
        if (aliveEnemies.isEmpty()) return

        selectedTargetIndex = if (selectedTargetIndex > 0) {
            selectedTargetIndex - 1
        } else {
            aliveEnemies.size - 1
        }
        updateUI()
    }

    private fun selectNextTarget() {
        val combat = GameState.getActiveCombat() ?: return
        val aliveEnemies = combat.getAliveEnemies()
        if (aliveEnemies.isEmpty()) return

        selectedTargetIndex = if (selectedTargetIndex < aliveEnemies.size - 1) {
            selectedTargetIndex + 1
        } else {
            0
        }
        updateUI()
    }

    private fun executeBasicAttack() {
        val combat = GameState.getActiveCombat() ?: return
        if (!combat.isPlayerTurn) return

        // Find actual enemy index in original list
        val aliveEnemies = combat.getAliveEnemies()
        if (selectedTargetIndex >= aliveEnemies.size) return

        val targetEnemy = aliveEnemies[selectedTargetIndex]
        val actualIndex = combat.enemyParty.indexOf(targetEnemy)

        GameState.executePlayerBasicAttack(actualIndex)
        updateUI()

        // Check for combat end
        if (combat.combatEnded) {
            handler.postDelayed({
                showCombatResult()
            }, 1000)
        }
    }

    private fun executeAbility(ability: PaladinAbility) {
        val combat = GameState.getActiveCombat() ?: return
        if (!combat.isPlayerTurn) return

        // Find actual enemy index
        val aliveEnemies = combat.getAliveEnemies()
        if (selectedTargetIndex >= aliveEnemies.size) return

        val targetEnemy = aliveEnemies[selectedTargetIndex]
        val actualIndex = combat.enemyParty.indexOf(targetEnemy)

        GameState.executePlayerAbility(ability, actualIndex)
        updateUI()

        // Check for combat end
        if (combat.combatEnded) {
            handler.postDelayed({
                showCombatResult()
            }, 1000)
        }
    }

    private fun updateUI() {
        val combat = GameState.getActiveCombat() ?: return

        // Update enemy displays
        updateEnemyDisplay(combat)

        // Update player stats
        updatePlayerStats(combat)

        // Update status effects
        updateStatusEffects(combat)

        // Update turn indicator
        updateTurnIndicator(combat)

        // Update skill buttons
        updateSkillButtons(combat)

        // Update combat log
        updateCombatLog(combat)

        // Enable/disable actions based on turn
        val isPlayerTurn = combat.isPlayerTurn && !combat.combatEnded
        basicAttackButton.isEnabled = isPlayerTurn
    }

    private fun updateEnemyDisplay(combat: CombatState) {
        enemyContainer.removeAllViews()

        combat.enemyParty.forEachIndexed { index, enemy ->
            val enemyView = TextView(this)
            val isSelected = combat.getAliveEnemies().getOrNull(selectedTargetIndex) == enemy
            val isAlive = enemy.isAlive()

            val indicator = when {
                !isAlive -> "💀"
                isSelected -> "👉"
                else -> "  "
            }

            val hpPercent = enemy.enemy.getHPPercent()
            val hpBar = createHPBar(hpPercent, 20)

            enemyView.text = "$indicator ${enemy.name} Lv${enemy.enemy.level}\n" +
                    "   ❤️ ${enemy.enemy.currentHP}/${enemy.enemy.maxHP} $hpBar\n" +
                    "   🛡️ AC: ${enemy.enemy.armor}"

            enemyView.textSize = 11f
            enemyView.setTextColor(if (isAlive) Color.rgb(255, 150, 150) else Color.GRAY)
            enemyView.typeface = Typeface.MONOSPACE
            enemyView.setPadding(5, 5, 5, 5)

            if (isSelected && isAlive) {
                enemyView.setBackgroundColor(Color.rgb(60, 30, 30))
            }

            enemyContainer.addView(enemyView)
        }
    }

    private fun updatePlayerStats(combat: CombatState) {
        val player = combat.playerParty.firstOrNull() ?: return
        val stats = player.stats

        val hpPercent = if (stats.maxHP > 0) (stats.currentHP * 100 / stats.maxHP) else 0
        val hpBar = createHPBar(hpPercent, 20)

        playerHPBar.text = "❤️ HP: ${stats.currentHP}/${stats.maxHP} $hpBar"

        val manaPercent = if (stats.maxMana > 0) (stats.currentMana * 100 / stats.maxMana) else 0
        val manaBar = createManaBar(manaPercent, 20)

        playerManaBar.text = "💙 Mana: ${stats.currentMana}/${stats.maxMana} $manaBar"
    }

    private fun updateStatusEffects(combat: CombatState) {
        val player = combat.playerParty.firstOrNull() ?: return
        val stats = player.stats
        val loadout = player.loadout
        val effects = mutableListOf<String>()

        // Active Cooldowns
        val activeCooldowns = combat.abilityCooldowns.filter { it.value > 0 }
        if (activeCooldowns.isNotEmpty()) {
            effects.add("⏳ COOLDOWNS:")
            activeCooldowns.forEach { (ability, rounds) ->
                effects.add("  ${ability.displayName}: ${rounds}R")
            }
        }

        // Active Passive Abilities
        val passives = PaladinPassive.values().filter { it.isUnlockedAt(stats.level) }
        if (passives.isNotEmpty()) {
            effects.add("✨ PASSIVES:")
            passives.forEach { passive ->
                effects.add("  ${passive.displayName}: ${passive.getScaledDescription(stats.level)}")
            }
        }

        // Zeitdilatation Status
        if (GameState.zeitdilatationActive) {
            effects.add("⚡ ZEITDILATATION AKTIV!")
            effects.add("  5x Click | 2x Auto | 2x Gold/XP")
        }

        // Lay on Hands Pool
        if (passives.any { it == PaladinPassive.LAY_ON_HANDS }) {
            val remaining = loadout.layOnHandsPool - loadout.layOnHandsUsed
            effects.add("🙏 Lay on Hands: ${remaining}/${loadout.layOnHandsPool} HP")
        }

        // Cleansing Touch Uses
        if (passives.any { it == PaladinPassive.CLEANSING_TOUCH }) {
            val maxUses = 3 + (stats.level / 10)
            val remaining = maxUses - loadout.cleansingTouchUsed
            effects.add("✋ Cleansing Touch: $remaining/$maxUses uses")
        }

        // Display all effects or "Keine aktiven Effekte"
        statusEffectsView.text = if (effects.isEmpty()) {
            "Keine aktiven Effekte"
        } else {
            effects.joinToString("\n")
        }
    }

    private fun updateTurnIndicator(combat: CombatState) {
        if (combat.combatEnded) {
            turnIndicator.text = "⚔️ KAMPF BEENDET"
            turnIndicator.setTextColor(Color.YELLOW)
            return
        }

        val currentParticipant = combat.getCurrentParticipant()
        val isPlayerTurn = combat.isPlayerTurn

        turnIndicator.text = if (isPlayerTurn) {
            "✅ DEIN ZUG! (Runde ${combat.currentRound})"
        } else {
            "⏳ Gegner am Zug... (Runde ${combat.currentRound})"
        }

        turnIndicator.setTextColor(if (isPlayerTurn) Color.GREEN else Color.YELLOW)
    }

    private fun updateSkillButtons(combat: CombatState) {
        skillButtonsContainer.removeAllViews()

        val player = combat.playerParty.firstOrNull() ?: return
        val isPlayerTurn = combat.isPlayerTurn && !combat.combatEnded

        // Get equipped abilities from loadout
        val normalAbilities = player.loadout.getEquippedNormalAbilities()
        val ultimate = player.loadout.ultimateAbility

        // Add normal abilities
        normalAbilities.forEach { ability ->
            addSkillButton(ability, combat, isPlayerTurn)
        }

        // Add ultimate if available
        ultimate?.let { ability ->
            addSkillButton(ability, combat, isPlayerTurn)
        }
    }

    private fun addSkillButton(ability: PaladinAbility, combat: CombatState, isPlayerTurn: Boolean) {
        val player = combat.playerParty.firstOrNull() ?: return

        val button = Button(this)

        // Get ability info
        val damage = ability.getDamage(player.stats.level)
        val healing = ability.getHealing(player.stats.level)
        val cooldown = combat.abilityCooldowns[ability] ?: 0

        // Build button text
        val typeIcon = if (ability.type == AbilityType.COMBAT) "⚡" else "💙"
        val costText = if (ability.type == AbilityType.COMBAT) "CD:${ability.cost}" else "${ability.cost} Mana"

        val effectText = when {
            damage > 0 -> "$damage DMG"
            healing > 0 -> "+$healing HP"
            else -> "Buff"
        }

        button.text = "$typeIcon ${ability.displayName}\n$costText | $effectText"

        // Check if ability can be used
        val canUse = when {
            !isPlayerTurn -> false
            cooldown > 0 -> false
            ability.type == AbilityType.SPELL && player.stats.currentMana < ability.cost -> false
            else -> true
        }

        button.isEnabled = canUse

        if (cooldown > 0) {
            button.text = "${button.text}\n[COOLDOWN: $cooldown]"
        }

        button.setOnClickListener {
            executeAbility(ability)
        }

        skillButtonsContainer.addView(button)
    }

    private fun updateCombatLog(combat: CombatState) {
        val logText = combat.combatLog
            .takeLast(15)  // Show last 15 entries
            .joinToString("\n") { entry ->
                val prefix = if (entry.isImportant) "⚡" else "•"
                "$prefix ${entry.message}"
            }

        combatLogView.text = logText
    }

    private fun createHPBar(percent: Int, length: Int): String {
        val filled = (percent * length / 100).coerceIn(0, length)
        val empty = length - filled
        return "[" + "█".repeat(filled) + "░".repeat(empty) + "]"
    }

    private fun createManaBar(percent: Int, length: Int): String {
        val filled = (percent * length / 100).coerceIn(0, length)
        val empty = length - filled
        return "[" + "▓".repeat(filled) + "░".repeat(empty) + "]"
    }

    private fun useLayOnHands() {
        val combat = GameState.getActiveCombat() ?: return
        if (!combat.isPlayerTurn) return

        val player = combat.playerParty.firstOrNull() ?: return
        val remaining = player.loadout.layOnHandsPool - player.loadout.layOnHandsUsed

        if (remaining <= 0) {
            android.widget.Toast.makeText(this, "❌ Kein Lay on Hands mehr verfügbar!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Use all remaining healing
        if (GameState.useLayOnHands(remaining)) {
            updateUI()
        }
    }

    private fun useCleansingTouch() {
        val combat = GameState.getActiveCombat() ?: return
        if (!combat.isPlayerTurn) return

        if (GameState.useCleansingTouch()) {
            updateUI()
        }
    }

    private fun showCombatResult() {
        val combat = GameState.getActiveCombat() ?: return
        val result = combat.combatResult ?: return

        val builder = AlertDialog.Builder(this)

        if (result.victory) {
            builder.setTitle("🎉 SIEG!")

            val messageBuilder = StringBuilder()
            messageBuilder.append("Glückwunsch! Du hast gewonnen!\n\n")
            messageBuilder.append("⚔️ Runden: ${result.roundsLasted}\n")

            if (result.xpGained > 0) {
                messageBuilder.append("✨ XP: +${result.xpGained}\n")
            }

            if (result.goldGained > 0) {
                messageBuilder.append("💰 Gold: +${result.goldGained}\n")
            }

            if (result.essenceGained > 0) {
                messageBuilder.append("💎 Divine Essence: +${result.essenceGained}\n")
            }

            result.lootDropped?.let { equipment ->
                messageBuilder.append("\n⚔️ EQUIPMENT GEFUNDEN!\n")
                messageBuilder.append("${equipment.rarity.displayName} ${equipment.slot.name}\n")
                messageBuilder.append("Tier ${equipment.tier} | ${equipment.set.displayName}\n")
            }

            builder.setMessage(messageBuilder.toString().trim())
        } else {
            builder.setTitle("💀 NIEDERLAGE")
            builder.setMessage("""
Du wurdest besiegt!

⚔️ Runden überlebt: ${result.roundsLasted}

(HP & Mana auf 50% wiederhergestellt)
            """.trimIndent())
        }

        builder.setPositiveButton("Zurück zur Taverne") { _, _ ->
            GameState.clearActiveCombat()
            finish()
        }

        builder.setCancelable(false)
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
