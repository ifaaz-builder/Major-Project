package com.scamshield.ai.voice

/** Contracts for future local input engines. They accept in-memory audio only. */
data class LanguageIdResult(val language: String?, val confidence: Float)
data class TranscriptCandidate(val text: String, val confidence: Float)
data class VoiceIntentResult(val intentId: String, val confidence: Float)

interface LocalLanguageIdentificationEngine {
    fun identify(samples16KhzMono: FloatArray): LanguageIdResult
}

interface OfflineSpeechRecognitionEngine {
    fun recognize(samples16KhzMono: FloatArray, language: String): TranscriptCandidate
}

/** Implementations must abstain below their validated confidence threshold. */
interface LocalVoiceIntentEngine {
    fun classify(transcript: String, language: String): VoiceIntentResult?
}
