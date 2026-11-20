package com.scamshield.ai.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import com.scamshield.ai.calls.CallerSpeechAlerts
import java.util.UUID

class IncomingSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        Log.i(TAG, "sms_notification_received source=sms")
        if (!NotificationScanStore.smsScanningEnabled(context)) {
            Log.i(TAG, "sms_scan_skipped reason=monitoring_disabled")
            return
        }
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "sms_scan_skipped reason=permission_not_granted")
            return
        }
        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent).orEmpty()
            val text = messages.joinToString("") { it.messageBody.orEmpty() }.take(6000)
            Log.i(TAG, "sms_text available=${text.isNotBlank()} length=${text.length} parts=${messages.size}")
            if (text.isBlank()) return
            val analysis = LocalNotificationAnalyzer.analyze(text)
            Log.i(TAG, "sms_analysis suspicious=${analysis.suspicious} rules=${analysis.matchedRules.joinToString(",").ifEmpty { "none" }}")
            if (!analysis.suspicious) return
            val smsTimestamp = messages.firstOrNull()?.timestampMillis?.takeIf { it > 0L } ?: 0L
            val duplicate = SmsDuplicateGuard.markDirect(text, smsTimestamp)
            Log.i(TAG, "sms_dedup source_path=direct suppressed=${duplicate.suppress} reason=${duplicate.reason} identity_source=${if (smsTimestamp > 0L) "sms_timestamp" else "unavailable"}")
            if (duplicate.suppress) {
                Log.i(TAG, "sms_warning_skipped source_path=direct reason=${duplicate.reason}")
                return
            }
            val historySaved = runCatching {
                NotificationScanStore.record(context, NotificationScanStore.SMS_SOURCE, NotificationScanStore.SMS_APP_NAME, "WARNING", analysis.reasons, analysis.matchedRules)
            }.isSuccess
            Log.i(TAG, "sms_history_saved source_path=direct saved=$historySaved source=sms metadata_only=true")
            val warningPosted = ScamWarningNotifier.post(context, NotificationScanStore.SMS_SOURCE, NotificationScanStore.SMS_APP_NAME, analysis.reasons)
            Log.i(TAG, "sms_warning_created source_path=direct created=$warningPosted")
            val speechEvent = if (smsTimestamp > 0L) "sms:$smsTimestamp" else "sms:${UUID.randomUUID()}"
            CallerSpeechAlerts.announceWarning(context, "sms", speechEvent, analysis.matchedRules)
        } catch (error: Exception) {
            Log.e(TAG, "sms_processing_failed error=${error.javaClass.simpleName}")
        }
    }
    companion object { private const val TAG = "ScamShieldNotificationScan" }
}
