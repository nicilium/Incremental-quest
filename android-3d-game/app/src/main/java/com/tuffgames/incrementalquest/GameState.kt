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

    // NEW: Permanent Click Multiplier (bought with Gold)
    var permanentMultiplierLevel = 0
        private set

    // Permanent color upgrades with Points (NOT reset on prestige!)
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

    // ========== NEW: Achievement System ==========
    var totalClicksAllTime = 0L
        private set

    var maxComboReached = 0
        private set

    private val unlockedAchievements = mutableSetOf<AchievementType>()

    // ========== Ad-Based Boost System Tracking ==========
    private val activeBoosts = mutableMapOf<BoostType, ActiveBoost>()
    private val boostCooldowns = mutableMapOf<BoostType, BoostCooldown>()
    var prestigeBoostActive = false  // For next prestige: 2x rewards
        private set

    // ========== In-App Purchase System ==========
    private val purchasedIAPs = mutableSetOf<IAPType>()
    private var adsRemoved = false  // Special flag for Remove Ads

    // ========== NEW: Click Combo System ==========
    private var currentCombo = 0
    private var lastClickTime = 0L
    private val comboWindow = 1000L  // 1 second window

    // ========== NEW: Color Synergy System ==========
    private var synergiesEnabled = false  // Unlocked via achievement

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

    // Base cost for permanent color upgrades by dice tier (uses Points now!)
    private fun getColorTierBaseCost(color: CubeColor): Double {
        val basePoints = baseColorPoints[color] ?: 1
        return when (basePoints) {
            in 1..4 -> 5000.0      // D4: 5k Points
            in 5..6 -> 25000.0     // D6: 25k Points
            in 7..8 -> 100000.0    // D8: 100k Points
            in 9..10 -> 500000.0   // D10: 500k Points
            in 11..12 -> 2000000.0 // D12: 2M Points
            else -> 10000000.0     // D20: 10M Points
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
        // Track total clicks
        totalClicksAllTime++

        // Update click combo
        updateCombo()

        // Check for new achievements
        checkAchievements()

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

        // Permanent Click Multiplier (bought with Gold)
        val permanentMulti = getPermanentClickMultiplier()

        // Achievement bonuses
        val achievementBonus = getAchievementClickBonus()

        // Synergy bonuses (if enabled)
        val synergyBonus = if (synergiesEnabled) getSynergyBonus(color) else 0.0

        // Combo multiplier
        val comboMulti = getComboMultiplier()

        // Ad Boost: Click multiplier
        val clickBoostMulti = getBoostMultiplier(BoostType.CLICK_2X)

        // IAP: VIP Multiplier (+50% to all points)
        val vipMulti = getIAPVIPMultiplier()

        // Total points with all multipliers applied
        var totalPoints = (basePoints + upgradeBonus).toDouble() *
                         permanentMulti *
                         (1.0 + achievementBonus + synergyBonus) *
                         comboMulti *
                         clickBoostMulti *
                         vipMulti

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
        val count = upgradeCount[color] ?: 0

        // Exponential scaling: baseCost × 1.15^count
        return baseCost * (1.15).pow(count)
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

        // costMultipliers wird nicht mehr benötigt (backwards compat only)

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

        // Permanent Click Multiplier (bought with Gold)
        val permanentMulti = getPermanentClickMultiplier()

        return ((basePoints + upgradeBonus).toDouble() * permanentMulti).toInt()
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

    // ========== NEW: Permanent Click Multiplier System (Gold-based) ==========

    // Get current click multiplier from permanent upgrades
    fun getPermanentClickMultiplier(): Double {
        // +5% per level
        return 1.0 + (permanentMultiplierLevel * 0.05)
    }

    // Get cost for next permanent multiplier level
    fun getPermanentMultiplierCost(): Int {
        // Cost: 5000 × 1.5^level
        return (5000 * (1.5).pow(permanentMultiplierLevel)).toInt()
    }

    fun canAffordPermanentMultiplier(): Boolean {
        return gold >= getPermanentMultiplierCost()
    }

    fun buyPermanentMultiplier(): Boolean {
        if (!canAffordPermanentMultiplier()) return false

        val cost = getPermanentMultiplierCost()
        gold -= cost
        permanentMultiplierLevel++

        return true
    }

    // ========== NEW: Achievement System Functions ==========

    // Check and unlock achievements
    private fun checkAchievements() {
        AchievementType.values().forEach { achievement ->
            if (!unlockedAchievements.contains(achievement) && achievement.isUnlocked(this)) {
                unlockedAchievements.add(achievement)

                // Special achievement: Unlock synergies
                if (achievement == AchievementType.UNLOCK_SYNERGIES) {
                    synergiesEnabled = true
                }
            }
        }
    }

    // Get total click bonus from achievements
    fun getAchievementClickBonus(): Double {
        return unlockedAchievements.sumOf { it.clickBonus }
    }

    // Get total passive bonus from achievements
    fun getAchievementPassiveBonus(): Double {
        return unlockedAchievements.sumOf { it.passiveBonus }
    }

    // Get all unlocked achievements
    fun getUnlockedAchievements(): List<AchievementType> {
        return unlockedAchievements.toList()
    }

    // Get locked achievements that can be progressed
    fun getLockedAchievements(): List<AchievementType> {
        return AchievementType.values().filter { !unlockedAchievements.contains(it) }
    }

    // ========== Ad-Based Boost Management ==========

    /**
     * Activate a boost after watching an ad
     * @return true if boost was activated, false if on cooldown
     */
    fun activateBoost(type: BoostType): Boolean {
        // Check cooldown (unless IAP removes cooldowns)
        if (!areBoostCooldownsRemoved()) {
            val cooldown = boostCooldowns[type]
            if (cooldown != null && cooldown.isOnCooldown()) {
                return false
            }
        }

        // Calculate end time
        val now = System.currentTimeMillis()
        val durationMillis = type.durationMinutes * 60 * 1000L
        val endTime = now + durationMillis

        // Activate boost
        activeBoosts[type] = ActiveBoost(type, now, endTime)
        boostCooldowns[type] = BoostCooldown(type, now)

        return true
    }

    /**
     * Check if a boost is currently active
     */
    fun isBoostActive(type: BoostType): Boolean {
        cleanupExpiredBoosts()
        val boost = activeBoosts[type]
        return boost != null && boost.isActive()
    }

    /**
     * Get active boost (if any)
     */
    fun getActiveBoost(type: BoostType): ActiveBoost? {
        cleanupExpiredBoosts()
        return activeBoosts[type]
    }

    /**
     * Check if boost is on cooldown
     */
    fun isBoostOnCooldown(type: BoostType): Boolean {
        val cooldown = boostCooldowns[type] ?: return false
        return cooldown.isOnCooldown()
    }

    /**
     * Get cooldown info for a boost
     */
    fun getBoostCooldown(type: BoostType): BoostCooldown? {
        return boostCooldowns[type]
    }

    /**
     * Get all active boosts
     */
    fun getAllActiveBoosts(): List<ActiveBoost> {
        cleanupExpiredBoosts()
        return activeBoosts.values.toList()
    }

    /**
     * Remove expired boosts
     */
    private fun cleanupExpiredBoosts() {
        val expired = activeBoosts.filter { !it.value.isActive() }.keys
        expired.forEach { activeBoosts.remove(it) }
    }

    /**
     * Get multiplier for a specific boost type
     * @return 1.0 if not active, otherwise the multiplier (e.g., 2.0 for 2x)
     */
    fun getBoostMultiplier(type: BoostType): Double {
        // Eternal Boost IAP: All boosts always active!
        if (areEternalBoostsActive()) {
            return type.getMultiplier()
        }

        // Normal: Check if boost is active
        return if (isBoostActive(type)) type.getMultiplier() else 1.0
    }

    /**
     * Get combo window bonus from boosts (in milliseconds)
     */
    fun getComboWindowBonus(): Long {
        return if (isBoostActive(BoostType.COMBO_EXTENDED)) 1000L else 0L
    }

    /**
     * Activate prestige boost (2x DE and Gold for next prestige)
     */
    fun activatePrestigeBoost() {
        prestigeBoostActive = true
    }

    /**
     * Check if prestige boost is ready
     */
    fun isPrestigeBoostActive(): Boolean {
        return prestigeBoostActive
    }

    // ========== In-App Purchase Management ==========

    /**
     * Purchase an IAP
     * @return true if successfully purchased
     */
    fun purchaseIAP(iapType: IAPType): Boolean {
        // Check if already purchased (except consumables)
        if (purchasedIAPs.contains(iapType)) {
            return false  // Already owned
        }

        // Apply IAP effects
        when (iapType) {
            IAPType.GOLDEN_START -> {
                // Consumable: Give resources
                gold += 50000
                divineEssence += 10
            }
            IAPType.MEGA_RESOURCE_PACK -> {
                // Consumable: Give resources
                gold += 500000
                divineEssence += 100
            }
            IAPType.ULTIMATE_BUNDLE -> {
                // Bundle: Activate multiple IAPs
                purchasedIAPs.add(IAPType.PASSIVE_2X)
                purchasedIAPs.add(IAPType.NO_COOLDOWNS)
                purchasedIAPs.add(IAPType.PRESTIGE_MASTER)
                purchasedIAPs.add(IAPType.COMBO_EXPERT)
            }
            IAPType.REMOVE_ADS -> {
                adsRemoved = true
            }
            else -> {
                // Permanent upgrade: just track it
            }
        }

        // Track purchase
        purchasedIAPs.add(iapType)
        return true
    }

    /**
     * Check if an IAP is purchased
     */
    fun hasPurchased(iapType: IAPType): Boolean {
        return purchasedIAPs.contains(iapType)
    }

    /**
     * Check if ads are removed
     */
    fun areAdsRemoved(): Boolean {
        return adsRemoved || hasPurchased(IAPType.REMOVE_ADS)
    }

    /**
     * Get all purchased IAPs
     */
    fun getPurchasedIAPs(): List<IAPType> {
        return purchasedIAPs.toList()
    }

    /**
     * Get IAP multiplier for passive income
     */
    private fun getIAPPassiveMultiplier(): Double {
        var multiplier = 1.0
        if (hasPurchased(IAPType.PASSIVE_2X)) multiplier *= 2.0
        return multiplier
    }

    /**
     * Get IAP multiplier for prestige DE
     */
    private fun getIAPPrestigeDEMultiplier(): Double {
        var multiplier = 1.0
        if (hasPurchased(IAPType.PRESTIGE_MASTER)) multiplier += 0.5  // +50%
        return multiplier
    }

    /**
     * Get IAP multiplier for prestige Gold
     */
    private fun getIAPPrestigeGoldMultiplier(): Double {
        var multiplier = 1.0
        if (hasPurchased(IAPType.PRESTIGE_MASTER)) multiplier *= 2.0
        return multiplier
    }

    /**
     * Get IAP multiplier for VIP (all points)
     */
    private fun getIAPVIPMultiplier(): Double {
        var multiplier = 1.0
        if (hasPurchased(IAPType.VIP_MULTIPLIER)) multiplier += 0.5  // +50%
        return multiplier
    }

    /**
     * Get auto-clicker speed multiplier from IAP
     */
    private fun getIAPAutoClickerSpeedMultiplier(): Double {
        var multiplier = 1.0
        if (hasPurchased(IAPType.AUTO_CLICKER_PRO)) multiplier *= 3.0
        return multiplier
    }

    /**
     * Get auto-clicker cost reduction from IAP
     */
    private fun getIAPAutoClickerCostReduction(): Double {
        return if (hasPurchased(IAPType.AUTO_CLICKER_PRO)) 0.5 else 1.0  // 50% off
    }

    /**
     * Get combo window bonus from IAP (in milliseconds)
     */
    private fun getIAPComboWindowBonus(): Long {
        return if (hasPurchased(IAPType.COMBO_EXPERT)) 500L else 0L  // +0.5 seconds
    }

    /**
     * Get combo bonus multiplier from IAP
     */
    private fun getIAPComboBonusMultiplier(): Double {
        return if (hasPurchased(IAPType.COMBO_EXPERT)) 1.5 else 1.0  // +50% stronger
    }

    /**
     * Check if boost cooldowns are removed
     */
    fun areBoostCooldownsRemoved(): Boolean {
        return hasPurchased(IAPType.NO_COOLDOWNS) || hasPurchased(IAPType.ETERNAL_BOOST)
    }

    /**
     * Check if eternal boosts are active (all boosts always on)
     */
    fun areEternalBoostsActive(): Boolean {
        return hasPurchased(IAPType.ETERNAL_BOOST)
    }

    // ========== NEW: Click Combo System ==========

    private fun updateCombo() {
        val currentTime = System.currentTimeMillis()
        val effectiveComboWindow = comboWindow + getComboWindowBonus() + getIAPComboWindowBonus()  // Apply boosts + IAPs

        if (currentTime - lastClickTime < effectiveComboWindow) {
            // Within combo window, increase combo
            currentCombo++
        } else {
            // Combo expired, reset
            currentCombo = 1
        }

        lastClickTime = currentTime

        // Track max combo reached
        if (currentCombo > maxComboReached) {
            maxComboReached = currentCombo
        }
    }

    // Get combo multiplier (max 3x at 50+ combo)
    private fun getComboMultiplier(): Double {
        if (currentCombo <= 1) return 1.0

        // +4% per combo, max 200% bonus (3x total at 50 combo)
        var bonus = (currentCombo * 0.04).coerceAtMost(2.0)

        // Apply IAP bonus multiplier (+50% stronger with Combo Expert)
        bonus *= getIAPComboBonusMultiplier()

        return 1.0 + bonus
    }

    // Get current combo count (for UI)
    fun getCurrentCombo(): Int = currentCombo

    // ========== NEW: Color Synergy System ==========

    // Get active synergies
    fun getActiveSynergies(): List<ColorSynergy> {
        if (!synergiesEnabled) return emptyList()

        return COLOR_SYNERGIES.filter { synergy ->
            // Check if all colors in synergy meet min level
            synergy.colors.all { color ->
                getUpgradeLevel(color) >= synergy.minLevel
            }
        }
    }

    // Get synergy bonus for specific color
    private fun getSynergyBonus(color: CubeColor): Double {
        val activeSynergies = getActiveSynergies()

        // Sum all synergy bonuses that include this color
        return activeSynergies
            .filter { it.colors.contains(color) }
            .sumOf { it.bonus }
    }

    // Check if synergies are unlocked
    fun areSynergiesUnlocked(): Boolean = synergiesEnabled

    // Permanent color upgrades with Points (stay after prestige!)
    fun getPermanentColorUpgradeLevel(color: CubeColor): Int {
        return permanentColorUpgrades[color] ?: 0
    }

    fun getPermanentColorUpgradeCost(color: CubeColor): Double {
        val currentLevel = getPermanentColorUpgradeLevel(color)

        // Max level is 10
        if (currentLevel >= maxPermanentColorLevel) {
            return Double.MAX_VALUE
        }

        // Cost = baseCost × 2^level (exponential!)
        // Example: RED Level 1 = 5k × 2^0 = 5k, Level 2 = 5k × 2^1 = 10k, Level 3 = 20k, etc.
        val baseCost = getColorTierBaseCost(color)
        return baseCost * (2.0).pow(currentLevel)
    }

    fun canAffordPermanentColorUpgrade(color: CubeColor): Boolean {
        return totalScore >= getPermanentColorUpgradeCost(color)
    }

    fun buyPermanentColorUpgrade(color: CubeColor): Boolean {
        if (!canAffordPermanentColorUpgrade(color)) return false

        val cost = getPermanentColorUpgradeCost(color)
        totalScore -= cost

        val currentLevel = permanentColorUpgrades[color] ?: 0
        permanentColorUpgrades[color] = currentLevel + 1

        return true
    }

    // Calculate passive points per second for a specific color
    fun getPassivePointsPerSecond(color: CubeColor): Double {
        val level = getPermanentColorUpgradeLevel(color)
        if (level == 0) return 0.0

        val basePoints = baseColorPoints[color] ?: 1
        // Level 10 = 1000% (10x) of basePoints/sec, Level 1 = 100%, etc.
        return (level.toDouble() / maxPermanentColorLevel) * basePoints * 10.0
    }

    // Calculate total passive points per second from all colors
    fun getTotalPassivePointsPerSecond(): Double {
        var total = 0.0
        CubeColor.values().forEach { color ->
            total += getPassivePointsPerSecond(color)
        }

        // Apply achievement bonus to passive income
        val achievementBonus = getAchievementPassiveBonus()
        total *= (1.0 + achievementBonus)

        // Apply ad boost to passive income
        val passiveBoostMulti = getBoostMultiplier(BoostType.PASSIVE_2X)
        total *= passiveBoostMulti

        // Apply IAP multipliers
        val iapPassiveMulti = getIAPPassiveMultiplier()  // 2x from Passive IAP
        val vipMulti = getIAPVIPMultiplier()              // +50% from VIP
        total *= iapPassiveMulti * vipMulti

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

    // Auto Clicker Speed Upgrade (now uses Gold!)
    fun getAutoClickerSpeedCost(): Int {
        if (autoClickerSpeedLevel >= 100) return -1  // Max Level
        // Cost: 100 × 1.5^level (with IAP discount)
        val baseCost = (100 * (1.5).pow(autoClickerSpeedLevel)).toInt()
        return (baseCost * getIAPAutoClickerCostReduction()).toInt()
    }

    fun canAffordAutoClickerSpeed(): Boolean {
        if (!autoClickerActive) return false  // Must have auto clicker first
        if (autoClickerSpeedLevel >= 100) return false
        val cost = getAutoClickerSpeedCost()
        return gold >= cost  // Uses Gold now!
    }

    fun buyAutoClickerSpeed(): Boolean {
        if (!canAffordAutoClickerSpeed()) return false

        val cost = getAutoClickerSpeedCost()
        gold -= cost  // Uses Gold now!
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
        return baseCost * rarityMultiplier
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
        val baseCost = 50  // Base cost in gold
        val rarityMultiplier = equipment.rarity.getMultiplier()
        return baseCost * rarityMultiplier * equipment.tier
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

    // Get lootbox cost (uses Gold now!)
    fun getLootboxCost(): Int = 1000  // 1000 Gold per box

    // Can afford lootbox?
    fun canAffordLootbox(): Boolean = gold >= getLootboxCost()

    // Buy lootbox - returns random equipment or null if can't afford
    fun buyLootbox(): Equipment? {
        if (!canAffordLootbox()) return null

        val playerClass = selectedClass ?: return null
        val availableSets = playerClass.getAvailableSets()
        if (availableSets.isEmpty()) return null

        gold -= getLootboxCost()  // Uses Gold now!

        // Random slot
        val slot = EquipmentSlot.values().random()

        // Random set from available sets
        val set = availableSets.random()

        // Random rarity (weighted toward lower rarities)
        val rarity = getRandomRarity()

        // Random tier 1-3 for lootbox drops
        val tier = (1..3).random()

        val equipment = Equipment(slot, set, rarity, tier)
        addItemToInventory(equipment)
        return equipment
    }

    // Get random rarity with weighted probabilities
    private fun getRandomRarity(): EquipmentRarity {
        val roll = (1..100).random()
        return when {
            roll <= 50 -> EquipmentRarity.GRAU    // 50%
            roll <= 75 -> EquipmentRarity.WEISS   // 25%
            roll <= 90 -> EquipmentRarity.GRUEN   // 15%
            roll <= 98 -> EquipmentRarity.BLAU    // 8%
            else -> EquipmentRarity.LILA          // 2%
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
    fun getActiveSetBonuses(): Map<EquipmentSet, Int> {
        val setBonuses = mutableMapOf<EquipmentSet, Int>()
        equippedItems.values.forEach { equipment ->
            setBonuses[equipment.set] = setBonuses.getOrDefault(equipment.set, 0) + 1
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
        val baseDamage = equipStats.weaponDamage + strMod + (player.stats.level / 2)
        val damage = calculateDamage(baseDamage, target.enemy.armor)

        target.enemy.takeDamage(damage)
        combat.addLog("${player.name} greift ${target.name} an und macht $damage Schaden!")

        if (target.enemy.isDead()) {
            combat.addLog("${target.name} wurde besiegt!", true)
        }

        advanceTurn(combat)
        return true
    }

    // Execute player ability
    fun executePlayerAbility(ability: PaladinAbility, targetIndex: Int = 0): Boolean {
        val combat = activeCombat ?: return false
        if (!combat.isCurrentPlayerTurn()) return false

        val player = combat.getCurrentParticipant() as? CombatParticipant.PlayerParticipant ?: return false

        // Check cooldown
        if (combat.abilityCooldowns.getOrDefault(ability, 0) > 0) {
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

        // Execute ability effect
        when {
            ability.baseDamage > 0 -> {
                // Damage ability
                val target = combat.enemyParty.getOrNull(targetIndex) ?: return false
                if (!target.isAlive()) return false

                val damage = calculateAbilityDamage(ability, player)
                target.enemy.takeDamage(damage)
                combat.addLog("${player.name} nutzt ${ability.displayName} gegen ${target.name} und macht $damage Schaden!")

                if (target.enemy.isDead()) {
                    combat.addLog("${target.name} wurde besiegt!", true)
                }
            }
            ability.baseHealing > 0 -> {
                // Healing ability
                val healing = calculateHealing(ability, player)
                player.stats.heal(healing)
                combat.addLog("${player.name} nutzt ${ability.displayName} und heilt sich um $healing HP!")
            }
            else -> {
                // Buff/Utility ability
                combat.addLog("${player.name} nutzt ${ability.displayName}!")
            }
        }

        // Set cooldown for COMBAT abilities
        if (ability.type == AbilityType.COMBAT) {
            combat.abilityCooldowns[ability] = ability.cost
        }

        advanceTurn(combat)
        return true
    }

    // Calculate damage with AC reduction
    private fun calculateDamage(baseDamage: Int, targetAC: Int): Int {
        // Simple formula: damage - (AC / 4)
        val reduction = targetAC / 4
        return (baseDamage - reduction).coerceAtLeast(1)
    }

    // Calculate ability damage
    private fun calculateAbilityDamage(ability: PaladinAbility, player: CombatParticipant.PlayerParticipant): Int {
        val equipStats = getTotalEquipmentStats()
        val baseDamage = ability.getDamage(player.stats.level)
        val weaponBonus = equipStats.weaponDamage / 2
        return baseDamage + weaponBonus
    }

    // Calculate healing
    private fun calculateHealing(ability: PaladinAbility, player: CombatParticipant.PlayerParticipant): Int {
        val equipStats = getTotalEquipmentStats()
        val baseHealing = ability.getHealing(player.stats.level)
        val healingBonus = (baseHealing * equipStats.healingPowerPercent) / 100
        return baseHealing + healingBonus
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

        // Reduce cooldowns
        combat.abilityCooldowns.keys.forEach { ability ->
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
    }

    // End combat and apply rewards
    private fun endCombat() {
        val combat = activeCombat ?: return
        val result = combat.combatResult ?: return

        if (result.victory) {
            // Apply XP
            giveExperience(result.xpGained)

            // Apply Gold (with ad boost)
            val goldBoostMulti = getBoostMultiplier(BoostType.GOLD_2X)
            val finalGold = (result.goldGained * goldBoostMulti).toInt()
            gold += finalGold

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

        // IAP: Auto-Clicker Pro (3x speed = 1/3 interval)
        val iapSpeedMulti = getIAPAutoClickerSpeedMultiplier()
        if (iapSpeedMulti > 1.0) {
            interval = (interval / iapSpeedMulti).toLong()
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
        val points = lifetimeScore

        // Calculate DE reward (milestone-based system)
        val deReward = when {
            points < 5000 -> 0          // Too early
            points < 50000 -> 1         // 5k-50k: 1 DE
            points < 200000 -> 2        // 50k-200k: 2 DE
            points < 500000 -> 3        // 200k-500k: 3 DE
            points < 1000000 -> 5       // 500k-1M: 5 DE
            else -> 5 + (points / 1000000).toInt()  // 1M+: 5 + 1 per M
        }

        if (deReward <= 0) return false

        // Apply Essence Multiplier buff if active
        var finalDEReward = deReward
        activeEssenceBuff?.let { buff ->
            if (System.currentTimeMillis() <= buff.endTime) {
                finalDEReward = (finalDEReward * buff.multiplier).toInt()
            }
        }

        // Calculate Gold reward (generous!)
        var goldReward = (points / 100).toInt()

        // Apply Prestige Boost (from ad) - 2x rewards!
        var boostedDEReward = finalDEReward
        if (prestigeBoostActive) {
            boostedDEReward *= 2
            goldReward *= 2
            prestigeBoostActive = false  // Consume boost
        }

        // Apply IAP multipliers
        val iapDEMulti = getIAPPrestigeDEMultiplier()      // +50% from Prestige Master
        val iapGoldMulti = getIAPPrestigeGoldMultiplier()  // 2x from Prestige Master
        boostedDEReward = (boostedDEReward * iapDEMulti).toInt()
        goldReward = (goldReward * iapGoldMulti).toInt()

        // Apply rewards
        divineEssence += boostedDEReward
        totalDivineEssenceEarned += boostedDEReward
        prestigesClaimed++
        gold += goldReward

        // Reset everything (except divineEssence, gold, lifetimeScore, permanentUpgrades)
        totalScore = 0.0
        upgradesUnlocked = false
        upgradeLevels.keys.forEach { upgradeLevels[it] = 0 }
        upgradeCount.keys.forEach { upgradeCount[it] = 0 }
        // costMultipliers no longer needed (backwards compat only)

        return true
    }

    fun reset() {
        totalScore = 0.0
        lifetimeScore = 0.0
        upgradesUnlocked = false
        divineEssence = 0
        gold = 0
        totalDivineEssenceEarned = 0
        prestigesClaimed = 0
        permanentMultiplierLevel = 0
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

        // NEW: Reset Achievement & Combo System
        totalClicksAllTime = 0L
        maxComboReached = 0
        currentCombo = 0
        lastClickTime = 0L
        unlockedAchievements.clear()

        // NEW: Reset Synergy System
        synergiesEnabled = false

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
        editor.putInt("gold", gold)
        editor.putInt("totalPaintCansEarned", totalDivineEssenceEarned)  // Keep old key for compatibility
        editor.putInt("prestigesClaimed", prestigesClaimed)

        // NEW: Permanent Click Multiplier
        editor.putInt("permanentMultiplierLevel", permanentMultiplierLevel)

        // OLD: Essence Power Level (kept for backwards compatibility, but set to 0)
        editor.putInt("essencePowerLevel", 0)

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

        // NEW: Achievement System
        editor.putLong("totalClicksAllTime", totalClicksAllTime)
        editor.putInt("maxComboReached", maxComboReached)
        editor.putString("unlockedAchievements", unlockedAchievements.joinToString(",") { it.name })

        // NEW: Synergy System
        editor.putBoolean("synergiesEnabled", synergiesEnabled)

        // NEW: Ad-Based Boost System
        editor.putBoolean("prestigeBoostActive", prestigeBoostActive)

        // Save active boosts
        editor.putInt("activeBoostsCount", activeBoosts.size)
        activeBoosts.values.forEachIndexed { index, boost ->
            editor.putString("activeBoost_${index}_type", boost.type.name)
            editor.putLong("activeBoost_${index}_startTime", boost.startTime)
            editor.putLong("activeBoost_${index}_endTime", boost.endTime)
        }

        // Save boost cooldowns
        editor.putInt("boostCooldownsCount", boostCooldowns.size)
        boostCooldowns.values.forEachIndexed { index, cooldown ->
            editor.putString("boostCooldown_${index}_type", cooldown.type.name)
            editor.putLong("boostCooldown_${index}_lastUsed", cooldown.lastUsedTime)
        }

        // NEW: In-App Purchase System
        editor.putString("purchasedIAPs", purchasedIAPs.joinToString(",") { it.name })
        editor.putBoolean("adsRemoved", adsRemoved)

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

        gold = try {
            prefs.getInt("gold", 0)
        } catch (e: ClassCastException) {
            0
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

        // NEW: Permanent Click Multiplier
        permanentMultiplierLevel = try {
            prefs.getInt("permanentMultiplierLevel", 0)
        } catch (e: ClassCastException) {
            0
        }

        // OLD: Essence Power Level - REMOVED, kept for backwards compat but always 0
        // Migration: Convert old essence power to gold (generous compensation!)
        val oldEssencePowerLevel = try {
            prefs.getInt("essencePowerLevel", 0)
        } catch (e: ClassCastException) {
            0
        }
        if (oldEssencePowerLevel > 0) {
            // Give generous gold compensation: 10000 gold per old level
            gold += oldEssencePowerLevel * 10000
        }

        // Also migrate old tier system
        if (prefs.contains("paintCanBonusUpgrade_0")) {
            // Old save detected - count how many tiers were purchased
            var oldTiersCount = 0
            for (i in 0 until 10) {
                if (prefs.getBoolean("paintCanBonusUpgrade_$i", false)) {
                    oldTiersCount++
                }
            }
            // Give compensation: 30000 gold per old tier
            gold += oldTiersCount * 30000
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

        // NEW: Achievement System
        totalClicksAllTime = prefs.getLong("totalClicksAllTime", 0L)
        maxComboReached = prefs.getInt("maxComboReached", 0)

        val achievementsString = prefs.getString("unlockedAchievements", "")
        unlockedAchievements.clear()
        if (!achievementsString.isNullOrEmpty()) {
            achievementsString.split(",").forEach { name ->
                try {
                    unlockedAchievements.add(AchievementType.valueOf(name))
                } catch (e: IllegalArgumentException) {
                    // Ignore invalid achievement names
                }
            }
        }

        // NEW: Synergy System
        synergiesEnabled = prefs.getBoolean("synergiesEnabled", false)

        // NEW: Ad-Based Boost System
        prestigeBoostActive = prefs.getBoolean("prestigeBoostActive", false)

        // Load active boosts
        activeBoosts.clear()
        val activeBoostsCount = prefs.getInt("activeBoostsCount", 0)
        for (i in 0 until activeBoostsCount) {
            val typeName = prefs.getString("activeBoost_${i}_type", null)
            val startTime = prefs.getLong("activeBoost_${i}_startTime", 0L)
            val endTime = prefs.getLong("activeBoost_${i}_endTime", 0L)

            if (typeName != null) {
                try {
                    val type = BoostType.valueOf(typeName)
                    val boost = ActiveBoost(type, startTime, endTime)
                    if (boost.isActive()) {  // Only restore if still active
                        activeBoosts[type] = boost
                    }
                } catch (e: IllegalArgumentException) {
                    // Ignore invalid boost types
                }
            }
        }

        // Load boost cooldowns
        boostCooldowns.clear()
        val boostCooldownsCount = prefs.getInt("boostCooldownsCount", 0)
        for (i in 0 until boostCooldownsCount) {
            val typeName = prefs.getString("boostCooldown_${i}_type", null)
            val lastUsedTime = prefs.getLong("boostCooldown_${i}_lastUsed", 0L)

            if (typeName != null) {
                try {
                    val type = BoostType.valueOf(typeName)
                    boostCooldowns[type] = BoostCooldown(type, lastUsedTime)
                } catch (e: IllegalArgumentException) {
                    // Ignore invalid boost types
                }
            }
        }

        // NEW: In-App Purchase System
        val iapsString = prefs.getString("purchasedIAPs", "")
        purchasedIAPs.clear()
        if (!iapsString.isNullOrEmpty()) {
            iapsString.split(",").forEach { name ->
                try {
                    purchasedIAPs.add(IAPType.valueOf(name))
                } catch (e: IllegalArgumentException) {
                    // Ignore invalid IAP names
                }
            }
        }
        adsRemoved = prefs.getBoolean("adsRemoved", false)

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
    LILA("Lila", android.graphics.Color.rgb(138, 43, 226), 0); // Maximale Rarity (5. Stufe)

    fun next(): EquipmentRarity? = when(this) {
        GRAU -> WEISS
        WEISS -> GRUEN
        GRUEN -> BLAU
        BLAU -> LILA
        LILA -> null  // Max erreicht
    }

    // Multiplier für Stats (1, 2, 4, 8, 16)
    fun getMultiplier(): Int = when(this) {
        GRAU -> 1
        WEISS -> 2
        GRUEN -> 4
        BLAU -> 8
        LILA -> 16
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
    PALADIN_SET1("Heiliger Beschützer", "Fokus auf Tank/Defense, hohe Rüstung, kann mehr aushalten"),
    PALADIN_SET2("Lichträcher", "Fokus auf heiligen Schaden, Balance zwischen Tank und Damage"),
    PALADIN_SET3("Heilung", "Fokus auf Heilung, erhöhte Heilung, kann auch andere heilen",
        "🩹 Hidden: Heilt deinen Schaden an Verbündete (Lifesteal für Team)");

    fun hasHiddenEffect(): Boolean = hiddenEffect != null
}

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
    var tier: Int = 1  // Stufe 1-10
) {
    fun canUpgrade(): Boolean = tier < 10
    fun canCombine(): Boolean = rarity != EquipmentRarity.LILA

    // Get rarity multiplier (1, 2, 4, 8, 16)
    private fun getRarityMultiplier(): Int = rarity.getMultiplier()

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

    // ULTIMATES
    // Level 10 - Erste Ulti
    UNERSCHUETTERLICHE_FESTUNG("Unerschütterliche Festung", "4 Runden UNVERWUNDBAR + zieht alle Angriffe + heilt 10% HP/Runde",
        AbilityType.COMBAT, AbilityCategory.ULTIMATE, 10, 1, baseDuration = 4),

    // Level 20 - Zweite Ulti
    URTEIL_DES_LICHTS("Urteil des Lichts", "Massiver heiliger AOE-Schaden + heilt Verbündete für 50% des Schadens",
        AbilityType.SPELL, AbilityCategory.ULTIMATE, 20, 1, baseDamage = 100),

    // Level 30 - Dritte Ulti
    GOETTLICHE_INTERVENTION("Göttliche Intervention", "Vollheilung aller + entfernt alle Debuffs + Immunität 3 Runden",
        AbilityType.SPELL, AbilityCategory.ULTIMATE, 30, 1, baseHealing = 999, baseDuration = 3);

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

    // Beschreibung mit Level-Werten
    fun getScaledDescription(characterLevel: Int): String {
        val dmg = if (baseDamage > 0) "${getDamage(characterLevel)} Schaden" else ""
        val heal = if (baseHealing > 0 && baseHealing < 999) "${getHealing(characterLevel)} HP"
                  else if (baseHealing >= 999) "Vollheilung" else ""
        val dur = if (baseDuration > 0) "${getDuration(characterLevel)} Runden" else ""
        val buff = if (this == SEGEN_DER_STAERKE) "+${getBuffPercentage(characterLevel)}% Angriff"
                  else if (this == VERTEIDIGUNGSHALTUNG) "-${getBuffPercentage(characterLevel)}% Schaden"
                  else if (this == VERGELTUNGSSCHLAG) "${getBuffPercentage(characterLevel)}% Konter"
                  else ""

        return buildString {
            append(description)
            if (dmg.isNotEmpty()) append(" [$dmg]")
            if (heal.isNotEmpty()) append(" [$heal]")
            if (dur.isNotEmpty()) append(" [$dur]")
            if (buff.isNotEmpty()) append(" [$buff]")
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
        val xpTable = listOf(
            0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000,
            85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000
        )
        return if (level < 20) xpTable[level] else 999999
    }

    fun canLevelUp(): Boolean = experience >= getNextLevelXP() && level < 20

    fun levelUp(playerClass: PlayerClass) {
        if (!canLevelUp()) return

        level++

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
}

// Character Loadout (vor Abenteuer gewählt, skaliert mit Level)
data class CharacterLoadout(
    val normalAbilities: MutableList<PaladinAbility?> = mutableListOf(null, null, null, null, null),  // Max 5
    var ultimateAbility: PaladinAbility? = null
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
        else -> {
            val rarityRoll = (1..100).random()
            when {
                rarityRoll <= 20 -> EquipmentRarity.GRAU   // 20%
                rarityRoll <= 45 -> EquipmentRarity.WEISS  // 25%
                rarityRoll <= 70 -> EquipmentRarity.GRUEN  // 25%
                rarityRoll <= 90 -> EquipmentRarity.BLAU   // 20%
                else -> EquipmentRarity.LILA               // 10%
            }
        }
    }

    // Tier based on enemy level
    val tier = ((enemyLevel / 2) + 1).coerceIn(1, 10)

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

    fun checkCombatEnd(): Boolean {
        val allEnemiesDead = enemyParty.all { it.enemy.isDead() }
        val allPlayersDead = playerParty.all { !it.isAlive() }

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

// ========== Ad-Based Boost System ==========

/**
 * Boost types available through watching ads
 */
enum class BoostType(
    val displayName: String,
    val description: String,
    val emoji: String,
    val durationMinutes: Int,
    val cooldownMinutes: Int
) {
    GOLD_2X(
        displayName = "2x Gold",
        description = "Double all Gold earned",
        emoji = "🪙",
        durationMinutes = 30,
        cooldownMinutes = 240  // 4 hours
    ),
    CLICK_2X(
        displayName = "2x Click Power",
        description = "Double your click value",
        emoji = "🎯",
        durationMinutes = 20,
        cooldownMinutes = 240  // 4 hours
    ),
    PASSIVE_2X(
        displayName = "2x Passive Income",
        description = "Double passive income from auto-clicker",
        emoji = "💤",
        durationMinutes = 60,
        cooldownMinutes = 240  // 4 hours
    ),
    COMBO_EXTENDED(
        displayName = "Extended Combo Window",
        description = "Combo window +1 second (easier combos!)",
        emoji = "🔥",
        durationMinutes = 15,
        cooldownMinutes = 180  // 3 hours
    );

    fun getMultiplier(): Double {
        return when (this) {
            GOLD_2X, CLICK_2X, PASSIVE_2X -> 2.0
            COMBO_EXTENDED -> 1.0  // Not a multiplier, changes combo window
        }
    }
}

/**
 * Represents an active boost with expiration time
 */
data class ActiveBoost(
    val type: BoostType,
    val startTime: Long,      // System.currentTimeMillis() when activated
    val endTime: Long         // When boost expires
) {
    fun isActive(): Boolean {
        return System.currentTimeMillis() < endTime
    }

    fun getRemainingSeconds(): Int {
        val remaining = (endTime - System.currentTimeMillis()) / 1000
        return remaining.toInt().coerceAtLeast(0)
    }

    fun getRemainingMinutes(): Int {
        return (getRemainingSeconds() / 60).coerceAtLeast(0)
    }
}

/**
 * Tracks when a boost was last used (for cooldown)
 */
data class BoostCooldown(
    val type: BoostType,
    val lastUsedTime: Long    // System.currentTimeMillis() when last activated
) {
    fun isOnCooldown(): Boolean {
        val cooldownMillis = type.cooldownMinutes * 60 * 1000L
        return System.currentTimeMillis() < (lastUsedTime + cooldownMillis)
    }

    fun getRemainingCooldownSeconds(): Int {
        val cooldownMillis = type.cooldownMinutes * 60 * 1000L
        val remaining = ((lastUsedTime + cooldownMillis) - System.currentTimeMillis()) / 1000
        return remaining.toInt().coerceAtLeast(0)
    }

    fun getRemainingCooldownMinutes(): Int {
        return (getRemainingCooldownSeconds() / 60).coerceAtLeast(0)
    }
}

// ========== NEW: Achievement System ==========

enum class AchievementType(
    val displayName: String,
    val description: String,
    val clickBonus: Double = 0.0,      // Multiplier bonus for clicks
    val passiveBonus: Double = 0.0,    // Multiplier bonus for passive income
    val requirement: (GameState) -> Boolean
) {
    // Click-based achievements
    FIRST_CLICK(
        "First Steps",
        "Make your first click",
        clickBonus = 0.05,
        requirement = { it.totalClicksAllTime >= 1 }
    ),
    CLICKER_100(
        "Clicker Novice",
        "Make 100 clicks",
        clickBonus = 0.10,
        requirement = { it.totalClicksAllTime >= 100 }
    ),
    CLICKER_1000(
        "Clicker Adept",
        "Make 1,000 clicks",
        clickBonus = 0.15,
        requirement = { it.totalClicksAllTime >= 1000 }
    ),
    CLICKER_10000(
        "Clicker Master",
        "Make 10,000 clicks",
        clickBonus = 0.25,
        requirement = { it.totalClicksAllTime >= 10000 }
    ),
    CLICKER_100000(
        "Clicker Legend",
        "Make 100,000 clicks",
        clickBonus = 0.50,
        requirement = { it.totalClicksAllTime >= 100000 }
    ),

    // Prestige-based achievements
    FIRST_PRESTIGE(
        "Rebirth",
        "Perform your first prestige",
        clickBonus = 0.10,
        requirement = { it.prestigesClaimed >= 1 }
    ),
    PRESTIGE_10(
        "Prestige Veteran",
        "Prestige 10 times",
        clickBonus = 0.25,
        requirement = { it.prestigesClaimed >= 10 }
    ),
    PRESTIGE_50(
        "Prestige Master",
        "Prestige 50 times",
        clickBonus = 0.50,
        passiveBonus = 0.25,
        requirement = { it.prestigesClaimed >= 50 }
    ),

    // Dice progression achievements
    UNLOCK_D20(
        "Rainbow Master",
        "Unlock the D20",
        clickBonus = 0.25,
        requirement = { it.d20Active }
    ),
    ALL_COLORS_LEVEL_10(
        "Color Mastery",
        "Get all unlocked colors to Level 10",
        clickBonus = 0.50,
        requirement = {
            val availableColors = it.getAvailableColors()
            availableColors.all { color -> it.getUpgradeLevel(color) >= 10 }
        }
    ),

    // Wealth achievements
    LIFETIME_100K(
        "Wealthy",
        "Earn 100,000 lifetime points",
        passiveBonus = 0.10,
        requirement = { it.lifetimeScore >= 100000 }
    ),
    LIFETIME_1M(
        "Rich",
        "Earn 1 million lifetime points",
        passiveBonus = 0.25,
        requirement = { it.lifetimeScore >= 1000000 }
    ),
    LIFETIME_10M(
        "Tycoon",
        "Earn 10 million lifetime points",
        passiveBonus = 0.50,
        clickBonus = 0.25,
        requirement = { it.lifetimeScore >= 10000000 }
    ),

    // Combat achievements
    FIRST_COMBAT(
        "Warrior's Path",
        "Win your first combat",
        clickBonus = 0.10,
        requirement = { it.storyCompletedCount >= 1 || it.auftragCompletedCount >= 1 }
    ),
    COMBAT_10(
        "Veteran Fighter",
        "Win 10 combats",
        clickBonus = 0.20,
        requirement = { (it.storyCompletedCount + it.auftragCompletedCount) >= 10 }
    ),

    // Combo achievements
    COMBO_10(
        "Combo Starter",
        "Reach a 10x combo",
        clickBonus = 0.10,
        requirement = { it.maxComboReached >= 10 }
    ),
    COMBO_50(
        "Combo Expert",
        "Reach a 50x combo",
        clickBonus = 0.25,
        requirement = { it.maxComboReached >= 50 }
    ),
    COMBO_100(
        "Combo Master",
        "Reach a 100x combo",
        clickBonus = 0.50,
        requirement = { it.maxComboReached >= 100 }
    ),

    // Special achievements (unlock features)
    UNLOCK_SYNERGIES(
        "Synergy Seeker",
        "Get any 3 colors to Level 5+",
        clickBonus = 0.0,
        requirement = {
            it.getAvailableColors().count { color -> it.getUpgradeLevel(color) >= 5 } >= 3
        }
    );

    fun isUnlocked(gameState: GameState): Boolean = requirement(gameState)
}

data class ColorSynergy(
    val name: String,
    val colors: List<CubeColor>,
    val minLevel: Int,
    val bonus: Double,  // Multiplier bonus
    val description: String
)

// Predefined synergies
val COLOR_SYNERGIES = listOf(
    ColorSynergy(
        name = "Primary Colors",
        colors = listOf(CubeColor.RED, CubeColor.GREEN, CubeColor.BLUE),
        minLevel = 10,
        bonus = 0.50,
        description = "Red, Green, Blue all at Level 10+ → +50% to these colors"
    ),
    ColorSynergy(
        name = "Secondary Colors",
        colors = listOf(CubeColor.YELLOW, CubeColor.MAGENTA, CubeColor.CYAN),
        minLevel = 10,
        bonus = 0.50,
        description = "Yellow, Magenta, Cyan all at Level 10+ → +50% to these colors"
    ),
    ColorSynergy(
        name = "Warm Spectrum",
        colors = listOf(CubeColor.RED, CubeColor.ORANGE, CubeColor.YELLOW, CubeColor.PINK),
        minLevel = 5,
        bonus = 0.25,
        description = "All warm colors at Level 5+ → +25% to these colors"
    ),
    ColorSynergy(
        name = "Cool Spectrum",
        colors = listOf(CubeColor.BLUE, CubeColor.CYAN, CubeColor.TURQUOISE, CubeColor.TEAL),
        minLevel = 5,
        bonus = 0.25,
        description = "All cool colors at Level 5+ → +25% to these colors"
    ),
    ColorSynergy(
        name = "Rainbow Unity",
        colors = CubeColor.values().toList(),
        minLevel = 5,
        bonus = 1.0,
        description = "ALL colors at Level 5+ → +100% to ALL colors!"
    )
)
