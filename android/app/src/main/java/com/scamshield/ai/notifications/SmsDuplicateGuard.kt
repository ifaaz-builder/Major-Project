package com.scamshield.ai.notifications

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Process-local, short-lived event ledger shared by the SMS receiver and listener.
 * It retains only keyed digests, never message text, sender numbers, or notification keys.
 */
object SmsDuplicateGuard {
    // Keep stable per-message IDs long enough to catch delayed/repeated platform
    // callbacks. Pairing the two delivery mechanisms uses a shorter window.
    private const val EVENT_TTL_MS = 10 * 60_000L
    private const val PAIR_TTL_MS = 2 * 60_000L
    private const val MAX_EVENTS = 512
    private val entries = mutableListOf<Entry>()
    private val processKey = ByteArray(32).also(SecureRandom()::nextBytes)

    private data class Entry(
        val source: String,
        val messageDigest: String,
        val eventDigest: String,
        val observedAt: Long,
        var paired: Boolean
    )

    data class Decision(val suppress: Boolean, val reason: String)

    /** Returns true when this direct SMS was already seen or paired with its notification. */
    @Synchronized
    fun markDirect(text: String, smsTimestamp: Long): Decision = mark(
        source = "direct",
        text = text,
        eventIdentity = "sms:$smsTimestamp",
        hasStableIdentity = smsTimestamp > 0L
    )

    /** Returns true when this notification event was already seen or paired with direct SMS. */
    @Synchronized
    fun markNotification(text: String, notificationKey: String, messageTimestamp: Long, hasStableTimestamp: Boolean): Decision = mark(
        source = "notification",
        text = text,
        // Some SMS apps omit a message timestamp or refresh notification.when on
        // every repost. In that case the stable notification key plus exact message
        // digest still collapses reposts; a new SMS_RECEIVED event remains distinct
        // because its own SMS timestamp is the authoritative history path.
        eventIdentity = "notification:$notificationKey:${if (hasStableTimestamp && messageTimestamp > 0L) messageTimestamp else "content"}",
        hasStableIdentity = notificationKey.isNotBlank()
    )

    private fun mark(source: String, text: String, eventIdentity: String, hasStableIdentity: Boolean): Decision {
        val now = android.os.SystemClock.elapsedRealtime()
        entries.removeAll { now - it.observedAt > EVENT_TTL_MS }

        val normalized = text.lowercase().replace(Regex("\\s+"), " ").trim()
        if (normalized.isEmpty()) return Decision(false, "empty_text")
        val messageDigest = digest("message:$normalized")
        val eventDigest = digest("event:$eventIdentity:$messageDigest")

        // Notification apps can repost the same notification while refreshing its UI.
        // A stable event ID suppresses those repeats, while a new message timestamp/key
        // remains eligible even when its text happens to be identical.
        if (hasStableIdentity && entries.any { it.source == source && it.eventDigest == eventDigest }) {
            return Decision(true, "same_path_event_repeat")
        }

        val otherSource = if (source == "direct") "notification" else "direct"
        val counterpart = entries.firstOrNull {
            it.source == otherSource && !it.paired && it.messageDigest == messageDigest && now - it.observedAt <= PAIR_TTL_MS
        }
        if (counterpart != null) {
            counterpart.paired = true
            entries += Entry(source, messageDigest, eventDigest, now, paired = true)
            trim()
            return Decision(true, "paired_with_$otherSource")
        }

        entries += Entry(source, messageDigest, eventDigest, now, paired = false)
        trim()
        return Decision(false, if (hasStableIdentity) "first_path_event" else "no_stable_event_identity")
    }

    private fun trim() {
        while (entries.size > MAX_EVENTS) entries.removeAt(0)
    }

    private fun digest(value: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(processKey, "HmacSHA256"))
        return Base64.encodeToString(mac.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }
}
