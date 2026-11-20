package com.scamshield.ai.calls

/** Bounded, single-consumer speech queue. It retains only opaque event IDs and phrase IDs. */
internal class SpeechQueue(
    private val capacity: Int = 24,
    private val dedupeTtlMs: Long = 10 * 60_000L,
    private val clock: () -> Long = System::currentTimeMillis
) {
    data class Item(val eventId: String, val phraseId: String, val priority: Int, val manual: Boolean = false, val sequence: Long = 0L)
    data class Offer(val accepted: Boolean, val duplicate: Boolean = false, val full: Boolean = false, val evictedLowerPriority: Boolean = false)

    private val items = mutableListOf<Item>()
    private val seen = LinkedHashMap<String, Long>()
    private var sequence = 0L
    private val maxSeen = 512

    @Synchronized fun offer(item: Item): Offer {
        val now = clock()
        seen.entries.removeAll { now - it.value > dedupeTtlMs }
        if (seen.containsKey(item.eventId)) return Offer(accepted = false, duplicate = true)
        var evicted = false
        if (items.size >= capacity) {
            val candidate = items.withIndex()
                .filter { it.value.priority < item.priority }
                .minWithOrNull(compareBy<IndexedValue<Item>> { it.value.priority }.thenBy { it.value.sequence })
            if (candidate == null) return Offer(accepted = false, full = true)
            items.removeAt(candidate.index)
            evicted = true
        }
        val acceptedItem = item.copy(sequence = sequence++)
        items += acceptedItem
        seen[item.eventId] = now
        while (seen.size > maxSeen) seen.remove(seen.keys.first())
        return Offer(accepted = true, evictedLowerPriority = evicted)
    }

    @Synchronized fun next(): Item? {
        val selected = items.maxWithOrNull(compareBy<Item> { it.priority }.thenBy { -it.sequence }) ?: return null
        items.remove(selected)
        return selected
    }

    @Synchronized fun removeManual(): List<Item> {
        val removed = items.filter { it.manual }
        items.removeAll { it.manual }
        return removed
    }

    @Synchronized fun removeAutomatic(): List<Item> {
        val removed = items.filterNot { it.manual }
        items.removeAll { !it.manual }
        return removed
    }

    @Synchronized fun clear(): List<Item> {
        val removed = items.toList()
        items.clear()
        return removed
    }

    @Synchronized fun size(): Int = items.size

    companion object {
        const val PRIORITY_ORDINARY = 10
        const val PRIORITY_MANUAL = 20
        const val PRIORITY_WARNING = 80
        const val PRIORITY_URGENT_NOTIFICATION = 90
        const val PRIORITY_URGENT_SMS = 95
        const val PRIORITY_CALL = 100
    }
}
