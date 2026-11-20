package com.scamshield.ai.notifications

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject

@CapacitorPlugin(name = "NotificationScanning", permissions = [Permission(alias = "sms", strings = [android.Manifest.permission.RECEIVE_SMS])])
class NotificationScanningPlugin : Plugin() {
    @PluginMethod fun getStatus(call: PluginCall) {
        val context = getContext()
        val result = JSObject()
        result.put("accessGranted", ScamShieldNotificationListenerService.isAccessGranted(context))
        result.put("monitoringEnabled", NotificationScanStore.enabled(context))
        result.put("retentionDays", NotificationScanStore.retentionDays(context))
        result.put("history", JSArray(NotificationScanStore.history(context).toString()))
        result.put("smsSupported", context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
        result.put("smsScanningEnabled", NotificationScanStore.smsScanningEnabled(context))
        result.put("smsPermissionGranted", ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED)
        result.put("apps", JSArray(apps(context).toString()))
        call.resolve(result)
    }

    @PluginMethod fun requestSmsPermission(call: PluginCall) {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            val result = JSObject(); result.put("granted", true); call.resolve(result); return
        }
        requestPermissionForAlias("sms", call, "smsPermissionResult")
    }

    @PermissionCallback private fun smsPermissionResult(call: PluginCall) {
        val granted = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val result = JSObject(); result.put("granted", granted); call.resolve(result)
    }

    @PluginMethod fun setSmsScanningEnabled(call: PluginCall) {
        val enabled = call.getBoolean("enabled") ?: run { call.reject("Enabled value is required"); return }
        val granted = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        if (enabled && !granted) { call.reject("SMS permission is not granted"); return }
        NotificationScanStore.setSmsScanningEnabled(getContext(), enabled)
        val result = JSObject(); result.put("smsScanningEnabled", enabled); call.resolve(result)
    }

    @PluginMethod fun openAccessSettings(call: PluginCall) {
        try {
            activity.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            call.resolve()
        } catch (e: Exception) { call.reject(e.message ?: "Could not open notification access settings") }
    }

    @PluginMethod fun setMonitoringEnabled(call: PluginCall) {
        val enabled = call.getBoolean("enabled") ?: run { call.reject("Enabled value is required"); return }
        NotificationScanStore.setEnabled(getContext(), enabled)
        val result = JSObject(); result.put("monitoringEnabled", enabled); call.resolve(result)
    }

    @PluginMethod fun setAppMonitored(call: PluginCall) {
        val packageName = call.getString("packageName") ?: run { call.reject("Package name is required"); return }
        val monitored = call.getBoolean("monitored") ?: run { call.reject("Monitored value is required"); return }
        NotificationScanStore.setMonitored(getContext(), packageName, monitored)
        call.resolve()
    }

    @PluginMethod fun setRetentionDays(call: PluginCall) {
        val days = call.getInt("days") ?: run { call.reject("Retention days are required"); return }
        try { NotificationScanStore.setRetentionDays(getContext(), days); call.resolve() }
        catch (e: Exception) { call.reject(e.message ?: "Invalid retention period") }
    }

    @PluginMethod fun clearHistory(call: PluginCall) {
        NotificationScanStore.clearHistory(getContext()); call.resolve()
    }

    private fun apps(context: android.content.Context): JSONArray {
        val pm = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(launcher, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcher, 0)
        }
        val appNames = linkedMapOf<String, String>()
        resolved.forEach { info ->
            val pkg = info.activityInfo?.packageName ?: return@forEach
            if (pkg != context.packageName) appNames[pkg] = info.loadLabel(pm).toString()
        }
        NotificationScanStore.observedPackages(context).forEach { pkg ->
            if (pkg == context.packageName || appNames.containsKey(pkg)) return@forEach
            appNames[pkg] = runCatching {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            }.getOrDefault(pkg)
        }
        val array = JSONArray()
        appNames.entries.sortedBy { it.value.lowercase() }.forEach { (pkg, label) ->
            array.put(JSONObject().put("packageName", pkg).put("label", label)
                .put("monitored", NotificationScanStore.isMonitored(context, pkg)))
        }
        return array
    }
}
