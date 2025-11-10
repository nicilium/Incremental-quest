package com.tuffgames.incrementalquest

import android.app.Activity
import android.util.Log

/**
 * AdManager - Interface für Werbung
 *
 * PLACEHOLDER: Aktuell simuliert dies Werbung mit direktem Callback.
 * TODO: Später mit echtem Ad-SDK integrieren (z.B. AdMob, Unity Ads, etc.)
 */
object AdManager {

    private const val TAG = "AdManager"

    // Für echte Ads später: Initialisierung mit App-ID
    fun initialize(activity: Activity) {
        Log.d(TAG, "AdManager initialized (placeholder mode)")
        // TODO: Echte Ad SDK Initialisierung
        // Example: MobileAds.initialize(activity) { }
    }

    /**
     * Zeigt eine Rewarded Ad
     * @param activity Die Activity, in der die Ad gezeigt wird
     * @param onRewardEarned Callback wenn Spieler die Ad komplett geschaut hat
     * @param onAdFailed Callback wenn Ad nicht verfügbar ist oder Fehler
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdFailed: (String) -> Unit
    ) {
        Log.d(TAG, "showRewardedAd called")

        // PLACEHOLDER: Simuliere erfolgreiche Ad
        // In echter Implementierung: Lade und zeige echte Ad

        // Simuliere Ad-Loading
        activity.runOnUiThread {
            // TODO: Echte Ad laden und zeigen
            // Example:
            // rewardedAd?.show(activity) { rewardItem ->
            //     onRewardEarned()
            // }

            // PLACEHOLDER: Direkter Erfolg (für Entwicklung/Testing)
            Log.d(TAG, "Rewarded ad completed successfully (placeholder)")
            onRewardEarned()
        }
    }

    /**
     * Prüft ob eine Rewarded Ad verfügbar ist
     * @return true wenn Ad geladen ist, false sonst
     */
    fun isRewardedAdReady(): Boolean {
        // PLACEHOLDER: Immer verfügbar (für Development)
        // TODO: Echte Ad-Verfügbarkeit prüfen
        // return rewardedAd?.isLoaded == true
        return true
    }

    /**
     * Lädt eine Rewarded Ad vor
     * Sollte aufgerufen werden um Ads vorzuladen
     */
    fun loadRewardedAd(activity: Activity) {
        Log.d(TAG, "loadRewardedAd called")
        // TODO: Echte Ad vorladen
        // Example:
        // rewardedAd = RewardedAd(activity, "ad-unit-id")
        // rewardedAd?.loadAd(AdRequest.Builder().build(), ...)
    }
}

/**
 * INTEGRATION NOTES:
 *
 * Für AdMob Integration später:
 * 1. build.gradle dependencies hinzufügen:
 *    implementation 'com.google.android.gms:play-services-ads:22.0.0'
 *
 * 2. AndroidManifest.xml: App-ID hinzufügen:
 *    <meta-data
 *        android:name="com.google.android.gms.ads.APPLICATION_ID"
 *        android:value="ca-app-pub-xxxxx~xxxxx"/>
 *
 * 3. In MainActivity onCreate():
 *    AdManager.initialize(this)
 *
 * 4. Permissions (optional für bessere Ads):
 *    <uses-permission android:name="android.permission.INTERNET"/>
 *    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
 */
