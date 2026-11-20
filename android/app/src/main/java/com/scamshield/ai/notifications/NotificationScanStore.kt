package com.scamshield.ai.notifications

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Stores only real warning/visibility events. Notification text is never persisted. */
object NotificationScanStore {
    private const val PREFS = "scamshield_notification_scanning"
    private const val ENABLED = "monitoring_enabled"
    private const val EXCLUDED = "excluded_packages"
    private const val OBSERVED = "observed_packages"
    private const val HISTORY = "history"
    private const val RETENTION_DAYS = "retention_days"
    private const val SMS_SCANNING_ENABLED = "sms_scanning_enabled"

    const val SMS_SOURCE = "sms"
    const val SMS_APP_NAME = "SMS"

    fun enabled(context: Context) = prefs(context).getBoolean(ENABLED, false)
    fun setEnabled(context: Context, enabled: Boolean) { prefs(context).edit().putBoolean(ENABLED, enabled).apply() }
    fun smsScanningEnabled(context: Context) = prefs(context).getBoolean(SMS_SCANNING_ENABLED, false)
    fun setSmsScanningEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(SMS_SCANNING_ENABLED, enabled).apply()
    }
    fun retentionDays(context: Context) = prefs(context).getInt(RETENTION_DAYS, 7)

    @Synchronized fun setRetentionDays(context: Context, days: Int) {
        require(days in setOf(1, 7, 30))
        prefs(context).edit().putInt(RETENTION_DAYS, days).apply()
        history(context)
    }

    @Synchronized fun isMonitored(context: Context, packageName: String): Boolean =
        !prefs(context).getStringSet(EXCLUDED, emptySet()).orEmpty().contains(packageName)

    @Synchronized fun setMonitored(context: Context, packageName: String, monitored: Boolean) {
        val excluded = prefs(context).getStringSet(EXCLUDED, emptySet()).orEmpty().toMutableSet()
        if (monitored) excluded.remove(packageName) else excluded.add(packageName)
        prefs(context).edit().putStringSet(EXCLUDED, excluded).apply()
    }

    @Synchronized fun observePackage(context: Context, packageName: String) {
        val observed = prefs(context).getStringSet(OBSERVED, emptySet()).orEmpty().toMutableSet()
        if (observed.add(packageName)) prefs(context).edit().putStringSet(OBSERVED, observed).apply()
    }

    fun observedPackages(context: Context): Set<String> =
        prefs(context).getStringSet(OBSERVED, emptySet()).orEmpty().toSet()

    @Synchronized fun history(context: Context): JSONArray {
        val now = System.currentTimeMillis()
        val cutoff = now - retentionDays(context) * 24L * 60L * 60L * 1000L
        val old = runCatching { JSONArray(prefs(context).getString(HISTORY, "[]")) }.getOrDefault(JSONArray())
        val kept = JSONArray()
        for (i in 0 until old.length()) {
            val row = old.optJSONObject(i) ?: continue
            if (row.optLong("time") >= cutoff) kept.put(row)
        }
        prefs(context).edit().putString(HISTORY, kept.toString()).apply()
        return kept
    }

    @Synchronized fun record(context: Context, packageName: String, appName: String, status: String, reasons: List<String>, matchedRules: List<String> = emptyList()) {
        val old = history(context)
        val next = JSONArray()
        next.put(JSONObject()
            .put("id", System.currentTimeMillis())
            .put("packageName", packageName)
            .put("appName", appName)
            .put("status", status)
            .put("reasons", JSONArray(reasons))
            .put("matchedRules", JSONArray(matchedRules))
            .put("time", System.currentTimeMillis()))
        for (i in 0 until minOf(old.length(), 199)) next.put(old.getJSONObject(i))
        prefs(context).edit().putString(HISTORY, next.toString()).apply()
    }

    @Synchronized fun clearHistory(context: Context) { prefs(context).edit().remove(HISTORY).apply() }
    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
