package com.scamshield.ai.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.scamshield.ai.MainActivity

/** Shared warning path for notification and direct SMS scans. */
object ScamWarningNotifier {
    fun post(context: Context, source: String, appName: String, reasons: List<String>): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(ScamShieldNotificationListenerService.CHANNEL_ID) == null) {
            manager.createNotificationChannel(NotificationChannel(ScamShieldNotificationListenerService.CHANNEL_ID, "Scam warnings", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Warnings when notification content has local scam indicators"
                setShowBadge(true)
            })
        }
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "warning_post_failed reason=notification_permission")
            return false
        }
        if (!manager.areNotificationsEnabled()) {
            Log.w(TAG, "warning_post_failed reason=app_notifications_disabled")
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(ScamShieldNotificationListenerService.CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE) {
            Log.w(TAG, "warning_post_failed reason=warning_channel_disabled")
            return false
        }
        val time = System.currentTimeMillis()
        val tap = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_scan_source", source)
            putExtra("notification_scan_time", time)
        }
        val pending = PendingIntent.getActivity(context, (time % Int.MAX_VALUE).toInt(), tap,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, ScamShieldNotificationListenerService.CHANNEL_ID)
            .setSmallIcon(com.scamshield.ai.R.mipmap.ic_launcher)
            .setContentTitle("ScamShield warning · $appName")
            .setContentText(reasons.firstOrNull() ?: "Suspicious content detected")
            .setStyle(NotificationCompat.BigTextStyle().bigText(reasons.joinToString("\n")))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        return try {
            manager.notify((time % Int.MAX_VALUE).toInt(), notification)
            Log.i(TAG, "warning_posted source=$source")
            true
        } catch (error: Exception) {
            Log.e(TAG, "warning_post_failed source=$source error=${error.javaClass.simpleName}")
            false
        }
    }
    private const val TAG = "ScamShieldNotificationScan"
}
