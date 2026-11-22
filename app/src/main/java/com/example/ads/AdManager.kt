package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Pure frequency-capping policy for interstitial ads so transitions never interrupt users.
 */
class InterstitialFrequencyGuard(
    private val cooldownMs: Long = AdConfig.INTERSTITIAL_COOLDOWN_MS,
    private val launchGraceMs: Long = AdConfig.INITIAL_SESSION_GRACE_MS,
    private val clockMs: () -> Long = { SystemClock.elapsedRealtime() }
) {
    private val sessionStartMs: Long = clockMs()
    private var lastShownMs: Long? = null

    fun canShowInterstitialNow(): Boolean {
        val now = clockMs()
        if (now - sessionStartMs < launchGraceMs) {
            return false
        }
        val previous = lastShownMs ?: return true
        return (now - previous) >= cooldownMs
    }

    fun recordShown() {
        lastShownMs = clockMs()
    }
}

/**
 * Lifecycle-aware Google Mobile Ads SDK manager for initialization and interstitial ads.
 */
class AdManager(
    private val frequencyGuard: InterstitialFrequencyGuard = InterstitialFrequencyGuard()
) {
    private val isMobileAdsInitCalled = AtomicBoolean(false)
    private val isLoadingInterstitial = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var interstitialAd: InterstitialAd? = null
    private var retryAttempt = 0
    private var pendingRetryRunnable: Runnable? = null

    private val _isSdkInitialized = MutableStateFlow(false)
    val isSdkInitialized: StateFlow<Boolean> = _isSdkInitialized.asStateFlow()

    private val _isInterstitialReady = MutableStateFlow(false)
    val isInterstitialReady: StateFlow<Boolean> = _isInterstitialReady.asStateFlow()

    fun initializeMobileAds(context: Context) {
        if (isMobileAdsInitCalled.getAndSet(true)) return
        val appContext = context.applicationContext
        try {
            MobileAds.initialize(appContext) {
                _isSdkInitialized.value = true
                preloadInterstitial(appContext)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "MobileAds initialization skipped safely: ${t.message}")
            isMobileAdsInitCalled.set(false)
        }
    }

    fun preloadInterstitial(context: Context) {
        if (!_isSdkInitialized.value) return
        if (interstitialAd != null || !isLoadingInterstitial.compareAndSet(false, true)) return

        val appContext = context.applicationContext
        val adRequest = AdRequest.Builder().build()

        try {
            InterstitialAd.load(
                appContext,
                AdConfig.interstitialAdUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        isLoadingInterstitial.set(false)
                        retryAttempt = 0
                        interstitialAd = ad
                        _isInterstitialReady.value = true
                        Log.d(TAG, "Interstitial preloaded successfully.")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingInterstitial.set(false)
                        interstitialAd = null
                        _isInterstitialReady.value = false
                        Log.w(TAG, "Interstitial failed to load: ${loadAdError.message}")
                        scheduleRetry(appContext)
                    }
                }
            )
        } catch (t: Throwable) {
            isLoadingInterstitial.set(false)
            Log.w(TAG, "Interstitial load error: ${t.message}")
        }
    }

    /**
     * Shows an interstitial ONLY at natural navigation transitions (never at app start,
     * never while typing, never interrupting calculations, never on app exit).
     * Always calls [onTransitionComplete] promptly if no ad is shown.
     */
    fun maybeShowOnNaturalTransition(
        activity: Activity?,
        canRequestAds: Boolean,
        onTransitionComplete: () -> Unit
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed || !canRequestAds) {
            onTransitionComplete()
            return
        }

        val ad = interstitialAd
        if (ad == null || !frequencyGuard.canShowInterstitialNow()) {
            if (ad == null && canRequestAds && _isSdkInitialized.value) {
                preloadInterstitial(activity.applicationContext)
            }
            onTransitionComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad.fullScreenContentCallback = null
                interstitialAd = null
                _isInterstitialReady.value = false
                onTransitionComplete()
                preloadInterstitial(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                ad.fullScreenContentCallback = null
                interstitialAd = null
                _isInterstitialReady.value = false
                onTransitionComplete()
            }

            override fun onAdShowedFullScreenContent() {
                frequencyGuard.recordShown()
                interstitialAd = null
                _isInterstitialReady.value = false
            }
        }

        ad.show(activity)
    }

    private fun scheduleRetry(appContext: Context) {
        if (retryAttempt >= MAX_RETRIES) return
        retryAttempt++
        val delayMs = (15_000L shl (retryAttempt - 1)).coerceAtMost(60_000L)
        pendingRetryRunnable?.let { mainHandler.removeCallbacks(it) }
        val runnable = Runnable { preloadInterstitial(appContext) }
        pendingRetryRunnable = runnable
        mainHandler.postDelayed(runnable, delayMs)
    }

    fun release() {
        pendingRetryRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingRetryRunnable = null
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
        _isInterstitialReady.value = false
    }

    companion object {
        private const val TAG = "DailyFixAdManager"
        private const val MAX_RETRIES = 3
    }
}
