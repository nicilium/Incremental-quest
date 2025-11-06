package com.tuffgames.incrementalquest

import android.content.Context
import kotlin.math.pow

object GameState {
    private var _totalScore = 0.0
    var totalScore: Double
        get() = _totalScore
        private set(value) {
            _totalScore = value.coerceAtLeast(0.0) // Niemals negativ
        }

    private var _lifetimeScore = 0.0
    var lifetimeScore: Double
        get() = _lifetimeScore
        private set(value) {
            _lifetimeScore = value.coerceAtLeast(0.0) // Niemals negativ
        }

    var upgradesUnlocked = false
        private set

    var divineEssence = 0
        private set

    // Total Divine Essence ever earned (for permanent bonus)
    var totalDivineEssenceEarned = 0
        private set

    // How many prestige levels have been claimed
    private var prestigesClaimed = 0

    // Essence Power Level (unbounded, linear progression with powerspikes)
    var essencePowerLevel = 0
        private set

    // Permanent color upgrades with Divine Essence (NOT reset on prestige!)
    private val permanentColorUpgrades = mutableMapOf<CubeColor, Int>()

    var autoClickerActive = false
        private set

    var autoClickerSpeedLevel = 0
        private set

    // Dice progression: D4 (start) -> D6 -> D8 -> D10 -> D12 -> D20
    var d4Active = true  // Start with D4
        private set

    var d6Active = false
        private set

    var d8Active = false
        private set

    var d10Active = false
        private set

    var d12Active = false
        private set

    var d20Active = false
        private set

    // Tavern System (unlocked after D20 + payment)
    var tavernUnlocked = false
        private set

    private var tavernUnlockPaid = false

    // Quest System
    private var currentQuest: String? = null  // null = no active quest
    var questProgress = 0
        private set

    // Class System (unlocked after first quest)
    var selectedClass: PlayerClass? = null
        private set
    var classUnlocked = false
        private set

    // Extra Dice System (max 5)
    private val extraDice = mutableListOf<ExtraDice>()
    private val maxExtraDice = 5

    // Active Buffs from Extra Dice rolls
    private var activeCritBuff: CritBuff? = null
    private var activePassiveBuff: PassiveBuff? = null
    private var activeEssenceBuff: EssenceBuff? = null
    private var flatScoreBonusNextClick = 0.0

    // Zeit-Tracking für Offline-Klicks
    private var lastActiveTime = 0L

    // Buff-System
    var lastBuffOfferTime = 0L
        private set

    var availableBuffType: BuffType? = null  // Der aktuell verfügbare Buff
        private set

    var activeBuffType: BuffType? = null
        private set

    var buffEndTime = 0L
        private set

    var buffTimeMultiplier = 1  // 1 = normal, 2 = doubled by watching ad
        private set

    private var buffOffersWithoutEssence = 0  // Bad Luck Protection Counter

    // Basis-Punktwerte für jede Farbe
    private val baseColorPoints = mapOf(
        CubeColor.RED to 1,
        CubeColor.GREEN to 2,
        CubeColor.BLUE to 3,
        CubeColor.YELLOW to 4,
        CubeColor.MAGENTA to 5,
        CubeColor.CYAN to 6,
        CubeColor.ORANGE to 7,
        CubeColor.PINK to 8,
        CubeColor.PURPLE to 9,
        CubeColor.TURQUOISE to 10,
        CubeColor.LIME to 11,
        CubeColor.BROWN to 12,
        CubeColor.GOLD to 13,
        CubeColor.SILVER to 14,
        CubeColor.BRONZE to 15,
        CubeColor.NAVY to 16,
        CubeColor.MAROON to 17,
        CubeColor.OLIVE to 18,
        CubeColor.TEAL to 19,
        CubeColor.CORAL to 20
    )

    // Base cost for permanent color upgrades by dice tier (3x multiplier between tiers)
    private fun getColorTierBaseCost(color: CubeColor): Int {
        val basePoints = baseColorPoints[color] ?: 1
        return when (basePoints) {
            in 1..4 -> 5      // D4: 5 DE
            in 5..6 -> 15     // D6: 15 DE (3x)
            in 7..8 -> 45     // D8: 45 DE (3x)
            in 9..10 -> 135   // D10: 135 DE (3x)
            in 11..12 -> 405  // D12: 405 DE (3x)
            else -> 1215      // D20: 1215 DE (3x)
        }
    }

    // Maximum level for permanent color upgrades
    private val maxPermanentColorLevel = 10

    // Time tracking for passive points from permanent color upgrades
    private var lastPassivePointsUpdate = System.currentTimeMillis()

    // Upgrade-Level für jede Farbe (gesamt hinzugefügte Punkte)
    private val upgradeLevels = mutableMapOf(
        CubeColor.RED to 0,
        CubeColor.GREEN to 0,
        CubeColor.BLUE to 0,
        CubeColor.YELLOW to 0,
        CubeColor.MAGENTA to 0,
        CubeColor.CYAN to 0,
        CubeColor.ORANGE to 0,
        CubeColor.PINK to 0,
        CubeColor.PURPLE to 0,
        CubeColor.TURQUOISE to 0,
        CubeColor.LIME to 0,
        CubeColor.BROWN to 0,
        CubeColor.GOLD to 0,
        CubeColor.SILVER to 0,
        CubeColor.BRONZE to 0,
        CubeColor.NAVY to 0,
        CubeColor.MAROON to 0,
        CubeColor.OLIVE to 0,
        CubeColor.TEAL to 0,
        CubeColor.CORAL to 0
    )

    // Anzahl der Upgrades für jede Farbe (für Anzeige)
    private val upgradeCount = mutableMapOf(
        CubeColor.RED to 0,
        CubeColor.GREEN to 0,
        CubeColor.BLUE to 0,
        CubeColor.YELLOW to 0,
        CubeColor.MAGENTA to 0,
        CubeColor.CYAN to 0,
        CubeColor.ORANGE to 0,
        CubeColor.PINK to 0,
        CubeColor.PURPLE to 0,
        CubeColor.TURQUOISE to 0,
        CubeColor.LIME to 0,
        CubeColor.BROWN to 0,
        CubeColor.GOLD to 0,
        CubeColor.SILVER to 0,
        CubeColor.BRONZE to 0,
        CubeColor.NAVY to 0,
        CubeColor.MAROON to 0,
        CubeColor.OLIVE to 0,
        CubeColor.TEAL to 0,
        CubeColor.CORAL to 0
    )

    // Kosten-Multiplikator für jede Farbe (startet bei 1.0)
    private val costMultipliers = mutableMapOf(
        CubeColor.RED to 1.0,
        CubeColor.GREEN to 1.0,
        CubeColor.BLUE to 1.0,
        CubeColor.YELLOW to 1.0,
        CubeColor.MAGENTA to 1.0,
        CubeColor.CYAN to 1.0,
        CubeColor.ORANGE to 1.0,
        CubeColor.PINK to 1.0,
        CubeColor.PURPLE to 1.0,
        CubeColor.TURQUOISE to 1.0,
        CubeColor.LIME to 1.0,
        CubeColor.BROWN to 1.0,
        CubeColor.GOLD to 1.0,
        CubeColor.SILVER to 1.0,
        CubeColor.BRONZE to 1.0,
        CubeColor.NAVY to 1.0,
        CubeColor.MAROON to 1.0,
        CubeColor.OLIVE to 1.0,
        CubeColor.TEAL to 1.0,
        CubeColor.CORAL to 1.0
    )

    fun onColorClicked(color: CubeColor) {
        // Apply passive points from permanent color upgrades
        applyPassivePoints()

        // Update extra dice buffs first
        updateExtraDiceBuffs()

        // Roll all extra dice and trigger buffs
        rollExtraDice()

        val basePoints = baseColorPoints[color] ?: 0

        // Upgrade bonus: simply basePoints × count (Red: +1/level, Green: +2/level, etc.)
        val upgradeCount = upgradeCount[color] ?: 0
        val upgradeBonus = basePoints * upgradeCount

        // Essence Power Multiplier (ULTRA EXTREME POWERSPIKES!)
        val essencePowerMulti = getEssencePowerClickMultiplier()

        // Total points with Essence Power multiplier applied
        // Note: Permanent color upgrades now give passive points/sec instead of flat bonus
        var totalPoints = (basePoints + upgradeBonus).toDouble() * essencePowerMulti

        // Extra Dice: Apply flat score bonus (consumed)
        if (flatScoreBonusNextClick > 0) {
            totalPoints += flatScoreBonusNextClick
            flatScoreBonusNextClick = 0.0
        }

        // Extra Dice: Apply crit buff
        activeCritBuff?.let { crit ->
            if (System.currentTimeMillis() <= crit.endTime) {
                totalPoints *= crit.multiplier
            }
        }

        // Buff: Double points
        if (activeBuffType == BuffType.DOUBLE_POINTS && isBuffActive()) {
            totalPoints *= 2.0
        }

        totalScore += totalPoints
        lifetimeScore += totalPoints

        // Unlock upgrades at 200 points
        if (totalScore >= 200 && !upgradesUnlocked) {
            upgradesUnlocked = true
        }

        // Abgelaufene Buffs clearen
        clearExpiredBuff()
    }

    fun getUpgradeCost(color: CubeColor): Double {
        val basePoints = baseColorPoints[color] ?: 1
        val baseCost = basePoints * 10.0  // Basis: basePoints × 10
        val multiplier = costMultipliers[color] ?: 1.0
        return baseCost * multiplier
    }

    fun canAffordUpgrade(color: CubeColor): Boolean {
        return totalScore >= getUpgradeCost(color)
    }

    fun buyUpgrade(color: CubeColor): Boolean {
        val cost = getUpgradeCost(color)
        if (cost <= 0 || totalScore < cost) return false

        totalScore -= cost

        // Erhöhe Upgrade-Anzahl um 1 (für skalierende Bonus-Berechnung)
        upgradeCount[color] = (upgradeCount[color] ?: 0) + 1

        // upgradeLevels wird nicht mehr verwendet (nur für Backwards-Compatibility im Save-System)
        val basePoints = baseColorPoints[color] ?: 1
        upgradeLevels[color] = (upgradeLevels[color] ?: 0) + basePoints

        // Neue Kosten-Steigerung: multiplier = multiplier + 1 + (1.000001²)
        val currentMultiplier = costMultipliers[color] ?: 1.0
        val increment = 1.0 + (1.000001 * 1.000001)  // = 1.000002000001
        costMultipliers[color] = currentMultiplier + increment

        return true
    }

    fun getUpgradeLevel(color: CubeColor): Int {
        return upgradeCount[color] ?: 0  // Gibt jetzt die Anzahl der Upgrades zurück, nicht die Bonuspunkte
    }

    fun getCurrentPoints(color: CubeColor): Int {
        val basePoints = baseColorPoints[color] ?: 0

        // Upgrade bonus: simply basePoints × count (Red: +1/level, Green: +2/level, etc.)
        val count = upgradeCount[color] ?: 0
        val upgradeBonus = basePoints * count

        // Essence Power Multiplier (ULTRA EXTREME POWERSPIKES!)
        val essencePowerMulti = getEssencePowerClickMultiplier()

        // Note: Permanent color upgrades now give passive points/sec instead of flat bonus
        return ((basePoints + upgradeBonus).toDouble() * essencePowerMulti).toInt()
    }

    // Average click value across all colors
    fun getAverageClickValue(): Double {
        val allColors = getAvailableColors()
        val sum = allColors.sumOf { getCurrentPoints(it) }
        return sum / allColors.size.toDouble()
    }

    // Returns all available colors (D4, D6, D8, D10, D12 or D20)
    fun getAvailableColors(): List<CubeColor> {
        return when {
            d20Active -> listOf(
                CubeColor.RED, CubeColor.GREEN, CubeColor.BLUE,
                CubeColor.YELLOW, CubeColor.MAGENTA, CubeColor.CYAN,
                CubeColor.ORANGE, CubeColor.PINK, CubeColor.PURPLE, CubeColor.TURQUOISE,
                CubeColor.LIME, CubeColor.BROWN,
                CubeColor.GOLD, CubeColor.SILVER, CubeColor.BRONZE,
                CubeColor.NAVY, CubeColor.MAROON, CubeColor.OLIVE, CubeColor.TEAL, CubeColor.CORAL
            )
            d12Active -> listOf(
                CubeColor.RED, CubeColor.GREEN, CubeColor.BLUE,
                CubeColor.YELLOW, CubeColor.MAGENTA, CubeColor.CYAN,
                CubeColor.ORANGE, CubeColor.PINK, CubeColor.PURPLE, CubeColor.TURQUOISE,
                CubeColor.LIME, CubeColor.BROWN
            )
            d10Active -> listOf(
                CubeColor.RED, CubeColor.GREEN, CubeColor.BLUE,
                CubeColor.YELLOW, CubeColor.MAGENTA, CubeColor.CYAN,
                CubeColor.ORANGE, CubeColor.PINK, CubeColor.PURPLE, CubeColor.TURQUOISE
            )
            d8Active -> listOf(
                CubeColor.RED, CubeColor.GREEN, CubeColor.BLUE,
                CubeColor.YELLOW, CubeColor.MAGENTA, CubeColor.CYAN,
                CubeColor.ORANGE, CubeColor.PINK
            )
            d6Active -> listOf(
                CubeColor.RED, CubeColor.GREEN, CubeColor.BLUE,
                CubeColor.YELLOW, CubeColor.MAGENTA, CubeColor.CYAN
            )
            else -> listOf(  // D4 (default start)
                CubeColor.RED, CubeColor.GREEN, CubeColor.BLUE, CubeColor.YELLOW
            )
        }
    }

    // Prestige Upgrade: Essence Power (unbounded level-based system)
    // Formula: level^1.3 scaling with powerspikes at 10, 25, 50, 100
    // Powerspike levels cost more but grant massive bonuses

    // Calculate essence bonus based on level
    // Get Essence Power multiplier for clicks (ULTRA EXTREME POWERSPIKES!)
    private fun getEssencePowerClickMultiplier(): Double {
        if (essencePowerLevel == 0) return 1.0

        // Base: +0.5% per level (gedämpft zwischen Powerspikes)
        var multiplier = 1.0 + (essencePowerLevel * 0.005)

        // MEGA POWERSPIKE BONI (ULTRA REWARDING!)
        // These stack cumulatively as you progress
        val powerspikeBonus = when {
            essencePowerLevel >= 100 -> 0.60 + 0.80 + 0.90 + 0.20  // All spikes: +250% total = ×4.20!
            essencePowerLevel >= 50 -> 0.60 + 0.80 + 0.90          // First 3 spikes: +230% = ×3.70!
            essencePowerLevel >= 25 -> 0.60 + 0.80                 // First 2 spikes: +140% = ×2.53!
            essencePowerLevel >= 10 -> 0.60                        // First spike: +60% = ×1.65!
            else -> 0.0
        }
        multiplier += powerspikeBonus

        return multiplier
    }

    // Legacy function - kept for backwards compatibility but no longer adds flat bonus
    private fun calculateEssenceBonus(): Double {
        return 0.0  // Essence Power now works as multiplier, not flat bonus
    }

    // Calculate cost for next essence power level
    fun getEssencePowerCost(): Int {
        val nextLevel = essencePowerLevel + 1

        // Powerspike levels (10, 25, 50, 100) are much more expensive
        val isPowerspike = nextLevel == 10 || nextLevel == 25 || nextLevel == 50 || nextLevel == 100

        return if (isPowerspike) {
            // Powerspikes: level^2.2 (expensive!)
            kotlin.math.ceil(nextLevel.toDouble().pow(2.2)).toInt()
        } else {
            // Normal levels: level^1.6
            kotlin.math.ceil(nextLevel.toDouble().pow(1.6)).toInt()
        }
    }

    fun canAffordEssencePower(): Boolean {
        val cost = getEssencePowerCost()
        return divineEssence >= cost
    }

    fun buyEssencePower(): Boolean {
        if (!canAffordEssencePower()) return false

        val cost = getEssencePowerCost()
        divineEssence -= cost
        essencePowerLevel++

        return true
    }

    fun getEssencePowerMultiplier(): Double {
        // Returns the click multiplier from Essence Power (for UI display)
        return getEssencePowerClickMultiplier()
    }

    fun getNextPowerspikeLevel(): Int {
        return when {
            essencePowerLevel < 10 -> 10
            essencePowerLevel < 25 -> 25
            essencePowerLevel < 50 -> 50
            essencePowerLevel < 100 -> 100
            else -> -1  // No more powerspikes
        }
    }

    // Permanent color upgrades with Divine Essence
    fun getPermanentColorUpgradeLevel(color: CubeColor): Int {
        return permanentColorUpgrades[color] ?: 0
    }

    fun getPermanentColorUpgradeCost(color: CubeColor): Int {
        val currentLevel = getPermanentColorUpgradeLevel(color)

        // Max level is 10
        if (currentLevel >= maxPermanentColorLevel) {
            return Int.MAX_VALUE
        }

        // Cost = baseCost × (nextLevel)
        // Example: RED Level 1 = 5 × 1 = 5 DE, Level 2 = 5 × 2 = 10 DE
        val baseCost = getColorTierBaseCost(color)
        val nextLevel = currentLevel + 1
        return baseCost * nextLevel
    }

    fun canAffordPermanentColorUpgrade(color: CubeColor): Boolean {
        return divineEssence >= getPermanentColorUpgradeCost(color)
    }

    fun buyPermanentColorUpgrade(color: CubeColor): Boolean {
        if (!canAffordPermanentColorUpgrade(color)) return false

        val cost = getPermanentColorUpgradeCost(color)
        divineEssence -= cost

        val currentLevel = permanentColorUpgrades[color] ?: 0
        permanentColorUpgrades[color] = currentLevel + 1

        return true
    }

    // Calculate passive points per second for a specific color
    fun getPassivePointsPerSecond(color: CubeColor): Double {
        val level = getPermanentColorUpgradeLevel(color)
        if (level == 0) return 0.0

        val basePoints = baseColorPoints[color] ?: 1
        // Level 10 = 100% of basePoints/sec, Level 1 = 10%, etc.
        return (level.toDouble() / maxPermanentColorLevel) * basePoints
    }

    // Calculate total passive points per second from all colors
    fun getTotalPassivePointsPerSecond(): Double {
        var total = 0.0
        CubeColor.values().forEach { color ->
            total += getPassivePointsPerSecond(color)
        }
        return total
    }

    // Apply passive points since last update
    private fun applyPassivePoints() {
        val currentTime = System.currentTimeMillis()
        val timeDelta = (currentTime - lastPassivePointsUpdate) / 1000.0  // Convert to seconds

        if (timeDelta > 0) {
            val passivePointsPerSec = getTotalPassivePointsPerSecond()
            val pointsToAdd = passivePointsPerSec * timeDelta

            if (pointsToAdd > 0) {
                totalScore += pointsToAdd
                lifetimeScore += pointsToAdd
            }

            lastPassivePointsUpdate = currentTime
        }
    }

    // Auto Clicker Upgrade
    fun getAutoClickerCost(): Int {
        return 1
    }

    fun canAffordAutoClicker(): Boolean {
        return !autoClickerActive && totalScore >= getAutoClickerCost()
    }

    fun buyAutoClicker(): Boolean {
        if (!canAffordAutoClicker()) return false

        totalScore -= getAutoClickerCost()
        autoClickerActive = true

        return true
    }

    // Auto Clicker Speed Upgrade
    fun getAutoClickerSpeedCost(): Int {
        if (autoClickerSpeedLevel >= 100) return -1  // Max Level
        // Cost doubles: 1, 2, 4, 8, 16, ...
        return 1 shl autoClickerSpeedLevel  // Bit-shift: 2^level
    }

    fun canAffordAutoClickerSpeed(): Boolean {
        if (!autoClickerActive) return false  // Must have auto clicker first
        if (autoClickerSpeedLevel >= 100) return false
        val cost = getAutoClickerSpeedCost()
        return divineEssence >= cost
    }

    fun buyAutoClickerSpeed(): Boolean {
        if (!canAffordAutoClickerSpeed()) return false

        val cost = getAutoClickerSpeedCost()
        divineEssence -= cost
        autoClickerSpeedLevel++

        return true
    }

    // D6 Upgrade - Cost: 1 Divine Essence
    fun canAffordD6(): Boolean {
        return !d6Active && divineEssence >= 1
    }

    fun buyD6(): Boolean {
        if (!canAffordD6()) return false

        divineEssence -= 1
        d6Active = true

        return true
    }

    // D8 Upgrade - Cost: 5 Divine Essence (5x increase)
    fun canAffordD8(): Boolean {
        return d6Active && !d8Active && divineEssence >= 5
    }

    fun buyD8(): Boolean {
        if (!canAffordD8()) return false

        divineEssence -= 5
        d8Active = true

        return true
    }

    // D10 Upgrade - Cost: 25 Divine Essence (5x increase)
    fun canAffordD10(): Boolean {
        return d8Active && !d10Active && divineEssence >= 25
    }

    fun buyD10(): Boolean {
        if (!canAffordD10()) return false

        divineEssence -= 25
        d10Active = true

        return true
    }

    // D12 Upgrade - Cost: 125 Divine Essence (5x increase)
    fun canAffordD12(): Boolean {
        return d10Active && !d12Active && divineEssence >= 125
    }

    fun buyD12(): Boolean {
        if (!canAffordD12()) return false

        divineEssence -= 125
        d12Active = true

        return true
    }

    // D20 Upgrade - Cost: 625 Divine Essence (5x increase)
    fun canAffordD20(): Boolean {
        return d12Active && !d20Active && divineEssence >= 625
    }

    fun buyD20(): Boolean {
        if (!canAffordD20()) return false

        divineEssence -= 625
        d20Active = true

        return true
    }

    // ========== Tavern System ==========

    // Check if tavern can be unlocked (D20 must be active)
    fun canUnlockTavern(): Boolean {
        return d20Active && !tavernUnlockPaid
    }

    // Get cost for unlocking tavern
    fun getTavernUnlockCost(): Pair<Int, Double> {
        // Returns (divineEssence, score)
        return Pair(625, 1_200_000.0)
    }

    // Check if player can afford tavern unlock
    fun canAffordTavernUnlock(): Boolean {
        if (!canUnlockTavern()) return false
        val (deCost, scoreCost) = getTavernUnlockCost()
        return divineEssence >= deCost && totalScore >= scoreCost
    }

    // Unlock the tavern
    fun unlockTavern(): Boolean {
        if (!canAffordTavernUnlock()) return false

        val (deCost, scoreCost) = getTavernUnlockCost()
        divineEssence -= deCost
        totalScore -= scoreCost
        tavernUnlockPaid = true
        tavernUnlocked = true

        // Start first quest: Get second D20
        currentQuest = "SECOND_D20"
        questProgress = 0

        return true
    }

    // Quest functions
    fun getCurrentQuest(): String? = currentQuest

    fun isQuestActive(questId: String): Boolean = currentQuest == questId

    fun updateQuestProgress(questId: String, progress: Int) {
        if (currentQuest == questId) {
            questProgress = progress
        }
    }

    fun completeQuest(questId: String) {
        if (currentQuest == questId) {
            when (questId) {
                "SECOND_D20" -> {
                    // Quest completed: Second D20 bought
                    classUnlocked = true
                    currentQuest = null
                    questProgress = 0
                }
            }
        }
    }

    // Class system
    fun selectClass(playerClass: PlayerClass) {
        if (classUnlocked) {
            selectedClass = playerClass
        }
    }

    // ========== Extra Dice System ==========

    // Get current number of extra dice
    fun getExtraDiceCount(): Int = extraDice.size

    // Get cost for next extra die
    fun getNextExtraDieCost(): Int {
        if (extraDice.size >= maxExtraDice) return Int.MAX_VALUE

        if (extraDice.isEmpty()) return 10  // First die costs 10 DE

        // Each die costs +50% more than the previous
        var cost = 10.0
        for (i in 1 until extraDice.size + 1) {
            cost *= 1.5
        }
        return kotlin.math.ceil(cost).toInt()
    }

    fun canAffordNextExtraDice(): Boolean {
        return extraDice.size < maxExtraDice && divineEssence >= getNextExtraDieCost()
    }

    fun buyExtraDice(): Boolean {
        if (!canAffordNextExtraDice()) return false

        val cost = getNextExtraDieCost()
        divineEssence -= cost

        extraDice.add(ExtraDice(index = extraDice.size))

        // Check if quest should be completed
        if (extraDice.size >= 2 && currentQuest == "SECOND_D20") {
            completeQuest("SECOND_D20")
        }

        return true
    }

    // Get all extra dice (read-only)
    fun getExtraDice(): List<ExtraDice> = extraDice.toList()

    // Get specific extra die
    fun getExtraDie(index: Int): ExtraDice? {
        return extraDice.getOrNull(index)
    }

    // Upgrade specific extra die
    fun canUpgradeExtraDice(index: Int): Boolean {
        val die = extraDice.getOrNull(index) ?: return false
        if (!die.canUpgrade()) return false
        return divineEssence >= die.getUpgradeCost()
    }

    fun upgradeExtraDice(index: Int): Boolean {
        val die = extraDice.getOrNull(index) ?: return false
        if (!canUpgradeExtraDice(index)) return false

        val cost = die.getUpgradeCost()
        divineEssence -= cost
        die.upgrade()
        return true
    }

    // Buy/Upgrade buff slot
    fun canUpgradeBuffSlot(diceIndex: Int, slotType: BuffSlotType): Boolean {
        val die = extraDice.getOrNull(diceIndex) ?: return false
        val slot = die.buffSlots.find { it.type == slotType } ?: return false

        // Must be unlocked
        if (!slot.isUnlocked(die.diceLevel)) return false

        return divineEssence >= slot.getUpgradeCost()
    }

    fun upgradeBuffSlot(diceIndex: Int, slotType: BuffSlotType): Boolean {
        val die = extraDice.getOrNull(diceIndex) ?: return false
        val slot = die.buffSlots.find { it.type == slotType } ?: return false

        if (!canUpgradeBuffSlot(diceIndex, slotType)) return false

        val cost = slot.getUpgradeCost()
        divineEssence -= cost
        slot.level++
        return true
    }

    // Roll all extra dice and trigger buffs
    private fun rollExtraDice() {
        val currentTime = System.currentTimeMillis()

        for (die in extraDice) {
            val roll = (1..die.diceLevel.faceCount).random()

            // Check each buff slot
            for (slot in die.buffSlots) {
                // Skip if not purchased or not unlocked
                if (!slot.isPurchased() || !slot.isUnlocked(die.diceLevel)) continue

                // Check if this roll triggers this slot
                if (!slot.type.isTriggeredBy(roll)) continue

                // Trigger the buff
                when (slot.type) {
                    BuffSlotType.FLAT_SCORE -> {
                        // Add flat bonus to next click
                        val bonus = 10.0 * slot.level  // 10 points per level
                        flatScoreBonusNextClick += bonus
                    }

                    BuffSlotType.CRIT_CHANCE -> {
                        // 5 second crit buff
                        val multiplier = 1.0 + (0.5 * slot.level)  // +50% per level
                        activeCritBuff = CritBuff(multiplier, currentTime + 5000)
                    }

                    BuffSlotType.PASSIVE_POINTS -> {
                        // 10 second passive points/sec
                        val pointsPerSec = 5.0 * slot.level  // 5 points/sec per level
                        activePassiveBuff = PassiveBuff(pointsPerSec, currentTime + 10000)
                    }

                    BuffSlotType.ESSENCE_MULT -> {
                        // 10 second essence multiplier
                        val multiplier = 1.0 + (0.25 * slot.level)  // +25% per level
                        activeEssenceBuff = EssenceBuff(multiplier, currentTime + 10000)
                    }

                    BuffSlotType.BUFF_DURATION -> {
                        // Extend all active buffs by 1s per level
                        val extension = 1000L * slot.level
                        activeCritBuff?.let {
                            activeCritBuff = it.copy(endTime = it.endTime + extension)
                        }
                        activePassiveBuff?.let {
                            activePassiveBuff = it.copy(endTime = it.endTime + extension)
                        }
                        activeEssenceBuff?.let {
                            activeEssenceBuff = it.copy(endTime = it.endTime + extension)
                        }
                    }

                    BuffSlotType.MEGA_CRIT -> {
                        // Instant burst
                        if (roll == 20) {
                            // OMNI BOOM - huge burst
                            val burst = 1000.0 * slot.level
                            totalScore += burst
                            lifetimeScore += burst
                        } else {
                            // Mini burst (13-19)
                            val burst = 100.0 * slot.level
                            totalScore += burst
                            lifetimeScore += burst
                        }
                    }
                }
            }
        }
    }

    // Update buffs and apply passive effects
    private fun updateExtraDiceBuffs() {
        val currentTime = System.currentTimeMillis()

        // Clear expired buffs
        if (activeCritBuff != null && currentTime > activeCritBuff!!.endTime) {
            activeCritBuff = null
        }
        if (activePassiveBuff != null && currentTime > activePassiveBuff!!.endTime) {
            activePassiveBuff = null
        }
        if (activeEssenceBuff != null && currentTime > activeEssenceBuff!!.endTime) {
            activeEssenceBuff = null
        }

        // Apply passive points if active
        activePassiveBuff?.let { buff ->
            if (currentTime <= buff.endTime) {
                // Add points based on time since last update
                // This is simplified - in production you'd track last update time
                val pointsToAdd = buff.pointsPerSecond / 60.0  // Approximate per frame
                totalScore += pointsToAdd
                lifetimeScore += pointsToAdd
            }
        }
    }

    // Get active buff info for UI
    fun getActiveCritBuff(): CritBuff? = activeCritBuff
    fun getActivePassiveBuff(): PassiveBuff? = activePassiveBuff
    fun getActiveEssenceBuff(): EssenceBuff? = activeEssenceBuff
    fun getFlatScoreBonus(): Double = flatScoreBonusNextClick

    // Calculates the interval in milliseconds based on speed level
    fun getAutoClickerInterval(): Long {
        if (!autoClickerActive) return 1000L
        // Formula: 1000ms - (level * 10ms)
        // Level 0: 1000ms, Level 1: 990ms, Level 2: 980ms, Level 100: 0ms
        // Minimum of 10ms to avoid division by zero
        var interval = 1000L - (autoClickerSpeedLevel * 10L)
        interval = interval.coerceAtLeast(10L)

        // Buff: Schnellerer Autoklicker (halbierte Zeit)
        if (activeBuffType == BuffType.FASTER_AUTOCLICK && isBuffActive()) {
            interval /= 2
        }

        return interval
    }

    // Markiert die aktuelle Zeit als letzten aktiven Zeitpunkt
    fun markActiveTime() {
        lastActiveTime = System.currentTimeMillis()
    }

    // Berechnet und fügt Offline-Klicks hinzu
    fun processOfflineClicks(): Int {
        if (!autoClickerActive) return 0
        if (lastActiveTime == 0L) {
            lastActiveTime = System.currentTimeMillis()
            return 0
        }

        val currentTime = System.currentTimeMillis()
        val timePassed = currentTime - lastActiveTime
        val interval = getAutoClickerInterval()
        val clicksToAdd = (timePassed / interval).toInt()

        if (clicksToAdd > 0) {
            // Durchschnittlichen Klickwert über alle Farben berechnen (mit Dezimalwerten)
            val avgClickValue = getAverageClickValue()
            val pointsToAdd = clicksToAdd * avgClickValue

            totalScore += pointsToAdd
            lifetimeScore += pointsToAdd

            // Upgrades freischalten bei 200 Punkten
            if (totalScore >= 200 && !upgradesUnlocked) {
                upgradesUnlocked = true
            }

            // Zeit nur um die verarbeiteten Klicks vorrücken, Rest bleibt übrig
            lastActiveTime += clicksToAdd * interval
        }

        return clicksToAdd
    }

    fun canPrestige(): Boolean {
        return lifetimeScore >= 1000
    }

    // Wie viele Lackdosen kann man beanspruchen?
    fun getAvailablePrestigeRewards(): Int {
        val totalPrestigesEarned = (lifetimeScore / 1000).toInt()
        val unclaimed = totalPrestigesEarned - prestigesClaimed
        return unclaimed
    }

    // How many points until next prestige?
    fun getPointsToNextPrestige(): Double {
        val nextMilestone = (prestigesClaimed + 1) * 1000.0
        val remaining = nextMilestone - lifetimeScore
        return if (remaining > 0) remaining else 0.0
    }

    fun performPrestige(): Boolean {
        var available = getAvailablePrestigeRewards()
        if (available <= 0) return false

        // Apply Essence Multiplier buff if active
        activeEssenceBuff?.let { buff ->
            if (System.currentTimeMillis() <= buff.endTime) {
                available = (available * buff.multiplier).toInt()
            }
        }

        // Add Divine Essence for all unclaimed levels
        divineEssence += available
        totalDivineEssenceEarned += available
        prestigesClaimed += available

        // Reset everything (except divineEssence, totalDivineEssenceEarned, lifetimeScore and prestigesClaimed)
        totalScore = 0.0
        upgradesUnlocked = false
        upgradeLevels.keys.forEach { upgradeLevels[it] = 0 }
        upgradeCount.keys.forEach { upgradeCount[it] = 0 }
        costMultipliers.keys.forEach { costMultipliers[it] = 1.0 }

        return true
    }

    fun reset() {
        totalScore = 0.0
        lifetimeScore = 0.0
        upgradesUnlocked = false
        divineEssence = 0
        totalDivineEssenceEarned = 0
        prestigesClaimed = 0
        essencePowerLevel = 0
        autoClickerActive = false
        autoClickerSpeedLevel = 0
        d4Active = true  // Reset to D4 start
        d6Active = false
        d8Active = false
        d10Active = false
        d12Active = false
        d20Active = false
        upgradeLevels.keys.forEach { upgradeLevels[it] = 0 }
        upgradeCount.keys.forEach { upgradeCount[it] = 0 }
        costMultipliers.keys.forEach { costMultipliers[it] = 1.0 }

        // Clear permanent color upgrades
        permanentColorUpgrades.clear()

        // Reset buff system
        lastBuffOfferTime = 0L
        availableBuffType = null
        activeBuffType = null
        buffEndTime = 0L
        buffTimeMultiplier = 1
        buffOffersWithoutEssence = 0

        // Reset time tracking
        lastActiveTime = System.currentTimeMillis()
    }

    // Save-System
    private const val PREFS_NAME = "CubeClickerSaveData"

    fun clearSaveData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun saveState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Basic values (save as Float, SharedPreferences doesn't support Double)
        editor.putFloat("totalScore", totalScore.toFloat())
        editor.putFloat("lifetimeScore", lifetimeScore.toFloat())
        editor.putBoolean("upgradesUnlocked", upgradesUnlocked)
        editor.putInt("paintCans", divineEssence)  // Keep old key for compatibility
        editor.putInt("totalPaintCansEarned", totalDivineEssenceEarned)  // Keep old key for compatibility
        editor.putInt("prestigesClaimed", prestigesClaimed)

        // Essence Power Level (new system)
        editor.putInt("essencePowerLevel", essencePowerLevel)

        editor.putBoolean("autoClickerActive", autoClickerActive)
        editor.putInt("autoClickerSpeedLevel", autoClickerSpeedLevel)
        editor.putBoolean("d4Active", d4Active)
        editor.putBoolean("d6Active", d6Active)
        editor.putBoolean("d8Active", d8Active)
        editor.putBoolean("d10Active", d10Active)
        editor.putBoolean("d12Active", d12Active)
        editor.putBoolean("d20Active", d20Active)

        // Time tracking
        editor.putLong("lastActiveTime", lastActiveTime)

        // Buff system
        editor.putLong("lastBuffOfferTime", lastBuffOfferTime)
        editor.putLong("buffEndTime", buffEndTime)
        editor.putInt("buffTimeMultiplier", buffTimeMultiplier)
        editor.putString("activeBuffType", activeBuffType?.name)
        editor.putString("availableBuffType", availableBuffType?.name)
        editor.putInt("buffOffersWithoutPaintCan", buffOffersWithoutEssence)  // Keep old key for compatibility

        // Upgrade-Levels für jede Farbe (Bonuspunkte)
        CubeColor.values().forEach { color ->
            val level = upgradeLevels[color] ?: 0
            editor.putInt("upgradeLevel_${color.name}", level)
        }

        // Upgrade-Anzahl für jede Farbe
        CubeColor.values().forEach { color ->
            val count = upgradeCount[color] ?: 0
            editor.putInt("upgradeCount_${color.name}", count)
        }

        // Kosten-Multiplikatoren für jede Farbe
        CubeColor.values().forEach { color ->
            val multiplier = costMultipliers[color] ?: 1.01
            editor.putFloat("costMultiplier_${color.name}", multiplier.toFloat())
        }

        // Permanente Farb-Upgrades (bleiben beim Prestige!)
        CubeColor.values().forEach { color ->
            val level = permanentColorUpgrades[color] ?: 0
            editor.putInt("permanentUpgrade_${color.name}", level)
        }

        // Extra Dice System
        editor.putInt("extraDiceCount", extraDice.size)
        extraDice.forEachIndexed { index, die ->
            editor.putString("extraDice_${index}_level", die.diceLevel.name)
            die.buffSlots.forEach { slot ->
                editor.putInt("extraDice_${index}_slot_${slot.type.name}", slot.level)
            }
        }

        // Tavern System
        editor.putBoolean("tavernUnlocked", tavernUnlocked)
        editor.putBoolean("tavernUnlockPaid", tavernUnlockPaid)
        editor.putString("currentQuest", currentQuest)
        editor.putInt("questProgress", questProgress)
        editor.putString("selectedClass", selectedClass?.name)
        editor.putBoolean("classUnlocked", classUnlocked)

        editor.commit()  // Synchrones Speichern statt apply() für sofortige Persistenz
    }

    fun loadState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Basis-Werte (mit Rückwärtskompatibilität für Int -> Float Migration)
        totalScore = try {
            prefs.getFloat("totalScore", 0f).toDouble()
        } catch (e: ClassCastException) {
            // Altes Int-Format migrieren
            prefs.getInt("totalScore", 0).toDouble()
        }.coerceAtLeast(0.0)

        lifetimeScore = try {
            prefs.getFloat("lifetimeScore", 0f).toDouble()
        } catch (e: ClassCastException) {
            // Migrate old Int format
            prefs.getInt("lifetimeScore", 0).toDouble()
        }.coerceAtLeast(0.0)
        upgradesUnlocked = prefs.getBoolean("upgradesUnlocked", false)

        divineEssence = try {
            prefs.getInt("paintCans", 0)  // Keep old key for compatibility
        } catch (e: ClassCastException) {
            prefs.getFloat("paintCans", 0f).toInt()
        }

        totalDivineEssenceEarned = try {
            prefs.getInt("totalPaintCansEarned", 0)  // Keep old key for compatibility
        } catch (e: ClassCastException) {
            prefs.getFloat("totalPaintCansEarned", 0f).toInt()
        }

        prestigesClaimed = try {
            prefs.getInt("prestigesClaimed", 0)
        } catch (e: ClassCastException) {
            prefs.getFloat("prestigesClaimed", 0f).toInt()
        }

        // Essence Power Level (new system)
        essencePowerLevel = try {
            prefs.getInt("essencePowerLevel", 0)
        } catch (e: ClassCastException) {
            0
        }

        // Migration: Convert old tier system to new level system
        if (essencePowerLevel == 0 && prefs.contains("paintCanBonusUpgrade_0")) {
            // Old save detected - count how many tiers were purchased
            var oldTiersCount = 0
            for (i in 0 until 10) {
                if (prefs.getBoolean("paintCanBonusUpgrade_$i", false)) {
                    oldTiersCount++
                }
            }
            // Convert to new system: 1 old tier ≈ 3 new levels (generous migration)
            essencePowerLevel = oldTiersCount * 3
        }

        autoClickerActive = prefs.getBoolean("autoClickerActive", false)

        autoClickerSpeedLevel = try {
            prefs.getInt("autoClickerSpeedLevel", 0)
        } catch (e: ClassCastException) {
            prefs.getFloat("autoClickerSpeedLevel", 0f).toInt()
        }

        d4Active = prefs.getBoolean("d4Active", true)  // Default true for new saves
        d6Active = prefs.getBoolean("d6Active", false)
        d8Active = prefs.getBoolean("d8Active", false)
        d10Active = prefs.getBoolean("d10Active", false)
        d12Active = prefs.getBoolean("d12Active", false)
        d20Active = prefs.getBoolean("d20Active", false)

        // Migration: If old save has no D4/D6/D8 flags but has D10+, activate all previous dice
        if (!prefs.contains("d4Active") && !prefs.contains("d6Active") && !prefs.contains("d8Active")) {
            // Old save detected - migrate to new system
            if (d10Active || d12Active || d20Active) {
                // They had D10+, so give them all previous dice
                d4Active = true
                d6Active = true
                d8Active = true
            } else {
                // Very old save, probably just had D6 (cube)
                d4Active = false  // They started with D6
                d6Active = true   // Default to D6 for old saves
            }
        }

        // Time tracking
        lastActiveTime = prefs.getLong("lastActiveTime", 0L)

        // Upgrade-Levels für jede Farbe (Bonuspunkte)
        CubeColor.values().forEach { color ->
            val level = prefs.getInt("upgradeLevel_${color.name}", 0)
            upgradeLevels[color] = level
        }

        // Upgrade-Anzahl für jede Farbe
        CubeColor.values().forEach { color ->
            val count = prefs.getInt("upgradeCount_${color.name}", 0)
            upgradeCount[color] = count
        }

        // Kosten-Multiplikatoren für jede Farbe (mit Rückwärtskompatibilität)
        CubeColor.values().forEach { color ->
            val multiplier = try {
                prefs.getFloat("costMultiplier_${color.name}", 1.0f).toDouble()
            } catch (e: ClassCastException) {
                // Falls alte Daten vorhanden sind
                1.0
            }
            costMultipliers[color] = multiplier
        }

        // Permanent color upgrades (survives prestige!)
        CubeColor.values().forEach { color ->
            val level = prefs.getInt("permanentUpgrade_${color.name}", 0)
            permanentColorUpgrades[color] = level
        }

        // Buff system
        lastBuffOfferTime = prefs.getLong("lastBuffOfferTime", 0L)
        buffEndTime = prefs.getLong("buffEndTime", 0L)

        buffTimeMultiplier = try {
            prefs.getInt("buffTimeMultiplier", 1)
        } catch (e: ClassCastException) {
            prefs.getFloat("buffTimeMultiplier", 1f).toInt()
        }

        val buffTypeName = prefs.getString("activeBuffType", null)
        activeBuffType = buffTypeName?.let { BuffType.valueOf(it) }
        val availableBuffTypeName = prefs.getString("availableBuffType", null)
        availableBuffType = availableBuffTypeName?.let { BuffType.valueOf(it) }

        buffOffersWithoutEssence = try {
            prefs.getInt("buffOffersWithoutPaintCan", 0)  // Keep old key for compatibility
        } catch (e: ClassCastException) {
            prefs.getFloat("buffOffersWithoutPaintCan", 0f).toInt()
        }

        // Extra Dice System
        extraDice.clear()
        val extraDiceCount = prefs.getInt("extraDiceCount", 0)
        for (i in 0 until extraDiceCount) {
            val die = ExtraDice(index = i)

            // Load dice level
            val levelName = prefs.getString("extraDice_${i}_level", "D4")
            die.diceLevel = try {
                DiceLevel.valueOf(levelName ?: "D4")
            } catch (e: IllegalArgumentException) {
                DiceLevel.D4
            }

            // Load buff slots
            die.buffSlots.forEach { slot ->
                slot.level = prefs.getInt("extraDice_${i}_slot_${slot.type.name}", 0)
            }

            extraDice.add(die)
        }

        // Tavern System
        tavernUnlocked = prefs.getBoolean("tavernUnlocked", false)
        tavernUnlockPaid = prefs.getBoolean("tavernUnlockPaid", false)
        currentQuest = prefs.getString("currentQuest", null)
        questProgress = prefs.getInt("questProgress", 0)
        val classNameStr = prefs.getString("selectedClass", null)
        selectedClass = if (classNameStr != null) {
            try {
                PlayerClass.valueOf(classNameStr)
            } catch (e: IllegalArgumentException) {
                null
            }
        } else {
            null
        }
        classUnlocked = prefs.getBoolean("classUnlocked", false)

        // Reset passive points timer to prevent huge point gains on first load
        lastPassivePointsUpdate = System.currentTimeMillis()
    }

    // Buff system functions
    fun markBuffOfferTime() {
        lastBuffOfferTime = System.currentTimeMillis()
    }

    // Checks if enough time has passed to offer a new buff
    fun shouldOfferNewBuff(): Boolean {
        val timeSinceLastOffer = System.currentTimeMillis() - lastBuffOfferTime
        val minTime = 5 * 60 * 1000L  // 5 minutes
        val maxTime = 10 * 60 * 1000L // 10 minutes
        val requiredTime = minTime + ((maxTime - minTime) * 0.5).toLong() // Average: 7.5 min
        return timeSinceLastOffer >= requiredTime && availableBuffType == null
    }

    // Selects a random buff with weighting
    fun selectRandomBuff() {
        // Bad Luck Protection: After 10 offers without essence, guarantee essence
        if (buffOffersWithoutEssence >= 10) {
            availableBuffType = BuffType.FREE_PAINT_CAN
            buffOffersWithoutEssence = 0
            return
        }

        // Weighting:
        // DOUBLE_POINTS: 10
        // FASTER_AUTOCLICK: 10
        // RANDOM_UPGRADE: 1
        // FREE_PAINT_CAN: 0.5
        val weights = listOf(
            BuffType.DOUBLE_POINTS to 10.0,
            BuffType.FASTER_AUTOCLICK to 10.0,
            BuffType.RANDOM_UPGRADE to 1.0,
            BuffType.FREE_PAINT_CAN to 0.5
        )

        val totalWeight = weights.sumOf { it.second }
        val random = Math.random() * totalWeight

        var cumulative = 0.0
        for ((buffType, weight) in weights) {
            cumulative += weight
            if (random < cumulative) {
                availableBuffType = buffType
                if (buffType != BuffType.FREE_PAINT_CAN) {
                    buffOffersWithoutEssence++
                } else {
                    buffOffersWithoutEssence = 0
                }
                return
            }
        }

        // Fallback (should never happen)
        availableBuffType = BuffType.DOUBLE_POINTS
        buffOffersWithoutEssence++
    }

    // Checks if a buff is available to claim
    fun hasAvailableBuff(): Boolean {
        return availableBuffType != null
    }

    fun isBuffActive(): Boolean {
        return activeBuffType != null && System.currentTimeMillis() < buffEndTime
    }

    fun getRemainingBuffTime(): Long {
        if (!isBuffActive()) return 0
        return buffEndTime - System.currentTimeMillis()
    }

    fun claimAvailableBuff(watchedAd: Boolean = false) {
        val buffType = availableBuffType ?: return

        activeBuffType = buffType
        buffTimeMultiplier = if (watchedAd) 2 else 1

        val baseDuration = when (buffType) {
            BuffType.DOUBLE_POINTS, BuffType.FASTER_AUTOCLICK -> 2 * 60 * 1000L // 2 minutes
            else -> 0L
        }

        buffEndTime = System.currentTimeMillis() + (baseDuration * buffTimeMultiplier)

        // Instant buffs
        when (buffType) {
            BuffType.FREE_PAINT_CAN -> {
                divineEssence++
                totalDivineEssenceEarned++
            }
            BuffType.RANDOM_UPGRADE -> {
                // Random upgrade from available colors
                val availableColors = getAvailableColors()
                val randomColor = availableColors.random()
                val basePoints = baseColorPoints[randomColor] ?: 1

                // Increase upgrade count
                upgradeCount[randomColor] = (upgradeCount[randomColor] ?: 0) + 1

                // upgradeLevels für Backwards-Compatibility
                upgradeLevels[randomColor] = (upgradeLevels[randomColor] ?: 0) + basePoints

                // Kosten-Multiplikator erhöhen (wie bei normalem Upgrade)
                val currentMultiplier = costMultipliers[randomColor] ?: 1.01
                costMultipliers[randomColor] = currentMultiplier * 1.5
            }
            else -> {} // Zeit-basierte Buffs
        }

        // Verfügbaren Buff verbrauchen
        availableBuffType = null
        markBuffOfferTime()
    }

    fun clearExpiredBuff() {
        if (activeBuffType != null && !isBuffActive()) {
            activeBuffType = null
            buffEndTime = 0L
            buffTimeMultiplier = 1
        }
    }
}

enum class CubeColor {
    RED,    // Vorderseite
    GREEN,  // Rückseite
    BLUE,   // Links
    YELLOW, // Rechts
    MAGENTA,// Oben
    CYAN,   // Unten
    // D10 Farben
    ORANGE,
    PINK,
    PURPLE,
    TURQUOISE,
    // D12 Farben
    LIME,
    BROWN,
    // D20 Farben
    GOLD,
    SILVER,
    BRONZE,
    NAVY,
    MAROON,
    OLIVE,
    TEAL,
    CORAL
}

enum class BuffType {
    DOUBLE_POINTS,      // 2 Minuten doppelte Punkte
    FASTER_AUTOCLICK,   // 2 Minuten schnellerer Autoklicker
    FREE_PAINT_CAN,     // 1 kostenlose Lackdose
    RANDOM_UPGRADE      // 1 zufälliges Farb-Upgrade
}

// Extra Dice System Data Classes
enum class DiceLevel(val faceCount: Int, val upgradeCost: Int) {
    D4(4, 0),      // Start level
    D6(6, 1),
    D8(8, 4),
    D10(10, 20),
    D12(12, 100),
    D20(20, 500);

    fun next(): DiceLevel? = when(this) {
        D4 -> D6
        D6 -> D8
        D8 -> D10
        D10 -> D12
        D12 -> D20
        D20 -> null
    }
}

enum class BuffSlotType(val slotIndex: Int, val triggerRange: IntRange, val unlockedAt: DiceLevel) {
    FLAT_SCORE(0, 1..4, DiceLevel.D4),          // Next click bonus
    CRIT_CHANCE(1, 5..6, DiceLevel.D6),          // 5s crit buff
    PASSIVE_POINTS(2, 7..8, DiceLevel.D8),       // 10s passive/sec
    ESSENCE_MULT(3, 9..10, DiceLevel.D10),       // 10s essence multiplier
    BUFF_DURATION(4, 11..12, DiceLevel.D12),     // Extends active buffs
    MEGA_CRIT(5, 13..20, DiceLevel.D20);         // Burst events (13-19 mini, 20 omni)

    fun isTriggeredBy(roll: Int): Boolean = roll in triggerRange
}

data class BuffSlot(
    val type: BuffSlotType,
    var level: Int = 0  // 0 = not purchased, 1+ = purchased and upgraded
) {
    fun isUnlocked(diceLevel: DiceLevel): Boolean {
        return diceLevel.ordinal >= type.unlockedAt.ordinal
    }

    fun isPurchased(): Boolean = level > 0

    fun getUpgradeCost(): Int {
        if (level == 0) return 5  // Initial purchase
        // Each upgrade: +75% scaling
        var cost = 5.0
        for (i in 1..level) {
            cost *= 1.75
        }
        return kotlin.math.ceil(cost).toInt()
    }
}

data class ExtraDice(
    val index: Int,  // 0-based index (0 = first extra die)
    var diceLevel: DiceLevel = DiceLevel.D4,
    val buffSlots: MutableList<BuffSlot> = mutableListOf(
        BuffSlot(BuffSlotType.FLAT_SCORE),
        BuffSlot(BuffSlotType.CRIT_CHANCE),
        BuffSlot(BuffSlotType.PASSIVE_POINTS),
        BuffSlot(BuffSlotType.ESSENCE_MULT),
        BuffSlot(BuffSlotType.BUFF_DURATION),
        BuffSlot(BuffSlotType.MEGA_CRIT)
    )
) {
    fun canUpgrade(): Boolean = diceLevel.next() != null

    fun getUpgradeCost(): Int = diceLevel.next()?.upgradeCost ?: 0

    fun upgrade() {
        diceLevel.next()?.let { diceLevel = it }
    }
}

// Active Buff tracking
data class CritBuff(
    val multiplier: Double,
    val endTime: Long
)

data class PassiveBuff(
    val pointsPerSecond: Double,
    val endTime: Long
)

data class EssenceBuff(
    val multiplier: Double,
    val endTime: Long
)

// Player Class System (5 classes)
enum class PlayerClass(val displayName: String, val description: String, val emoji: String) {
    WARRIOR("Krieger", "Stark und mutig, schwingt sein Schwert!", "⚔️"),
    MAGE("Magier", "Meister der arkanen Künste!", "🔮"),
    ROGUE("Schurke", "Schnell und tödlich aus dem Schatten!", "🗡️"),
    CLERIC("Kleriker", "Heiler und göttlicher Kämpfer!", "✨"),
    RANGER("Waldläufer", "Meisterschütze und Naturfreund!", "🏹");

    fun getBonusDescription(): String {
        return when (this) {
            WARRIOR -> "+20% auf alle Klicks"
            MAGE -> "+50% Essence Multiplier"
            ROGUE -> "+30% Crit Chance von Extra Dice"
            CLERIC -> "+100% Passive Points/Sec"
            RANGER -> "Extra Dice kosten 20% weniger"
        }
    }
}
