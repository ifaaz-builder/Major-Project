package com.scamshield.ai.calls

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
object ScreeningStore {
    private const val PREFS = "scamshield_call_screening"
    private const val HISTORY = "history"
    private const val LABELS = "labels"
    private const val SPOKEN_ALERTS = "spoken_alerts_enabled"

    fun normalize(number: String): String = number.filter { it.isDigit() }

    @Synchronized
    fun classify(context: Context, normalizedNumber: String): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(LABELS, null)
            ?.let { JSONObject(it).optString(normalizedNumber, "UNKNOWN") }
            ?.let { if (it == "VERIFIED") "TRUSTED" else it }
            ?.takeIf { it == "SCAM" || it == "TRUSTED" }
            ?: "UNKNOWN"

    @Synchronized
    fun setLabel(context: Context, number: String, label: String) {
        require(label == "SCAM" || label == "TRUSTED" || label == "VERIFIED")
        val normalized = normalize(number)
        require(normalized.length in 8..15) { "Enter a phone number with 8 to 15 digits" }
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val labels = JSONObject(prefs.getString(LABELS, "{}") ?: "{}")
        labels.put(normalized, if (label == "VERIFIED") "TRUSTED" else label)
        prefs.edit().putString(LABELS, labels.toString()).apply()
    }

    @Synchronized
    fun history(context: Context): JSONArray = try {
        JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(HISTORY, "[]"))
    } catch (_: Exception) { JSONArray() }

    @Synchronized
    fun record(context: Context, number: String, classification: String): Long {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val old = history(context)
        val next = JSONArray()
        val eventId = System.currentTimeMillis()
        next.put(JSONObject()
            .put("id", eventId)
            .put("number", number)
            .put("classification", classification)
            .put("source", "on-device list")
            .put("time", System.currentTimeMillis()))
        for (i in 0 until minOf(old.length(), 199)) next.put(old.getJSONObject(i))
        prefs.edit().putString(HISTORY, next.toString()).apply()
        return eventId
    }

    fun spokenAlertsEnabled(context: Context): Boolean = context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(SPOKEN_ALERTS, false)

    fun setSpokenAlertsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(SPOKEN_ALERTS, enabled).apply()
    }
}
