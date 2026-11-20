package com.scamshield.ai.calls

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.scamshield.ai.settings.SpokenLanguageStore
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** One offline, serialized speech path shared by all warning sources and manual read-aloud. */
object CallerSpeechAlerts {
    private const val TAG = "ScamShieldSpeech"
    private val mainHandler = Handler(Looper.getMainLooper())
    private val callbacks = ConcurrentHashMap<String, (SpeechResult) -> Unit>()
    private val statusCallbacks = mutableListOf<(List<LanguageStatus>, String) -> Unit>()
    private val queue = SpeechQueue()
    @Volatile private var engineStatus = "not initialized"
    @Volatile private var lastOutcome = "No speech attempted"
    private var tts: TextToSpeech? = null
    private var ready = false
    private var activeEventId: String? = null
    private var activeIsManual = false
    private var activeOutput: SpeechOutputEngine? = null
    private val recordedPromptEngine = RecordedPromptEngine()
    private var activeFocusManager: AudioManager? = null
    private var activeFocusRequest: AudioFocusRequest? = null
    private var legacyFocusListener: AudioManager.OnAudioFocusChangeListener? = null
    private var languageStatuses: List<LanguageStatus> = emptyList()
    private var engineFailure = ""

    data class SpeechResult(val queued: Boolean, val status: String)
    data class LanguageStatus(val code: String, val tag: String, val status: String, val voiceLocale: String?)
    data class Diagnostics(
        val engineStatus: String,
        val lastOutcome: String,
        val suppressionReason: String,
        val notificationVolume: Int,
        val notificationVolumeMax: Int,
        val ringerMode: String,
        val dndMode: Int
    )

    /** Warm-up is best effort and never blocks a Telecom callback. */
    fun prepare(context: Context) { mainHandler.post { ensureEngine(context.applicationContext) } }

    /** Call only after CallScreeningServiceImpl has sent its allow response. */
    fun announceCall(context: Context, classification: String, eventId: Long) {
        val phrase = when (classification.uppercase(Locale.ROOT)) {
            "SCAM" -> SpokenPhraseCatalog.CALL_SCAM
            "SPAM" -> SpokenPhraseCatalog.CALL_SPAM
            else -> {
                Log.i(TAG, "automatic_speech_skipped source=call reason=non_suspicious_classification")
                return
            }
        }
        announce(context, "call:$eventId", phrase, SpeechQueue.PRIORITY_CALL)
    }

    /** Used by SMS and notification warning paths only after their shared dedupe gate allows the event. */
    fun announceWarning(context: Context, source: String, eventId: String, matchedRules: Collection<String>) {
        val phrase = SpokenPhraseCatalog.messagePhrase(matchedRules)
        val urgent = phrase == SpokenPhraseCatalog.MESSAGE_SECRET || phrase == SpokenPhraseCatalog.MESSAGE_ACCOUNT
        val priority = when {
            source == "sms" && urgent -> SpeechQueue.PRIORITY_URGENT_SMS
            urgent -> SpeechQueue.PRIORITY_URGENT_NOTIFICATION
            source == "sms" -> SpeechQueue.PRIORITY_WARNING + 5
            else -> SpeechQueue.PRIORITY_WARNING
        }
        announce(context, "$source:$eventId", phrase, priority)
    }

    private fun announce(context: Context, eventId: String, phraseId: String, priority: Int) {
        if (!ScreeningStore.spokenAlertsEnabled(context)) {
            Log.i(TAG, "automatic_speech_skipped source=${eventId.substringBefore(':')} reason=setting_disabled")
            return
        }
        enqueue(context.applicationContext, eventId, phraseId, priority, manual = false, callback = null)
    }

    /** User initiated sample, independent of the automatic-warning toggle. */
    fun test(context: Context, callback: (SpeechResult) -> Unit) =
        speakManual(context, SpokenPhraseCatalog.TEST, callback)

    /** Manual read-aloud uses fixed phrases; arbitrary message or scan text is never passed to TTS. */
    fun speakManual(context: Context, phraseId: String, callback: (SpeechResult) -> Unit) {
        if (SpokenPhraseCatalog.phrase("en", phraseId) == null) {
            callback(SpeechResult(false, "Unsupported safety phrase"))
            return
        }
        enqueue(context.applicationContext, "manual:${UUID.randomUUID()}", phraseId,
            SpeechQueue.PRIORITY_MANUAL, manual = true, callback = callback)
    }

    fun stopManual() {
        mainHandler.post {
            queue.removeManual().forEach { callbacks.remove(it.eventId)?.invoke(SpeechResult(false, "Manual speech stopped")) }
            if (activeIsManual) {
                val id = activeEventId
                activeOutput?.stop()
                if (id != null) finishUtterance(id, "Manual speech stopped", false) else startNext()
            } else startNext()
        }
    }

    fun stopAutomatic() {
        mainHandler.post {
            queue.removeAutomatic().forEach { callbacks.remove(it.eventId)?.invoke(SpeechResult(false, "Automatic warnings are disabled")) }
            if (!activeIsManual && activeEventId != null) {
                val id = activeEventId
                activeOutput?.stop()
                if (id != null) finishUtterance(id, "Automatic warnings are disabled", false) else startNext()
            } else startNext()
        }
    }

    fun languageStatus(context: Context, callback: (List<LanguageStatus>, String) -> Unit) {
        mainHandler.post {
            if (ready) {
                tts?.let { languageStatuses = inspectLanguages(it) }
                callback(languageStatuses, engineFailure)
            }
            else {
                statusCallbacks += callback
                ensureEngine(context.applicationContext)
            }
        }
    }

    /** Tulu and Beary remain informational capabilities until approved exact-language audio exists. */
    internal fun outputCapabilities(): List<SpeechOutputCapability> = listOf(
        recordedPromptEngine.capability("tulu", requiredPromptIds()),
        recordedPromptEngine.capability("beary", requiredPromptIds())
    )

    fun diagnostics(context: Context): Diagnostics {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val dnd = readDndMode(context)
        return Diagnostics(engineStatus, lastOutcome, suppressionReason(context, audio, dnd).orEmpty(),
            audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION),
            audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
            when (audio.ringerMode) {
                AudioManager.RINGER_MODE_SILENT -> "silent"
                AudioManager.RINGER_MODE_VIBRATE -> "vibrate"
                else -> "normal"
            }, dnd)
    }

    private fun enqueue(context: Context, eventId: String, phraseId: String, priority: Int, manual: Boolean,
                        callback: ((SpeechResult) -> Unit)?) {
        appContext = context.applicationContext
        val result = queue.offer(SpeechQueue.Item(eventId, phraseId, priority, manual))
        if (!result.accepted) {
            val why = if (result.duplicate) "duplicate_event" else "queue_full"
            lastOutcome = "Speech not queued: $why"
            Log.w(TAG, "speech_not_queued reason=$why priority=$priority visual_warning_remains=${!manual}")
            callback?.invoke(SpeechResult(false, lastOutcome))
            return
        }
        if (callback != null) callbacks[eventId] = callback
        if (result.evictedLowerPriority) Log.w(TAG, "speech_queue_evicted_lower_priority pending=${queue.size()}")
        Log.i(TAG, "speech_queued priority=$priority manual=$manual pending=${queue.size()}")
        mainHandler.post {
            if (ready) startNext() else ensureEngine(context)
        }
    }

    private fun ensureEngine(context: Context) {
        if (ready || engineStatus == "initializing") return
        appContext = context.applicationContext
        engineStatus = "initializing"
        engineFailure = ""
        Log.i(TAG, "tts_initializing")
        try {
            tts?.shutdown()
            tts = TextToSpeech(context.applicationContext) { status ->
                val engine = tts
                if (status != TextToSpeech.SUCCESS || engine == null) {
                    failEngine("initialization_failed")
                    return@TextToSpeech
                }
                try {
                    val attrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
                    if (engine.setAudioAttributes(attrs) != TextToSpeech.SUCCESS) {
                        failEngine("audio_setup_failed")
                        return@TextToSpeech
                    }
                    languageStatuses = inspectLanguages(engine)
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String) {
                            engineStatus = "speaking"
                            lastOutcome = "Playback started"
                            Log.i(TAG, "tts_started")
                        }
                        override fun onDone(utteranceId: String) = finishUtterance(utteranceId, "Playback completed", true)
                        @Deprecated("Overridden for Android TTS listener compatibility")
                        override fun onError(utteranceId: String) = finishUtterance(utteranceId, "TTS playback error", false)
                        override fun onError(utteranceId: String, errorCode: Int) = finishUtterance(utteranceId, "TTS playback error ($errorCode)", false)
                        override fun onStop(utteranceId: String, interrupted: Boolean) = finishUtterance(utteranceId, "Playback stopped", false)
                    })
                    ready = true
                    engineStatus = "ready"
                    Log.i(TAG, "tts_ready languages_ready=${languageStatuses.count { it.status == "READY" }} languages_total=${languageStatuses.size}")
                    statusCallbacks.toList().also { statusCallbacks.clear() }.forEach { it(languageStatuses, engineFailure) }
                    startNext()
                } catch (error: Exception) {
                    failEngine("engine_setup_${error.javaClass.simpleName}")
                }
            }
        } catch (error: Exception) {
            failEngine("initialization_${error.javaClass.simpleName}")
        }
    }

    private fun isInstalledOfflineVoice(voice: Voice): Boolean =
        !voice.isNetworkConnectionRequired && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features

    private fun inspectLanguages(engine: TextToSpeech): List<LanguageStatus> {
        val voices = engine.voices.orEmpty()
        return SpeechVoiceMatcher.languages.map { language ->
            val requested = Locale.forLanguageTag(language.tag)
            val languageVoices = voices.filter { SpeechVoiceMatcher.matchesLanguage(language.code, it.locale) }
            val installedOffline = languageVoices.any { isInstalledOfflineVoice(it) }
            val checked = SpeechVoiceMatcher.status(language.code, languageVoices.map { it.locale }, installedOffline,
                engine.isLanguageAvailable(requested) == TextToSpeech.LANG_MISSING_DATA)
            LanguageStatus(language.code, language.tag, checked.status, checked.localeTag)
        }
    }

    private fun failEngine(reason: String) {
        ready = false
        engineStatus = "unavailable"
        engineFailure = reason
        lastOutcome = "Text-to-speech is unavailable"
        Log.e(TAG, "tts_unavailable reason=$reason queued_speech_retained=true visual_warnings_remain=true")
        tts?.shutdown()
        tts = null
        callbacks.entries.toList().forEach { (id, cb) ->
            callbacks.remove(id)
            cb(SpeechResult(false, "Text-to-speech is unavailable"))
        }
        val unavailable = SpeechVoiceMatcher.languages.map { LanguageStatus(it.code, it.tag, "UNSUPPORTED", null) }
        statusCallbacks.toList().also { statusCallbacks.clear() }.forEach { it(unavailable, reason) }
        startNext()
    }

    private fun startNext() {
        if (activeEventId != null) return
        val request = queue.next() ?: return
        val context = appContext ?: return
        if (!request.manual && !ScreeningStore.spokenAlertsEnabled(context)) {
            callbacks.remove(request.eventId)?.invoke(SpeechResult(false, "Automatic warnings are disabled"))
            startNext()
            return
        }
        if (ready) tts?.let { languageStatuses = inspectLanguages(it) }
        val language = SpokenLanguageStore.get(context) ?: "en"
        val androidTtsEngine = AndroidTtsOutputEngine(
            isLanguageReady = { code -> ready && languageStatuses.any { it.code == code && it.status == "READY" } },
            phraseFor = SpokenPhraseCatalog::phrase,
            speakText = { text, utteranceId -> tts?.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId) == TextToSpeech.SUCCESS },
            stopTts = { tts?.stop() }
        )
        val selectedOutput = SpeechOutputRouter(listOf(recordedPromptEngine, androidTtsEngine))
            .select(language, request.phraseId)
        if (selectedOutput == null) {
            val status = languageStatuses.firstOrNull { it.code == language }?.status ?: "UNSUPPORTED"
            lastOutcome = "Selected voice unavailable ($status)"
            Log.w(TAG, "speech_skipped reason=exact_language_output_unavailable language=$language status=$status")
            callbacks.remove(request.eventId)?.invoke(SpeechResult(false, lastOutcome))
            startNext()
            return
        }
        val blocked = suppressionReason(context)
        if (blocked != null) {
            lastOutcome = "Suppressed: $blocked"
            Log.i(TAG, "speech_suppressed reason=$blocked")
            callbacks.remove(request.eventId)?.invoke(SpeechResult(false, lastOutcome))
            startNext()
            return
        }
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
        if (!requestFocus(audio, attrs)) {
            lastOutcome = "Suppressed: Android did not grant transient audio focus"
            callbacks.remove(request.eventId)?.invoke(SpeechResult(false, lastOutcome))
            startNext()
            return
        }
        if (selectedOutput.id == "android_tts") {
            val expected = SpeechVoiceMatcher.languages.firstOrNull { it.code == language }
            val voice = tts?.voices.orEmpty().firstOrNull {
                SpeechVoiceMatcher.matchesLanguage(language, it.locale) && isInstalledOfflineVoice(it)
            }
            if (expected == null || voice == null || tts?.setVoice(voice) != TextToSpeech.SUCCESS ||
                tts?.voice?.locale?.language != expected.code) {
                lastOutcome = "Selected voice could not be selected exactly"
                Log.w(TAG, "speech_skipped reason=exact_voice_selection_failed language=$language")
                callbacks.remove(request.eventId)?.invoke(SpeechResult(false, lastOutcome))
                abandonFocus()
                startNext()
                return
            }
        }
        activeEventId = request.eventId
        activeIsManual = request.manual
        activeOutput = selectedOutput
        val accepted = selectedOutput.play(context, language, request.phraseId, request.eventId) { success ->
            finishUtterance(request.eventId, if (success) "Playback completed" else "Speech playback error", success)
        }
        if (!accepted) finishUtterance(request.eventId, "Speech backend rejected request", false)
        else {
            engineStatus = "queued"
            lastOutcome = "Speech queued in $language via ${selectedOutput.id}"
            Log.i(TAG, "speech_accepted language=$language backend=${selectedOutput.id} priority=${request.priority} manual=${request.manual}")
        }
    }

    private fun finishUtterance(id: String, outcome: String, success: Boolean) {
        mainHandler.post {
            if (activeEventId != id) return@post
            activeEventId = null
            activeIsManual = false
            activeOutput = null
            engineStatus = if (success) "ready" else "playback ended"
            lastOutcome = outcome
            callbacks.remove(id)?.invoke(SpeechResult(success, outcome))
            abandonFocus()
            startNext()
        }
    }

    private fun requestFocus(audio: AudioManager, attributes: AudioAttributes): Boolean {
        abandonFocus()
        val listener = AudioManager.OnAudioFocusChangeListener { change ->
            if (change == AudioManager.AUDIOFOCUS_LOSS) {
                Log.i(TAG, "audio_focus_lost")
                activeOutput?.stop()
                activeEventId?.let { finishUtterance(it, "Playback stopped after audio focus loss", false) }
                abandonFocus()
            }
        }
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes).setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener(listener, mainHandler).build()
            val result = audio.requestAudioFocus(request)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                activeFocusManager = audio; activeFocusRequest = request
            }
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audio.requestAudioFocus(listener, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                activeFocusManager = audio; legacyFocusListener = listener
            }
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        return granted
    }

    private fun suppressionReason(context: Context): String? {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return suppressionReason(context, audio, readDndMode(context))
    }

    private fun suppressionReason(context: Context, audio: AudioManager, dnd: Int): String? {
        if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) return "ringer mode is not normal"
        if (audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION) <= 0 || audio.isStreamMute(AudioManager.STREAM_NOTIFICATION)) return "notification volume is muted"
        if (dnd != 0) return "Do Not Disturb is active"
        return null
    }

    private fun readDndMode(context: Context): Int = try { Settings.Global.getInt(context.contentResolver, "zen_mode", 0) } catch (_: Exception) { -1 }

    private fun abandonFocus() {
        val audio = activeFocusManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) activeFocusRequest?.let { audio.abandonAudioFocusRequest(it) }
        else {
            @Suppress("DEPRECATION")
            legacyFocusListener?.let { audio.abandonAudioFocus(it) }
        }
        activeFocusManager = null; activeFocusRequest = null; legacyFocusListener = null
    }

    private fun requiredPromptIds(): List<String> = listOf(
        SpokenPhraseCatalog.CALL_SCAM, SpokenPhraseCatalog.CALL_SPAM,
        SpokenPhraseCatalog.MESSAGE_SECRET, SpokenPhraseCatalog.MESSAGE_ACCOUNT,
        SpokenPhraseCatalog.MESSAGE_LINK, SpokenPhraseCatalog.MESSAGE_GENERIC,
        SpokenPhraseCatalog.MANUAL_DANGER, SpokenPhraseCatalog.MANUAL_SAFE,
        SpokenPhraseCatalog.LINK_DANGER, SpokenPhraseCatalog.LINK_CAUTION,
        SpokenPhraseCatalog.LINK_SAFE, SpokenPhraseCatalog.TEST
    )

    @Volatile private var appContext: Context? = null
}
