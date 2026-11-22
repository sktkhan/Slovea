package com.example.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Handles Google User Messaging Platform (UMP) consent gathering and privacy options.
 * Safe offline: never crashes or delays app usability if consent servers are unreachable.
 */
class ConsentManager {

    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _isPrivacyOptionsRequired = MutableStateFlow(false)
    val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()

    fun gatherConsent(activity: Activity, onCanRequestAds: () -> Unit) {
        try {
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            updateStates(consentInformation)

            if (consentInformation.canRequestAds()) {
                onCanRequestAds()
            }

            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    if (activity.isFinishing || activity.isDestroyed) return@requestConsentInfoUpdate
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                        if (loadAndShowError != null) {
                            Log.w(TAG, "Consent form notice: ${loadAndShowError.errorCode} ${loadAndShowError.message}")
                        }
                        updateStates(consentInformation)
                        if (consentInformation.canRequestAds()) {
                            onCanRequestAds()
                        }
                    }
                },
                { requestConsentError ->
                    Log.w(TAG, "Consent info update unavailable (possibly offline): ${requestConsentError.errorCode} ${requestConsentError.message}")
                    updateStates(consentInformation)
                    if (consentInformation.canRequestAds()) {
                        onCanRequestAds()
                    }
                }
            )
        } catch (t: Throwable) {
            Log.w(TAG, "UMP consent check skipped safely: ${t.message}")
        }
    }

    fun showPrivacyOptionsForm(activity: Activity, onComplete: (String?) -> Unit) {
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
                updateStates(consentInformation)
                onComplete(formError?.message)
            }
        } catch (t: Throwable) {
            onComplete(t.message ?: "Unable to open privacy options right now.")
        }
    }

    private fun updateStates(consentInformation: ConsentInformation) {
        _canRequestAds.value = consentInformation.canRequestAds()
        _isPrivacyOptionsRequired.value =
            consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    companion object {
        private const val TAG = "DailyFixConsent"
    }
}
