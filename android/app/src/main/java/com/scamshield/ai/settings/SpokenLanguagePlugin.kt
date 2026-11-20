package com.scamshield.ai.settings

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import org.json.JSONObject

@CapacitorPlugin(name = "SpokenLanguage")
class SpokenLanguagePlugin : Plugin() {
    @PluginMethod
    fun getPreferredLanguage(call: PluginCall) {
        val result = JSObject()
        result.put("language", SpokenLanguageStore.get(getContext()) ?: JSONObject.NULL)
        call.resolve(result)
    }

    @PluginMethod
    fun setPreferredLanguage(call: PluginCall) {
        val language = call.getString("language")
            ?: run { call.reject("A spoken language is required"); return }
        try {
            SpokenLanguageStore.set(getContext(), language)
            val result = JSObject()
            result.put("language", language)
            call.resolve(result)
        } catch (error: IllegalArgumentException) {
            call.reject("Unsupported spoken language")
        }
    }
}
