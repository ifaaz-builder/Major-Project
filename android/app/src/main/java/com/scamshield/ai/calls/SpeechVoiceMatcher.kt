package com.scamshield.ai.calls

import java.util.Locale

/** Exact language matching helpers. A different language is never a successful fallback. */
internal object SpeechVoiceMatcher {
    data class Language(val code: String, val tag: String)
    data class Result(val status: String, val localeTag: String? = null)

    val languages = listOf(
        Language("en", "en-US"), Language("hi", "hi-IN"), Language("ml", "ml-IN"),
        Language("kn", "kn-IN"), Language("ta", "ta-IN"), Language("te", "te-IN"),
        Language("bn", "bn-IN"), Language("mr", "mr-IN"), Language("gu", "gu-IN"),
        Language("pa", "pa-IN"), Language("ur", "ur-PK"), Language("es", "es-ES")
    )

    fun matchesLanguage(expectedCode: String, voiceLocale: Locale): Boolean =
        voiceLocale.language.equals(expectedCode, ignoreCase = true)

    fun status(code: String, availableVoiceLocales: Collection<Locale>, hasOfflineVoice: Boolean, missingVoiceData: Boolean): Result {
        val exact = availableVoiceLocales.firstOrNull { matchesLanguage(code, it) }
        if (hasOfflineVoice && exact != null) return Result("READY", exact.toLanguageTag())
        if (missingVoiceData || exact != null) {
            return Result("MISSING VOICE DATA", exact?.toLanguageTag())
        }
        return Result("UNSUPPORTED")
    }
}
