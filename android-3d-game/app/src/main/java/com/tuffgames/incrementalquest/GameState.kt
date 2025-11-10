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

    // Gold currency (for equipment upgrades, fusion, etc.)
    var gold = 0
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

    // RPG System - Character Data
    private var characterStats: CharacterStats? = null
    private val equippedItems = mutableMapOf<EquipmentSlot, Equipment>()
    private val inventory = mutableListOf<Equipment>()  // Unequipped equipment
    private var characterLoadout = CharacterLoadout()

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

    // Skill Tree System
    private val unlockedSkills = mutableSetOf<UniversalSkill>()  // Which skills are unlocked
    var availableSkillPoints = 0  // Unspent skill points
        private set
    private var totalSkillPointsEarned = 0  // For tracking
    private var lastRespecTime = 0L  // Cooldown tracking

    // Level 100 Active Skill: Zeitdilatation
    var zeitdilatationActive = false
        private set
    var zeitdilatationEndTime = 0L
        private set
    private var zeitdilatationLastUsed = 0L

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
        if (!classUnlocked) return

        selectedClass = playerClass

        // Get base D&D attributes for this class
        val baseAttrs = playerClass.getBaseAttributes()

        // Initialize character stats with D&D 5e system
        val stats = CharacterStats(
            strength = baseAttrs[DndAttribute.STRENGTH] ?: 10,
            dexterity = baseAttrs[DndAttribute.DEXTERITY] ?: 10,
            constitution = baseAttrs[DndAttribute.CONSTITUTION] ?: 10,
            intelligence = baseAttrs[DndAttribute.INTELLIGENCE] ?: 10,
            wisdom = baseAttrs[DndAttribute.WISDOM] ?: 10,
            charisma = baseAttrs[DndAttribute.CHARISMA] ?: 10,
            level = 1,
            experience = 0,
            maxHP = playerClass.baseHP,
            currentHP = playerClass.baseHP,
            maxMana = calculateMaxMana(playerClass, baseAttrs, 1),
            currentMana = calculateMaxMana(playerClass, baseAttrs, 1),
            baseAC = 10,
            armorBonus = 0,
            initiativeBonus = 0
        )

        // Add skill proficiencies
        playerClass.skillProficiencies.forEach { skill ->
            stats.proficientSkills.add(skill)
        }

        characterStats = stats

        // Give starter equipment (Set 1, all GRAU, Tier 1)
        val sets = playerClass.getAvailableSets()
        if (sets.isNotEmpty()) {
            val starterSet = sets[0]  // Set 1 als Starter
            equippedItems[EquipmentSlot.WEAPON] = Equipment(EquipmentSlot.WEAPON, starterSet)
            equippedItems[EquipmentSlot.ARMOR] = Equipment(EquipmentSlot.ARMOR, starterSet)
            equippedItems[EquipmentSlot.ACCESSORY] = Equipment(EquipmentSlot.ACCESSORY, starterSet)
        }

        // Set default loadout for Paladin
        if (playerClass == PlayerClass.PALADIN) {
            // 3 starter abilities (available at level 1)
            characterLoadout.normalAbilities[0] = PaladinAbility.SCHILDSCHLAG
            characterLoadout.normalAbilities[1] = PaladinAbility.HEILENDES_LICHT
            // Slot 2 bleibt leer bis Level 10 (dann 3 normal slots)
        }
    }

    // Calculate max mana based on casting stat and level
    private fun calculateMaxMana(playerClass: PlayerClass, attrs: Map<DndAttribute, Int>, level: Int): Int {
        val castingStat = playerClass.castingStat ?: return 0  // Barbar hat keine Magie
        val modifier = DndAttribute.getModifier(attrs[castingStat] ?: 10)
        return (modifier * level * 5).coerceAtLeast(0)
    }

    // ==================== RPG SYSTEM FUNCTIONS ====================

    // Get character stats (read-only)
    fun getCharacterStats(): CharacterStats? = characterStats

    // Get character loadout (read-only)
    fun getCharacterLoadout(): CharacterLoadout = characterLoadout

    // Give experience to character
    fun giveExperience(amount: Int) {
        val playerClass = selectedClass ?: return
        characterStats?.let { stats ->
            stats.experience += amount
            while (stats.canLevelUp()) {
                stats.levelUp(playerClass)
            }
        }
    }

    // Equipment functions
    fun getEquippedItem(slot: EquipmentSlot): Equipment? = equippedItems[slot]

    fun getInventory(): List<Equipment> = inventory.toList()

    fun equipItem(equipment: Equipment) {
        // Unequip current item in slot if exists
        equippedItems[equipment.slot]?.let { currentItem ->
            inventory.add(currentItem)
        }
        // Equip new item
        equippedItems[equipment.slot] = equipment
        inventory.remove(equipment)
        // Apply equipment bonuses to character
        applyEquipmentBonuses()
    }

    fun unequipItem(slot: EquipmentSlot) {
        equippedItems[slot]?.let { item ->
            inventory.add(item)
            equippedItems.remove(slot)
            // Update equipment bonuses
            applyEquipmentBonuses()
        }
    }

    fun addItemToInventory(equipment: Equipment) {
        inventory.add(equipment)
    }

    // Get fusion cost (3 items → 1 higher rarity)
    fun getFusionCost(equipment: Equipment): Int {
        if (!equipment.canCombine()) return Int.MAX_VALUE
        val baseCost = 100  // Base fusion cost in gold
        val rarityMultiplier = equipment.rarity.getMultiplier()
        return (baseCost * rarityMultiplier).toInt()
    }

    // Combine 3 same items to upgrade rarity (costs gold)
    fun canCombineEquipment(equipment: Equipment): Boolean {
        if (!equipment.canCombine()) return false
        val sameItems = inventory.filter {
            it.slot == equipment.slot &&
            it.set == equipment.set &&
            it.rarity == equipment.rarity &&
            it.tier == equipment.tier
        }
        val hasEnoughItems = sameItems.size >= 3
        val hasEnoughGold = gold >= getFusionCost(equipment)
        return hasEnoughItems && hasEnoughGold
    }

    fun combineEquipment(equipment: Equipment): Boolean {
        if (!canCombineEquipment(equipment)) return false

        // Check gold cost
        val cost = getFusionCost(equipment)
        if (gold < cost) return false

        // Find 3 same items
        val sameItems = inventory.filter {
            it.slot == equipment.slot &&
            it.set == equipment.set &&
            it.rarity == equipment.rarity &&
            it.tier == equipment.tier
        }.take(3)

        if (sameItems.size < 3) return false

        // Pay gold cost
        gold -= cost

        // Remove the 3 items
        sameItems.forEach { inventory.remove(it) }

        // Create new item with higher rarity
        val newRarity = equipment.rarity.next() ?: equipment.rarity
        val newItem = Equipment(
            slot = equipment.slot,
            set = equipment.set,
            rarity = newRarity,
            tier = equipment.tier
        )
        inventory.add(newItem)

        return true
    }

    // Upgrade equipment tier (costs DE)
    fun getEquipmentUpgradeCost(equipment: Equipment): Int {
        if (!equipment.canUpgrade()) return Int.MAX_VALUE

        val baseCost = 50.0  // Base cost in gold
        val rarityMultiplier = equipment.rarity.getMultiplier()
        val currentTier = equipment.tier

        // Formula: BaseKosten × Seltenheits-Multiplikator × (Level² / 10) × 1.03^Level
        val quadraticComponent = (currentTier * currentTier) / 10.0
        val exponentialComponent = 1.03.pow(currentTier.toDouble())

        val cost = baseCost * rarityMultiplier * quadraticComponent * exponentialComponent

        return cost.toInt().coerceAtLeast(10)  // Minimum 10 Gold
    }

    fun canUpgradeEquipment(equipment: Equipment): Boolean {
        return equipment.canUpgrade() && gold >= getEquipmentUpgradeCost(equipment)
    }

    fun upgradeEquipment(equipment: Equipment): Boolean {
        if (!canUpgradeEquipment(equipment)) return false
        val cost = getEquipmentUpgradeCost(equipment)
        gold -= cost
        equipment.tier++
        // If this equipment is currently equipped, update bonuses
        if (equippedItems.values.contains(equipment)) {
            applyEquipmentBonuses()
        }
        return true
    }

    // ========== Lootbox System ==========

    // Get lootbox cost (low cost as requested by user)
    fun getLootboxCost(): Int = 5  // 5 DE per box

    // Can afford lootbox?
    fun canAffordLootbox(): Boolean = divineEssence >= getLootboxCost()

    // Buy lootbox - returns random equipment or null if can't afford
    fun buyLootbox(): Equipment? {
        if (!canAffordLootbox()) return null

        val playerClass = selectedClass ?: return null
        val availableSets = playerClass.getAvailableSets()
        if (availableSets.isEmpty()) return null

        divineEssence -= getLootboxCost()

        // Random slot
        val slot = EquipmentSlot.values().random()

        // Random set from available sets
        val set = availableSets.random()

        // Random rarity (weighted toward lower rarities, includes GELB)
        val rarity = getRandomRarity()

        // Random tier based on rarity (higher rarity = higher tier potential)
        val maxTierForRarity = when(rarity) {
            EquipmentRarity.GRAU -> 5
            EquipmentRarity.WEISS -> 10
            EquipmentRarity.GRUEN -> 15
            EquipmentRarity.BLAU -> 20
            EquipmentRarity.LILA -> 30
            EquipmentRarity.GELB -> 50
        }
        val tier = (1..maxTierForRarity).random().coerceAtMost(rarity.getMaxUpgradeLevel())

        val equipment = Equipment(slot, set, rarity, tier)
        addItemToInventory(equipment)
        return equipment
    }

    // Get random rarity with weighted probabilities (includes GELB at 0.5%)
    private fun getRandomRarity(): EquipmentRarity {
        val roll = (1..1000).random()  // Use 1000 for finer control
        return when {
            roll <= 500 -> EquipmentRarity.GRAU    // 50.0%
            roll <= 750 -> EquipmentRarity.WEISS   // 25.0%
            roll <= 900 -> EquipmentRarity.GRUEN   // 15.0%
            roll <= 970 -> EquipmentRarity.BLAU    // 7.0%
            roll <= 995 -> EquipmentRarity.LILA    // 2.5%
            else -> EquipmentRarity.GELB           // 0.5% (sehr selten!)
        }
    }

    // ========== Set Bonus System ==========

    // Get number of equipped pieces from a specific set
    fun getEquippedSetPieces(set: EquipmentSet): Int {
        return equippedItems.values.count { it.set == set }
    }

    // Check if player has set bonus active
    fun hasSetBonus(set: EquipmentSet, pieces: Int): Boolean {
        return getEquippedSetPieces(set) >= pieces
    }

    // Get all active set bonuses
    // Get active set counts (for UI display)
    fun getActiveSetCounts(): Map<EquipmentSet, Int> {
        val setBonuses = mutableMapOf<EquipmentSet, Int>()
        equippedItems.values.forEach { equipment ->
            setBonuses[equipment.set] = (setBonuses[equipment.set] ?: 0) + 1
        }
        return setBonuses.filter { it.value >= 2 }  // Only show sets with 2+ pieces
    }

    // Calculate total equipment stats (all equipped items combined)
    fun getTotalEquipmentStats(): EquipmentStats {
        val totalStats = EquipmentStats()

        equippedItems.values.forEach { equipment ->
            val itemStats = equipment.getStats()
            totalStats.acBonus += itemStats.acBonus
            totalStats.hpBonus += itemStats.hpBonus
            totalStats.manaBonus += itemStats.manaBonus
            totalStats.weaponDamage += itemStats.weaponDamage
            totalStats.strengthBonus += itemStats.strengthBonus
            totalStats.dexterityBonus += itemStats.dexterityBonus
            totalStats.constitutionBonus += itemStats.constitutionBonus
            totalStats.intelligenceBonus += itemStats.intelligenceBonus
            totalStats.wisdomBonus += itemStats.wisdomBonus
            totalStats.charismaBonus += itemStats.charismaBonus
            totalStats.healingPowerPercent += itemStats.healingPowerPercent
            totalStats.critChancePercent += itemStats.critChancePercent
        }

        return totalStats
    }

    // Get active set bonuses from equipped items
    fun getActiveSetBonuses(): List<SetBonus> {
        val setBonuses = mutableListOf<SetBonus>()

        // Count pieces per set
        val setCount = mutableMapOf<EquipmentSet, Int>()
        equippedItems.values.forEach { equipment ->
            setCount[equipment.set] = (setCount[equipment.set] ?: 0) + 1
        }

        // Get bonuses for each set
        setCount.forEach { (set, count) ->
            set.getSetBonuses(count)?.let { setBonuses.add(it) }
        }

        return setBonuses
    }

    // Apply equipment bonuses to character stats (called when equipping/unequipping)
    fun applyEquipmentBonuses() {
        val stats = characterStats ?: return
        val equipStats = getTotalEquipmentStats()

        // Apply AC bonus
        stats.armorBonus = equipStats.acBonus

        // Apply HP bonus (need to track base max HP separately)
        // For now, we recalculate max HP with equipment
        val playerClass = selectedClass ?: return
        val baseMaxHP = playerClass.baseHP + (stats.level - 1) * (playerClass.hitDice.average() + stats.getModifier(DndAttribute.CONSTITUTION))
        stats.maxHP = baseMaxHP + equipStats.hpBonus

        // Apply mana bonus
        val baseMana = calculateMaxMana(playerClass, mapOf(
            DndAttribute.STRENGTH to stats.strength,
            DndAttribute.DEXTERITY to stats.dexterity,
            DndAttribute.CONSTITUTION to stats.constitution,
            DndAttribute.INTELLIGENCE to stats.intelligence,
            DndAttribute.WISDOM to stats.wisdom,
            DndAttribute.CHARISMA to stats.charisma
        ), stats.level)
        stats.maxMana = baseMana + equipStats.manaBonus

        // Apply attribute bonuses (these modify the base attributes)
        // Note: We need to track base attributes separately to avoid stacking
        // For now, equipment bonuses are added on top of base stats
        // This will be refined when we add proper base stat tracking
    }

    // Loadout functions
    fun getLoadout(): CharacterLoadout = characterLoadout

    // Set normale Fähigkeit an Slot (0-4)
    fun setLoadoutNormalAbility(index: Int, ability: PaladinAbility?): Boolean {
        val level = characterStats?.level ?: 1
        return characterLoadout.setNormalAbility(index, ability, level)
    }

    // Set Ultimate
    fun setLoadoutUltimate(ability: PaladinAbility?): Boolean {
        val level = characterStats?.level ?: 1
        if (!characterLoadout.hasUltimateSlot(level)) return false
        if (ability != null && !ability.isUltimate()) return false

        characterLoadout.ultimateAbility = ability
        return true
    }

    // Check ob Loadout komplett ist
    fun isLoadoutComplete(): Boolean {
        val level = characterStats?.level ?: 1
        return characterLoadout.isComplete(level)
    }

    // Get verfügbare normale Slots basierend auf Level
    fun getMaxNormalAbilitySlots(): Int {
        val level = characterStats?.level ?: 1
        return characterLoadout.getMaxNormalSlots(level)
    }

    // Get alle freigeschalteten Fähigkeiten
    fun getUnlockedAbilities(): List<PaladinAbility> {
        val level = characterStats?.level ?: 1
        val playerClass = selectedClass ?: return emptyList()
        return playerClass.getAvailableAbilities().filter { it.isUnlockedAt(level) }
    }

    // Get alle freigeschalteten Ultimates
    fun getUnlockedUltimates(): List<PaladinAbility> {
        val level = characterStats?.level ?: 1
        val playerClass = selectedClass ?: return emptyList()
        return playerClass.getAvailableUltimates().filter { it.isUnlockedAt(level) }
    }

    // ========== COMBAT SYSTEM ==========

    // Current active combat
    private var activeCombat: CombatState? = null

    // Story progress tracking
    private var storyCompletedCount = 0
    private var hasCompletedTutorialCombat = false

    // Auftrag progress tracking
    private var auftragCompletedCount = 0

    // Get active combat
    fun getActiveCombat(): CombatState? = activeCombat

    // Start combat (Story or Auftrag)
    fun startCombat(combatType: CombatType, enemies: List<Enemy>): CombatState? {
        val stats = characterStats ?: return null
        val playerClass = selectedClass ?: return null

        // Check if tutorial combat for first story
        val isTutorial = combatType == CombatType.STORY && !hasCompletedTutorialCombat

        // Initialize Lay on Hands pool for this combat
        if (PaladinPassive.LAY_ON_HANDS.isUnlockedAt(stats.level)) {
            characterLoadout.layOnHandsPool = 5 * stats.level
            characterLoadout.layOnHandsUsed = 0
        }

        // Reset Cleansing Touch uses
        characterLoadout.cleansingTouchUsed = 0

        // Create player participant
        val playerParticipant = CombatParticipant.PlayerParticipant(
            stats = stats,
            loadout = characterLoadout,
            playerClass = playerClass,
            name = "Du"
        )

        // Create enemy participants
        val enemyParticipants = enemies.mapIndexed { index, enemy ->
            CombatParticipant.EnemyParticipant(enemy, index)
        }

        // Create combat state
        val combat = CombatState(
            combatType = combatType,
            isTutorial = isTutorial,
            playerParty = mutableListOf(playerParticipant),
            enemyParty = enemyParticipants.toMutableList(),
            auftragCount = if (combatType == CombatType.AUFTRAG) auftragCompletedCount + 1 else 0
        )

        // Calculate initiative order
        calculateInitiative(combat)

        // Set first turn
        combat.currentRound = 1
        combat.currentTurnIndex = 0
        combat.isPlayerTurn = combat.isCurrentPlayerTurn()

        // Add combat start log
        combat.addLog("⚔️ Kampf beginnt!", true)
        if (isTutorial) {
            combat.addLog("📖 TUTORIAL: Dies ist dein erster Kampf! Nutze deine Fähigkeiten weise.", true)
        }

        activeCombat = combat
        return combat
    }

    // Calculate initiative order (DEX-based)
    private fun calculateInitiative(combat: CombatState) {
        val allParticipants = (combat.playerParty + combat.enemyParty).toMutableList()

        // Sort by initiative (highest first), then random for ties
        combat.turnOrder = allParticipants.sortedWith(
            compareByDescending<CombatParticipant> { it.initiative }
                .thenBy { kotlin.random.Random.nextInt() }
        )
    }

    // Execute player attack (basic attack)
    fun executePlayerBasicAttack(targetIndex: Int): Boolean {
        val combat = activeCombat ?: return false
        if (!combat.isCurrentPlayerTurn()) return false

        val player = combat.getCurrentParticipant() as? CombatParticipant.PlayerParticipant ?: return false
        val target = combat.enemyParty.getOrNull(targetIndex) ?: return false

        if (!target.isAlive()) return false

        // Calculate damage
        val equipStats = getTotalEquipmentStats()
        val strMod = player.stats.getModifier(DndAttribute.STRENGTH)
        var baseDamage = equipStats.weaponDamage + strMod + (player.stats.level / 2)

        // Apply Divine Smite (Level 2 Passive - Optional +2d8)
        if (player.loadout.divineSmiteActive) {
            val smiteDamage = PaladinPassive.DIVINE_SMITE.getScaledValue(player.stats.level)
            baseDamage += smiteDamage
            player.loadout.divineSmiteActive = false  // Reset after use
            combat.addLog("💥 DIVINE SMITE! +$smiteDamage heiliger Schaden!")
        }

        // Apply Improved Divine Smite (Level 11 Passive - Auto)
        if (PaladinPassive.IMPROVED_DIVINE_SMITE.isUnlockedAt(player.stats.level)) {
            val holyDamage = PaladinPassive.IMPROVED_DIVINE_SMITE.getScaledValue(player.stats.level)
            baseDamage += holyDamage
            combat.addLog("⚡ Improved Divine Smite: +$holyDamage heiliger Schaden!")
        }

        val damage = calculateDamage(baseDamage, target.enemy.armor)

        target.enemy.takeDamage(damage)
        combat.addLog("${player.name} greift ${target.name} an und macht $damage Schaden!")

        if (target.enemy.isDead()) {
            combat.addLog("${target.name} wurde besiegt!", true)
        }

        advanceTurn(combat)
        return true
    }

    // Activate Divine Smite (Level 2 Passive - +2d8 Holy Damage for 15 Mana)
    fun activateDivineSmite(): Boolean {
        val combat = activeCombat ?: return false
        if (!combat.isCurrentPlayerTurn()) return false

        val player = combat.getCurrentParticipant() as? CombatParticipant.PlayerParticipant ?: return false

        // Check if passive is unlocked
        if (!PaladinPassive.DIVINE_SMITE.isUnlockedAt(player.stats.level)) {
            combat.addLog("❌ Divine Smite noch nicht freigeschaltet!")
            return false
        }

        // Check mana cost (15)
        if (player.stats.currentMana < 15) {
            combat.addLog("❌ Nicht genug Mana für Divine Smite! (15 Mana benötigt)")
            return false
        }

        // Check if already active
        if (player.loadout.divineSmiteActive) {
            combat.addLog("⚠️ Divine Smite ist bereits aktiv!")
            return false
        }

        // Activate Divine Smite
        player.stats.currentMana -= 15
        player.loadout.divineSmiteActive = true
        combat.addLog("⚡ Divine Smite aktiviert! Nächster Angriff macht +2d8 heiligen Schaden!")

        return true
    }

    // Use Lay on Hands (Level 6 Passive - Healing Pool)
    fun useLayOnHands(amount: Int): Boolean {
        val combat = activeCombat ?: return false
        if (!combat.isCurrentPlayerTurn()) return false

        val player = combat.getCurrentParticipant() as? CombatParticipant.PlayerParticipant ?: return false

        // Check if passive is unlocked
        if (!PaladinPassive.LAY_ON_HANDS.isUnlockedAt(player.stats.level)) {
            combat.addLog("❌ Lay on Hands noch nicht freigeschaltet!")
            return false
        }

        // Check if enough healing available
        val loadout = player.loadout
        val remaining = loadout.layOnHandsPool - loadout.layOnHandsUsed
        if (remaining < amount) {
            combat.addLog("❌ Nicht genug Lay on Hands verfügbar! ($remaining HP verbleibend)")
            return false
        }

        // Apply healing
        val actualAmount = amount.coerceAtMost(player.stats.maxHP - player.stats.currentHP)
        player.stats.heal(actualAmount)
        loadout.layOnHandsUsed += actualAmount

        combat.addLog("${player.name} nutzt Lay on Hands und heilt sich um $actualAmount HP! (${loadout.layOnHandsPool - loadout.layOnHandsUsed}/${loadout.layOnHandsPool} verbleibend)")

        advanceTurn(combat)
        return true
    }

    // Use Cleansing Touch (Level 14 Passive - Remove Debuffs)
    fun useCleansingTouch(): Boolean {
        val combat = activeCombat ?: return false
        if (!combat.isCurrentPlayerTurn()) return false

        val player = combat.getCurrentParticipant() as? CombatParticipant.PlayerParticipant ?: return false

        // Check if passive is unlocked
        if (!PaladinPassive.CLEANSING_TOUCH.isUnlockedAt(player.stats.level)) {
            combat.addLog("❌ Cleansing Touch noch nicht freigeschaltet!")
            return false
        }

        // Check uses remaining
        val loadout = player.loadout
        val maxUses = 3 + (player.stats.level / 10)
        if (loadout.cleansingTouchUsed >= maxUses) {
            combat.addLog("❌ Keine Cleansing Touch Uses mehr verfügbar!")
            return false
        }

        // Remove all debuffs
        val debuffs = listOf(
            StatusEffectType.BLINDED,
            StatusEffectType.BURNED,
            StatusEffectType.STUNNED,
            StatusEffectType.POISONED,
            StatusEffectType.WEAKENED
        )

        var removed = 0
        debuffs.forEach { debuff ->
            if (combat.hasStatusEffect(player, debuff)) {
                combat.removeStatusEffect(player, debuff)
                removed++
            }
        }

        loadout.cleansingTouchUsed++

        if (removed > 0) {
            combat.addLog("${player.name} nutzt Cleansing Touch und entfernt $removed Debuff(s)!")
        } else {
            combat.addLog("${player.name} nutzt Cleansing Touch, aber es gibt keine Debuffs zu entfernen.")
        }

        advanceTurn(combat)
        return true
    }

    // ============================================================================
    // BARBAR RAGE SYSTEM
    // ============================================================================

    // Activate Rage (Level 2 Passive - No Mana Cost, 3 Rounds Duration)
    fun activateRage(): Boolean {
        val combat = activeCombat ?: return false
        if (!combat.isCurrentPlayerTurn()) return false

        val player = combat.getCurrentParticipant() as? CombatParticipant.PlayerParticipant ?: return false

        // Check if passive is unlocked
        if (!BarbarPassive.RAGE.isUnlockedAt(player.stats.level)) {
            combat.addLog("❌ Rage noch nicht freigeschaltet!")
            return false
        }

        // Check if already active
        if (player.loadout.rageActive) {
            combat.addLog("⚠️ Rage ist bereits aktiv!")
            return false
        }

        // Activate Rage
        val duration = BarbarPassive.RAGE.getScaledValue(player.stats.level)
        player.loadout.rageActive = true
        player.loadout.rageRounds = duration

        combat.addLog("💢 RAGE AKTIVIERT! +Schaden, +Schadensresistenz für $duration Runden!")
        return true
    }

    // Deactivate Rage (manually or when duration ends)
    fun deactivateRage() {
        val combat = activeCombat ?: return
        val player = combat.playerParty.firstOrNull() ?: return

        if (player.loadout.rageActive) {
            player.loadout.rageActive = false
            player.loadout.rageRounds = 0
            combat.addLog("💢 Rage endet.")
        }
    }

    // Toggle Reckless Attack (Level 5 Passive)
    fun toggleRecklessAttack(): Boolean {
        val combat = activeCombat ?: return false
        val player = combat.playerParty.firstOrNull() ?: return false

        if (!BarbarPassive.RECKLESS_ATTACK.isUnlockedAt(player.stats.level)) {
            combat.addLog("❌ Reckless Attack noch nicht freigeschaltet!")
            return false
        }

        player.loadout.recklessAttackActive = !player.loadout.recklessAttackActive

        if (player.loadout.recklessAttackActive) {
            combat.addLog("⚔️ Reckless Attack AKTIVIERT! +30% Schaden, -10 AC")
        } else {
            combat.addLog("🛡️ Reckless Attack DEAKTIVIERT. Normale Defense wiederhergestellt.")
        }

        return true
    }

    // Get all targets for an ability (AOE support)
    private fun getAbilityTargets(ability: PaladinAbility, combat: CombatState, primaryTargetIndex: Int): List<CombatParticipant> {
        return when(ability) {
            // AOE Damage Skills
            PaladinAbility.LICHTEXPLOSION,
            PaladinAbility.HEILIGER_KREIS,
            PaladinAbility.URTEIL_DER_GOETTER,
            PaladinAbility.GOTTES_ZORN -> {
                // All living enemies
                combat.getAliveEnemies()
            }

            // Group Healing
            PaladinAbility.MASSENHEILUNG -> {
                // All living allies
                combat.getAliveAllies()
            }

            // Single Target (default)
            else -> {
                val target = combat.enemyParty.getOrNull(primaryTargetIndex)
                if (target != null && target.isAlive()) listOf(target) else emptyList()
            }
        }
    }

    // Execute player ability
    fun executePlayerAbility(ability: PaladinAbility, targetIndex: Int = 0): Boolean {
        val combat = activeCombat ?: return false
        if (!combat.isCurrentPlayerTurn()) return false

        val player = combat.getCurrentParticipant() as? CombatParticipant.PlayerParticipant ?: return false

        // Check cooldown
        if (combat.abilityCooldowns[ability] ?: 0 > 0) {
            combat.addLog("${ability.displayName} ist noch auf Cooldown!")
            return false
        }

        // Check mana cost for SPELL abilities
        if (ability.type == AbilityType.SPELL) {
            if (player.stats.currentMana < ability.cost) {
                combat.addLog("Nicht genug Mana! Benötigt: ${ability.cost}, verfügbar: ${player.stats.currentMana}")
                return false
            }
            player.stats.currentMana -= ability.cost
        }

        // Get targets (supports AOE)
        val targets = getAbilityTargets(ability, combat, targetIndex)
        if (targets.isEmpty()) return false

        // Execute ability effect with special mechanics
        executeAbilityEffects(ability, player, targets, combat)

        // Set cooldown for COMBAT abilities
        if (ability.type == AbilityType.COMBAT) {
            combat.abilityCooldowns[ability] = ability.cost
        }

        advanceTurn(combat)
        return true
    }

    // Execute ability effects (separated for better organization)
    private fun executeAbilityEffects(
        ability: PaladinAbility,
        player: CombatParticipant.PlayerParticipant,
        targets: List<CombatParticipant>,
        combat: CombatState
    ) {
        when {
            ability.baseDamage > 0 -> {
                // Damage ability
                var damage = calculateAbilityDamage(ability, player, combat)

                // Armor-Ignore for specific abilities
                val ignoreArmor = ability == PaladinAbility.HEILIGES_GERICHT

                targets.forEach { target ->
                    if (target is CombatParticipant.EnemyParticipant) {
                        val actualDamage = if (ignoreArmor) {
                            damage  // No armor reduction
                        } else {
                            calculateDamage(damage, target.enemy.armor)
                        }

                        target.enemy.takeDamage(actualDamage)

                        if (target.enemy.isDead()) {
                            combat.addLog("${target.name} wurde besiegt!", true)
                        }
                    }
                }

                if (targets.size > 1) {
                    combat.addLog("${player.name} nutzt ${ability.displayName} gegen ${targets.size} Ziele und macht je $damage Schaden!")
                } else {
                    combat.addLog("${player.name} nutzt ${ability.displayName} gegen ${targets.first().name} und macht $damage Schaden!")
                }
            }
            ability.baseHealing > 0 -> {
                // Healing ability
                val healing = calculateHealing(ability, player, combat)

                targets.forEach { target ->
                    if (target is CombatParticipant.PlayerParticipant) {
                        target.stats.heal(healing)
                    }
                }

                if (targets.size > 1) {
                    combat.addLog("${player.name} nutzt ${ability.displayName} und heilt ${targets.size} Verbündete um je $healing HP!")
                } else {
                    combat.addLog("${player.name} nutzt ${ability.displayName} und heilt sich um $healing HP!")
                }
            }
            else -> {
                // Buff/Utility ability
                combat.addLog("${player.name} nutzt ${ability.displayName}!")
            }
        }

        // Apply special ability effects (Status Effects, Buffs, etc.)
        applyAbilitySpecialEffects(ability, player, targets, combat)
    }

    // Apply special ability effects (Status Effects, Transformations, etc.)
    private fun applyAbilitySpecialEffects(
        ability: PaladinAbility,
        player: CombatParticipant.PlayerParticipant,
        targets: List<CombatParticipant>,
        combat: CombatState
    ) {
        // Execute Mechanics (kill if below threshold)
        if (ability == PaladinAbility.HEILIGE_RACHE_ULT) {
            targets.forEach { target ->
                if (target is CombatParticipant.EnemyParticipant) {
                    val hpPercent = (target.enemy.currentHP * 100) / target.enemy.maxHP
                    if (hpPercent <= 30) {
                        val overkill = target.enemy.currentHP
                        target.enemy.takeDamage(9999)
                        player.stats.heal(overkill)  // Heal for overkill damage
                        combat.addLog("⚔️ EXECUTE! ${target.name} wurde unter 30% HP sofort getötet! (+$overkill HP)")
                    }
                }
            }
        }

        // Resurrection + One-Hit-Kill
        if (ability == PaladinAbility.ERLOESUNGSSCHLAG) {
            targets.forEach { target ->
                if (target is CombatParticipant.EnemyParticipant) {
                    target.enemy.takeDamage(9999)  // Instant kill
                    combat.addLog("💀 ${target.name} wurde durch Erlösungsschlag sofort getötet!")
                }
            }
            // Revive all dead allies (if any in future party system)
            player.stats.heal(100)
            combat.addLog("✨ Alle Verbündeten werden wiederbelebt!")
        }

        // Armor-Ignore Mechanic
        if (ability == PaladinAbility.HEILIGES_GERICHT) {
            // Already handled in damage calculation, but add log
            combat.addLog("⚡ Ignoriert Rüstung des Ziels!")
            val healing = 50 + (player.stats.level * 2)
            player.stats.heal(healing)
            combat.addLog("💚 Heilt für $healing HP!")
        }

        // Apply effects based on specific abilities
        when (ability) {
            // Temp HP / Shield
            PaladinAbility.HEILIGER_SCHUTZSCHILD -> {
                val tempHP = 30 + (player.stats.level * 2)
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.SHIELD, 0, tempHP, ability.displayName))
            }

            // Blinding AOE
            PaladinAbility.LICHTEXPLOSION -> {
                targets.forEach { target ->
                    combat.addStatusEffect(target, StatusEffect(StatusEffectType.BLINDED, 2, 0, ability.displayName))
                }
            }

            // Defense Buff
            PaladinAbility.SEGEN_DER_VERTEIDIGUNG -> {
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.DAMAGE_RESIST, 3, 20, ability.displayName))
            }

            // Damage Reduction Wall
            PaladinAbility.WALL_OF_FAITH -> {
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.DAMAGE_RESIST, 2, 75, ability.displayName))
            }

            // Burn DOT
            PaladinAbility.GOETTLICHER_ZORN -> {
                targets.forEach { target ->
                    combat.addStatusEffect(target, StatusEffect(StatusEffectType.BURNED, 3, 15, ability.displayName))
                }
            }

            // Haste Buff
            PaladinAbility.SEGEN_DER_EILE -> {
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.HASTE, 2, 0, ability.displayName))
            }

            // Reflect Shield
            PaladinAbility.HEILIGE_AEGIS -> {
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.REFLECT, 1, 50, ability.displayName))
            }

            // Revive on Death
            PaladinAbility.LICHT_DER_HOFFNUNG -> {
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.REVIVE_ON_DEATH, 1, 0, ability.displayName))
            }

            // Healing Boost
            PaladinAbility.SEGEN_DES_LICHTS -> {
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.REGENERATION, 3, 15, ability.displayName))
            }

            // Immunity + Damage
            PaladinAbility.CHAMPION_DES_LICHTS -> {
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.IMMUNE, 3, 0, ability.displayName))
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.DIVINE_POWER, 3, 50, ability.displayName))
            }

            // God Mode Transformation
            PaladinAbility.HEILIGE_VERWANDLUNG -> {
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.TRANSFORMATION, 4, 100, ability.displayName))
            }

            // Ultimate God Mode
            PaladinAbility.AVATAR_DES_LICHTS -> {
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.TRANSFORMATION, 10, 100, ability.displayName))
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.IMMUNE, 10, 0, ability.displayName))
            }

            // Ultimate with Full Heal + Immunity
            PaladinAbility.GOTTES_ZORN -> {
                player.stats.heal(999)
                combat.addStatusEffect(player, StatusEffect(StatusEffectType.IMMUNE, 5, 0, ability.displayName))
            }

            else -> {
                // No special effects for this ability
            }
        }
    }

    // Calculate damage with AC reduction
    private fun calculateDamage(baseDamage: Int, targetAC: Int): Int {
        // Simple formula: damage - (AC / 4)
        val reduction = targetAC / 4
        return (baseDamage - reduction).coerceAtLeast(1)
    }

    // Calculate ability damage
    private fun calculateAbilityDamage(ability: PaladinAbility, player: CombatParticipant.PlayerParticipant, combat: CombatState): Int {
        val equipStats = getTotalEquipmentStats()
        val baseDamage = ability.getDamage(player.stats.level)
        val weaponBonus = equipStats.weaponDamage / 2
        var totalDamage = baseDamage + weaponBonus

        // Apply status effect modifiers
        val playerEffects = combat.getActiveEffects(player)
        playerEffects.forEach { effect ->
            when (effect.type) {
                StatusEffectType.DIVINE_POWER -> {
                    totalDamage = (totalDamage * (100 + effect.value)) / 100
                }
                StatusEffectType.WEAKENED -> {
                    totalDamage = totalDamage / 2
                }
                StatusEffectType.TRANSFORMATION -> {
                    totalDamage = totalDamage * 2  // 100% mehr Damage
                }
                else -> {}
            }
        }

        // Apply equipment set bonuses
        val setBonuses = getActiveSetBonuses()
        for (setBonus in setBonuses) {
            totalDamage = (totalDamage * (100 + setBonus.damageBonus)) / 100
        }

        return totalDamage.coerceAtLeast(1)
    }

    // Calculate healing
    private fun calculateHealing(ability: PaladinAbility, player: CombatParticipant.PlayerParticipant, combat: CombatState): Int {
        val equipStats = getTotalEquipmentStats()
        val baseHealing = ability.getHealing(player.stats.level)
        val healingBonus = (baseHealing * equipStats.healingPowerPercent) / 100
        var totalHealing = baseHealing + healingBonus

        // Apply status effect modifiers
        val playerEffects = combat.getActiveEffects(player)
        playerEffects.forEach { effect ->
            when (effect.type) {
                StatusEffectType.TRANSFORMATION -> {
                    totalHealing = totalHealing * 2  // 100% mehr Healing
                }
                StatusEffectType.POISONED -> {
                    totalHealing = totalHealing / 2  // 50% weniger Healing
                }
                else -> {}
            }
        }

        // Apply equipment set bonuses
        val setBonuses = getActiveSetBonuses()
        for (setBonus in setBonuses) {
            totalHealing = (totalHealing * (100 + setBonus.healingBonus)) / 100
        }

        return totalHealing.coerceAtLeast(0)
    }

    // Execute enemy turn (AI)
    fun executeEnemyTurn() {
        val combat = activeCombat ?: return

        val enemy = combat.getCurrentParticipant() as? CombatParticipant.EnemyParticipant ?: return

        // Simple AI: Attack random or weakest player
        val target = when (enemy.enemy.aiType) {
            EnemyAIType.SIMPLE -> combat.getAliveAllies().randomOrNull()
            EnemyAIType.SMART -> combat.getAliveAllies().minByOrNull { it.getCurrentHP() }
        } ?: return

        val damage = calculateDamage(enemy.enemy.baseDamage, target.stats.getArmorClass())
        target.stats.takeDamage(damage)

        combat.addLog("${enemy.name} greift ${target.name} an und macht $damage Schaden!")

        if (!target.isAlive()) {
            combat.addLog("${target.name} wurde besiegt!", true)
        }

        advanceTurn(combat)
    }

    // Advance to next turn
    private fun advanceTurn(combat: CombatState) {
        // Check if combat ended
        if (combat.checkCombatEnd()) {
            endCombat()
            return
        }

        // Move to next participant
        combat.currentTurnIndex++

        // If we've gone through all participants, start new round
        if (combat.currentTurnIndex >= combat.turnOrder.size) {
            startNewRound(combat)
            combat.currentTurnIndex = 0
        }

        // Skip dead participants
        while (combat.currentTurnIndex < combat.turnOrder.size &&
               !combat.getCurrentParticipant()!!.isAlive()) {
            combat.currentTurnIndex++
        }

        // Update player turn flag
        combat.isPlayerTurn = combat.isCurrentPlayerTurn()

        // Auto-execute enemy turns
        if (!combat.isPlayerTurn && !combat.combatEnded) {
            executeEnemyTurn()
        }
    }

    // Start new round
    private fun startNewRound(combat: CombatState) {
        combat.currentRound++
        combat.addLog("--- Runde ${combat.currentRound} ---", true)

        // Process status effects (DOT, HOT, etc.)
        combat.updateStatusEffects()

        // Decrease status effect durations
        combat.decreaseStatusEffectDurations()

        // Reduce cooldowns
        combat.abilityCooldowns.keys.toList().forEach { ability ->
            val current = combat.abilityCooldowns[ability] ?: 0
            if (current > 0) {
                combat.abilityCooldowns[ability] = current - 1
            }
        }

        // Mana regeneration: 1 base per round
        combat.playerParty.forEach { player ->
            val manaRegen = 1  // Base regeneration
            player.stats.currentMana = (player.stats.currentMana + manaRegen).coerceAtMost(player.stats.maxMana)
        }

        // Decrease Rage duration (Barbar)
        combat.playerParty.forEach { player ->
            if (player.loadout.rageActive && player.loadout.rageRounds > 0) {
                player.loadout.rageRounds--
                if (player.loadout.rageRounds <= 0) {
                    player.loadout.rageActive = false
                    combat.addLog("💢 Rage von ${player.name} endet!")
                }
            }
        }
    }

    // End combat and apply rewards
    private fun endCombat() {
        val combat = activeCombat ?: return
        val result = combat.combatResult ?: return

        if (result.victory) {
            // Apply XP
            giveExperience(result.xpGained)

            // Apply Gold
            gold += result.goldGained

            // Apply Divine Essence
            divineEssence += result.essenceGained

            // Add equipment drop to inventory if present
            result.lootDropped?.let { equipment ->
                addItemToInventory(equipment)
                combat.addLog("⚔️ Equipment gefunden: ${equipment.rarity.displayName} ${equipment.slot.name} (Tier ${equipment.tier})", true)
            }

            // Track completion
            when (combat.combatType) {
                CombatType.STORY -> {
                    storyCompletedCount++
                    if (combat.isTutorial) {
                        hasCompletedTutorialCombat = true
                    }
                }
                CombatType.AUFTRAG -> {
                    auftragCompletedCount++
                }
            }

            // After combat: restore 50% of lost mana
            characterStats?.let { stats ->
                val lostMana = stats.maxMana - stats.currentMana
                val restoreAmount = lostMana / 2
                stats.currentMana = (stats.currentMana + restoreAmount).coerceAtMost(stats.maxMana)
                combat.addLog("Nach dem Kampf: $restoreAmount Mana wiederhergestellt")
            }

            val rewardsText = buildString {
                append("Belohnungen:")
                if (result.xpGained > 0) append(" +${result.xpGained} XP")
                if (result.goldGained > 0) append(" +${result.goldGained} Gold")
                if (result.essenceGained > 0) append(" +${result.essenceGained} DE")
            }
            combat.addLog(rewardsText, true)
        } else {
            // Defeat - restore player to 50% HP for next attempt
            characterStats?.let { stats ->
                stats.currentHP = stats.maxHP / 2
                stats.currentMana = stats.maxMana / 2
            }
        }
    }

    // Get tutorial combat
    fun getTutorialCombat(): CombatState? {
        if (hasCompletedTutorialCombat) return null
        val enemy = MonsterTemplates.createTutorialBandit()
        return startCombat(CombatType.STORY, listOf(enemy))
    }

    // Get random story combat
    fun getStoryCombat(playerLevel: Int = characterStats?.level ?: 1): CombatState? {
        val enemyCount = (1..2).random()
        val enemies = List(enemyCount) {
            MonsterTemplates.getRandomEnemy(playerLevel, smartAI = true)
        }
        return startCombat(CombatType.STORY, enemies)
    }

    // Get random auftrag combat
    fun getAuftragCombat(playerLevel: Int = characterStats?.level ?: 1): CombatState? {
        // Every 10th auftrag has smart AI
        val nextCount = auftragCompletedCount + 1
        val useSmartAI = nextCount % 10 == 0

        val enemyCount = (1..3).random()
        val enemies = List(enemyCount) {
            MonsterTemplates.getRandomEnemy(playerLevel, smartAI = useSmartAI)
        }
        return startCombat(CombatType.AUFTRAG, enemies)
    }

    // Clear active combat
    fun clearActiveCombat() {
        activeCombat = null
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

    // ========== Skill Tree System ==========

    // Grant skill point (called on level up)
    fun grantSkillPoint() {
        availableSkillPoints++
        totalSkillPointsEarned++
    }

    // Check if skill can be unlocked
    fun canUnlockSkill(skill: UniversalSkill): Boolean {
        if (unlockedSkills.contains(skill)) return false  // Already unlocked
        if (availableSkillPoints < 1) return false  // No points available

        val characterLevel = characterStats?.level ?: 1
        return skill.isUnlockable(characterLevel, unlockedSkills)
    }

    // Unlock a skill
    fun unlockSkill(skill: UniversalSkill): Boolean {
        if (!canUnlockSkill(skill)) return false

        unlockedSkills.add(skill)
        availableSkillPoints--

        // Apply immediate effects (like max HP/Mana increases)
        applySkillEffects(skill)

        return true
    }

    // Apply immediate stat changes from skills
    private fun applySkillEffects(skill: UniversalSkill) {
        characterStats?.let { stats ->
            when (skill) {
                // Flat HP bonuses
                UniversalSkill.ROBUSTHEIT_I -> stats.maxHP += 10
                UniversalSkill.ROBUSTHEIT_II -> stats.maxHP += 20
                UniversalSkill.ROBUSTHEIT_III -> stats.maxHP += 30
                UniversalSkill.ROBUSTHEIT_IV -> stats.maxHP += 50
                UniversalSkill.ROBUSTHEIT_V -> stats.maxHP += 75

                // Flat Armor bonuses
                UniversalSkill.RUESTUNGS_TRAINING_I -> stats.armorBonus += 1

                // Flat Mana bonuses
                UniversalSkill.MANA_POOL_I -> stats.maxMana += 10
                UniversalSkill.MANA_POOL_II -> stats.maxMana += 20
                UniversalSkill.MANA_POOL_III -> stats.maxMana += 30
                UniversalSkill.MANA_POOL_IV -> stats.maxMana += 50

                // Mana regeneration (applied dynamically in combat, not stored)
                UniversalSkill.SCHNELLE_REGENERATION -> { /* Applied in combat system */ }

                else -> { /* Other skills are applied dynamically via getSkillBonus() */ }
            }
        }
    }

    // Get bonus multiplier from skills for a specific category
    fun getSkillBonus(category: String): Double {
        var bonus = 1.0

        for (skill in unlockedSkills) {
            when (category) {
                "click_power" -> when (skill) {
                    UniversalSkill.STAERKERE_KLICKS -> bonus += 0.10
                    UniversalSkill.FLEISSIGE_HAENDE -> bonus += 0.15
                    UniversalSkill.MEGA_KLICK_I -> bonus += 0.25
                    UniversalSkill.MEGA_KLICK_II -> bonus += 0.50
                    UniversalSkill.CLICK_MULTI_I -> bonus += 0.20
                    UniversalSkill.CLICK_MULTI_II -> bonus += 0.30
                    UniversalSkill.CLICK_MULTI_III -> bonus += 0.50
                    UniversalSkill.CLICK_MULTI_IV -> bonus += 0.75
                    UniversalSkill.CLICK_MULTI_V -> bonus += 1.00
                    UniversalSkill.ULTRA_KLICK -> bonus += 1.00
                    UniversalSkill.TITANISCHER_KLICK -> bonus += 2.00
                    UniversalSkill.APOKALYPTISCHER_KLICK -> bonus += 3.00
                    UniversalSkill.UNIVERSAL_BONUS -> bonus += 0.10
                    else -> {}
                }
                "auto_clicker_speed" -> when (skill) {
                    UniversalSkill.AUTO_CLICKER_SPEED_I -> bonus += 0.50  // +0.5 clicks/sec
                    UniversalSkill.AUTO_CLICKER_II -> bonus += 1.00  // +1 click/sec
                    UniversalSkill.AUTO_CLICK_GESCHWINDIGKEIT -> bonus += 1.50  // +1.5 clicks/sec
                    UniversalSkill.AUTO_CLICKER_III -> bonus += 1.50  // +1.5 clicks/sec
                    UniversalSkill.AUTO_CLICKER_IV -> bonus += 2.00  // +2 clicks/sec
                    UniversalSkill.AUTO_CLICKER_V -> bonus += 3.00  // +3 clicks/sec
                    else -> {}
                }
                "gold" -> when (skill) {
                    UniversalSkill.GOLD_FINDER_I -> bonus += 0.10
                    UniversalSkill.GOLD_BONUS_I -> bonus += 0.15
                    UniversalSkill.GOLD_FINDER_II -> bonus += 0.20
                    UniversalSkill.GOLD_BONUS_II -> bonus += 0.25
                    UniversalSkill.GOLD_FINDER_III -> bonus += 0.30
                    UniversalSkill.GOLD_BONUS_III -> bonus += 0.40
                    UniversalSkill.GOLD_MEISTER -> bonus += 0.50
                    UniversalSkill.GOLD_GOTT -> bonus += 0.75
                    UniversalSkill.UNIVERSAL_BONUS -> bonus += 0.10
                    else -> {}
                }
                "xp" -> when (skill) {
                    UniversalSkill.KAMPF_ERFAHRUNG_I -> bonus += 0.10
                    UniversalSkill.KAMPF_MEISTERSCHAFT_I -> bonus += 0.15
                    UniversalSkill.KAMPF_MEISTERSCHAFT_II -> bonus += 0.25
                    UniversalSkill.KAMPF_MEISTERSCHAFT_III -> bonus += 0.40
                    UniversalSkill.KAMPF_GOTT -> bonus += 0.60
                    UniversalSkill.UNIVERSAL_BONUS -> bonus += 0.10
                    else -> {}
                }
                "damage" -> when (skill) {
                    UniversalSkill.SCHADENS_BONUS_I -> bonus += 0.05
                    UniversalSkill.SCHADENS_BONUS_II -> bonus += 0.10
                    UniversalSkill.SCHADENS_BONUS_III -> bonus += 0.15
                    UniversalSkill.SCHADENS_BONUS_IV -> bonus += 0.25
                    UniversalSkill.SCHADENS_BONUS_V -> bonus += 0.40
                    UniversalSkill.UNIVERSAL_BONUS -> bonus += 0.10
                    else -> {}
                }
                "armor" -> when (skill) {
                    UniversalSkill.VERTEIDIGUNG_I -> bonus += 0.05
                    UniversalSkill.VERTEIDIGUNG_II -> bonus += 0.10
                    UniversalSkill.VERTEIDIGUNG_III -> bonus += 0.15
                    UniversalSkill.VERTEIDIGUNG_IV -> bonus += 0.25
                    else -> {}
                }
                "hp_percent" -> when (skill) {
                    UniversalSkill.ZAEHIGKEIT -> bonus += 0.05
                    else -> {}
                }
                "healing" -> when (skill) {
                    UniversalSkill.ERSTE_HILFE -> bonus += 0.10
                    UniversalSkill.SCHNELLE_ERHOLUNG -> bonus += 0.50
                    else -> {}
                }
                "drop_chance" -> when (skill) {
                    UniversalSkill.LOOT_GLUECK_I -> bonus += 0.05
                    UniversalSkill.LOOT_GLUECK_II -> bonus += 0.10
                    UniversalSkill.LOOT_GLUECK_III -> bonus += 0.15
                    UniversalSkill.LOOT_MAGNET -> bonus += 0.20
                    else -> {}
                }
                "divine_essence" -> when (skill) {
                    UniversalSkill.DE_FINDER_I -> bonus += 0.10
                    UniversalSkill.DE_FINDER_II -> bonus += 0.20
                    UniversalSkill.DE_FINDER_III -> bonus += 0.30
                    UniversalSkill.DE_FINDER_IV -> bonus += 0.50
                    UniversalSkill.UNIVERSAL_BONUS -> bonus += 0.10
                    else -> {}
                }
                "prestige" -> when (skill) {
                    UniversalSkill.PRESTIGE_BONUS_I -> bonus += 0.10
                    UniversalSkill.PRESTIGE_BONUS_II -> bonus += 0.20
                    UniversalSkill.PRESTIGE_MEISTER -> bonus += 0.30
                    UniversalSkill.PRESTIGE_GOTT -> bonus += 0.50
                    else -> {}
                }
                "idle_gains" -> when (skill) {
                    UniversalSkill.IDLE_GAINS_I -> bonus += 0.10
                    UniversalSkill.IDLE_GAINS_II -> bonus += 0.20
                    UniversalSkill.IDLE_GAINS_III -> bonus += 0.30
                    UniversalSkill.IDLE_GAINS_IV -> bonus += 0.50
                    UniversalSkill.IDLE_MEISTER -> bonus += 0.75
                    else -> {}
                }
            }
        }

        // Zeitdilatation active effects
        if (zeitdilatationActive && System.currentTimeMillis() < zeitdilatationEndTime) {
            when (category) {
                "click_power" -> bonus *= 5.0  // 5x multiplier
                "auto_clicker_speed" -> bonus *= 2.0  // 2x multiplier
                "gold", "xp" -> bonus *= 2.0  // 2x multiplier
                else -> {}
            }
        }

        return bonus
    }

    // Get crit chance from skills
    fun getCritChance(): Double {
        var critChance = 0.0

        if (unlockedSkills.contains(UniversalSkill.KRITISCHER_KLICK_I)) critChance += 0.05
        if (unlockedSkills.contains(UniversalSkill.KRITISCHER_KLICK_II)) critChance += 0.10
        if (unlockedSkills.contains(UniversalSkill.SUPER_CRIT)) critChance += 0.15
        if (unlockedSkills.contains(UniversalSkill.MEGA_CRIT)) critChance += 0.20
        if (unlockedSkills.contains(UniversalSkill.GOETTLICHER_CRIT)) critChance += 0.25

        return critChance
    }

    // Get crit multiplier from skills
    fun getCritMultiplier(): Double {
        var critMult = 1.0

        if (unlockedSkills.contains(UniversalSkill.KRITISCHER_KLICK_I)) critMult = 2.0
        if (unlockedSkills.contains(UniversalSkill.KRITISCHER_KLICK_II)) critMult = 2.5
        if (unlockedSkills.contains(UniversalSkill.SUPER_CRIT)) critMult = 3.0
        if (unlockedSkills.contains(UniversalSkill.MEGA_CRIT)) critMult = 4.0
        if (unlockedSkills.contains(UniversalSkill.GOETTLICHER_CRIT)) critMult = 5.0

        return critMult
    }

    // Check if player has Zeitdilatation unlocked and ready
    fun canUseZeitdilatation(): Boolean {
        if (!unlockedSkills.contains(UniversalSkill.ZEITDILATATION)) return false
        if (zeitdilatationActive) return false

        val cooldown = 10 * 60 * 1000L  // 10 minutes
        val timeSinceLastUse = System.currentTimeMillis() - zeitdilatationLastUsed
        return timeSinceLastUse >= cooldown
    }

    // Activate Zeitdilatation
    fun activateZeitdilatation(): Boolean {
        if (!canUseZeitdilatation()) return false

        zeitdilatationActive = true
        zeitdilatationEndTime = System.currentTimeMillis() + (60 * 1000L)  // 60 seconds
        zeitdilatationLastUsed = System.currentTimeMillis()

        return true
    }

    // Check and clear expired Zeitdilatation
    fun updateZeitdilatation() {
        if (zeitdilatationActive && System.currentTimeMillis() >= zeitdilatationEndTime) {
            zeitdilatationActive = false
        }
    }

    // Get remaining Zeitdilatation cooldown in seconds
    fun getZeitdilatationCooldown(): Long {
        if (!unlockedSkills.contains(UniversalSkill.ZEITDILATATION)) return -1

        val cooldown = 10 * 60 * 1000L
        val timeSinceLastUse = System.currentTimeMillis() - zeitdilatationLastUsed
        val remaining = cooldown - timeSinceLastUse

        return if (remaining > 0) remaining / 1000 else 0
    }

    // Respec skill tree (costs Divine Essence)
    fun getRespecCost(): Int {
        return 50  // 50 DE to respec
    }

    fun canRespec(): Boolean {
        return unlockedSkills.isNotEmpty() && divineEssence >= getRespecCost()
    }

    fun respecSkills(): Boolean {
        if (!canRespec()) return false

        // Remove all stat bonuses that were applied
        for (skill in unlockedSkills) {
            removeSkillEffects(skill)
        }

        // Refund all skill points
        availableSkillPoints = totalSkillPointsEarned

        // Clear unlocked skills
        unlockedSkills.clear()

        // Pay cost
        divineEssence -= getRespecCost()
        lastRespecTime = System.currentTimeMillis()

        // Reset Zeitdilatation state
        zeitdilatationActive = false
        zeitdilatationEndTime = 0L

        return true
    }

    // Remove stat effects when respeccing
    private fun removeSkillEffects(skill: UniversalSkill) {
        characterStats?.let { stats ->
            when (skill) {
                UniversalSkill.ROBUSTHEIT_I -> stats.maxHP -= 10
                UniversalSkill.ROBUSTHEIT_II -> stats.maxHP -= 20
                UniversalSkill.ROBUSTHEIT_III -> stats.maxHP -= 30
                UniversalSkill.ROBUSTHEIT_IV -> stats.maxHP -= 50
                UniversalSkill.ROBUSTHEIT_V -> stats.maxHP -= 75
                UniversalSkill.RUESTUNGS_TRAINING_I -> stats.armorBonus -= 1
                UniversalSkill.MANA_POOL_I -> stats.maxMana -= 10
                UniversalSkill.MANA_POOL_II -> stats.maxMana -= 20
                UniversalSkill.MANA_POOL_III -> stats.maxMana -= 30
                UniversalSkill.MANA_POOL_IV -> stats.maxMana -= 50
                UniversalSkill.SCHNELLE_REGENERATION -> { /* Removed dynamically */ }
                else -> {}
            }
            // Ensure stats don't go negative
            stats.maxHP = stats.maxHP.coerceAtLeast(1)
            stats.maxMana = stats.maxMana.coerceAtLeast(0)
            stats.armorBonus = stats.armorBonus.coerceAtLeast(0)
        }
    }

    // Get unlocked skills (for UI)
    fun getUnlockedSkills(): Set<UniversalSkill> = unlockedSkills.toSet()

    // Get all available skills for current level
    fun getAvailableSkills(): List<UniversalSkill> {
        val characterLevel = characterStats?.level ?: 1
        return UniversalSkill.values().filter {
            it.isUnlockable(characterLevel, unlockedSkills)
        }
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

        // RPG System - Character Stats (D&D 5e)
        characterStats?.let { stats ->
            // D&D Attributes
            editor.putInt("char_strength", stats.strength)
            editor.putInt("char_dexterity", stats.dexterity)
            editor.putInt("char_constitution", stats.constitution)
            editor.putInt("char_intelligence", stats.intelligence)
            editor.putInt("char_wisdom", stats.wisdom)
            editor.putInt("char_charisma", stats.charisma)

            // Level & XP
            editor.putInt("char_level", stats.level)
            editor.putInt("char_experience", stats.experience)

            // HP
            editor.putInt("char_maxHP", stats.maxHP)
            editor.putInt("char_currentHP", stats.currentHP)
            editor.putInt("char_temporaryHP", stats.temporaryHP)

            // Mana
            editor.putInt("char_maxMana", stats.maxMana)
            editor.putInt("char_currentMana", stats.currentMana)

            // AC & Initiative
            editor.putInt("char_baseAC", stats.baseAC)
            editor.putInt("char_armorBonus", stats.armorBonus)
            editor.putInt("char_initiativeBonus", stats.initiativeBonus)

            // Proficient Skills (als komma-separierter String)
            val skillsString = stats.proficientSkills.joinToString(",") { it.name }
            editor.putString("char_proficientSkills", skillsString)
        }

        // RPG System - Equipped Items
        EquipmentSlot.values().forEach { slot ->
            equippedItems[slot]?.let { item ->
                editor.putString("equipped_${slot.name}_set", item.set.name)
                editor.putString("equipped_${slot.name}_rarity", item.rarity.name)
                editor.putInt("equipped_${slot.name}_tier", item.tier)
            }
        }

        // RPG System - Inventory
        editor.putInt("inventory_size", inventory.size)
        inventory.forEachIndexed { index, item ->
            editor.putString("inventory_${index}_slot", item.slot.name)
            editor.putString("inventory_${index}_set", item.set.name)
            editor.putString("inventory_${index}_rarity", item.rarity.name)
            editor.putInt("inventory_${index}_tier", item.tier)
        }

        // RPG System - Loadout (5 normale + 1 ultimate)
        for (i in 0 until 5) {
            editor.putString("loadout_normal_$i", characterLoadout.getNormalAbility(i)?.name)
        }
        editor.putString("loadout_ultimate", characterLoadout.ultimateAbility?.name)

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

        // RPG System - Character Stats (D&D 5e)
        if (prefs.contains("char_level")) {
            val stats = CharacterStats(
                // D&D Attributes
                strength = prefs.getInt("char_strength", 10),
                dexterity = prefs.getInt("char_dexterity", 10),
                constitution = prefs.getInt("char_constitution", 10),
                intelligence = prefs.getInt("char_intelligence", 10),
                wisdom = prefs.getInt("char_wisdom", 10),
                charisma = prefs.getInt("char_charisma", 10),

                // Level & XP
                level = prefs.getInt("char_level", 1),
                experience = prefs.getInt("char_experience", 0),

                // HP
                maxHP = prefs.getInt("char_maxHP", 10),
                currentHP = prefs.getInt("char_currentHP", 10),
                temporaryHP = prefs.getInt("char_temporaryHP", 0),

                // Mana
                maxMana = prefs.getInt("char_maxMana", 0),
                currentMana = prefs.getInt("char_currentMana", 0),

                // AC & Initiative
                baseAC = prefs.getInt("char_baseAC", 10),
                armorBonus = prefs.getInt("char_armorBonus", 0),
                initiativeBonus = prefs.getInt("char_initiativeBonus", 0)
            )

            // Load proficient skills
            val skillsString = prefs.getString("char_proficientSkills", "")
            if (!skillsString.isNullOrEmpty()) {
                skillsString.split(",").forEach { skillName ->
                    try {
                        val skill = DndSkill.valueOf(skillName)
                        stats.proficientSkills.add(skill)
                    } catch (e: IllegalArgumentException) {
                        // Skip invalid skill
                    }
                }
            }

            characterStats = stats
        }

        // RPG System - Equipped Items
        equippedItems.clear()
        EquipmentSlot.values().forEach { slot ->
            val setName = prefs.getString("equipped_${slot.name}_set", null)
            if (setName != null) {
                try {
                    val set = EquipmentSet.valueOf(setName)
                    val rarity = EquipmentRarity.valueOf(
                        prefs.getString("equipped_${slot.name}_rarity", "GRAU") ?: "GRAU"
                    )
                    val tier = prefs.getInt("equipped_${slot.name}_tier", 1)
                    equippedItems[slot] = Equipment(slot, set, rarity, tier)
                } catch (e: IllegalArgumentException) {
                    // Skip invalid equipment
                }
            }
        }

        // RPG System - Inventory
        inventory.clear()
        val inventorySize = prefs.getInt("inventory_size", 0)
        for (i in 0 until inventorySize) {
            try {
                val slotName = prefs.getString("inventory_${i}_slot", null) ?: continue
                val setName = prefs.getString("inventory_${i}_set", null) ?: continue
                val rarityName = prefs.getString("inventory_${i}_rarity", "GRAU") ?: "GRAU"
                val tier = prefs.getInt("inventory_${i}_tier", 1)

                val slot = EquipmentSlot.valueOf(slotName)
                val set = EquipmentSet.valueOf(setName)
                val rarity = EquipmentRarity.valueOf(rarityName)

                inventory.add(Equipment(slot, set, rarity, tier))
            } catch (e: IllegalArgumentException) {
                // Skip invalid item
            }
        }

        // RPG System - Loadout (5 normale + 1 ultimate)
        val normalAbilities = mutableListOf<PaladinAbility?>(null, null, null, null, null)
        for (i in 0 until 5) {
            val abilityName = prefs.getString("loadout_normal_$i", null)
            normalAbilities[i] = abilityName?.let {
                try { PaladinAbility.valueOf(it) } catch (e: IllegalArgumentException) { null }
            }
        }

        val ultimateName = prefs.getString("loadout_ultimate", null)
        val ultimateAbility = ultimateName?.let {
            try { PaladinAbility.valueOf(it) } catch (e: IllegalArgumentException) { null }
        }

        characterLoadout = CharacterLoadout(
            normalAbilities = normalAbilities,
            ultimateAbility = ultimateAbility
        )

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
// ==================== RPG SYSTEM ====================

// Equipment Rarity (5 Stufen, kann kombiniert werden: 3x GRAU = 1x WEISS, etc.)
enum class EquipmentRarity(val displayName: String, val color: Int, val combineCount: Int) {
    GRAU("Grau", android.graphics.Color.GRAY, 3),
    WEISS("Weiß", android.graphics.Color.WHITE, 3),
    GRUEN("Grün", android.graphics.Color.rgb(50, 205, 50), 3),
    BLAU("Blau", android.graphics.Color.rgb(30, 144, 255), 3),
    LILA("Lila", android.graphics.Color.rgb(138, 43, 226), 3),
    GELB("Gold", android.graphics.Color.rgb(255, 215, 0), 0); // Maximale Rarity (6. Stufe)

    fun next(): EquipmentRarity? = when(this) {
        GRAU -> WEISS
        WEISS -> GRUEN
        GRUEN -> BLAU
        BLAU -> LILA
        LILA -> GELB
        GELB -> null  // Max erreicht
    }

    // Multiplier für Stats (1, 1.5, 2.5, 4, 7, 10)
    fun getMultiplier(): Double = when(this) {
        GRAU -> 1.0
        WEISS -> 1.5
        GRUEN -> 2.5
        BLAU -> 4.0
        LILA -> 7.0
        GELB -> 10.0
    }

    // Max upgrade level for this rarity
    fun getMaxUpgradeLevel(): Int = when(this) {
        GRAU -> 10    // Upgrades 1-10
        WEISS -> 20   // Upgrades 1-20
        GRUEN -> 35   // Upgrades 1-35
        BLAU -> 50    // Upgrades 1-50
        LILA -> 75    // Upgrades 1-75
        GELB -> 100   // Upgrades 1-100
    }
}

// Equipment Slot
enum class EquipmentSlot {
    WEAPON,     // Waffe
    ARMOR,      // Rüstung
    ACCESSORY   // Accessoire
}

// Equipment Set für Klasse
enum class EquipmentSet(
    val displayName: String,
    val description: String,
    val hiddenEffect: String? = null
) {
    // Paladin Sets
    PALADIN_SET1("Heiliger Beschützer", "Fokus auf Tank/Defense, hohe Rüstung, kann mehr aushalten"),
    PALADIN_SET2("Lichträcher", "Fokus auf heiligen Schaden, Balance zwischen Tank und Damage"),
    PALADIN_SET3("Heilung", "Fokus auf Heilung, erhöhte Heilung, kann auch andere heilen",
        "🩹 Hidden: Heilt deinen Schaden an Verbündete (Lifesteal für Team)"),

    // Barbar Sets
    BARBAR_SET1("Unstoppable Tank", "Fokus auf massiven HP-Pool und Damage Reduction"),
    BARBAR_SET2("Berserker Rage", "Fokus auf rohen Schaden - Glaskanone mit maximalem Damage"),
    BARBAR_SET3("Blutdurst Krieger", "Fokus auf Selbstheilung durch Lifesteal",
        "🩸 Hidden: Heile dich für 25% deines verursachten Schadens");

    fun hasHiddenEffect(): Boolean = hiddenEffect != null

    // Get set bonuses for this set
    fun getSetBonuses(pieceCount: Int): SetBonus? {
        if (pieceCount < 2) return null  // Mindestens 2 Teile für Set-Bonus

        return when(this) {
            // Paladin Sets
            PALADIN_SET1 -> SetBonus(  // Tank Set
                damageBonus = 0,
                healingBonus = 0,
                damageReduction = 10 + (pieceCount - 2) * 5,  // 10% bei 2, 15% bei 3
                maxHPBonus = 15 + (pieceCount - 2) * 10       // 15% bei 2, 25% bei 3
            )
            PALADIN_SET2 -> SetBonus(  // Damage Set
                damageBonus = 15 + (pieceCount - 2) * 10,     // 15% bei 2, 25% bei 3
                healingBonus = 0,
                damageReduction = 0,
                maxHPBonus = 0
            )
            PALADIN_SET3 -> SetBonus(  // Healing Set
                damageBonus = 0,
                healingBonus = 20 + (pieceCount - 2) * 15,    // 20% bei 2, 35% bei 3
                damageReduction = 0,
                maxHPBonus = 0
            )
            // Barbar Sets
            BARBAR_SET1 -> SetBonus(  // Tank Set (mehr HP + DR als Paladin)
                damageBonus = 0,
                healingBonus = 0,
                damageReduction = 15 + (pieceCount - 2) * 5,  // 15% bei 2, 20% bei 3
                maxHPBonus = 25 + (pieceCount - 2) * 15       // 25% bei 2, 40% bei 3
            )
            BARBAR_SET2 -> SetBonus(  // Glaskanone (massiv Damage)
                damageBonus = 30 + (pieceCount - 2) * 15,     // 30% bei 2, 45% bei 3
                healingBonus = 0,
                damageReduction = 0,
                maxHPBonus = 0
            )
            BARBAR_SET3 -> SetBonus(  // Lifesteal Set
                damageBonus = 10 + (pieceCount - 2) * 5,      // 10% bei 2, 15% bei 3
                healingBonus = 0,
                damageReduction = 0,
                maxHPBonus = 10 + (pieceCount - 2) * 5        // 10% bei 2, 15% bei 3
            )
        }
    }
}

// Set Bonus Stats
data class SetBonus(
    val damageBonus: Int = 0,          // % Damage increase
    val healingBonus: Int = 0,         // % Healing increase
    val damageReduction: Int = 0,      // % Damage reduction
    val maxHPBonus: Int = 0            // % Max HP increase
)

// Equipment Stats Container
data class EquipmentStats(
    var acBonus: Int = 0,
    var hpBonus: Int = 0,
    var manaBonus: Int = 0,
    var weaponDamage: Int = 0,
    var strengthBonus: Int = 0,
    var dexterityBonus: Int = 0,
    var constitutionBonus: Int = 0,
    var intelligenceBonus: Int = 0,
    var wisdomBonus: Int = 0,
    var charismaBonus: Int = 0,
    var healingPowerPercent: Int = 0,  // Percentage bonus
    var critChancePercent: Int = 0      // Percentage bonus
)

// Equipment Item (mit vollständigen Stats)
data class Equipment(
    val slot: EquipmentSlot,
    val set: EquipmentSet,
    var rarity: EquipmentRarity = EquipmentRarity.GRAU,
    var tier: Int = 1  // Stufe 1-100 (abhängig von Rarity)
) {
    fun canUpgrade(): Boolean = tier < rarity.getMaxUpgradeLevel()
    fun canCombine(): Boolean = rarity != EquipmentRarity.GELB

    // Get rarity multiplier (1.0, 1.5, 2.5, 4.0, 7.0, 10.0)
    private fun getRarityMultiplier(): Double = rarity.getMultiplier()

    // Calculate all stats for this equipment piece
    fun getStats(): EquipmentStats {
        val stats = EquipmentStats()
        val rarityMult = getRarityMultiplier()

        when(set) {
            // SET 1: Heiliger Beschützer (Tank)
            EquipmentSet.PALADIN_SET1 -> when(slot) {
                EquipmentSlot.WEAPON -> {  // Schild des Glaubens
                    stats.acBonus = (2 + (rarityMult * 0.5) + (tier * 0.2)).toInt()
                    stats.hpBonus = (10 + (rarityMult * 5) + (tier * 3)).toInt()
                }
                EquipmentSlot.ARMOR -> {  // Plattenrüstung des Wächters
                    stats.acBonus = (8 + (rarityMult * 1.0) + (tier * 0.5)).toInt()
                    stats.hpBonus = (15 + (rarityMult * 8) + (tier * 5)).toInt()
                    stats.constitutionBonus = (2 + (rarityMult / 8)).toInt()
                }
                EquipmentSlot.ACCESSORY -> {  // Ring der Unnachgiebigkeit
                    stats.hpBonus = (5 + (rarityMult * 3) + (tier * 2)).toInt()
                    stats.strengthBonus = (1 + (rarityMult / 16)).toInt()
                    stats.constitutionBonus = (1 + (rarityMult / 16)).toInt()
                }
            }

            // SET 2: Lichträcher (Balance)
            EquipmentSet.PALADIN_SET2 -> when(slot) {
                EquipmentSlot.WEAPON -> {  // Hammer der Vergeltung
                    stats.weaponDamage = (15 + (rarityMult * 2) + (tier * 1.5)).toInt()
                    stats.strengthBonus = (2 + (rarityMult / 8)).toInt()
                    stats.charismaBonus = (1 + (rarityMult / 16)).toInt()
                }
                EquipmentSlot.ARMOR -> {  // Brustplatte des Lichtbringers
                    stats.acBonus = (5 + (rarityMult * 0.5) + (tier * 0.3)).toInt()
                    stats.hpBonus = (10 + (rarityMult * 5) + (tier * 3)).toInt()
                    stats.strengthBonus = (2 + (rarityMult / 10)).toInt()
                    stats.charismaBonus = (2 + (rarityMult / 10)).toInt()
                }
                EquipmentSlot.ACCESSORY -> {  // Heiliges Symbol der Rache
                    stats.weaponDamage = (10 + (rarityMult * 1.5) + tier).toInt()
                    stats.charismaBonus = (2 + (rarityMult / 8)).toInt()
                    stats.critChancePercent = (5 + (rarityMult / 4)).toInt()
                }
            }

            // SET 3: Heilung (Support/Healer)
            EquipmentSet.PALADIN_SET3 -> when(slot) {
                EquipmentSlot.WEAPON -> {  // Stab des Lebens
                    stats.weaponDamage = (5 + (rarityMult * 0.5) + (tier * 0.5)).toInt()
                    stats.manaBonus = (20 + (rarityMult * 3) + (tier * 2)).toInt()
                    stats.wisdomBonus = (2 + (rarityMult / 8)).toInt()
                    stats.healingPowerPercent = (15 + (rarityMult / 2)).toInt()
                }
                EquipmentSlot.ARMOR -> {  // Robe der Barmherzigkeit
                    stats.acBonus = (2 + (rarityMult * 0.2) + (tier * 0.1)).toInt()
                    stats.manaBonus = (25 + (rarityMult * 4) + (tier * 3)).toInt()
                    stats.wisdomBonus = (2 + (rarityMult / 10)).toInt()
                    stats.charismaBonus = (2 + (rarityMult / 10)).toInt()
                    stats.healingPowerPercent = (10 + (rarityMult / 2)).toInt()
                }
                EquipmentSlot.ACCESSORY -> {  // Amulett der Erneuerung
                    stats.manaBonus = (15 + (rarityMult * 2) + (tier * 1.5)).toInt()
                    stats.wisdomBonus = (1 + (rarityMult / 16)).toInt()
                    stats.charismaBonus = (1 + (rarityMult / 16)).toInt()
                    stats.healingPowerPercent = (10 + (rarityMult / 4)).toInt()
                }
            }

            // BARBAR SET 1: Unstoppable Tank (massiver HP-Pool + DR)
            EquipmentSet.BARBAR_SET1 -> when(slot) {
                EquipmentSlot.WEAPON -> {  // Kriegsaxt des Titanen
                    stats.weaponDamage = (18 + (rarityMult * 2) + (tier * 1.5)).toInt()
                    stats.hpBonus = (15 + (rarityMult * 6) + (tier * 4)).toInt()
                    stats.strengthBonus = (3 + (rarityMult / 6)).toInt()
                }
                EquipmentSlot.ARMOR -> {  // Barbarenrüstung der Unsterblichkeit
                    stats.acBonus = (6 + (rarityMult * 0.8) + (tier * 0.4)).toInt()
                    stats.hpBonus = (30 + (rarityMult * 12) + (tier * 8)).toInt()
                    stats.constitutionBonus = (3 + (rarityMult / 6)).toInt()
                }
                EquipmentSlot.ACCESSORY -> {  // Totem der Zähigkeit
                    stats.hpBonus = (20 + (rarityMult * 8) + (tier * 5)).toInt()
                    stats.constitutionBonus = (2 + (rarityMult / 8)).toInt()
                }
            }

            // BARBAR SET 2: Berserker Rage (Glaskanone - massiver Damage)
            EquipmentSet.BARBAR_SET2 -> when(slot) {
                EquipmentSlot.WEAPON -> {  // Zweihandaxt der Vernichtung
                    stats.weaponDamage = (25 + (rarityMult * 3.5) + (tier * 2.5)).toInt()
                    stats.strengthBonus = (4 + (rarityMult / 5)).toInt()
                    stats.critChancePercent = (8 + (rarityMult / 3)).toInt()
                }
                EquipmentSlot.ARMOR -> {  // Leichte Berserkerrüstung
                    stats.acBonus = (3 + (rarityMult * 0.3) + (tier * 0.2)).toInt()
                    stats.weaponDamage = (15 + (rarityMult * 2) + tier).toInt()
                    stats.strengthBonus = (3 + (rarityMult / 8)).toInt()
                }
                EquipmentSlot.ACCESSORY -> {  // Ring des Zorns
                    stats.weaponDamage = (18 + (rarityMult * 2.5) + (tier * 1.5)).toInt()
                    stats.strengthBonus = (3 + (rarityMult / 6)).toInt()
                    stats.critChancePercent = (10 + (rarityMult / 3)).toInt()
                }
            }

            // BARBAR SET 3: Blutdurst Krieger (Lifesteal/Selbstheilung)
            EquipmentSet.BARBAR_SET3 -> when(slot) {
                EquipmentSlot.WEAPON -> {  // Klinge des Blutsaugers
                    stats.weaponDamage = (20 + (rarityMult * 2.5) + (tier * 2)).toInt()
                    stats.hpBonus = (10 + (rarityMult * 5) + (tier * 3)).toInt()
                    stats.strengthBonus = (2 + (rarityMult / 10)).toInt()
                }
                EquipmentSlot.ARMOR -> {  // Rüstung des Blutkriegers
                    stats.acBonus = (5 + (rarityMult * 0.5) + (tier * 0.3)).toInt()
                    stats.hpBonus = (20 + (rarityMult * 8) + (tier * 5)).toInt()
                    stats.constitutionBonus = (2 + (rarityMult / 10)).toInt()
                }
                EquipmentSlot.ACCESSORY -> {  // Amulett der Vampirkraft
                    stats.hpBonus = (15 + (rarityMult * 6) + (tier * 4)).toInt()
                    stats.constitutionBonus = (1 + (rarityMult / 16)).toInt()
                    stats.strengthBonus = (1 + (rarityMult / 16)).toInt()
                }
            }
        }

        return stats
    }

    // Legacy function for compatibility
    fun getStatValue(): Int = getStats().hpBonus + getStats().acBonus
}

// Ability Type
enum class AbilityType {
    COMBAT,  // Rundenbasierter Cooldown
    SPELL    // Mana-basiert
}

// Ability Category
enum class AbilityCategory {
    NORMAL,   // Normale Fähigkeit (10 verfügbar)
    ULTIMATE  // Ultimate (3 verfügbar)
}

// Paladin Abilities (mit Level-Skalierung und Requirements)
enum class PaladinAbility(
    val displayName: String,
    val description: String,
    val type: AbilityType,
    val category: AbilityCategory,
    val levelRequirement: Int,  // Welches Level benötigt um freizuschalten
    val cost: Int,  // Cooldown in Runden (COMBAT) oder Mana (SPELL)
    val baseDamage: Int = 0,
    val baseHealing: Int = 0,
    val baseDuration: Int = 0  // Dauer in Runden für Buffs/Debuffs
) {
    // START-SKILLS (Level 1) - 3 Stück
    SCHILDSCHLAG("Schildschlag", "Schlägt mit dem Schild, macht moderaten Schaden + betäubt",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 1, 2, baseDamage = 25, baseDuration = 1),

    HEILENDES_LICHT("Heilendes Licht", "Heilt dich oder Verbündeten",
        AbilityType.SPELL, AbilityCategory.NORMAL, 1, 30, baseHealing = 40),

    VERTEIDIGUNGSHALTUNG("Verteidigungshaltung", "Reduziert Schaden um 50%",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 1, 4, baseDuration = 2),

    // Level 3
    HEILIGER_HAMMER("Heiliger Hammer", "Schwerer Angriff, hoher Schaden, ignoriert Rüstung",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 3, 3, baseDamage = 50),

    // Level 5
    SEGEN_DER_STAERKE("Segen der Stärke", "Erhöht Angriffskraft um 30% (+1% pro Level)",
        AbilityType.SPELL, AbilityCategory.NORMAL, 5, 25, baseDuration = 3),

    // Level 7
    VERGELTUNGSSCHLAG("Vergeltungsschlag", "Kontert letzten Schaden und gibt 150% zurück",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 7, 3, baseDamage = 0),

    // Level 10
    PROVOKATION("Provokation", "Zwingt Gegner dich anzugreifen",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 10, 5, baseDuration = 2),

    // Level 12
    GOETTLICHER_SCHUTZ("Göttlicher Schutz", "Gewährt Schutzschild (absorbiert Schaden)",
        AbilityType.SPELL, AbilityCategory.NORMAL, 12, 40, baseDuration = 3),

    // Level 15
    REINIGUNG("Reinigung", "Entfernt alle negativen Effekte",
        AbilityType.SPELL, AbilityCategory.NORMAL, 15, 35),

    // Level 18
    AURA_DER_RECHTSCHAFFENHEIT("Aura der Rechtschaffenheit", "Heilt dich und Verbündete pro Runde",
        AbilityType.SPELL, AbilityCategory.NORMAL, 18, 50, baseHealing = 5, baseDuration = 4),

    // EXTENDED SKILLS (Level 20-100)
    // Level 22
    HEILIGER_SCHUTZSCHILD("Heiliger Schutzschild", "Gewährt Verbündeten temporäre HP",
        AbilityType.SPELL, AbilityCategory.NORMAL, 22, 35, baseHealing = 30),

    // Level 25
    LICHTEXPLOSION("Lichtexplosion", "AOE Schaden + blendet Gegner",
        AbilityType.SPELL, AbilityCategory.NORMAL, 25, 40, baseDamage = 40, baseDuration = 2),

    // Level 28
    SEGEN_DER_VERTEIDIGUNG("Segen der Verteidigung", "+20% Armor für 3 Runden",
        AbilityType.SPELL, AbilityCategory.NORMAL, 28, 30, baseDuration = 3),

    // Level 32
    HEILIGE_RACHE("Heilige Rache", "Nächster Angriff macht 200% Schaden",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 32, 3, baseDamage = 60),

    // Level 35
    MASSENHEILUNG("Massenheilung", "Heilt alle Verbündeten",
        AbilityType.SPELL, AbilityCategory.NORMAL, 35, 55, baseHealing = 35),

    // Level 40
    HEILIGES_GERICHT("Heiliges Gericht", "Richtet Gegner - ignoriert Rüstung + Heilung",
        AbilityType.SPELL, AbilityCategory.NORMAL, 40, 60, baseDamage = 70, baseHealing = 20),

    // Level 45
    WALL_OF_FAITH("Wall of Faith", "Reduziert allen Schaden um 75% für 2 Runden",
        AbilityType.SPELL, AbilityCategory.NORMAL, 45, 50, baseDuration = 2),

    // Level 50
    GOETTLICHER_ZORN("Göttlicher Zorn", "Massiver Schaden + brennt Gegner",
        AbilityType.SPELL, AbilityCategory.NORMAL, 50, 65, baseDamage = 90, baseDuration = 3),

    // Level 55
    SEGEN_DER_EILE("Segen der Eile", "+2 Initiative + Extra-Aktion",
        AbilityType.SPELL, AbilityCategory.NORMAL, 55, 45, baseDuration = 2),

    // Level 60
    HEILIGE_AEGIS("Heilige Aegis", "Unverwundbar gegen nächsten Angriff + reflektiert 50%",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 60, 4, baseDuration = 1),

    // Level 65
    LICHT_DER_HOFFNUNG("Licht der Hoffnung", "Wiederbelebt bei Tod (1x pro Kampf)",
        AbilityType.SPELL, AbilityCategory.NORMAL, 65, 80, baseHealing = 50),

    // Level 70
    HEILIGER_KREIS("Heiliger Kreis", "AOE Heilung + Schaden an Untoten",
        AbilityType.SPELL, AbilityCategory.NORMAL, 70, 70, baseDamage = 60, baseHealing = 40),

    // Level 75
    SEGEN_DES_LICHTS("Segen des Lichts", "Alle Heilungen +100% für 3 Runden",
        AbilityType.SPELL, AbilityCategory.NORMAL, 75, 55, baseDuration = 3),

    // Level 80
    CHAMPION_DES_LICHTS("Champion des Lichts", "+50% Schaden + Immunität gegen Debuffs",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 80, 5, baseDuration = 3),

    // Level 85
    GOETTLICHE_INTERVENTION_II("Göttliche Intervention II", "Vollheilung + entfernt Tod (besser als Lv30)",
        AbilityType.SPELL, AbilityCategory.NORMAL, 85, 100, baseHealing = 999, baseDuration = 4),

    // Level 90
    HEILIGE_VERWANDLUNG("Heilige Verwandlung", "Wird zum Engelkrieger - +100% alle Stats",
        AbilityType.SPELL, AbilityCategory.NORMAL, 90, 90, baseDuration = 4),

    // Level 95
    URTEIL_DER_GOETTER("Urteil der Götter", "Massiver AOE - löscht Untote sofort aus",
        AbilityType.SPELL, AbilityCategory.NORMAL, 95, 85, baseDamage = 200),

    // Level 100 (Capstone)
    AVATAR_DES_LICHTS("Avatar des Lichts", "Wird zur Licht-Inkarnation - 10 Runden Gottmodus",
        AbilityType.SPELL, AbilityCategory.NORMAL, 100, 120, baseDuration = 10),

    // ULTIMATES
    // Level 10 - Erste Ulti
    UNERSCHUETTERLICHE_FESTUNG("Unerschütterliche Festung", "4 Runden UNVERWUNDBAR + zieht alle Angriffe + heilt 10% HP/Runde",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 10, 1, baseDuration = 4),

    // Level 20 - Zweite Ulti
    URTEIL_DES_LICHTS("Urteil des Lichts", "Massiver heiliger AOE-Schaden + heilt Verbündete für 50% des Schadens",
        AbilityType.SPELL, AbilityCategory.ULTIMATE, 20, 1, baseDamage = 100),

    // Level 30 - Dritte Ulti
    GOETTLICHE_INTERVENTION("Göttliche Intervention", "Vollheilung aller + entfernt alle Debuffs + Immunität 3 Runden",
        AbilityType.SPELL, AbilityCategory.ULTIMATE, 30, 1, baseHealing = 999, baseDuration = 3),

    // Level 50 - Vierte Ulti
    HEILIGE_RACHE_ULT("Göttliche Vergeltung", "Tötet Gegner unter 30% HP sofort + heilt dich für Overkill",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 50, 1, baseDamage = 150, baseHealing = 50),

    // Level 75 - Fünfte Ulti
    ERLOESUNGSSCHLAG("Erlösungsschlag", "One-Hit-Kill + resurrect gefallene Verbündete",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 75, 1, baseDamage = 500, baseHealing = 100),

    // Level 100 - Ultimate Capstone
    GOTTES_ZORN("Gottes Zorn", "Löscht alle Gegner aus + Vollheilung aller + 5 Runden Immunität",
        AbilityType.SPELL, AbilityCategory.ULTIMATE, 100, 1, baseDamage = 999, baseHealing = 999, baseDuration = 5);

    fun isNormal(): Boolean = category == AbilityCategory.NORMAL
    fun isUltimate(): Boolean = category == AbilityCategory.ULTIMATE

    // Berechne skalierten Schaden basierend auf Character Level
    fun getDamage(characterLevel: Int): Int {
        if (baseDamage == 0) return 0
        // Formel: Base + (Level × 2.5)
        return (baseDamage + (characterLevel * 2.5)).toInt()
    }

    // Berechne skalierte Heilung basierend auf Character Level
    fun getHealing(characterLevel: Int): Int {
        if (baseHealing == 0) return 0
        if (baseHealing >= 999) return 999  // Vollheilung bleibt Vollheilung
        // Formel: Base + (Level × 2)
        return (baseHealing + (characterLevel * 2)).toInt()
    }

    // Berechne skalierte Duration basierend auf Character Level
    fun getDuration(characterLevel: Int): Int {
        if (baseDuration == 0) return 0
        // Formel: Base + (Level / 5) - alle 5 Level +1 Runde
        return baseDuration + (characterLevel / 5)
    }

    // Berechne Buff-Percentage (für Segen der Stärke etc.)
    fun getBuffPercentage(characterLevel: Int): Int {
        return when(this) {
            SEGEN_DER_STAERKE -> 30 + characterLevel  // 30% + 1% pro Level
            VERTEIDIGUNGSHALTUNG -> 50  // Fix 50% Damage Reduction
            VERGELTUNGSSCHLAG -> 150 + characterLevel  // 150% + 1% pro Level
            else -> 0
        }
    }

    // Check ob Skill bei diesem Level freigeschaltet ist
    fun isUnlockedAt(characterLevel: Int): Boolean {
        return characterLevel >= levelRequirement
    }
}

// ============================================================================
// PASSIVE ABILITIES SYSTEM
// ============================================================================

enum class PaladinPassive(
    val displayName: String,
    val description: String,
    val levelRequirement: Int
) {
    // Level 2 - Divine Smite
    DIVINE_SMITE(
        "Divine Smite",
        "Verbrauche Mana (15) um zusätzlichen Holy-Schaden zu machen (+2d8, scales mit Level)",
        2
    ),

    // Level 6 - Lay on Hands
    LAY_ON_HANDS(
        "Lay on Hands",
        "Heilungs-Pool (5 × Level HP). Regeneriert nach Rast. Kann als Bonus-Action genutzt werden",
        6
    ),

    // Level 10 - Aura of Protection
    AURA_OF_PROTECTION(
        "Aura of Protection",
        "Du und Verbündete im Umkreis erhalten +CHA-Mod zu Rettungswürfen",
        10
    ),

    // Level 11 - Improved Divine Smite
    IMPROVED_DIVINE_SMITE(
        "Improved Divine Smite",
        "Alle Nahkampf-Angriffe machen automatisch +1d8 Holy-Schaden",
        11
    ),

    // Level 14 - Cleansing Touch
    CLEANSING_TOUCH(
        "Cleansing Touch",
        "Kann CHA-Mod pro Tag negative Effekte von sich/Verbündeten entfernen (Bonus-Action)",
        14
    ),

    // Level 18 - Aura Improvements
    AURA_RADIUS_INCREASE(
        "Aura Expansion",
        "Aura-Reichweite erhöht sich auf 30ft (vorher 10ft)",
        18
    );

    fun isUnlockedAt(characterLevel: Int): Boolean {
        return characterLevel >= levelRequirement
    }

    fun getScaledValue(characterLevel: Int): Int {
        return when(this) {
            DIVINE_SMITE -> 2 + (characterLevel / 5)  // +1d8 alle 5 Level
            LAY_ON_HANDS -> 5 * characterLevel  // 5 HP pro Level
            AURA_OF_PROTECTION -> (characterLevel - 10) / 2  // Skaliert mit CHA
            IMPROVED_DIVINE_SMITE -> 1 + (characterLevel / 10)  // +1d8 alle 10 Level
            CLEANSING_TOUCH -> 3 + (characterLevel / 10)  // CHA-Mod + Bonus
            AURA_RADIUS_INCREASE -> 30  // Fixed 30ft
        }
    }

    fun getScaledDescription(characterLevel: Int): String {
        return when(this) {
            DIVINE_SMITE -> "$description [+${getScaledValue(characterLevel)}d8 Holy-Schaden]"
            LAY_ON_HANDS -> "$description [${getScaledValue(characterLevel)} HP Pool]"
            AURA_OF_PROTECTION -> "$description [+${getScaledValue(characterLevel)} zu Saves]"
            IMPROVED_DIVINE_SMITE -> "$description [+${getScaledValue(characterLevel)}d8 pro Hit]"
            CLEANSING_TOUCH -> "$description [${getScaledValue(characterLevel)}x pro Tag]"
            AURA_RADIUS_INCREASE -> description
        }
    }
}

// ============================================================================
// BARBAR ABILITIES
// ============================================================================

enum class BarbarAbility(
    val displayName: String,
    val description: String,
    val type: AbilityType,
    val category: AbilityCategory,
    val levelRequirement: Int,
    val cooldown: Int,
    val manaCost: Int = 0,  // Barbar nutzt Rage statt Mana
    val baseDamage: Int = 0,
    val baseHealing: Int = 0,
    val baseDuration: Int = 0
) {
    // ========== NORMAL SKILLS (28) ==========

    // Level 1
    WILDES_SCHLAGEN("Wildes Schlagen", "Wilder Nahkampfangriff mit erhöhtem Schaden",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 1, 2, baseDamage = 25),

    // Level 3
    WUTSCHREI("Wutschrei", "Schrei der Wut - AOE Schaden + aktiviert Rage",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 3, 4, baseDamage = 20),

    // Level 5
    BRUTALER_HIEB("Brutaler Hieb", "Verheerender Schlag mit massivem Schaden",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 5, 3, baseDamage = 40),

    // Level 7
    UNAUFHALTSAM("Unaufhaltsam", "Regeneriere HP über Zeit",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 7, 5, baseHealing = 15, baseDuration = 3),

    // Level 9
    ZORNESRAUSCH("Zornesrausch", "Erhöhe Schaden und erhalte Schadensresistenz",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 9, 5, baseDuration = 3),

    // Level 12
    BERSERKERWUT("Berserkerwut", "Schlage 2x zu in dieser Runde",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 12, 4, baseDamage = 30),

    // Level 14
    ERDERSCHUETTERUNG("Erderschütterung", "Schlage auf den Boden - AOE + Stun",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 14, 5, baseDamage = 35, baseDuration = 1),

    // Level 16
    TODESSTOSS("Todesstoß", "Execute-Angriff bei Gegnern unter 20% HP",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 16, 4, baseDamage = 50),

    // Level 18
    BLUTDURST("Blutdurst", "Lifesteal-Angriff - heile dich für 50% des Schadens",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 18, 3, baseDamage = 45),

    // Level 21
    KETTENANGRIFF("Kettenangriff", "Greife 3 Gegner gleichzeitig an",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 21, 5, baseDamage = 35),

    // Level 24
    TITANISCHE_STAERKE("Titanische Stärke", "+50% Schaden für 3 Runden",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 24, 6, baseDuration = 3),

    // Level 27
    UNVERWUNDBAR("Unverwundbar", "Erhalte massiven temporären HP-Schild",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 27, 5, baseHealing = 80),

    // Level 33
    FURCHTEINFLOESSEND("Furchteinflößend", "Schwäche alle Gegner (-30% Schaden)",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 33, 6, baseDuration = 3),

    // Level 36
    KRIEGSSCHREI("Kriegsschrei", "Buff alle Verbündeten (+30% Schaden)",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 36, 6, baseDuration = 3),

    // Level 39
    KNOCHENBRECHEND("Knochenbrechend", "Brich Rüstung des Gegners (-50% AC)",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 39, 4, baseDamage = 60),

    // Level 42
    RASEREI("Raserei", "Erhöhe Angriffsgeschwindigkeit massiv",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 42, 5, baseDuration = 2),

    // Level 45
    VERWUESTUNG("Verwüstung", "Massive AOE-Zerstörung",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 45, 6, baseDamage = 70),

    // Level 48
    URZEITLICHER_ZORN("Urzeitlicher Zorn", "Kanalisiere den Zorn der Urahnen",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 48, 6, baseDamage = 80, baseDuration = 3),

    // Level 52
    WELTENZERSTOERER("Weltenzerstörer", "Verheerender Einzelschlag",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 52, 5, baseDamage = 100),

    // Level 56
    UNSTERBLICH("Unsterblich", "Kann nicht sterben für 2 Runden",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 56, 8, baseDuration = 2),

    // Level 60
    RACHEGEIST("Rachegeist", "Konter-Angriff - reflektiere 100% Schaden",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 60, 4, baseDuration = 2),

    // Level 65
    TITANENBLUT("Titanenblut", "Erhöhe Max HP permanent um 20%",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 65, 8, baseHealing = 100),

    // Level 70
    BERSERKERINSTINKT("Berserkerinstinkt", "Auto-Ausweichen für 2 Runden",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 70, 6, baseDuration = 2),

    // Level 75
    VULKANFAUST("Vulkanfaust", "Feuer + Physical Hybrid-Schaden",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 75, 5, baseDamage = 120),

    // Level 80
    CHAOSBRECHER("Chaosbrecher", "Ignoriere alle Verteidigungen",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 80, 5, baseDamage = 140),

    // Level 85
    WELTENRICHTER("Weltenrichter", "Richte alle Gegner - massiver AOE",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 85, 7, baseDamage = 130),

    // Level 90
    RAGNAROEK("Ragnarök", "Das Ende der Welt - AOE + Burn + Stun",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 90, 8, baseDamage = 150, baseDuration = 2),

    // Level 95
    TITANENFORM("Titanenform", "Verwandle dich in einen Titanen (+100% Stats)",
        AbilityType.COMBAT, AbilityCategory.NORMAL, 95, 10, baseDuration = 4),

    // ========== ULTIMATES (6) ==========

    // Level 10
    URGEWALT("Urgewalt", "Entfessle die Urgewalt - Massiver Schaden + Rage",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 10, 10, baseDamage = 80),

    // Level 20
    BERSERKERWAHN("Berserkerwahn", "Totale Raserei - Multi-Attack + Lifesteal",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 20, 10, baseDamage = 100),

    // Level 30
    TITANENZORN("Titanenzorn", "Zorn der Titanen - AOE + Massive Buffs",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 30, 10, baseDamage = 130),

    // Level 50
    WELTENZERSCHMETTERER("Weltenzerschmetterer", "Zerschmettere die Welt selbst",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 50, 10, baseDamage = 180),

    // Level 75
    APOKALYPSE("Apokalypse", "Bringe die Apokalypse - Wipe All + Buff Self",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 75, 10, baseDamage = 220),

    // Level 100
    GOETTERSCHLAECHTER("Götterschlächter", "Schlächte die Götter selbst",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 100, 10, baseDamage = 300);

    // Check ob Normal Skill
    fun isNormal(): Boolean = category == AbilityCategory.NORMAL

    // Check ob Ultimate
    fun isUltimate(): Boolean = category == AbilityCategory.ULTIMATE

    // Calculate actual damage (scales with level)
    fun getDamage(characterLevel: Int): Int {
        if (baseDamage == 0) return 0
        return baseDamage + (characterLevel * 2)  // +2 damage per level
    }

    // Calculate actual healing (scales with level)
    fun getHealing(characterLevel: Int): Int {
        if (baseHealing == 0) return 0
        return baseHealing + (characterLevel / 2)  // +0.5 healing per level
    }

    // Calculate duration
    fun getDuration(characterLevel: Int): Int = baseDuration

    // Check if ability is unlocked at level
    fun isUnlockedAt(characterLevel: Int): Boolean {
        return characterLevel >= levelRequirement
    }
}

// ============================================================================
// BARBAR PASSIVE ABILITIES
// ============================================================================

enum class BarbarPassive(
    val displayName: String,
    val description: String,
    val levelRequirement: Int
) {
    // Level 2 - Rage
    RAGE(
        "Rage",
        "Aktiviere Rage: +Schaden, +Schadensresistenz, kostet keine Ressourcen. Hält 3 Runden",
        2
    ),

    // Level 3 - Unarmored Defense
    UNARMORED_DEFENSE(
        "Unarmored Defense",
        "Wenn du keine Rüstung trägst: AC = 10 + DEX-Mod + CON-Mod",
        3
    ),

    // Level 5 - Reckless Attack
    RECKLESS_ATTACK(
        "Reckless Attack",
        "Toggle: +30% Schaden, aber -10 AC. Riskantes Kämpfen für mehr Power",
        5
    ),

    // Level 9 - Brutal Critical
    BRUTAL_CRITICAL(
        "Brutal Critical",
        "Kritische Treffer machen +100% mehr Schaden (normal: 2x, brutal: 3x)",
        9
    ),

    // Level 11 - Fast Movement
    FAST_MOVEMENT(
        "Fast Movement",
        "+3 Initiative. Du bewegst dich schneller und schlägst früher zu",
        11
    ),

    // Level 14 - Relentless Rage
    RELENTLESS_RAGE(
        "Relentless Rage",
        "Während Rage aktiv: Kann nicht unter 1 HP fallen. Cheate den Tod!",
        14
    );

    fun isUnlockedAt(characterLevel: Int): Boolean {
        return characterLevel >= levelRequirement
    }

    fun getScaledValue(characterLevel: Int): Int {
        return when(this) {
            RAGE -> 3  // 3 Runden Duration
            UNARMORED_DEFENSE -> 10  // Base AC bonus
            RECKLESS_ATTACK -> 30  // +30% Damage
            BRUTAL_CRITICAL -> 100  // +100% Crit Damage
            FAST_MOVEMENT -> 3  // +3 Initiative
            RELENTLESS_RAGE -> 1  // Cannot die threshold
        }
    }

    fun getScaledDescription(characterLevel: Int): String {
        return when(this) {
            RAGE -> "$description [${getScaledValue(characterLevel)} Runden]"
            UNARMORED_DEFENSE -> "$description [+${getScaledValue(characterLevel)} Base AC]"
            RECKLESS_ATTACK -> "$description [+${getScaledValue(characterLevel)}% Schaden, -10 AC]"
            BRUTAL_CRITICAL -> "$description [+${getScaledValue(characterLevel)}% Krit-Schaden]"
            FAST_MOVEMENT -> "$description [+${getScaledValue(characterLevel)} Initiative]"
            RELENTLESS_RAGE -> description
        }
    }
}

// ==================== D&D 5E SYSTEM ====================

// D&D Attributes
enum class DndAttribute(val displayName: String, val shortName: String) {
    STRENGTH("Strength", "STR"),      // Nahkampfschaden, Tragkraft
    DEXTERITY("Dexterity", "DEX"),    // Fernkampf, Ausweichen, Initiative, AC
    CONSTITUTION("Constitution", "CON"), // HP, Ausdauer
    INTELLIGENCE("Intelligence", "INT"), // Magier-Zauber, Wissen
    WISDOM("Wisdom", "WIS"),          // Priester/Ranger-Zauber, Wahrnehmung
    CHARISMA("Charisma", "CHA");      // Paladin-Zauber, Überzeugung

    companion object {
        // Berechne Modifier aus Attribut-Wert (D&D 5e Formel)
        fun getModifier(score: Int): Int = (score - 10) / 2
    }
}

// Hit Dice für Klassen (D&D 5e)
enum class HitDice(val sides: Int, val displayName: String) {
    D6(6, "d6"),    // Magier - Glaskanone
    D8(8, "d8"),    // Priester, Jäger
    D10(10, "d10"), // Paladin
    D12(12, "d12"); // Barbar - Tank

    fun roll(): Int = (1..sides).random()
    fun average(): Int = (sides / 2) + 1
}

// D&D Skills
enum class DndSkill(
    val displayName: String,
    val attribute: DndAttribute
) {
    ATHLETICS("Athletics", DndAttribute.STRENGTH),
    ACROBATICS("Acrobatics", DndAttribute.DEXTERITY),
    SLEIGHT_OF_HAND("Sleight of Hand", DndAttribute.DEXTERITY),
    STEALTH("Stealth", DndAttribute.DEXTERITY),
    ARCANA("Arcana", DndAttribute.INTELLIGENCE),
    HISTORY("History", DndAttribute.INTELLIGENCE),
    INVESTIGATION("Investigation", DndAttribute.INTELLIGENCE),
    NATURE("Nature", DndAttribute.INTELLIGENCE),
    RELIGION("Religion", DndAttribute.INTELLIGENCE),
    ANIMAL_HANDLING("Animal Handling", DndAttribute.WISDOM),
    INSIGHT("Insight", DndAttribute.WISDOM),
    MEDICINE("Medicine", DndAttribute.WISDOM),
    PERCEPTION("Perception", DndAttribute.WISDOM),
    SURVIVAL("Survival", DndAttribute.WISDOM),
    DECEPTION("Deception", DndAttribute.CHARISMA),
    INTIMIDATION("Intimidation", DndAttribute.CHARISMA),
    PERFORMANCE("Performance", DndAttribute.CHARISMA),
    PERSUASION("Persuasion", DndAttribute.CHARISMA);
}

// Player Class (D&D 5e)
enum class PlayerClass(
    val displayName: String,
    val description: String,
    val emoji: String,
    val hitDice: HitDice,
    val baseHP: Int, // Level 1 HP
    val castingStat: DndAttribute?, // Welches Attribut für Zauber (null = keine Magie)
    val savingThrowProficiencies: List<DndAttribute>,
    val skillProficiencies: List<DndSkill>
) {
    PALADIN(
        "Paladin",
        "Heiliger Krieger - Tank mit Heilmagie",
        "🛡️",
        HitDice.D10,
        12, // 10 + CON-Mod (+2)
        DndAttribute.CHARISMA, // Paladin nutzt CHA für Zauber!
        listOf(DndAttribute.WISDOM, DndAttribute.CHARISMA),
        listOf(DndSkill.ATHLETICS, DndSkill.RELIGION, DndSkill.PERSUASION)
    ),

    BARBAR(
        "Barbar",
        "Berserker - Brutale DPS, KEINE Magie!",
        "⚔️",
        HitDice.D12,
        15, // 12 + CON-Mod (+3)
        null, // KEINE MAGIE!
        listOf(DndAttribute.STRENGTH, DndAttribute.CONSTITUTION),
        listOf(DndSkill.ATHLETICS, DndSkill.INTIMIDATION, DndSkill.SURVIVAL)
    ),

    JAEGER(
        "Jäger",
        "Ranger - Mobiler Fernkämpfer mit Naturmagie",
        "🏹",
        HitDice.D10,
        12, // 10 + CON-Mod (+2)
        DndAttribute.WISDOM, // Jäger nutzt WIS für Naturmagie
        listOf(DndAttribute.STRENGTH, DndAttribute.DEXTERITY),
        listOf(DndSkill.STEALTH, DndSkill.SURVIVAL, DndSkill.PERCEPTION, DndSkill.NATURE)
    ),

    MAGIER(
        "Magier",
        "Wizard - Glaskanone mit mächtiger Magie",
        "🔮",
        HitDice.D6,
        6, // 6 + CON-Mod (+0)
        DndAttribute.INTELLIGENCE, // Magier nutzt INT
        listOf(DndAttribute.INTELLIGENCE, DndAttribute.WISDOM),
        listOf(DndSkill.ARCANA, DndSkill.HISTORY, DndSkill.INVESTIGATION)
    ),

    PRIESTER(
        "Priester",
        "Cleric - Heiler mit göttlicher Magie",
        "✨",
        HitDice.D8,
        10, // 8 + CON-Mod (+2)
        DndAttribute.WISDOM, // Priester nutzt WIS
        listOf(DndAttribute.WISDOM, DndAttribute.CHARISMA),
        listOf(DndSkill.MEDICINE, DndSkill.RELIGION, DndSkill.INSIGHT)
    );

    // D&D 5e Base Attributes für diese Klasse
    fun getBaseAttributes(): Map<DndAttribute, Int> = when(this) {
        PALADIN -> mapOf(
            DndAttribute.STRENGTH to 16,
            DndAttribute.DEXTERITY to 10,
            DndAttribute.CONSTITUTION to 14,
            DndAttribute.INTELLIGENCE to 8,
            DndAttribute.WISDOM to 12,
            DndAttribute.CHARISMA to 16
        )
        BARBAR -> mapOf(
            DndAttribute.STRENGTH to 18,
            DndAttribute.DEXTERITY to 14,
            DndAttribute.CONSTITUTION to 16,
            DndAttribute.INTELLIGENCE to 6,
            DndAttribute.WISDOM to 8,
            DndAttribute.CHARISMA to 10
        )
        JAEGER -> mapOf(
            DndAttribute.STRENGTH to 12,
            DndAttribute.DEXTERITY to 18,
            DndAttribute.CONSTITUTION to 14,
            DndAttribute.INTELLIGENCE to 10,
            DndAttribute.WISDOM to 16,
            DndAttribute.CHARISMA to 8
        )
        MAGIER -> mapOf(
            DndAttribute.STRENGTH to 8,
            DndAttribute.DEXTERITY to 14,
            DndAttribute.CONSTITUTION to 10,
            DndAttribute.INTELLIGENCE to 18,
            DndAttribute.WISDOM to 12,
            DndAttribute.CHARISMA to 10
        )
        PRIESTER -> mapOf(
            DndAttribute.STRENGTH to 10,
            DndAttribute.DEXTERITY to 10,
            DndAttribute.CONSTITUTION to 14,
            DndAttribute.INTELLIGENCE to 12,
            DndAttribute.WISDOM to 18,
            DndAttribute.CHARISMA to 14
        )
    }

    // Gibt die verfügbaren Sets für diese Klasse zurück
    fun getAvailableSets(): List<EquipmentSet> = when(this) {
        PALADIN -> listOf(EquipmentSet.PALADIN_SET1, EquipmentSet.PALADIN_SET2, EquipmentSet.PALADIN_SET3)
        BARBAR -> listOf(EquipmentSet.BARBAR_SET1, EquipmentSet.BARBAR_SET2, EquipmentSet.BARBAR_SET3)
        else -> emptyList() // Andere Klassen haben noch keine Sets
    }

    // Gibt die verfügbaren normalen Fähigkeiten zurück
    fun getAvailableAbilities(): List<PaladinAbility> = when(this) {
        PALADIN -> PaladinAbility.values().filter { it.isNormal() }
        else -> emptyList()
    }

    // Gibt die verfügbaren Ultimates zurück
    fun getAvailableUltimates(): List<PaladinAbility> = when(this) {
        PALADIN -> PaladinAbility.values().filter { it.isUltimate() }
        else -> emptyList()
    }

    // Gibt die verfügbaren Barbar normalen Fähigkeiten zurück
    fun getAvailableBarbarAbilities(): List<BarbarAbility> = when(this) {
        BARBAR -> BarbarAbility.values().filter { it.isNormal() }
        else -> emptyList()
    }

    // Gibt die verfügbaren Barbar Ultimates zurück
    fun getAvailableBarbarUltimates(): List<BarbarAbility> = when(this) {
        BARBAR -> BarbarAbility.values().filter { it.isUltimate() }
        else -> emptyList()
    }

    // Berechne Proficiency Bonus basierend auf Level (D&D 5e)
    fun getProficiencyBonus(level: Int): Int = when {
        level <= 4 -> 2
        level <= 8 -> 3
        level <= 12 -> 4
        level <= 16 -> 5
        else -> 6
    }
}

// Character Stats (D&D 5e vollständig)
data class CharacterStats(
    // D&D Attribute (8-20)
    var strength: Int = 10,
    var dexterity: Int = 10,
    var constitution: Int = 10,
    var intelligence: Int = 10,
    var wisdom: Int = 10,
    var charisma: Int = 10,

    // Level & XP
    var level: Int = 1,
    var experience: Int = 0,

    // HP
    var maxHP: Int = 10,
    var currentHP: Int = 10,
    var temporaryHP: Int = 0, // Temp HP (D&D Mechanik)

    // Mana (vereinfacht, nicht Standard D&D aber für unser Spiel)
    var maxMana: Int = 0,
    var currentMana: Int = 0,

    // Armor Class (10 + DEX-Mod + Rüstung)
    var baseAC: Int = 10,
    var armorBonus: Int = 0,

    // Initiative
    var initiativeBonus: Int = 0,

    // Proficient Skills
    val proficientSkills: MutableSet<DndSkill> = mutableSetOf()
) {
    // Get attribute value
    fun getAttribute(attr: DndAttribute): Int = when(attr) {
        DndAttribute.STRENGTH -> strength
        DndAttribute.DEXTERITY -> dexterity
        DndAttribute.CONSTITUTION -> constitution
        DndAttribute.INTELLIGENCE -> intelligence
        DndAttribute.WISDOM -> wisdom
        DndAttribute.CHARISMA -> charisma
    }

    // Set attribute value
    fun setAttribute(attr: DndAttribute, value: Int) {
        when(attr) {
            DndAttribute.STRENGTH -> strength = value
            DndAttribute.DEXTERITY -> dexterity = value
            DndAttribute.CONSTITUTION -> constitution = value
            DndAttribute.INTELLIGENCE -> intelligence = value
            DndAttribute.WISDOM -> wisdom = value
            DndAttribute.CHARISMA -> charisma = value
        }
    }

    // Get modifier for attribute (D&D 5e)
    fun getModifier(attr: DndAttribute): Int = DndAttribute.getModifier(getAttribute(attr))

    // Calculate Proficiency Bonus based on level
    fun getProficiencyBonus(): Int = when {
        level <= 4 -> 2
        level <= 8 -> 3
        level <= 12 -> 4
        level <= 16 -> 5
        else -> 6
    }

    // Calculate Armor Class
    fun getArmorClass(): Int = baseAC + getModifier(DndAttribute.DEXTERITY) + armorBonus

    // Calculate Initiative
    fun getInitiative(): Int = getModifier(DndAttribute.DEXTERITY) + initiativeBonus

    // Skill Check (d20 + modifier + proficiency if proficient)
    fun getSkillBonus(skill: DndSkill): Int {
        var bonus = getModifier(skill.attribute)
        if (skill in proficientSkills) {
            bonus += getProficiencyBonus()
        }
        return bonus
    }

    // Saving Throw (d20 + modifier + proficiency if proficient)
    fun getSavingThrowBonus(attr: DndAttribute, proficiencies: List<DndAttribute>): Int {
        var bonus = getModifier(attr)
        if (attr in proficiencies) {
            bonus += getProficiencyBonus()
        }
        return bonus
    }

    // XP needed for next level (D&D 5e progression)
    fun getNextLevelXP(): Int {
        // D&D 5e XP for levels 1-20
        if (level < 20) {
            val xpTable = listOf(
                0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000,
                85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000
            )
            return xpTable[level]
        }

        // Extended XP for levels 20-100 (exponential scaling)
        // Formula: 355000 × 1.15^(level - 19)
        val baseXP = 355000.0
        val exponent = level - 19
        return (baseXP * 1.15.pow(exponent.toDouble())).toInt()
    }

    fun canLevelUp(): Boolean = experience >= getNextLevelXP() && level < 100

    fun levelUp(playerClass: PlayerClass) {
        if (!canLevelUp()) return

        level++

        // Grant 1 Skill Point per level
        GameState.grantSkillPoint()

        // Roll Hit Dice (oder nehme Durchschnitt) und addiere CON-Mod
        val hpGain = playerClass.hitDice.average() + getModifier(DndAttribute.CONSTITUTION)
        maxHP += hpGain.coerceAtLeast(1)

        // Heal to full on level up
        currentHP = maxHP
        currentMana = maxMana
    }

    // Take damage (berücksichtigt Temp HP)
    fun takeDamage(damage: Int): Int {
        var remainingDamage = damage

        // Temp HP absorbiert Schaden zuerst
        if (temporaryHP > 0) {
            if (temporaryHP >= remainingDamage) {
                temporaryHP -= remainingDamage
                return 0
            } else {
                remainingDamage -= temporaryHP
                temporaryHP = 0
            }
        }

        // Rest geht auf normale HP
        currentHP -= remainingDamage
        if (currentHP < 0) currentHP = 0

        return remainingDamage
    }

    // Heal
    fun heal(amount: Int) {
        currentHP = (currentHP + amount).coerceAtMost(maxHP)
    }

    // Rest (Short Rest in D&D)
    fun shortRest(playerClass: PlayerClass): Int {
        val healing = playerClass.hitDice.roll() + getModifier(DndAttribute.CONSTITUTION)
        heal(healing)
        return healing
    }

    // Long Rest
    fun longRest() {
        currentHP = maxHP
        currentMana = maxMana
        temporaryHP = 0
    }

    // Aura of Protection: Get CHA modifier bonus to saving throws
    fun getAuraOfProtectionBonus(): Int {
        if (!PaladinPassive.AURA_OF_PROTECTION.isUnlockedAt(level)) return 0
        return getModifier(DndAttribute.CHARISMA)
    }

    // Aura Range: 10ft default, 30ft with Aura Expansion
    fun getAuraRange(): Int {
        return if (PaladinPassive.AURA_RADIUS_INCREASE.isUnlockedAt(level)) 30 else 10
    }

    // Enhanced Saving Throw with Aura of Protection
    fun getSavingThrowWithAura(attr: DndAttribute, proficiencies: List<DndAttribute>): Int {
        var bonus = getSavingThrowBonus(attr, proficiencies)
        bonus += getAuraOfProtectionBonus()
        return bonus
    }

    // ============================================================================
    // BARBAR PASSIVE BONI
    // ============================================================================

    // Unarmored Defense (Lv3): AC = 10 + DEX-Mod + CON-Mod (wenn keine Rüstung)
    fun getUnarmoredDefenseBonus(): Int {
        if (!BarbarPassive.UNARMORED_DEFENSE.isUnlockedAt(level)) return 0
        // Only wenn armorBonus = 0 (keine Rüstung getragen)
        if (armorBonus > 0) return 0
        return getModifier(DndAttribute.CONSTITUTION)
    }

    // Fast Movement (Lv11): +3 Initiative
    fun getFastMovementBonus(): Int {
        if (!BarbarPassive.FAST_MOVEMENT.isUnlockedAt(level)) return 0
        return BarbarPassive.FAST_MOVEMENT.getScaledValue(level)
    }

    // Calculate Initiative with Fast Movement
    fun getInitiativeWithBarbar(): Int {
        var init = getInitiative()
        init += getFastMovementBonus()
        return init
    }

    // Brutal Critical: Get crit multiplier (2x normal, 3x with Brutal Critical)
    fun getCriticalMultiplier(): Int {
        if (BarbarPassive.BRUTAL_CRITICAL.isUnlockedAt(level)) {
            return 3  // 3x damage on crit
        }
        return 2  // Normal 2x damage on crit
    }
}

// Character Loadout (vor Abenteuer gewählt, skaliert mit Level)
data class CharacterLoadout(
    // Paladin Abilities
    val normalAbilities: MutableList<PaladinAbility?> = mutableListOf(null, null, null, null, null),  // Max 5
    var ultimateAbility: PaladinAbility? = null,
    var layOnHandsPool: Int = 0,  // Heilungs-Pool (Level × 5)
    var layOnHandsUsed: Int = 0,  // Wie viel bereits genutzt
    var cleansingTouchUsed: Int = 0,  // Wie oft Cleansing Touch genutzt (resets täglich)
    var divineSmiteActive: Boolean = false,  // Divine Smite für nächsten Angriff aktiviert

    // Barbar Abilities
    val barbarAbilities: MutableList<BarbarAbility?> = mutableListOf(null, null, null, null, null),  // Max 5
    var barbarUltimate: BarbarAbility? = null,
    var rageActive: Boolean = false,  // Rage aktiviert
    var rageRounds: Int = 0,  // Verbleibende Rage-Runden
    var recklessAttackActive: Boolean = false  // Reckless Attack Toggle
) {
    // Wie viele normale Slots sind verfügbar basierend auf Level?
    fun getMaxNormalSlots(characterLevel: Int): Int = when {
        characterLevel < 10 -> 2   // Level 1-9: 2 Slots
        characterLevel < 20 -> 3   // Level 10-19: 3 Slots
        characterLevel < 30 -> 4   // Level 20-29: 4 Slots
        else -> 5                   // Level 30+: 5 Slots
    }

    // Hat Character Zugang zu Ultimate-Slot?
    fun hasUltimateSlot(characterLevel: Int): Boolean = characterLevel >= 10

    // Setze normale Fähigkeit an Index
    fun setNormalAbility(index: Int, ability: PaladinAbility?, characterLevel: Int): Boolean {
        if (index >= getMaxNormalSlots(characterLevel)) return false
        if (ability != null && !ability.isNormal()) return false

        // Check for duplicates
        if (ability != null && normalAbilities.contains(ability)) return false

        normalAbilities[index] = ability
        return true
    }

    // Get normale Fähigkeit an Index
    fun getNormalAbility(index: Int): PaladinAbility? = normalAbilities.getOrNull(index)

    // Get alle ausgerüsteten normalen Fähigkeiten
    fun getEquippedNormalAbilities(): List<PaladinAbility> {
        return normalAbilities.filterNotNull()
    }

    // Check ob Loadout komplett ist (für dieses Level)
    fun isComplete(characterLevel: Int): Boolean {
        val requiredNormalSlots = getMaxNormalSlots(characterLevel)
        val filledNormalSlots = normalAbilities.take(requiredNormalSlots).count { it != null }

        val hasRequiredNormals = filledNormalSlots == requiredNormalSlots
        val hasRequiredUltimate = if (hasUltimateSlot(characterLevel)) ultimateAbility != null else true

        return hasRequiredNormals && hasRequiredUltimate
    }

    // Clear loadout
    fun clear() {
        normalAbilities.fill(null)
        ultimateAbility = null
    }
}

// ==================== COMBAT SYSTEM ====================

// Helper function: Generate equipment drop from combat (30% chance)
internal fun generateCombatEquipmentDrop(enemyLevel: Int, playerClass: PlayerClass?): Equipment? {
    if (playerClass == null) return null
    val availableSets = playerClass.getAvailableSets()
    if (availableSets.isEmpty()) return null

    // Drop chance: 30% base
    val dropChance = 30
    val roll = (1..100).random()

    if (roll > dropChance) return null

    // Random slot
    val slot = EquipmentSlot.values().random()

    // Random set
    val set = availableSets.random()

    // Rarity based on enemy level (higher level = better drops)
    val rarity = when {
        enemyLevel <= 2 -> {
            val rarityRoll = (1..100).random()
            when {
                rarityRoll <= 80 -> EquipmentRarity.GRAU   // 80%
                else -> EquipmentRarity.WEISS              // 20%
            }
        }
        enemyLevel <= 5 -> {
            val rarityRoll = (1..100).random()
            when {
                rarityRoll <= 50 -> EquipmentRarity.GRAU   // 50%
                rarityRoll <= 85 -> EquipmentRarity.WEISS  // 35%
                else -> EquipmentRarity.GRUEN              // 15%
            }
        }
        enemyLevel <= 10 -> {
            val rarityRoll = (1..100).random()
            when {
                rarityRoll <= 30 -> EquipmentRarity.GRAU   // 30%
                rarityRoll <= 60 -> EquipmentRarity.WEISS  // 30%
                rarityRoll <= 85 -> EquipmentRarity.GRUEN  // 25%
                else -> EquipmentRarity.BLAU               // 15%
            }
        }
        enemyLevel <= 15 -> {
            val rarityRoll = (1..100).random()
            when {
                rarityRoll <= 15 -> EquipmentRarity.GRAU   // 15%
                rarityRoll <= 35 -> EquipmentRarity.WEISS  // 20%
                rarityRoll <= 60 -> EquipmentRarity.GRUEN  // 25%
                rarityRoll <= 85 -> EquipmentRarity.BLAU   // 25%
                else -> EquipmentRarity.LILA               // 15%
            }
        }
        else -> { // Level 16+
            val rarityRoll = (1..100).random()
            when {
                rarityRoll <= 10 -> EquipmentRarity.GRAU   // 10%
                rarityRoll <= 25 -> EquipmentRarity.WEISS  // 15%
                rarityRoll <= 45 -> EquipmentRarity.GRUEN  // 20%
                rarityRoll <= 70 -> EquipmentRarity.BLAU   // 25%
                rarityRoll <= 95 -> EquipmentRarity.LILA   // 25%
                else -> EquipmentRarity.GELB               // 5% (sehr selten!)
            }
        }
    }

    // Tier based on enemy level (up to 100)
    val maxTier = rarity.getMaxUpgradeLevel()
    val tier = ((enemyLevel / 2) + 1).coerceIn(1, maxTier)

    return Equipment(slot, set, rarity, tier)
}

// Monster Type
enum class MonsterType(val displayName: String) {
    BEAST("Bestie"),
    UNDEAD("Untot"),
    DEMON("Dämon"),
    HUMANOID("Humanoider"),
    DRAGON("Drache"),
    ELEMENTAL("Elementar")
}

// Enemy AI Type
enum class EnemyAIType {
    SIMPLE,  // Random attacks
    SMART    // Targets weakest, uses abilities tactically
}

// Monster/Enemy
data class Enemy(
    val name: String,
    val type: MonsterType,
    val level: Int,
    var maxHP: Int,
    var currentHP: Int,
    val armor: Int,  // AC
    val baseDamage: Int,
    val initiative: Int,
    val aiType: EnemyAIType = EnemyAIType.SIMPLE,
    val xpReward: Int,
    val goldReward: Int = 0,
    val essenceReward: Int = 0,
    var abilities: List<EnemyAbility> = emptyList()
) {
    fun isAlive(): Boolean = currentHP > 0
    fun isDead(): Boolean = !isAlive()

    fun takeDamage(damage: Int): Int {
        val actualDamage = damage.coerceAtLeast(0)
        currentHP -= actualDamage
        if (currentHP < 0) currentHP = 0
        return actualDamage
    }

    fun heal(amount: Int) {
        currentHP = (currentHP + amount).coerceAtMost(maxHP)
    }

    fun getHPPercent(): Int = if (maxHP > 0) (currentHP * 100 / maxHP) else 0
}

// Enemy Ability
data class EnemyAbility(
    val name: String,
    val damage: Int = 0,
    val healing: Int = 0,
    val cooldown: Int = 3,  // Rounds
    var currentCooldown: Int = 0,
    val targetType: TargetType = TargetType.SINGLE_ENEMY
)

// Target Type for abilities
enum class TargetType {
    SINGLE_ENEMY,   // Target one enemy
    ALL_ENEMIES,    // AOE
    SELF,           // Self-buff/heal
    ALLY,           // Target ally
    ALL_ALLIES      // Group buff/heal
}

// Combat Participant (wrapper for both players and enemies)
sealed class CombatParticipant {
    abstract val name: String
    abstract val initiative: Int
    abstract fun isAlive(): Boolean
    abstract fun getCurrentHP(): Int
    abstract fun getMaxHP(): Int

    data class PlayerParticipant(
        val stats: CharacterStats,
        val loadout: CharacterLoadout,
        val playerClass: PlayerClass,
        override val name: String = "Du"
    ) : CombatParticipant() {
        override val initiative: Int get() = stats.getInitiative()
        override fun isAlive(): Boolean = stats.currentHP > 0
        override fun getCurrentHP(): Int = stats.currentHP
        override fun getMaxHP(): Int = stats.maxHP
    }

    data class EnemyParticipant(
        val enemy: Enemy,
        val index: Int  // Which enemy in the group (0, 1, 2...)
    ) : CombatParticipant() {
        override val name: String get() = if (index > 0) "${enemy.name} ${index + 1}" else enemy.name
        override val initiative: Int get() = enemy.initiative
        override fun isAlive(): Boolean = enemy.isAlive()
        override fun getCurrentHP(): Int = enemy.currentHP
        override fun getMaxHP(): Int = enemy.maxHP
    }
}

// Combat Type
enum class CombatType {
    STORY,      // Story kämpfe - immer smart AI
    AUFTRAG     // Aufträge - erste 9 simple, dann jeder 10. smart
}

// Combat Result
data class CombatResult(
    val victory: Boolean,
    val xpGained: Int,
    val goldGained: Int,
    val essenceGained: Int,
    val lootDropped: Equipment? = null,
    val roundsLasted: Int
)

// Combat Log Entry
data class CombatLogEntry(
    val round: Int,
    val message: String,
    val isImportant: Boolean = false
)

// Status Effects System
enum class StatusEffectType {
    // Debuffs
    BLINDED,        // -4 zu Attack Rolls
    BURNED,         // X Schaden pro Runde
    STUNNED,        // Kann nicht agieren
    POISONED,       // X Schaden pro Runde + reduzierte Heilung
    WEAKENED,       // -50% Damage

    // Buffs
    BLESSED,        // +2 zu allen Rolls
    SHIELD,         // Temporäre HP
    DAMAGE_RESIST,  // % Damage Reduction
    REFLECT,        // % Schaden reflektieren
    IMMUNE,         // Immun gegen Debuffs
    HASTE,          // +Initiative, Extra Actions
    DIVINE_POWER,   // +% Damage
    REGENERATION,   // +X HP pro Runde

    // Special
    TRANSFORMATION, // God-Mode (Avatar des Lichts)
    REVIVE_ON_DEATH // Auto-Resurrect
}

data class StatusEffect(
    val type: StatusEffectType,
    val duration: Int,          // Verbleibende Runden (-1 = permanent bis Kampfende)
    val value: Int = 0,         // Stärke des Effekts (Damage, %, etc.)
    val source: String = ""     // Name des Skills der das verursacht hat
) {
    fun getRemainingRounds(): Int = duration

    fun getDescription(): String = when(type) {
        StatusEffectType.BLINDED -> "Geblendet (-4 Angriff) - $duration Runden"
        StatusEffectType.BURNED -> "Brennend ($value Schaden/Runde) - $duration Runden"
        StatusEffectType.STUNNED -> "Betäubt (kein Zug) - $duration Runden"
        StatusEffectType.POISONED -> "Vergiftet ($value Schaden/Runde) - $duration Runden"
        StatusEffectType.WEAKENED -> "Geschwächt (-50% Damage) - $duration Runden"
        StatusEffectType.BLESSED -> "Gesegnet (+2 Rolls) - $duration Runden"
        StatusEffectType.SHIELD -> "Schild ($value Temp-HP)"
        StatusEffectType.DAMAGE_RESIST -> "Schadensreduktion ($value%) - $duration Runden"
        StatusEffectType.REFLECT -> "Schadensspiegelung ($value%) - $duration Runden"
        StatusEffectType.IMMUNE -> "Immun gegen Debuffs - $duration Runden"
        StatusEffectType.HASTE -> "Eile (+Initiative) - $duration Runden"
        StatusEffectType.DIVINE_POWER -> "Göttliche Kraft (+$value% Damage) - $duration Runden"
        StatusEffectType.REGENERATION -> "Regeneration (+$value HP/Runde) - $duration Runden"
        StatusEffectType.TRANSFORMATION -> "Göttliche Transformation! - $duration Runden"
        StatusEffectType.REVIVE_ON_DEATH -> "Wiederbelebung bei Tod - $duration Runden"
    }
}

// Combat State (active combat instance)
data class CombatState(
    val combatType: CombatType,
    val isTutorial: Boolean = false,
    val playerParty: MutableList<CombatParticipant.PlayerParticipant>,
    val enemyParty: MutableList<CombatParticipant.EnemyParticipant>,
    var currentRound: Int = 0,
    var turnOrder: List<CombatParticipant> = emptyList(),
    var currentTurnIndex: Int = 0,
    val combatLog: MutableList<CombatLogEntry> = mutableListOf(),
    val abilityCooldowns: MutableMap<PaladinAbility, Int> = mutableMapOf(),
    val statusEffects: MutableMap<CombatParticipant, MutableList<StatusEffect>> = mutableMapOf(),
    var isPlayerTurn: Boolean = false,
    var combatEnded: Boolean = false,
    var combatResult: CombatResult? = null,
    var auftragCount: Int = 0  // Track aufträge for smart AI every 10
) {
    fun getCurrentParticipant(): CombatParticipant? = turnOrder.getOrNull(currentTurnIndex)

    fun isCurrentPlayerTurn(): Boolean = getCurrentParticipant() is CombatParticipant.PlayerParticipant

    fun getAliveAllies(): List<CombatParticipant.PlayerParticipant> =
        playerParty.filter { it.isAlive() }

    fun getAliveEnemies(): List<CombatParticipant.EnemyParticipant> =
        enemyParty.filter { it.isAlive() }

    fun addLog(message: String, important: Boolean = false) {
        combatLog.add(CombatLogEntry(currentRound, message, important))
    }

    // Status Effect Management
    fun addStatusEffect(target: CombatParticipant, effect: StatusEffect) {
        // Check immunity
        if (hasStatusEffect(target, StatusEffectType.IMMUNE)) {
            addLog("${target.name} ist immun gegen ${effect.type}!")
            return
        }

        val effects = statusEffects.getOrPut(target) { mutableListOf() }

        // Special handling for SHIELD (add to temp HP)
        if (effect.type == StatusEffectType.SHIELD) {
            if (target is CombatParticipant.PlayerParticipant) {
                target.stats.temporaryHP += effect.value
                addLog("${target.name} erhält ${effect.value} temporäre HP!")
            }
            return
        }

        // Aura of Protection: Saving throw for debuffs (Player only)
        if (target is CombatParticipant.PlayerParticipant) {
            val debuffTypes = listOf(
                StatusEffectType.BLINDED, StatusEffectType.BURNED,
                StatusEffectType.STUNNED, StatusEffectType.POISONED, StatusEffectType.WEAKENED
            )

            if (effect.type in debuffTypes) {
                // Determine saving throw attribute based on effect type
                val saveAttr = when(effect.type) {
                    StatusEffectType.BLINDED, StatusEffectType.STUNNED -> DndAttribute.CONSTITUTION
                    StatusEffectType.POISONED -> DndAttribute.CONSTITUTION
                    StatusEffectType.WEAKENED -> DndAttribute.STRENGTH
                    else -> DndAttribute.WISDOM
                }

                // Roll saving throw: d20 + modifier + aura bonus
                val roll = (1..20).random()
                val savingThrowBonus = target.stats.getSavingThrowWithAura(saveAttr, listOf(DndAttribute.CONSTITUTION, DndAttribute.WISDOM))
                val totalRoll = roll + savingThrowBonus

                // DC based on effect power (10 + effect level/power)
                val dc = 10 + (effect.value / 2).coerceAtLeast(2)

                // Aura of Protection bonus display
                val auraBonus = target.stats.getAuraOfProtectionBonus()
                val auraText = if (auraBonus > 0) " (+$auraBonus Aura)" else ""

                if (totalRoll >= dc) {
                    addLog("✅ ${target.name} widerstet ${effect.type}! (Roll: $roll + $savingThrowBonus$auraText = $totalRoll vs DC $dc)")
                    return
                } else {
                    addLog("❌ ${target.name} widerstet ${effect.type} nicht! (Roll: $roll + $savingThrowBonus$auraText = $totalRoll vs DC $dc)")
                }
            }
        }

        effects.add(effect)
        addLog("${target.name} erhält: ${effect.type} (${effect.duration} Runden)")
    }

    fun removeStatusEffect(target: CombatParticipant, type: StatusEffectType) {
        statusEffects[target]?.removeAll { it.type == type }
    }

    fun hasStatusEffect(target: CombatParticipant, type: StatusEffectType): Boolean {
        return statusEffects[target]?.any { it.type == type } ?: false
    }

    fun getStatusEffect(target: CombatParticipant, type: StatusEffectType): StatusEffect? {
        return statusEffects[target]?.firstOrNull { it.type == type }
    }

    fun getActiveEffects(target: CombatParticipant): List<StatusEffect> {
        return statusEffects[target] ?: emptyList()
    }

    fun updateStatusEffects() {
        // Process all status effects for all participants
        val allParticipants = (playerParty as List<CombatParticipant>) + (enemyParty as List<CombatParticipant>)

        allParticipants.forEach { participant ->
            if (!participant.isAlive()) return@forEach

            val effects = statusEffects[participant] ?: return@forEach
            val toRemove = mutableListOf<StatusEffect>()

            effects.forEach { effect ->
                when (effect.type) {
                    StatusEffectType.BURNED, StatusEffectType.POISONED -> {
                        // DOT Damage
                        if (participant is CombatParticipant.PlayerParticipant) {
                            participant.stats.takeDamage(effect.value)
                        } else if (participant is CombatParticipant.EnemyParticipant) {
                            participant.enemy.takeDamage(effect.value)
                        }
                        addLog("${participant.name} nimmt ${effect.value} ${effect.type}-Schaden!")
                    }
                    StatusEffectType.REGENERATION -> {
                        // HOT Healing
                        if (participant is CombatParticipant.PlayerParticipant) {
                            participant.stats.heal(effect.value)
                        } else if (participant is CombatParticipant.EnemyParticipant) {
                            participant.enemy.currentHP = (participant.enemy.currentHP + effect.value).coerceAtMost(participant.enemy.maxHP)
                        }
                        addLog("${participant.name} regeneriert ${effect.value} HP!")
                    }
                    else -> {}
                }
            }

            // Remove expired effects (duration reaches 0)
            effects.removeAll { toRemove.contains(it) }
        }
    }

    fun decreaseStatusEffectDurations() {
        val allParticipants = (playerParty as List<CombatParticipant>) + (enemyParty as List<CombatParticipant>)

        allParticipants.forEach { participant ->
            val effects = statusEffects[participant] ?: return@forEach

            effects.forEach { effect ->
                if (effect.duration > 0) {
                    // Decrease duration (create new object with reduced duration)
                    val index = effects.indexOf(effect)
                    effects[index] = effect.copy(duration = effect.duration - 1)
                }
            }

            // Remove expired effects
            effects.removeAll { it.duration == 0 }
        }
    }

    fun checkCombatEnd(): Boolean {
        val allEnemiesDead = enemyParty.all { it.enemy.isDead() }
        var allPlayersDead = playerParty.all { !it.isAlive() }

        // Check for Revive-on-Death effects
        if (allPlayersDead) {
            playerParty.forEach { player ->
                if (!player.isAlive() && hasStatusEffect(player, StatusEffectType.REVIVE_ON_DEATH)) {
                    // Revive with 50% HP
                    player.stats.currentHP = player.stats.maxHP / 2
                    removeStatusEffect(player, StatusEffectType.REVIVE_ON_DEATH)
                    addLog("✨ ${player.name} wird durch ${getStatusEffect(player, StatusEffectType.REVIVE_ON_DEATH)?.source ?: "göttliche Intervention"} wiederbelebt!", true)
                    allPlayersDead = false
                }
            }
        }

        if (allEnemiesDead || allPlayersDead) {
            combatEnded = true

            if (allEnemiesDead) {
                // Victory!
                val totalXP = enemyParty.sumOf { it.enemy.xpReward }
                val totalGold = enemyParty.sumOf { it.enemy.goldReward }
                val totalEssence = enemyParty.sumOf { it.enemy.essenceReward }

                // Generate equipment drop (30% chance, based on highest enemy level)
                val highestEnemyLevel = enemyParty.maxOfOrNull { it.enemy.level } ?: 1
                val playerClass = playerParty.firstOrNull()?.playerClass
                val equipmentDrop = generateCombatEquipmentDrop(highestEnemyLevel, playerClass)

                combatResult = CombatResult(
                    victory = true,
                    xpGained = totalXP,
                    goldGained = totalGold,
                    essenceGained = totalEssence,
                    lootDropped = equipmentDrop,
                    roundsLasted = currentRound
                )
                addLog("🎉 SIEG! Du hast gewonnen!", true)
            } else {
                // Defeat
                combatResult = CombatResult(
                    victory = false,
                    xpGained = 0,
                    goldGained = 0,
                    essenceGained = 0,
                    roundsLasted = currentRound
                )
                addLog("💀 NIEDERLAGE! Du wurdest besiegt!", true)
            }
            return true
        }
        return false
    }
}

// Monster Templates für verschiedene Level-Bereiche
object MonsterTemplates {
    // Level 1-5: Starter Enemies
    fun createGoblin(level: Int): Enemy {
        val hp = 20 + (level * 5)
        return Enemy(
            name = "Goblin",
            type = MonsterType.HUMANOID,
            level = level,
            maxHP = hp,
            currentHP = hp,
            armor = 12 + level,
            baseDamage = 5 + level,
            initiative = 2,
            aiType = EnemyAIType.SIMPLE,
            xpReward = 50 * level,
            goldReward = 10 * level,
            essenceReward = 0
        )
    }

    fun createWolf(level: Int): Enemy {
        val hp = 25 + (level * 6)
        return Enemy(
            name = "Wolf",
            type = MonsterType.BEAST,
            level = level,
            maxHP = hp,
            currentHP = hp,
            armor = 13 + level,
            baseDamage = 6 + level,
            initiative = 3,
            aiType = EnemyAIType.SIMPLE,
            xpReward = 60 * level,
            goldReward = 5 * level,
            essenceReward = 0
        )
    }

    fun createSkeleton(level: Int): Enemy {
        val hp = 30 + (level * 5)
        return Enemy(
            name = "Skelett",
            type = MonsterType.UNDEAD,
            level = level,
            maxHP = hp,
            currentHP = hp,
            armor = 14 + level,
            baseDamage = 7 + level,
            initiative = 1,
            aiType = EnemyAIType.SIMPLE,
            xpReward = 70 * level,
            goldReward = 15 * level,
            essenceReward = 1
        )
    }

    // Level 5-10: Mid-tier
    fun createOrc(level: Int): Enemy {
        val hp = 50 + (level * 8)
        return Enemy(
            name = "Orc",
            type = MonsterType.HUMANOID,
            level = level,
            maxHP = hp,
            currentHP = hp,
            armor = 15 + level,
            baseDamage = 10 + (level * 2),
            initiative = 1,
            aiType = EnemyAIType.SMART,
            xpReward = 100 * level,
            goldReward = 20 * level,
            essenceReward = 2
        )
    }

    fun createDarkMage(level: Int): Enemy {
        val hp = 40 + (level * 6)
        return Enemy(
            name = "Dunkler Magier",
            type = MonsterType.HUMANOID,
            level = level,
            maxHP = hp,
            currentHP = hp,
            armor = 12 + level,
            baseDamage = 12 + (level * 2),
            initiative = 4,
            aiType = EnemyAIType.SMART,
            xpReward = 120 * level,
            goldReward = 30 * level,
            essenceReward = 3,
            abilities = listOf(
                EnemyAbility("Feuerball", damage = 20 + level * 3, cooldown = 3)
            )
        )
    }

    // Boss: Story Tutorial
    fun createTutorialBandit(): Enemy {
        return Enemy(
            name = "Bandit",
            type = MonsterType.HUMANOID,
            level = 1,
            maxHP = 30,
            currentHP = 30,
            armor = 12,
            baseDamage = 4,
            initiative = 2,
            aiType = EnemyAIType.SIMPLE,
            xpReward = 100,
            goldReward = 25,
            essenceReward = 1
        )
    }

    // Get random enemy for level range
    fun getRandomEnemy(level: Int, smartAI: Boolean = false): Enemy {
        val enemy = when (level) {
            in 1..5 -> listOf(
                createGoblin(level),
                createWolf(level),
                createSkeleton(level)
            ).random()
            in 6..10 -> listOf(
                createOrc(level),
                createDarkMage(level),
                createSkeleton(level)
            ).random()
            else -> createOrc(level.coerceAtMost(20))
        }

        // Override AI if requested
        if (smartAI && enemy.aiType == EnemyAIType.SIMPLE) {
            return enemy.copy(aiType = EnemyAIType.SMART)
        }

        return enemy
    }
}

// ============================================================================
// UNIVERSAL SKILL TREE SYSTEM
// ============================================================================

enum class SkillCategory {
    INCREMENTAL,  // 70% - Idle/Clicker gameplay
    COMBAT        // 30% - DND/Combat gameplay
}

enum class UniversalSkill(
    val displayName: String,
    val description: String,
    val category: SkillCategory,
    val tier: Int,  // 1-5 (every 20 levels)
    val levelRequirement: Int,
    val isChoice: Boolean = false,  // Is part of A/B choice
    val choiceGroup: Int? = null,  // Which choice group (1-8)
    val prerequisites: List<UniversalSkill> = emptyList()
) {
    // ========== TIER 1 (Level 1-20) - Grundlagen ==========
    // Incremental (14)
    STAERKERE_KLICKS("Stärkere Klicks", "Click Power +10%", SkillCategory.INCREMENTAL, 1, 1),
    SCHNELLERE_FINGER("Schnellere Finger", "Click Rate +5%", SkillCategory.INCREMENTAL, 1, 2),
    FLEISSIGE_HAENDE("Fleißige Hände", "Click Power +15%", SkillCategory.INCREMENTAL, 1, 3),
    GOLD_FINDER_I("Gold-Finder I", "+10% Gold aus allen Quellen", SkillCategory.INCREMENTAL, 1, 4),
    KRITISCHER_KLICK_I("Kritischer Klick I", "5% Crit Chance, 2x Damage", SkillCategory.INCREMENTAL, 1, 5, isChoice = true, choiceGroup = 1),
    MEGA_KLICK_I("Mega-Klick I", "+25% Click Power", SkillCategory.INCREMENTAL, 1, 5, isChoice = true, choiceGroup = 1),
    AUTO_CLICKER_SPEED_I("Auto-Clicker Speed I", "Auto-Clicker +0.5 clicks/sec", SkillCategory.INCREMENTAL, 1, 7),
    IDLE_GAINS_I("Idle Gains I", "+10% Punkte während AFK", SkillCategory.INCREMENTAL, 1, 8),
    OFFLINE_PROGRESS_I("Offline-Progress I", "25% der Idle-Rate offline", SkillCategory.INCREMENTAL, 1, 9),
    LOOT_GLUECK_I("Loot-Glück I", "+5% Equipment Drop Chance", SkillCategory.INCREMENTAL, 1, 10),
    GOLD_BONUS_I("Gold-Bonus I", "+15% Gold aus Kämpfen", SkillCategory.INCREMENTAL, 1, 11),
    CLICK_MULTI_I("Click-Multiplikator I", "Click Power +20%", SkillCategory.INCREMENTAL, 1, 12),
    DE_FINDER_I("DE-Finder I", "+10% Divine Essence", SkillCategory.INCREMENTAL, 1, 13),
    FARBSTOFF_BONUS_I("Farbstoff-Bonus I", "+10% Farbpunkte", SkillCategory.INCREMENTAL, 1, 14),

    // Combat (6)
    ROBUSTHEIT_I("Robustheit I", "Max HP +10", SkillCategory.COMBAT, 1, 15),
    ZAEHIGKEIT("Zähigkeit", "Max HP +5%", SkillCategory.COMBAT, 1, 16),
    RUESTUNGS_TRAINING_I("Rüstungs-Training I", "Armor +1", SkillCategory.COMBAT, 1, 17),
    KAMPF_ERFAHRUNG_I("Kampf-Erfahrung I", "+10% XP aus Kämpfen", SkillCategory.COMBAT, 1, 18),
    ERSTE_HILFE("Erste Hilfe", "+10% Heilung erhalten", SkillCategory.COMBAT, 1, 19),
    SCHADENS_BONUS_I("Schadens-Bonus I", "+5% Schaden", SkillCategory.COMBAT, 1, 20),

    // ========== TIER 2 (Level 21-40) - Fortgeschritten ==========
    // Incremental (14)
    KRITISCHER_KLICK_II("Kritischer Klick II", "10% Crit, 2.5x Damage", SkillCategory.INCREMENTAL, 2, 21, isChoice = true, choiceGroup = 2),
    MEGA_KLICK_II("Mega-Klick II", "+50% Click Power", SkillCategory.INCREMENTAL, 2, 21, isChoice = true, choiceGroup = 2),
    AUTO_CLICKER_II("Auto-Clicker II", "+1 click/sec", SkillCategory.INCREMENTAL, 2, 23),
    CLICK_MULTI_II("Click-Multiplikator II", "+30%", SkillCategory.INCREMENTAL, 2, 24),
    GOLD_FINDER_II("Gold-Finder II", "+20% Gold", SkillCategory.INCREMENTAL, 2, 25),
    FARB_EFFIZIENZ_I("Farb-Effizienz I", "Färbe-Kosten -10%", SkillCategory.INCREMENTAL, 2, 26),
    LOOT_QUALITAET_I("Loot-Qualität I", "+10% höhere Rarity Chance", SkillCategory.INCREMENTAL, 2, 27),
    IDLE_GAINS_II("Idle Gains II", "+20% AFK", SkillCategory.INCREMENTAL, 2, 28),
    OFFLINE_PROGRESS_II("Offline-Progress II", "50% offline", SkillCategory.INCREMENTAL, 2, 29),
    PRESTIGE_BONUS_I("Prestige-Bonus I", "+10% DE beim Prestige", SkillCategory.INCREMENTAL, 2, 30),
    AUTO_CLICK_KRAFT("Auto-Click Kraft", "Auto-Clicker = 50% Click-Schaden", SkillCategory.INCREMENTAL, 2, 31, isChoice = true, choiceGroup = 3),
    AUTO_CLICK_GESCHWINDIGKEIT("Auto-Click Geschwindigkeit", "+1.5 clicks/sec", SkillCategory.INCREMENTAL, 2, 31, isChoice = true, choiceGroup = 3),
    GOLD_BONUS_II("Gold-Bonus II", "+25% Gold", SkillCategory.INCREMENTAL, 2, 33),
    WUERFEL_GLUECK_I("Würfel-Glück I", "+5% höherer Würfel", SkillCategory.INCREMENTAL, 2, 34),

    // Combat (6)
    ROBUSTHEIT_II("Robustheit II", "Max HP +20", SkillCategory.COMBAT, 2, 35),
    VERTEIDIGUNG_I("Verteidigung I", "Rüstung +5%", SkillCategory.COMBAT, 2, 36),
    KAMPF_MEISTERSCHAFT_I("Kampf-Meisterschaft I", "+15% XP", SkillCategory.COMBAT, 2, 37),
    SCHADENS_BONUS_II("Schadens-Bonus II", "+10% Schaden", SkillCategory.COMBAT, 2, 38),
    MANA_POOL_I("Mana-Pool I", "Max Mana +10", SkillCategory.COMBAT, 2, 39),
    SCHNELLE_REGENERATION("Schnelle Regeneration", "+1 Mana/Runde", SkillCategory.COMBAT, 2, 40),

    // ========== TIER 3 (Level 41-60) - Experte ==========
    // Incremental (14)
    CLICK_MULTI_III("Click-Multiplikator III", "+50%", SkillCategory.INCREMENTAL, 3, 41),
    AUTO_CLICKER_III("Auto-Clicker III", "+1.5 clicks/sec", SkillCategory.INCREMENTAL, 3, 42),
    SUPER_CRIT("Super-Crit", "15% Crit, 3x Damage", SkillCategory.INCREMENTAL, 3, 43, isChoice = true, choiceGroup = 4),
    ULTRA_KLICK("Ultra-Klick", "+100% Click Power", SkillCategory.INCREMENTAL, 3, 43, isChoice = true, choiceGroup = 4),
    GOLD_FINDER_III("Gold-Finder III", "+30%", SkillCategory.INCREMENTAL, 3, 45),
    LOOT_GLUECK_II("Loot-Glück II", "+10% Drop Chance", SkillCategory.INCREMENTAL, 3, 46),
    IDLE_GAINS_III("Idle Gains III", "+30% AFK", SkillCategory.INCREMENTAL, 3, 47),
    OFFLINE_PROGRESS_III("Offline-Progress III", "75% offline", SkillCategory.INCREMENTAL, 3, 48),
    PRESTIGE_BONUS_II("Prestige-Bonus II", "+20% DE", SkillCategory.INCREMENTAL, 3, 49),
    DE_FINDER_II("DE-Finder II", "+20% DE", SkillCategory.INCREMENTAL, 3, 50),
    LOOT_QUALITAET_II("Loot-Qualität II", "+20% höhere Rarity", SkillCategory.INCREMENTAL, 3, 51),
    FARB_EFFIZIENZ_II("Farb-Effizienz II", "-20% Kosten", SkillCategory.INCREMENTAL, 3, 52),
    GOLD_BONUS_III("Gold-Bonus III", "+40%", SkillCategory.INCREMENTAL, 3, 53),
    LOOTBOX_RABATT("Lootbox-Rabatt", "Lootbox -10% Kosten", SkillCategory.INCREMENTAL, 3, 54, isChoice = true, choiceGroup = 5),
    FUSION_RABATT("Fusion-Rabatt", "Fusion -15% Kosten", SkillCategory.INCREMENTAL, 3, 54, isChoice = true, choiceGroup = 5),

    // Combat (6)
    ROBUSTHEIT_III("Robustheit III", "Max HP +30", SkillCategory.COMBAT, 3, 56),
    VERTEIDIGUNG_II("Verteidigung II", "Rüstung +10%", SkillCategory.COMBAT, 3, 57),
    KAMPF_MEISTERSCHAFT_II("Kampf-Meisterschaft II", "+25% XP", SkillCategory.COMBAT, 3, 58),
    SCHADENS_BONUS_III("Schadens-Bonus III", "+15% Schaden", SkillCategory.COMBAT, 3, 59),
    MANA_POOL_II("Mana-Pool II", "Max Mana +20", SkillCategory.COMBAT, 3, 60),

    // ========== TIER 4 (Level 61-80) - Meister ==========
    // Incremental (14)
    CLICK_MULTI_IV("Click-Multiplikator IV", "+75%", SkillCategory.INCREMENTAL, 4, 61),
    AUTO_CLICKER_IV("Auto-Clicker IV", "+2 clicks/sec", SkillCategory.INCREMENTAL, 4, 62),
    MEGA_CRIT("Mega-Crit", "20% Crit, 4x Damage", SkillCategory.INCREMENTAL, 4, 63, isChoice = true, choiceGroup = 6),
    TITANISCHER_KLICK("Titanischer Klick", "+200% Click Power", SkillCategory.INCREMENTAL, 4, 63, isChoice = true, choiceGroup = 6),
    GOLD_MEISTER("Gold-Meister", "+50% Gold", SkillCategory.INCREMENTAL, 4, 65),
    LOOT_GLUECK_III("Loot-Glück III", "+15% Drop Chance", SkillCategory.INCREMENTAL, 4, 66),
    IDLE_GAINS_IV("Idle Gains IV", "+50% AFK", SkillCategory.INCREMENTAL, 4, 67),
    OFFLINE_PROGRESS_IV("Offline-Progress IV", "100% offline (FULL!)", SkillCategory.INCREMENTAL, 4, 68),
    PRESTIGE_MEISTER("Prestige-Meister", "+30% DE", SkillCategory.INCREMENTAL, 4, 69),
    DE_FINDER_III("DE-Finder III", "+30% DE", SkillCategory.INCREMENTAL, 4, 70),
    LOOT_QUALITAET_III("Loot-Qualität III", "+30% höhere Rarity", SkillCategory.INCREMENTAL, 4, 71),
    WUERFEL_GLUECK_II("Würfel-Glück II", "+10% höherer Würfel", SkillCategory.INCREMENTAL, 4, 72),
    FARB_MEISTERSCHAFT("Farb-Meisterschaft", "-30% Kosten", SkillCategory.INCREMENTAL, 4, 73),
    UPGRADE_RABATT("Upgrade-Rabatt", "Equipment Upgrades -10%", SkillCategory.INCREMENTAL, 4, 74),

    // Combat (6)
    ROBUSTHEIT_IV("Robustheit IV", "Max HP +50", SkillCategory.COMBAT, 4, 75),
    VERTEIDIGUNG_III("Verteidigung III", "Rüstung +15%", SkillCategory.COMBAT, 4, 76),
    KAMPF_MEISTERSCHAFT_III("Kampf-Meisterschaft III", "+40% XP", SkillCategory.COMBAT, 4, 77),
    SCHADENS_BONUS_IV("Schadens-Bonus IV", "+25% Schaden", SkillCategory.COMBAT, 4, 78),
    MANA_POOL_III("Mana-Pool III", "Max Mana +30", SkillCategory.COMBAT, 4, 79),
    SCHNELLE_ERHOLUNG("Schnelle Erholung", "+50% Heilung", SkillCategory.COMBAT, 4, 80),

    // ========== TIER 5 (Level 81-100) - Legende ==========
    // Incremental (14)
    CLICK_MULTI_V("Click-Multiplikator V", "+100%", SkillCategory.INCREMENTAL, 5, 81),
    AUTO_CLICKER_V("Auto-Clicker V", "+3 clicks/sec", SkillCategory.INCREMENTAL, 5, 82),
    GOETTLICHER_CRIT("Göttlicher Crit", "25% Crit, 5x Damage", SkillCategory.INCREMENTAL, 5, 83, isChoice = true, choiceGroup = 7),
    APOKALYPTISCHER_KLICK("Apokalyptischer Klick", "+300% Click Power", SkillCategory.INCREMENTAL, 5, 83, isChoice = true, choiceGroup = 7),
    GOLD_GOTT("Gold-Gott", "+75% Gold", SkillCategory.INCREMENTAL, 5, 85),
    LOOT_MAGNET("Loot-Magnet", "+20% Drop Chance", SkillCategory.INCREMENTAL, 5, 86),
    IDLE_MEISTER("Idle-Meister", "+75% AFK", SkillCategory.INCREMENTAL, 5, 87),
    PRESTIGE_GOTT("Prestige-Gott", "+50% DE beim Prestige", SkillCategory.INCREMENTAL, 5, 88),
    DE_FINDER_IV("DE-Finder IV", "+50% DE", SkillCategory.INCREMENTAL, 5, 89),
    LOOT_QUALITAET_IV("Loot-Qualität IV", "+50% höhere Rarity", SkillCategory.INCREMENTAL, 5, 90),
    WUERFEL_MEISTER("Würfel-Meister", "+15% höherer Würfel", SkillCategory.INCREMENTAL, 5, 91),
    UPGRADE_MEISTER("Upgrade-Meister", "-20% Upgrade-Kosten", SkillCategory.INCREMENTAL, 5, 92),
    FARB_GOTT("Farb-Gott", "-50% Färbe-Kosten", SkillCategory.INCREMENTAL, 5, 93),
    UNIVERSAL_BONUS("Universal-Bonus", "+10% auf ALLES", SkillCategory.INCREMENTAL, 5, 94),

    // Combat (5)
    ROBUSTHEIT_V("Robustheit V", "Max HP +75", SkillCategory.COMBAT, 5, 95),
    VERTEIDIGUNG_IV("Verteidigung IV", "Rüstung +25%", SkillCategory.COMBAT, 5, 96),
    KAMPF_GOTT("Kampf-Gott", "+60% XP", SkillCategory.COMBAT, 5, 97),
    SCHADENS_BONUS_V("Schadens-Bonus V", "+40% Schaden", SkillCategory.COMBAT, 5, 98),
    MANA_POOL_IV("Mana-Pool IV", "Max Mana +50", SkillCategory.COMBAT, 5, 99),

    // ========== LEVEL 100 - ACTIVE ABILITY ==========
    ZEITDILATATION("⚡ Zeitdilatation", "60s: 5x Click, 2x Auto, 2x Gold/XP (CD: 10 Min)", SkillCategory.INCREMENTAL, 5, 100);

    fun isUnlockable(characterLevel: Int, unlockedSkills: Set<UniversalSkill>): Boolean {
        // Check level requirement
        if (characterLevel < levelRequirement) return false

        // Check prerequisites
        if (prerequisites.isNotEmpty() && !unlockedSkills.containsAll(prerequisites)) return false

        // Check if part of choice group and already chose the other option
        if (isChoice && choiceGroup != null) {
            val otherChoice = values().find {
                it.choiceGroup == choiceGroup && it != this
            }
            if (otherChoice != null && unlockedSkills.contains(otherChoice)) {
                return false  // Already chose the other option
            }
        }

        return true
    }
}
