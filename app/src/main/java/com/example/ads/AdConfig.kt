package com.example.ads

import com.example.BuildConfig

/**
 * Centralized AdMob configuration.
 * Debug builds automatically use Google's official test Ad Unit IDs via BuildConfig.
 * Release builds use the production AdMob Ad Unit IDs configured in app/build.gradle.kts.
 */
object AdConfig {
    const val ADMOB_APP_ID = "ca-app-pub-3781293651763401~4637525567"

    // Official Google Test Ad Unit IDs (debug)
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Production Ad Unit IDs (release)
    const val PRODUCTION_BANNER_AD_UNIT_ID = "ca-app-pub-3781293651763401/4652556171"
    const val PRODUCTION_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3781293651763401/5270457094"

    val useTestAds: Boolean
        get() = BuildConfig.USE_TEST_ADS

    val bannerAdUnitId: String
        get() = BuildConfig.ADMOB_BANNER_AD_UNIT_ID

    val interstitialAdUnitId: String
        get() = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID

    /** Minimum 3 minutes between interstitial ads. */
    const val INTERSTITIAL_COOLDOWN_MS: Long = 180_000L

    /** Never show an interstitial immediately after app launch (90s grace window). */
    const val INITIAL_SESSION_GRACE_MS: Long = 90_000L
}
