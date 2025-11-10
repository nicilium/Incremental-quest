package com.tuffgames.incrementalquest

import kotlin.random.Random

// ========== ASCENSION SYSTEM ==========

enum class AscensionUpgrade(
    val displayName: String,
    val description: String,
    val maxLevel: Int,
    val baseCost: Int,
    val costMultiplier: Double = 1.5
) {
    UNIVERSAL_MULTIPLIER(
        displayName = "Universal Multiplier",
        description = "+10% to ALL gains per level",
        maxLevel = 50,
        baseCost = 1
    ),
    PRESTIGE_POTENCY(
        displayName = "Prestige Potency",
        description = "Double DE gain from prestige",
        maxLevel = 1,
        baseCost = 5
    ),
    COMBO_CAP_INCREASE(
        displayName = "Combo Master",
        description = "Increase combo cap: 50→100→200",
        maxLevel = 3,
        baseCost = 3
    ),
    COLOR_HARMONY(
        displayName = "Color Harmony",
        description = "Unlock super-synergies",
        maxLevel = 1,
        baseCost = 10
    ),
    TIME_WARP(
        displayName = "Time Warp",
        description = "+50% passive income generation speed",
        maxLevel = 10,
        baseCost = 2
    ),
    DIVINE_AUTO_CLICKER(
        displayName = "Divine Auto-Clicker",
        description = "+100% auto-clicker speed per level",
        maxLevel = 20,
        baseCost = 1
    ),
    OFFLINE_MASTERY(
        displayName = "Offline Mastery",
        description = "+100% offline gains per level",
        maxLevel = 10,
        baseCost = 2
    ),
    RESEARCH_ACCELERATOR(
        displayName = "Research Accelerator",
        description = "+50% research points generation",
        maxLevel = 5,
        baseCost = 3
    );

    fun getCost(currentLevel: Int): Int {
        if (currentLevel >= maxLevel) return -1
        return (baseCost * costMultiplier.pow(currentLevel)).toInt()
    }

    private fun Double.pow(n: Int): Double {
        return kotlin.math.pow(this, n.toDouble())
    }
}

// ========== ARTIFACTS SYSTEM ==========

enum class ArtifactRarity(
    val displayName: String,
    val color: Int,  // Android Color int
    val dropChance: Double,
    val statMultiplier: Double
) {
    COMMON("Common", android.graphics.Color.LTGRAY, 0.50, 1.0),
    RARE("Rare", android.graphics.Color.rgb(50, 150, 255), 0.30, 1.5),
    EPIC("Epic", android.graphics.Color.rgb(150, 50, 255), 0.15, 2.5),
    LEGENDARY("Legendary", android.graphics.Color.rgb(255, 150, 0), 0.05, 4.0);

    companion object {
        fun randomRarity(): ArtifactRarity {
            val roll = Random.nextDouble()
            return when {
                roll < 0.05 -> LEGENDARY
                roll < 0.20 -> EPIC
                roll < 0.50 -> RARE
                else -> COMMON
            }
        }
    }
}

enum class ArtifactType(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    GOLD_BOOST("Ancient Coin", "🏺", "Increases Gold from all sources"),
    CLICK_SPEED("Lightning Rune", "⚡", "Increases click value"),
    OFFLINE_BOOST("Moon Crystal", "🌙", "Increases offline gains"),
    COMBO_DURATION("Flame Shard", "🔥", "Extends combo window"),
    DE_BOOST("Divine Prism", "💎", "Increases DE from prestige"),
    LUCKY_MULTIPLIER("Lucky Die", "🎲", "Chance for massive multiplier"),
    PASSIVE_BOOST("Eternal Hourglass", "⏳", "Increases passive income"),
    RESEARCH_BOOST("Scholar's Tome", "📖", "Increases research point gain"),
    AUTO_CLICKER_BOOST("Mechanical Heart", "⚙️", "Boosts auto-clicker speed"),
    SYNERGY_AMPLIFIER("Prism of Unity", "🌈", "Amplifies color synergies");

    fun getBaseValue(rarity: ArtifactRarity): Double {
        val base = when (this) {
            GOLD_BOOST -> 0.15
            CLICK_SPEED -> 0.25
            OFFLINE_BOOST -> 0.50
            COMBO_DURATION -> 0.5  // +0.5 seconds
            DE_BOOST -> 0.10
            LUCKY_MULTIPLIER -> 0.05  // 5% chance
            PASSIVE_BOOST -> 0.30
            RESEARCH_BOOST -> 0.25
            AUTO_CLICKER_BOOST -> 0.50
            SYNERGY_AMPLIFIER -> 0.25
        }
        return base * rarity.statMultiplier
    }
}

data class Artifact(
    val id: Int,
    val type: ArtifactType,
    val rarity: ArtifactRarity,
    val value: Double = type.getBaseValue(rarity)
) {
    fun getDisplayName(): String {
        return "${rarity.displayName} ${type.displayName}"
    }

    fun getDescription(): String {
        return when (type) {
            ArtifactType.GOLD_BOOST -> "+${(value * 100).toInt()}% Gold"
            ArtifactType.CLICK_SPEED -> "+${(value * 100).toInt()}% Click Value"
            ArtifactType.OFFLINE_BOOST -> "+${(value * 100).toInt()}% Offline Gains"
            ArtifactType.COMBO_DURATION -> "+${value}s Combo Window"
            ArtifactType.DE_BOOST -> "+${(value * 100).toInt()}% DE from Prestige"
            ArtifactType.LUCKY_MULTIPLIER -> "${(value * 100).toInt()}% chance for 10x click"
            ArtifactType.PASSIVE_BOOST -> "+${(value * 100).toInt()}% Passive Income"
            ArtifactType.RESEARCH_BOOST -> "+${(value * 100).toInt()}% Research Points"
            ArtifactType.AUTO_CLICKER_BOOST -> "+${(value * 100).toInt()}% Auto-Clicker Speed"
            ArtifactType.SYNERGY_AMPLIFIER -> "+${(value * 100).toInt()}% Synergy Bonuses"
        }
    }
}

// ========== RESEARCH SYSTEM ==========

enum class ResearchPath {
    PRODUCTION,
    EFFICIENCY,
    POWER,
    AUTOMATION
}

enum class ResearchNode(
    val displayName: String,
    val description: String,
    val path: ResearchPath,
    val cost: Double,
    val prerequisites: List<ResearchNode> = emptyList()
) {
    // Production Path
    MULTI_CLICK(
        "Multi-Click",
        "Each click counts twice",
        ResearchPath.PRODUCTION,
        100.0
    ),
    PRESTIGE_ECHO(
        "Prestige Echo",
        "Next prestige gives +25% more DE",
        ResearchPath.PRODUCTION,
        250.0,
        listOf(MULTI_CLICK)
    ),
    COLOR_FUSION(
        "Color Fusion",
        "Combine 3 colors for super-color",
        ResearchPath.PRODUCTION,
        500.0,
        listOf(PRESTIGE_ECHO)
    ),

    // Efficiency Path
    COST_REDUCTION_1(
        "Efficiency I",
        "All upgrades -10% cost",
        ResearchPath.EFFICIENCY,
        100.0
    ),
    COST_REDUCTION_2(
        "Efficiency II",
        "All upgrades -20% cost",
        ResearchPath.EFFICIENCY,
        250.0,
        listOf(COST_REDUCTION_1)
    ),
    SMART_PRESTIGE(
        "Smart Prestige",
        "Auto-prestige only at optimal time",
        ResearchPath.EFFICIENCY,
        400.0,
        listOf(COST_REDUCTION_2)
    ),

    // Power Path
    IDLE_MASTERY(
        "Idle Mastery",
        "+100% offline gains",
        ResearchPath.POWER,
        150.0
    ),
    POWER_SURGE(
        "Power Surge",
        "+50% to all active click bonuses",
        ResearchPath.POWER,
        300.0,
        listOf(IDLE_MASTERY)
    ),
    ULTIMATE_COMBO(
        "Ultimate Combo",
        "Combo bonus doubled",
        ResearchPath.POWER,
        600.0,
        listOf(POWER_SURGE)
    ),

    // Automation Path
    AUTO_UPGRADE_BASIC(
        "Auto-Upgrade I",
        "Unlock basic auto-upgrade",
        ResearchPath.AUTOMATION,
        200.0
    ),
    AUTO_UPGRADE_SMART(
        "Auto-Upgrade II",
        "Smart auto-upgrade (most efficient)",
        ResearchPath.AUTOMATION,
        400.0,
        listOf(AUTO_UPGRADE_BASIC)
    ),
    FULL_AUTOMATION(
        "Full Automation",
        "Game plays itself optimally",
        ResearchPath.AUTOMATION,
        1000.0,
        listOf(AUTO_UPGRADE_SMART)
    );
}

// ========== CHALLENGES SYSTEM ==========

enum class ChallengeType(
    val displayName: String,
    val description: String,
    val goal: String,
    val reward: String,
    val rewardValue: Double
) {
    NO_UPGRADES(
        "Minimalist",
        "No color upgrades allowed",
        "Reach 10,000 points",
        "+50% Click Power (Permanent)",
        0.50
    ),
    SPEED_RUN(
        "Speed Demon",
        "Prestige as fast as possible",
        "Prestige in under 5 minutes",
        "+25% DE Gain (Permanent)",
        0.25
    ),
    PASSIVE_ONLY(
        "True Idle",
        "No manual clicking allowed",
        "Reach 50,000 with passive only",
        "+100% Passive Income (Permanent)",
        1.0
    ),
    ONE_COLOR(
        "Monochrome",
        "Only upgrade ONE color",
        "Reach 25,000 points",
        "Unlock Mono-Synergy",
        1.0
    ),
    HARDCORE(
        "Nightmare Mode",
        "All costs 5x higher",
        "Prestige successfully",
        "+200% All Rewards (Permanent)",
        2.0
    ),
    NO_PRESTIGE_UPGRADES(
        "Pure Skill",
        "No permanent upgrades from prestige",
        "Reach 100,000 points",
        "+3 CE on next Ascension",
        3.0
    );

    fun isCompleted(gameState: GameState, challengeStartPoints: Double): Boolean {
        return when (this) {
            NO_UPGRADES -> {
                // Check if any upgrades were bought
                CubeColor.values().all { gameState.getUpgradeLevel(it) == 0 } &&
                gameState.totalScore >= 10000
            }
            SPEED_RUN -> {
                // Check prestige time (would need timestamp tracking)
                true // Placeholder
            }
            PASSIVE_ONLY -> {
                // Would need click tracking during challenge
                gameState.totalScore >= 50000
            }
            ONE_COLOR -> {
                // Check only one color upgraded
                CubeColor.values().count { gameState.getUpgradeLevel(it) > 0 } == 1 &&
                gameState.totalScore >= 25000
            }
            HARDCORE -> {
                // Just need to prestige (costs are modified elsewhere)
                gameState.totalScore >= 5000
            }
            NO_PRESTIGE_UPGRADES -> {
                gameState.totalScore >= 100000
            }
        }
    }
}

// ========== DAILY QUEST SYSTEM ==========

enum class QuestType(
    val displayName: String,
    val description: String,
    val isWeekly: Boolean = false
) {
    // Daily Quests
    CLICK_1000("Click Master", "Make 1,000 clicks today"),
    REACH_COMBO_100("Combo King", "Reach a 100x combo"),
    PRESTIGE_3("Triple Prestige", "Prestige 3 times today"),
    UNLOCK_SYNERGY("Synergy Seeker", "Unlock any synergy"),
    EARN_50K_GOLD("Gold Rush", "Earn 50,000 Gold today"),
    COMPLETE_COMBAT("Warrior", "Win any combat"),
    UPGRADE_10_LEVELS("Power Grind", "Buy 10 upgrade levels"),
    WATCH_5_ADS("Ad Supporter", "Watch 5 ads today"),

    // Weekly Quest
    REACH_1M_POINTS("Weekly Challenge", "Reach 1 Million points in a single run", true);

    fun getReward(): QuestReward {
        return when (this) {
            CLICK_1000 -> QuestReward(gold = 5000)
            REACH_COMBO_100 -> QuestReward(de = 10)
            PRESTIGE_3 -> QuestReward(artifact = true)
            UNLOCK_SYNERGY -> QuestReward(researchPoints = 50.0)
            EARN_50K_GOLD -> QuestReward(de = 5)
            COMPLETE_COMBAT -> QuestReward(gold = 10000)
            UPGRADE_10_LEVELS -> QuestReward(researchPoints = 25.0)
            WATCH_5_ADS -> QuestReward(gold = 15000)
            REACH_1M_POINTS -> QuestReward(artifact = true, artifactRarity = ArtifactRarity.LEGENDARY)
        }
    }
}

data class QuestReward(
    val gold: Int = 0,
    val de: Int = 0,
    val researchPoints: Double = 0.0,
    val artifact: Boolean = false,
    val artifactRarity: ArtifactRarity? = null
)

data class Quest(
    val type: QuestType,
    var progress: Int = 0,
    val goal: Int = 1,
    var completed: Boolean = false
)

// ========== MILESTONES SYSTEM ==========

data class Milestone(
    val lifetimePoints: Long,
    val displayName: String,
    val reward: String,
    val bonus: MilestoneBonus
)

data class MilestoneBonus(
    val passiveMultiplier: Double = 1.0,
    val allMultiplier: Double = 1.0,
    val ceReward: Int = 0,
    val unlockFeature: String? = null
)

val LIFETIME_MILESTONES = listOf(
    Milestone(
        lifetimePoints = 1_000_000L,
        displayName = "First Million",
        reward = "+50% Passive Income Forever",
        bonus = MilestoneBonus(passiveMultiplier = 1.5)
    ),
    Milestone(
        lifetimePoints = 10_000_000L,
        displayName = "Ten Million Club",
        reward = "+10% to All Multipliers Forever",
        bonus = MilestoneBonus(allMultiplier = 1.1)
    ),
    Milestone(
        lifetimePoints = 100_000_000L,
        displayName = "Hundred Million",
        reward = "Unlock Ascension + 5 CE",
        bonus = MilestoneBonus(ceReward = 5, unlockFeature = "Ascension")
    ),
    Milestone(
        lifetimePoints = 1_000_000_000L,
        displayName = "Billion Points",
        reward = "+1 Auto-Prestige per Day",
        bonus = MilestoneBonus(unlockFeature = "DailyAutoPrestige")
    ),
    Milestone(
        lifetimePoints = 10_000_000_000L,
        displayName = "Ten Billion",
        reward = "+25% to All Multipliers Forever",
        bonus = MilestoneBonus(allMultiplier = 1.25)
    )
)
