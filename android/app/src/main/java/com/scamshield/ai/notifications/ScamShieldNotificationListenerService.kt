package com.scamshield.ai.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.scamshield.ai.MainActivity
import com.scamshield.ai.calls.CallerSpeechAlerts

class ScamShieldNotificationListenerService : NotificationListenerService() {
    // Listener callbacks run on the service thread. Limit rebind requests to one per
    // disconnection until Android reports a successful connection again.
    private var rebindRequestedSinceConnection = false

    override fun onListenerConnected() {
        super.onListenerConnected()
        rebindRequestedSinceConnection = false
        Log.i(TAG, "listener_connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "listener_disconnected")
        if (rebindRequestedSinceConnection) {
            Log.i(TAG, "listener_rebind_skipped reason=already_requested")
            return
        }

        rebindRequestedSinceConnection = true
        try {
            requestRebind(ComponentName(this, ScamShieldNotificationListenerService::class.java))
            Log.i(TAG, "listener_rebind_requested")
        } catch (error: Exception) {
            // Log only the exception class; never include notification-derived data.
            Log.e(TAG, "listener_rebind_request_failed error=${error.javaClass.simpleName}")
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "listener_destroyed")
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val sourcePackage = sbn.packageName
        Log.i(TAG, "notification_received package=$sourcePackage")
        try {
            val monitoringEnabled = NotificationScanStore.enabled(this)
            val monitored = NotificationScanStore.isMonitored(this, sourcePackage)
            if (sourcePackage == packageName) {
                Log.i(TAG, "notification_skipped package=$sourcePackage reason=self_notification")
                return
            }
            if (!monitoringEnabled || !monitored) {
                Log.i(TAG, "notification_skipped package=$sourcePackage reason=filter monitoringEnabled=$monitoringEnabled monitored=$monitored")
                return
            }
            if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
                Log.i(TAG, "notification_skipped package=$sourcePackage reason=group_summary")
                return
            }

            NotificationScanStore.observePackage(this, sourcePackage)
            val appName = resolveAppName(sourcePackage)
            val smsNotification = isDefaultSmsNotification(sourcePackage) && NotificationScanStore.smsScanningEnabled(this)
            val smsPayload = if (smsNotification) extractSmsBody(sbn.notification.extras) else null
            val content = smsPayload?.text?.takeIf { it.isNotBlank() } ?: extractText(sbn.notification.extras)
            val textAvailable = content.isNotBlank()
            Log.i(TAG, "notification_text package=$sourcePackage available=$textAvailable length=${content.length}")
            if (!textAvailable) {
                if (smsNotification) {
                    Log.i(TAG, "notification_history_skipped source_path=sms_notification reason=direct_sms_path_is_authoritative")
                    return
                }
                val saved = recordHistory(sourcePackage, appName, "NOT_SCANNED", listOf("Notification content is hidden or unavailable; it was not scanned"))
                Log.i(TAG, "notification_history package=$sourcePackage status=NOT_SCANNED saved=$saved")
                return
            }

            val result = LocalNotificationAnalyzer.analyze(content)
            Log.i(TAG, "notification_analysis package=$sourcePackage suspicious=${result.suspicious} rules=${result.matchedRules.joinToString(",").ifEmpty { "none" }}")
            if (!result.suspicious) return
            if (smsNotification) {
                val notificationTime = smsPayload?.timestamp ?: sbn.notification.`when`.takeIf { it > 0 } ?: sbn.postTime
                val stableTimestamp = smsPayload?.timestamp != null || sbn.notification.`when` > 0L
                val decision = SmsDuplicateGuard.markNotification(
                    smsPayload?.text?.takeIf { it.isNotBlank() } ?: content,
                    sbn.key,
                    notificationTime,
                    stableTimestamp
                )
                Log.i(TAG, "sms_dedup source_path=notification package=$sourcePackage suppressed=${decision.suppress} reason=${decision.reason} identity_source=${when { smsPayload?.timestamp != null -> "message_timestamp"; sbn.notification.`when` > 0L -> "notification_when"; else -> "notification_key_and_text" }}")
                if (decision.suppress) {
                    Log.i(TAG, "notification_skipped package=$sourcePackage reason=sms_event_duplicate")
                    return
                }
            }
            val historyPackage = if (smsNotification && NotificationScanStore.smsScanningEnabled(this)) NotificationScanStore.SMS_SOURCE else sourcePackage
            val historyApp = if (historyPackage == NotificationScanStore.SMS_SOURCE) NotificationScanStore.SMS_APP_NAME else appName
            val saved = recordHistory(historyPackage, historyApp, "WARNING", result.reasons, result.matchedRules)
            Log.i(TAG, "notification_history source_path=${if (smsNotification) "sms_notification" else "app_notification"} package=$sourcePackage status=WARNING saved=$saved")
            val posted = ScamWarningNotifier.post(this, historyPackage, historyApp, result.reasons)
            Log.i(TAG, "notification_warning source_path=${if (smsNotification) "sms_notification" else "app_notification"} package=$sourcePackage created=$posted")
            val speechEvent = if (smsNotification) {
                "sms:${smsPayload?.timestamp ?: sbn.postTime}"
            } else {
                NotificationSpeechEvents.markAndGetEventId(
                    sbn.key, content, sbn.notification.`when`.takeIf { it > 0L } ?: sbn.postTime
                )
            }
            if (speechEvent == null) {
                Log.i(TAG, "notification_speech_skipped reason=duplicate_notification_event")
            } else {
                CallerSpeechAlerts.announceWarning(this, if (smsNotification) "sms" else "notification", speechEvent, result.matchedRules)
            }
        } catch (error: Exception) {
            // Exception messages can contain notification-derived data; log only the class.
            Log.e(TAG, "notification_processing_failed package=$sourcePackage error=${error.javaClass.simpleName}")
        }
    }

    private fun recordHistory(sourcePackage: String, appName: String, status: String, reasons: List<String>, matchedRules: List<String> = emptyList()): Boolean = try {
        NotificationScanStore.record(this, sourcePackage, appName, status, reasons, matchedRules)
        true
    } catch (error: Exception) {
        Log.e(TAG, "notification_history_failed package=$sourcePackage status=$status error=${error.javaClass.simpleName}")
        false
    }

    private fun extractText(extras: Bundle?): String {
        if (extras == null) return ""
        val pieces = mutableListOf<String>()
        fun add(value: CharSequence?) { value?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(pieces::add) }
        add(extras.getCharSequence(Notification.EXTRA_TITLE))
        add(extras.getCharSequence(Notification.EXTRA_TEXT))
        add(extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
        add(extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach(::add)
        @Suppress("DEPRECATION")
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        messages?.forEach { item ->
            val bundle = item as? Bundle ?: return@forEach
            add(bundle.getCharSequence("text"))
        }
        return pieces.distinct().joinToString(" ").take(6000)
    }

    private data class SmsNotificationPayload(val text: String, val timestamp: Long?)

    private fun extractSmsBody(extras: Bundle?): SmsNotificationPayload {
        if (extras == null) return SmsNotificationPayload("", null)
        @Suppress("DEPRECATION")
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)?.mapNotNull { it as? Bundle }.orEmpty()
        if (messages.isNotEmpty()) {
            val latest = messages.last()
            val latestText = latest.getCharSequence("text")?.toString()?.trim().orEmpty()
            val timestamp = latest.getLong("time").takeIf { it > 0L }
            if (latestText.isNotBlank()) return SmsNotificationPayload(latestText.take(6000), timestamp)
        }
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim().orEmpty()
        val text = bigText.ifBlank { extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty() }
        return SmsNotificationPayload(text.take(6000), null)
    }

    private fun resolveAppName(sourcePackage: String): String = runCatching {
        val app = packageManager.getApplicationInfo(sourcePackage, 0)
        packageManager.getApplicationLabel(app).toString()
    }.getOrDefault(sourcePackage)

    private fun isDefaultSmsNotification(sourcePackage: String): Boolean =
        runCatching { android.provider.Telephony.Sms.getDefaultSmsPackage(this) == sourcePackage }.getOrDefault(false)

    companion object {
        const val CHANNEL_ID = "scamshield_notification_warnings_v1"
        private const val TAG = "ScamShieldNotificationScan"
        fun isAccessGranted(context: Context): Boolean {
            val component = ComponentName(context, ScamShieldNotificationListenerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                return (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .isNotificationListenerAccessGranted(component)
            }
            val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
            return enabled.split(':').any { runCatching { ComponentName.unflattenFromString(it) == component }.getOrDefault(false) }
        }
    }
}
