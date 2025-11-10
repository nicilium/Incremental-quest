package com.tuffgames.incrementalquest

import android.app.Activity
import android.util.Log

/**
 * IAPManager - In-App Purchase Interface
 *
 * PLACEHOLDER: Aktuell simuliert dies IAPs mit direktem Callback.
 * TODO: Später mit echtem Billing SDK integrieren (Google Play Billing Library)
 */
object IAPManager {

    private const val TAG = "IAPManager"

    // Für echte IAPs später: Initialisierung mit BillingClient
    fun initialize(activity: Activity) {
        Log.d(TAG, "IAPManager initialized (placeholder mode)")
        // TODO: Echte Billing Client Initialisierung
        // Example: billingClient = BillingClient.newBuilder(activity).enablePendingPurchases().build()
    }

    /**
     * Startet den Kaufprozess für ein IAP
     * @param activity Die Activity, in der der Kauf gestartet wird
     * @param iapType Der IAP-Typ
     * @param onPurchaseSuccess Callback wenn Kauf erfolgreich
     * @param onPurchaseFailed Callback wenn Kauf fehlschlägt
     */
    fun purchaseIAP(
        activity: Activity,
        iapType: IAPType,
        onPurchaseSuccess: () -> Unit,
        onPurchaseFailed: (String) -> Unit
    ) {
        Log.d(TAG, "purchaseIAP called for: ${iapType.productId}")

        // PLACEHOLDER: Simuliere erfolgreichen Kauf
        // In echter Implementierung: Starte Billing Flow

        // TODO: Echten Purchase Flow starten
        // Example:
        // val productDetails = ProductDetails(...)
        // val billingFlowParams = BillingFlowParams.newBuilder()
        //     .setProductDetailsParamsList(listOf(productDetails))
        //     .build()
        // billingClient.launchBillingFlow(activity, billingFlowParams)

        // PLACEHOLDER: Direkter Erfolg (für Entwicklung/Testing)
        Log.d(TAG, "Purchase successful for: ${iapType.displayName} (placeholder)")
        onPurchaseSuccess()
    }

    /**
     * Überprüft ob ein IAP bereits gekauft wurde
     * @return true wenn gekauft, false sonst
     */
    fun isPurchased(iapType: IAPType): Boolean {
        // PLACEHOLDER: Immer false (Käufe werden im GameState getrackt)
        // TODO: Echte Purchase-Überprüfung via Billing Client
        // return billingClient.queryPurchasesAsync(...)
        return false  // GameState managed purchases
    }

    /**
     * Stellt Käufe wieder her (für neues Gerät, etc.)
     */
    fun restorePurchases(
        activity: Activity,
        onRestoreComplete: (List<IAPType>) -> Unit
    ) {
        Log.d(TAG, "restorePurchases called")
        // TODO: Echte Purchase-Wiederherstellung
        // Example:
        // billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP) { result, purchases ->
        //     val restoredIAPs = purchases.mapNotNull { ... }
        //     onRestoreComplete(restoredIAPs)
        // }

        // PLACEHOLDER: Keine Käufe zum wiederherstellen
        onRestoreComplete(emptyList())
    }
}

/**
 * IAP-Typen mit Preisen und Produktinformationen
 */
enum class IAPType(
    val productId: String,
    val displayName: String,
    val description: String,
    val priceString: String,  // Display price
    val tier: Int,            // 1=Starter, 2=Progression, 3=Power, 4=Endgame
    val emoji: String
) {
    // Tier 1: Starter Boosts
    PASSIVE_2X(
        productId = "com.tuffgames.incrementalquest.passive_2x",
        displayName = "2x Passive Income",
        description = "Permanently double all passive income!\nPerfect for idle players.",
        priceString = "$1.99",
        tier = 1,
        emoji = "💤"
    ),
    GOLDEN_START(
        productId = "com.tuffgames.incrementalquest.golden_start",
        displayName = "Golden Start Pack",
        description = "50,000 Gold + 10 Divine Essence\nGreat kickstart for new players!",
        priceString = "$0.99",
        tier = 1,
        emoji = "🎁"
    ),
    NO_COOLDOWNS(
        productId = "com.tuffgames.incrementalquest.no_cooldowns",
        displayName = "No Boost Cooldowns",
        description = "Remove cooldowns from all ad boosts!\nWatch ads more often for bigger gains.",
        priceString = "$2.99",
        tier = 1,
        emoji = "⏱️"
    ),

    // Tier 2: Progression Boosts
    PRESTIGE_MASTER(
        productId = "com.tuffgames.incrementalquest.prestige_master",
        displayName = "Prestige Master",
        description = "+50% Divine Essence from prestige\n+2x Gold from prestige\nAccelerate meta-progression!",
        priceString = "$4.99",
        tier = 2,
        emoji = "✨"
    ),
    AUTO_CLICKER_PRO(
        productId = "com.tuffgames.incrementalquest.autoclicker_pro",
        displayName = "Auto-Clicker Pro",
        description = "3x faster auto-clicker!\nUpgrade costs 50% less Gold.\nPerfect for passive play.",
        priceString = "$4.99",
        tier = 2,
        emoji = "🤖"
    ),
    COMBO_EXPERT(
        productId = "com.tuffgames.incrementalquest.combo_expert",
        displayName = "Combo Expert",
        description = "Combo window +0.5 seconds\nCombo bonus +50% stronger\nEasier and more powerful combos!",
        priceString = "$3.99",
        tier = 2,
        emoji = "🔥"
    ),

    // Tier 3: Power Boosts
    VIP_MULTIPLIER(
        productId = "com.tuffgames.incrementalquest.vip_multiplier",
        displayName = "VIP Multiplier",
        description = "+50% to ALL point sources!\nStacks with everything else.\nMassive power boost!",
        priceString = "$9.99",
        tier = 3,
        emoji = "👑"
    ),
    MEGA_RESOURCE_PACK(
        productId = "com.tuffgames.incrementalquest.mega_resources",
        displayName = "Mega Resource Pack",
        description = "500,000 Gold + 100 Divine Essence\nSkip ahead significantly!",
        priceString = "$9.99",
        tier = 3,
        emoji = "💰"
    ),
    ULTIMATE_BUNDLE(
        productId = "com.tuffgames.incrementalquest.ultimate_bundle",
        displayName = "Ultimate Bundle",
        description = "Includes: 2x Passive, No Cooldowns,\nPrestige Master, Combo Expert\nBest value!",
        priceString = "$14.99",
        tier = 3,
        emoji = "🎉"
    ),

    // Tier 4: Endgame / Whale
    ETERNAL_BOOST(
        productId = "com.tuffgames.incrementalquest.eternal_boost",
        displayName = "Eternal Boost Pack",
        description = "ALL ad boosts permanently active!\nNo ads needed, always 2x everything.\nUltimate power!",
        priceString = "$19.99",
        tier = 4,
        emoji = "⚡"
    ),
    REMOVE_ADS(
        productId = "com.tuffgames.incrementalquest.remove_ads",
        displayName = "Remove Ads Forever",
        description = "Remove all ads!\nGet boost rewards automatically every 4 hours.\nPeace of mind.",
        priceString = "$4.99",
        tier = 4,
        emoji = "🚫"
    );

    companion object {
        fun fromProductId(productId: String): IAPType? {
            return values().find { it.productId == productId }
        }

        fun getTierIAPs(tier: Int): List<IAPType> {
            return values().filter { it.tier == tier }
        }
    }
}

/**
 * INTEGRATION NOTES:
 *
 * Für Google Play Billing Integration später:
 * 1. build.gradle dependencies hinzufügen:
 *    implementation 'com.android.billingclient:billing-ktx:6.0.1'
 *
 * 2. Google Play Console: IAP-Produkte anlegen mit den productIds oben
 *
 * 3. In MainActivity onCreate():
 *    IAPManager.initialize(this)
 *
 * 4. Permissions (automatisch durch Billing Library):
 *    <uses-permission android:name="com.android.vending.BILLING" />
 *
 * 5. BillingClient Setup:
 *    - Connect to billing service
 *    - Query product details
 *    - Handle purchase updates
 *    - Acknowledge purchases
 *    - Query existing purchases for restore
 */
