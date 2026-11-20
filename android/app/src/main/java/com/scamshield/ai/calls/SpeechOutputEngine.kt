package com.scamshield.ai.calls

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer

/** Playback backends used by the single CallerSpeechAlerts queue. */
internal interface SpeechOutputEngine {
    val id: String
    fun supports(language: String, phraseId: String): Boolean
    fun play(context: Context, language: String, phraseId: String, utteranceId: String, onComplete: (Boolean) -> Unit): Boolean
    fun stop()
}

internal data class SpeechOutputCapability(
    val language: String,
    val status: String,
    val backend: String,
    val reason: String
)

/** Populate only with reviewed, licensed audio assets; empty in this foundation release. */
internal object RecordedPromptResources {
    val assets: Map<Pair<String, String>, Int> = emptyMap()
}

/** Exact language routing. It deliberately has no closest-language fallback. */
internal class SpeechOutputRouter(private val engines: List<SpeechOutputEngine>) {
    fun select(language: String, phraseId: String): SpeechOutputEngine? =
        engines.firstOrNull { it.supports(language, phraseId) }
}

/**
 * Playback for approved, bundled native-speaker prompts. The default map is empty until
 * real recordings and rights are supplied. No synthesized or substitute audio is generated.
 */
internal class RecordedPromptEngine(
    private val promptResources: Map<Pair<String, String>, Int> = RecordedPromptResources.assets
) : SpeechOutputEngine {
    override val id: String = "recorded_prompt"
    private var player: MediaPlayer? = null

    override fun supports(language: String, phraseId: String): Boolean =
        promptResources[language to phraseId]?.let { it != 0 } == true

    fun capability(language: String, requiredPhraseIds: Collection<String>): SpeechOutputCapability {
        val complete = requiredPhraseIds.isNotEmpty() && requiredPhraseIds.all { supports(language, it) }
        return SpeechOutputCapability(
            language = language,
            status = if (complete) "READY" else "MISSING RECORDINGS",
            backend = id,
            reason = if (complete) "approved_recordings_ready" else "approved_recordings_not_supplied"
        )
    }

    override fun play(
        context: Context,
        language: String,
        phraseId: String,
        utteranceId: String,
        onComplete: (Boolean) -> Unit
    ): Boolean {
        val resourceId = promptResources[language to phraseId]?.takeIf { it != 0 } ?: return false
        return try {
            stop()
            val created = MediaPlayer.create(context.applicationContext, resourceId) ?: return false
            player = created
            created.setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build())
            created.setOnCompletionListener {
                release(it)
                onComplete(true)
            }
            created.setOnErrorListener { mp, _, _ ->
                release(mp)
                onComplete(false)
                true
            }
            created.start()
            true
        } catch (_: Exception) {
            stop()
            false
        }
    }

    override fun stop() {
        val current = player ?: return
        player = null
        runCatching { current.stop() }
        runCatching { current.release() }
    }

    private fun release(completed: MediaPlayer) {
        if (player === completed) player = null
        runCatching { completed.release() }
    }
}

/** Adapter keeps Android TextToSpeech inside the existing shared speech coordinator. */
internal class AndroidTtsOutputEngine(
    private val isLanguageReady: (String) -> Boolean,
    private val phraseFor: (String, String) -> String?,
    private val speakText: (String, String) -> Boolean,
    private val stopTts: () -> Unit
) : SpeechOutputEngine {
    override val id: String = "android_tts"

    override fun supports(language: String, phraseId: String): Boolean =
        isLanguageReady(language) && phraseFor(language, phraseId) != null

    override fun play(context: Context, language: String, phraseId: String, utteranceId: String, onComplete: (Boolean) -> Unit): Boolean {
        val text = phraseFor(language, phraseId) ?: return false
        return speakText(text, utteranceId)
    }

    override fun stop() = stopTts()
}
