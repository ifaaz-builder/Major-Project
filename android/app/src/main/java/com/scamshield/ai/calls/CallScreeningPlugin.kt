package com.scamshield.ai.calls

import android.app.role.RoleManager
import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import androidx.activity.result.ActivityResult
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import com.scamshield.ai.calls.CallerAlertNotifications
import org.json.JSONObject

@CapacitorPlugin(name = "CallScreening", permissions = [
    Permission(alias = "notifications", strings = [Manifest.permission.POST_NOTIFICATIONS])
])
class CallScreeningPlugin : Plugin() {
    @PluginMethod
    fun getStatus(call: PluginCall) {
        val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val history = ScreeningStore.history(getContext())
        val result = JSObject()
        result.put("supported", supported)
        result.put("roleHeld", supported &&
            (getContext().getSystemService(Context.ROLE_SERVICE) as RoleManager).isRoleHeld(RoleManager.ROLE_CALL_SCREENING))
        result.put("history", JSArray(history.toString()))
        result.put("lastScreeningAt", if (history.length() > 0) history.optJSONObject(0)?.optLong("time") ?: 0L else 0L)
        result.put("provider", "none")
        result.put("notificationsGranted", Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
        val manager = getContext().getSystemService(NotificationManager::class.java)
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.getNotificationChannel(CallerAlertNotifications.CHANNEL_ID) else null
        result.put("notificationsEnabled", manager.areNotificationsEnabled())
        result.put("callerAlertChannelImportance", channel?.importance ?: -1)
        result.put("spokenAlertsEnabled", ScreeningStore.spokenAlertsEnabled(getContext()))
        val speech = CallerSpeechAlerts.diagnostics(getContext())
        result.put("speechEngineStatus", speech.engineStatus)
        result.put("speechLastOutcome", speech.lastOutcome)
        result.put("speechSuppressionReason", speech.suppressionReason)
        result.put("notificationVolume", speech.notificationVolume)
        result.put("notificationVolumeMax", speech.notificationVolumeMax)
        result.put("speechRingerMode", speech.ringerMode)
        result.put("speechDndMode", speech.dndMode)
        call.resolve(result)
    }

    @PluginMethod
    fun requestNotificationPermission(call: PluginCall) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val result = JSObject(); result.put("granted", true); call.resolve(result)
            return
        }
        requestPermissionForAlias("notifications", call, "notificationPermissionResult")
    }

    @PluginMethod
    fun openCallerAlertSettings(call: PluginCall) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.content.Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, getContext().packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, CallerAlertNotifications.CHANNEL_ID)
            }
        } else {
            android.content.Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, getContext().packageName)
            }
        }
        try {
            activity.startActivity(intent)
            call.resolve()
        } catch (e: Exception) { call.reject(e.message ?: "Could not open notification settings") }
    }

    @PermissionCallback
    private fun notificationPermissionResult(call: PluginCall) {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        val result = JSObject(); result.put("granted", granted); call.resolve(result)
    }

    @PluginMethod
    fun setSpokenAlertsEnabled(call: PluginCall) {
        val enabled = call.getBoolean("enabled") ?: run { call.reject("Enabled value is required"); return }
        ScreeningStore.setSpokenAlertsEnabled(getContext(), enabled)
        if (enabled) CallerSpeechAlerts.prepare(getContext())
        else CallerSpeechAlerts.stopAutomatic()
        val result = JSObject(); result.put("enabled", enabled); call.resolve(result)
    }

    @PluginMethod
    fun testSpokenAlert(call: PluginCall) {
        CallerSpeechAlerts.test(getContext()) { speech ->
            val result = JSObject()
            result.put("queued", speech.queued)
            result.put("status", speech.status)
            call.resolve(result)
        }
    }

    @PluginMethod
    fun getSpeechLanguageStatus(call: PluginCall) {
        CallerSpeechAlerts.languageStatus(getContext()) { statuses, failure ->
            val rows = JSArray()
            statuses.forEach { item ->
                val row = JSObject()
                row.put("code", item.code)
                row.put("tag", item.tag)
                row.put("status", item.status)
                row.put("voiceLocale", item.voiceLocale ?: JSONObject.NULL)
                rows.put(row)
            }
            val result = JSObject()
            result.put("selectedLanguage", com.scamshield.ai.settings.SpokenLanguageStore.get(getContext()) ?: JSONObject.NULL)
            result.put("engineStatus", CallerSpeechAlerts.diagnostics(getContext()).engineStatus)
            result.put("engineFailure", failure)
            result.put("languages", rows)
            val outputCapabilities = JSArray()
            CallerSpeechAlerts.outputCapabilities().forEach { capability ->
                val row = JSObject()
                row.put("language", capability.language)
                row.put("status", capability.status)
                row.put("backend", capability.backend)
                row.put("reason", capability.reason)
                outputCapabilities.put(row)
            }
            result.put("additionalOutputCapabilities", outputCapabilities)
            call.resolve(result)
        }
    }

    @PluginMethod
    fun speakManualPhrase(call: PluginCall) {
        val phraseId = call.getString("phraseId") ?: run { call.reject("A safety phrase is required"); return }
        CallerSpeechAlerts.speakManual(getContext(), phraseId) { speech ->
            val result = JSObject()
            result.put("queued", speech.queued)
            result.put("status", speech.status)
            call.resolve(result)
        }
    }

    @PluginMethod
    fun stopManualSpeech(call: PluginCall) {
        CallerSpeechAlerts.stopManual()
        call.resolve()
    }

    @PluginMethod
    fun requestRole(call: PluginCall) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            call.reject("Call screening requires Android 10 or newer")
            return
        }
        val role = getContext().getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (role.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            val result = JSObject(); result.put("roleHeld", true); call.resolve(result)
            return
        }
        startActivityForResult(call, role.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), "roleResult")
    }

    @ActivityCallback
    private fun roleResult(call: PluginCall, result: ActivityResult) {
        val held = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            (getContext().getSystemService(Context.ROLE_SERVICE) as RoleManager)
                .isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        val response = JSObject(); response.put("roleHeld", held); call.resolve(response)
    }

    @PluginMethod
    fun setNumberLabel(call: PluginCall) {
        val number = call.getString("number") ?: run { call.reject("Number is required"); return }
            val label = call.getString("classification")?.uppercase() ?: run { call.reject("Classification is required"); return }
        try {
            ScreeningStore.setLabel(getContext(), number, label)
            val result = JSObject(); result.put("saved", true); call.resolve(result)
        } catch (e: Exception) { call.reject(e.message ?: "Could not save number label") }
    }

    @PluginMethod
    fun getHistory(call: PluginCall) {
        val arr = ScreeningStore.history(getContext())
        val result = JSObject()
        result.put("history", JSArray(arr.toString()))
        call.resolve(result)
    }
}
