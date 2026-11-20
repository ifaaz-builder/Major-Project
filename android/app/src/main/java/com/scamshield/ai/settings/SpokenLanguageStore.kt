package com.scamshield.ai.settings

import android.content.Context

/** Persistent, app-wide spoken-language selection shared with native services. */
object SpokenLanguageStore {
    private const val PREFS = "scamshield_app_settings"
    private const val SPOKEN_LANGUAGE = "spoken_language"
    private val supported = setOf("en", "hi", "ml", "kn", "ta", "te", "bn", "mr", "gu", "pa", "ur", "es")

    /** Exposed for capability reporting and regression tests; does not add languages. */
    fun supportedLanguageCodes(): Set<String> = supported.toSet()

    fun get(context: Context): String? = context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(SPOKEN_LANGUAGE, null)
        ?.takeIf(supported::contains)

    fun set(context: Context, language: String) {
        require(language in supported) { "Unsupported spoken language" }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(SPOKEN_LANGUAGE, language)
            .apply()
    }
}
