package com.scamshield.ai.notifications

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** Process-local notification speech dedupe. No body, sender, or derived key is persisted. */
internal class NotificationSpeechDeduper(
    private val ttlMs: Long = 2 * 60_000L,
    private val clock: () -> Long = System::currentTimeMillis,
    private val secret: ByteArray = ByteArray(32).also(SecureRandom()::nextBytes)
) {
    private val seen = LinkedHashMap<String, Long>()

    @Synchronized fun shouldAnnounce(notificationKey: String, content: String, eventTimestamp: Long = 0L): Boolean {
        val now = clock()
        seen.entries.removeAll { now - it.value > ttlMs }
        val normalized = content.lowercase().replace(Regex("\\s+"), " ").trim()
        if (notificationKey.isBlank() || normalized.isBlank()) return true
        val digest = digest("$notificationKey|$eventTimestamp|$normalized")
        if (seen.containsKey(digest)) return false
        seen[digest] = now
        while (seen.size > 256) seen.remove(seen.keys.first())
        return true
    }

    /** Opaque, process-local identity usable by the speech queue's duplicate guard. */
    @Synchronized fun eventId(notificationKey: String, content: String, eventTimestamp: Long = 0L): String = digest(
        "$notificationKey|$eventTimestamp|${content.lowercase().replace(Regex("\\s+"), " ").trim()}"
    )

    private fun digest(value: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret, "HmacSHA256"))
        return Base64.getEncoder().withoutPadding().encodeToString(mac.doFinal(value.toByteArray(Charsets.UTF_8)))
    }
}

internal object NotificationSpeechEvents {
    private val deduper = NotificationSpeechDeduper()
    fun markAndGetEventId(key: String, text: String, eventTimestamp: Long): String? =
        if (deduper.shouldAnnounce(key, text, eventTimestamp)) deduper.eventId(key, text, eventTimestamp) else null
}
