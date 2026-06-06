package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log

/**
 * Representation of supported ad monetization networks.
 */
enum class AdProvider {
    ADMOB,
    HOUSE
}

/**
 * A centralized orchestration brain for mobile ad mediation.
 * Evaluates Firebase Remote Config status, directs A/B testing splits, 
 * manages SDK initialization, and resolves cascading fallback configurations.
 */
object AdMediationManager {
    private const val TAG = "AdMediationManager"

    /**
     * Initializes Firebase Remote Config, waits for remote parameters to activate, and
     * then proceeds to initialize AdMob SDK based on the synchronized ruleset.
     */
    fun initialize(context: Context, onComplete: () -> Unit = {}) {
        Log.i(TAG, "Initializing Ad Mediation utility...")
        RemoteConfigManager.initialize(context) {
            val isAdMobNeeded = RemoteConfigManager.isAdMobEnabled()

            Log.i(TAG, "Sync complete. Initialization decisions - AdMob needed: $isAdMobNeeded")

            if (isAdMobNeeded) {
                AdMobManager.initialize(context)
                Log.d(TAG, "AdMob SDK initiated in mediation.")
            }
            onComplete()
        }
    }

    /**
     * Dynamically determines the starting ad network based on active configuration keys.
     * Since we only want AdMob, this returns ADMOB if enabled, otherwise details the HOUSE fallback placement.
     */
    fun getResolvedProvider(adType: String = "general"): AdProvider {
        val admobEnabled = RemoteConfigManager.isAdMobEnabled()
        Log.d(TAG, "Resolving provider for adType: $adType. AdMob enabled: $admobEnabled")

        return if (admobEnabled) {
            AdProvider.ADMOB
        } else {
            AdProvider.HOUSE
        }
    }

    /**
     * Resolves the secondary (fallback) provider once the primary fails to load an ad.
     */
    fun getFallbackProvider(failedProvider: AdProvider): AdProvider {
        return AdProvider.HOUSE
    }
}
