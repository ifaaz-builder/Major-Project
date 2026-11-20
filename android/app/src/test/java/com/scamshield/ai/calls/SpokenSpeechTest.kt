package com.scamshield.ai.calls

import com.scamshield.ai.notifications.NotificationSpeechDeduper
import com.scamshield.ai.settings.SpokenLanguageStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SpokenSpeechTest {
    private class TestOutput(
        override val id: String,
        private val available: Set<Pair<String, String>>
    ) : SpeechOutputEngine {
        override fun supports(language: String, phraseId: String) = available.contains(language to phraseId)
        override fun play(context: android.content.Context, language: String, phraseId: String, utteranceId: String, onComplete: (Boolean) -> Unit) =
            supports(language, phraseId)
        override fun stop() = Unit
    }

    @Test fun backendSelectionRequiresAnExactLanguageAndPhrase() {
        val tts = TestOutput("android_tts", setOf("kn" to "speech_test"))
        val recorded = TestOutput("recorded_prompt", setOf("tulu" to "speech_test"))
        val router = SpeechOutputRouter(listOf(recorded, tts))
        assertEquals("recorded_prompt", router.select("tulu", "speech_test")?.id)
        assertEquals("android_tts", router.select("kn", "speech_test")?.id)
        assertEquals(null, router.select("tulu", "call_scam"))
        assertEquals(null, router.select("beary", "speech_test"))
        assertEquals(null, router.select("kn-IN", "speech_test"))
    }

    @Test fun missingRecordedVoiceIsUnavailableUntilEveryRequiredPhraseExists() {
        val engine = RecordedPromptEngine()
        assertFalse(engine.supports("tulu", "speech_test"))
        assertEquals("MISSING RECORDINGS", engine.capability("tulu", listOf("speech_test")).status)
        val partial = RecordedPromptEngine(mapOf(("beary" to "speech_test") to 1))
        assertEquals("MISSING RECORDINGS", partial.capability("beary", listOf("speech_test", "call_scam")).status)
        val complete = RecordedPromptEngine(mapOf(
            ("beary" to "speech_test") to 1,
            ("beary" to "call_scam") to 2
        ))
        assertEquals("READY", complete.capability("beary", listOf("speech_test", "call_scam")).status)
    }

    @Test fun existingPreferenceCodesRemainUnchangedAndDoNotExposeUnreadyLanguages() {
        assertEquals(setOf("en", "hi", "ml", "kn", "ta", "te", "bn", "mr", "gu", "pa", "ur", "es"),
            SpokenLanguageStore.supportedLanguageCodes())
        assertFalse("tulu" in SpokenLanguageStore.supportedLanguageCodes())
        assertFalse("beary" in SpokenLanguageStore.supportedLanguageCodes())
    }

    @Test fun allTwelveLanguagesHaveEveryPhraseAndNoEmptyText() {
        assertEquals(setOf("en", "hi", "ml", "kn", "ta", "te", "bn", "mr", "gu", "pa", "ur", "es"), SpokenPhraseCatalog.supportedLanguages())
        SpokenPhraseCatalog.supportedLanguages().forEach { language ->
            listOf(SpokenPhraseCatalog.CALL_SCAM, SpokenPhraseCatalog.CALL_SPAM,
                SpokenPhraseCatalog.MESSAGE_SECRET, SpokenPhraseCatalog.MESSAGE_ACCOUNT,
                SpokenPhraseCatalog.MESSAGE_LINK, SpokenPhraseCatalog.MESSAGE_GENERIC,
                SpokenPhraseCatalog.MANUAL_DANGER, SpokenPhraseCatalog.MANUAL_SAFE,
                SpokenPhraseCatalog.LINK_DANGER, SpokenPhraseCatalog.LINK_CAUTION,
                SpokenPhraseCatalog.LINK_SAFE, SpokenPhraseCatalog.TEST).forEach { phrase ->
                assertFalse("$language/$phrase", SpokenPhraseCatalog.phrase(language, phrase).isNullOrBlank())
            }
        }
    }

    @Test fun messagePhraseUsesMatchedRuleIdsOnly() {
        assertEquals(SpokenPhraseCatalog.MESSAGE_SECRET, SpokenPhraseCatalog.messagePhrase(listOf("secret_request")))
        assertEquals(SpokenPhraseCatalog.MESSAGE_ACCOUNT, SpokenPhraseCatalog.messagePhrase(listOf("urgent_payment_or_account_action")))
        assertEquals(SpokenPhraseCatalog.MESSAGE_LINK, SpokenPhraseCatalog.messagePhrase(listOf("shortened_link")))
        assertEquals(SpokenPhraseCatalog.MESSAGE_GENERIC, SpokenPhraseCatalog.messagePhrase(emptyList()))
    }

    @Test fun voiceMatchingRejectsClosestDifferentLanguageAndRequiresOfflineVoice() {
        assertTrue(SpeechVoiceMatcher.matchesLanguage("hi", Locale.forLanguageTag("hi-IN")))
        assertFalse(SpeechVoiceMatcher.matchesLanguage("hi", Locale.forLanguageTag("en-IN")))
        assertEquals("READY", SpeechVoiceMatcher.status("hi", listOf(Locale.forLanguageTag("hi-IN")), true, false).status)
        assertEquals("MISSING VOICE DATA", SpeechVoiceMatcher.status("hi", listOf(Locale.forLanguageTag("hi-IN")), false, false).status)
        assertEquals("UNSUPPORTED", SpeechVoiceMatcher.status("hi", listOf(Locale.forLanguageTag("en-IN")), false, false).status)
    }

    @Test fun queueIsBoundedPrioritizedAndDoesNotSilentlyAcceptOverflow() {
        val queue = SpeechQueue(capacity = 2)
        assertTrue(queue.offer(SpeechQueue.Item("n1", "message_generic", SpeechQueue.PRIORITY_WARNING)).accepted)
        assertTrue(queue.offer(SpeechQueue.Item("manual", "speech_test", SpeechQueue.PRIORITY_MANUAL, manual = true)).accepted)
        val urgent = queue.offer(SpeechQueue.Item("call", "call_scam", SpeechQueue.PRIORITY_CALL))
        assertTrue(urgent.accepted)
        assertTrue(urgent.evictedLowerPriority)
        assertEquals("call", queue.next()?.eventId)
        assertEquals("n1", queue.next()?.eventId)
        assertEquals(0, queue.size())

        queue.offer(SpeechQueue.Item("a", "message_generic", SpeechQueue.PRIORITY_WARNING))
        queue.offer(SpeechQueue.Item("b", "message_generic", SpeechQueue.PRIORITY_WARNING))
        assertTrue(queue.offer(SpeechQueue.Item("c", "message_generic", SpeechQueue.PRIORITY_WARNING)).full)
    }

    @Test fun queueAndNotificationGuardSuppressDuplicateEventsButAllowDifferentMessages() {
        var now = 1_000L
        val queue = SpeechQueue(clock = { now })
        assertTrue(queue.offer(SpeechQueue.Item("same", "message_secret", SpeechQueue.PRIORITY_WARNING)).accepted)
        assertTrue(queue.offer(SpeechQueue.Item("same", "message_secret", SpeechQueue.PRIORITY_WARNING)).duplicate)
        now += 10 * 60_000L + 1
        assertTrue(queue.offer(SpeechQueue.Item("same", "message_secret", SpeechQueue.PRIORITY_WARNING)).accepted)

        var dedupeNow = 5_000L
        val deduper = NotificationSpeechDeduper(clock = { dedupeNow }, secret = ByteArray(32) { 4 })
        assertTrue(deduper.shouldAnnounce("thread-key", "Urgent request for OTP", 100L))
        assertFalse(deduper.shouldAnnounce("thread-key", "  urgent   request for otp ", 100L))
        assertNotNull(deduper.eventId("thread-key", "Urgent request for OTP", 100L))
        assertTrue(deduper.shouldAnnounce("thread-key", "Urgent request for OTP", 101L))
        assertTrue(deduper.shouldAnnounce("thread-key", "A different alert", 100L))
        dedupeNow += 2 * 60_000L + 1
        assertTrue(deduper.shouldAnnounce("thread-key", "Urgent request for OTP", 100L))
    }

    @Test fun disablingAutomaticSpeechLeavesManualTestQueued() {
        val queue = SpeechQueue()
        queue.offer(SpeechQueue.Item("warning", "message_secret", SpeechQueue.PRIORITY_WARNING))
        queue.offer(SpeechQueue.Item("test", "speech_test", SpeechQueue.PRIORITY_MANUAL, manual = true))
        assertEquals(1, queue.removeAutomatic().size)
        assertEquals("test", queue.next()?.eventId)
    }
}
