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
        val basePoints = baseColorPoints[color] ?: 0

        // Upgrade bonus: simply basePoints × count (Red: +1/level, Green: +2/level, etc.)
        val upgradeCount = upgradeCount[color] ?: 0
        val upgradeBonus = basePoints * upgradeCount

        // Permanent color upgrades: +1 per upgrade (survives prestige!)
        val permanentBonus = permanentColorUpgrades[color] ?: 0

        // Divine Essence bonus (multiplicative with level-based scaling):
        val essenceBonus = calculateEssenceBonus()

        // Total points (as Double because of Divine Essence bonus)
        var totalPoints = (basePoints + upgradeBonus + permanentBonus).toDouble() + essenceBonus

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

        // Permanent color upgrades: +1 per upgrade (survives prestige!)
        val permanentBonus = permanentColorUpgrades[color] ?: 0

        // Divine Essence bonus (multiplicative with level-based scaling):
        val essenceBonus = calculateEssenceBonus()

        return (basePoints + upgradeBonus + permanentBonus + essenceBonus).toInt()
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
    private fun calculateEssenceBonus(): Double {
        if (essencePowerLevel == 0) {
            // Base: 1% of total earned essence
            return totalDivineEssenceEarned * 0.01
        }

        // Base multiplier: 1%
        var multiplier = 0.01

        // Level bonus: level^1.3 × 0.01
        val levelBonus = essencePowerLevel.toDouble().pow(1.3) * 0.01
        multiplier += levelBonus

        // Powerspike bonuses (cumulative)
        val powerspikeBonus = when {
            essencePowerLevel >= 100 -> 1.0  // +100% at level 100
            essencePowerLevel >= 50 -> 0.4   // +40% at level 50
            essencePowerLevel >= 25 -> 0.2   // +20% at level 25
            essencePowerLevel >= 10 -> 0.1   // +10% at level 10
            else -> 0.0
        }
        multiplier += powerspikeBonus

        return totalDivineEssenceEarned * multiplier
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
        if (essencePowerLevel == 0) return 0.01  // 1% base

        var multiplier = 0.01
        val levelBonus = essencePowerLevel.toDouble().pow(1.3) * 0.01
        multiplier += levelBonus

        val powerspikeBonus = when {
            essencePowerLevel >= 100 -> 1.0
            essencePowerLevel >= 50 -> 0.4
            essencePowerLevel >= 25 -> 0.2
            essencePowerLevel >= 10 -> 0.1
            else -> 0.0
        }
        multiplier += powerspikeBonus

        return multiplier
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

        if (currentLevel == 0) {
            return 1  // Initial: 1 Lackdose
        }

        // Kosten erhöhen sich um ×1.001 pro Level (aufgerundet)
        var cost = 1.0
        for (i in 1..currentLevel) {
            cost *= 1.001
        }

        return kotlin.math.ceil(cost).toInt()
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
        val available = getAvailablePrestigeRewards()
        if (available <= 0) return false

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
