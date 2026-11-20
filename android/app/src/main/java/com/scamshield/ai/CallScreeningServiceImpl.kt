package com.scamshield.ai

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.scamshield.ai.calls.CallerAlertNotifications
import com.scamshield.ai.calls.CallerSpeechAlerts
import com.scamshield.ai.calls.ScreeningStore

/** Fast, offline-only screening. Every call is allowed; reputation is informational. */
class CallScreeningServiceImpl : CallScreeningService() {
    override fun onCreate() {
        super.onCreate()
        if (ScreeningStore.spokenAlertsEnabled(this)) CallerSpeechAlerts.prepare(this)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) return

        val rawNumber = callDetails.handle?.schemeSpecificPart
        val number = rawNumber?.let(ScreeningStore::normalize).orEmpty()
        val classification = if (number.isBlank()) "UNKNOWN" else
            runCatching { ScreeningStore.classify(this, number) }.getOrDefault("UNKNOWN")

        Log.i(TAG, "Received eligible incoming call; classification=$classification")

        var eventId = System.currentTimeMillis()
        try {
            // This is a local, bounded write; no network lookup can delay ringing.
            eventId = runCatching {
                ScreeningStore.record(this, rawNumber ?: "Hidden/Unavailable", classification)
            }.getOrDefault(eventId)
        } finally {
            // Always return an allow response even if local storage fails.
            respondToCall(callDetails, CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSilenceCall(false)
                .build())
        }

        // Return the telecom response before optional UI/audio work. A notification tap
        // opens the detail screen; Android 14 does not permit relying on a background
        // activity launch from this service.
        runCatching {
            CallerAlertNotifications.post(this, eventId, rawNumber ?: "Unknown number", classification)
        }.onFailure { Log.w(TAG, "Caller notification could not be posted", it) }
        CallerSpeechAlerts.announceCall(this, classification, eventId)
    }

    companion object { private const val TAG = "ScamShieldScreening" }
}
