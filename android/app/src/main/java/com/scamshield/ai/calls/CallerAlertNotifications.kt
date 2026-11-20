package com.scamshield.ai.calls

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.scamshield.ai.CallerIdentificationActivity
import com.scamshield.ai.R

/** Posts an actionable alert for an actual CallScreeningService callback. */
object CallerAlertNotifications {
    const val CHANNEL_ID = "scamshield_caller_alerts"
    private const val TAG = "ScamShieldAlert"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(NotificationChannel(
                CHANNEL_ID,
                "Incoming caller alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Caller number and ScamShield classification for real incoming calls"
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                setShowBadge(false)
                setSound(null, null)
            })
        }
    }

    fun post(context: Context, eventId: Long, number: String, classification: String) {
        ensureChannel(context)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val detailIntent = Intent(context, CallerIdentificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(CallerIdentificationActivity.EXTRA_NUMBER, number)
            putExtra(CallerIdentificationActivity.EXTRA_CLASSIFICATION, classification)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, eventId.toInt(), detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val label = when (classification) {
            "SCAM" -> "Likely scam · marked on this device"
            "TRUSTED" -> "Trusted · marked by you on this device"
            else -> "Unknown · no reliable match"
        }
        val content = "${if (number.isBlank()) "Unknown number" else number} · $label"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("ScamShield caller alert")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(eventId.toInt(), notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission is not granted; caller alert was not posted", e)
        } catch (e: Exception) {
            Log.w(TAG, "Could not post caller alert", e)
        }
        // Keep status query side-effects free. The OS and user still control presentation.
        if (!notificationManager.areNotificationsEnabled()) {
            Log.i(TAG, "App notifications are disabled by the user")
        }
    }
}
