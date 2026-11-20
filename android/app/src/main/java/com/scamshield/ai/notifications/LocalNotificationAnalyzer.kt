package com.scamshield.ai.notifications

import android.net.Uri
import java.net.IDN
import java.util.Locale

/** Conservative local-only rules. This intentionally has no network or persistence access. */
object LocalNotificationAnalyzer {
    data class Result(val suspicious: Boolean, val reasons: List<String>, val matchedRules: List<String>)

    private val urlRegex = Regex("(?i)(?:https?://|www\\.)[^\\s<>\\\"']+")
    private val shorteners = setOf("bit.ly", "tinyurl.com", "t.co", "cutt.ly", "rb.gy", "is.gd", "shorturl.at")
    private val brandDomains = mapOf(
        "sbi" to listOf("sbi.co.in", "onlinesbi.sbi"),
        "hdfc" to listOf("hdfcbank.com"),
        "icici" to listOf("icicibank.com"),
        "axis" to listOf("axisbank.com"),
        "paytm" to listOf("paytm.com"),
        "phonepe" to listOf("phonepe.com"),
        "google pay" to listOf("pay.google.com"),
        "gpay" to listOf("pay.google.com")
    )

    fun analyze(text: String): Result {
        // Normalize whitespace only. Keep the original punctuation for URL parsing while
        // using word-boundary regexes below so punctuation/case variations remain supported.
        val normalized = text.replace(Regex("\\s+"), " ").trim()
        val lower = normalized.lowercase(Locale.ROOT)
        // Treat common separators (slashes, commas, dashes, repeated punctuation)
        // as word boundaries for rule matching, while retaining normalized text for URLs.
        val ruleText = normalized.replace(Regex("[^\\p{L}\\p{N}'’]+"), " ").replace(Regex("\\s+"), " ").trim()
        val reasons = linkedSetOf<String>()
        val matchedRules = linkedSetOf<String>()
        val asksSecret = Regex("\\b(otp|one\\s*time\\s+password|pin|password|passcode|cvv|mpin|upi\\s+pin)\\b", RegexOption.IGNORE_CASE).containsMatchIn(ruleText)
        val requestsSecret = Regex("\\b(send|share|tell|reply|enter|provide|verify|confirm|submit|click|give|request|requests|requested|requesting|ask|asks|asked|asking)\\b", RegexOption.IGNORE_CASE).containsMatchIn(ruleText)
        val warnsAgainstSharing = Regex("\\b(never|do\\s+not|don['’]t|dont|not)\\s+(share|send|reveal|give|tell|enter|request)\\b", RegexOption.IGNORE_CASE).containsMatchIn(ruleText)
        if (asksSecret && requestsSecret && !warnsAgainstSharing) {
            reasons += "Requests an OTP, PIN, password, CVV or other sign-in secret"
            matchedRules += "secret_request"
        }

        val payment = Regex("\\b(pay|payment|upi|collect\\s+request|transfer|refund|fee|fine|cashback|prize|wallet|bank\\s+account)\\b", RegexOption.IGNORE_CASE).containsMatchIn(ruleText)
        val urgency = Regex("\\b(immediately|urgent|within\\s+\\d+\\s+minutes?|today|expire[sd]?|suspend(?:ed|ing)?|block(?:ed|ing)?|last\\s+warning|act\\s+now|final\\s+notice)\\b", RegexOption.IGNORE_CASE).containsMatchIn(ruleText)
        if (payment && urgency) {
            reasons += "Urgent payment or account-action demand"
            matchedRules += "urgent_payment_or_account_action"
        }

        val credentialThreat = Regex("\\b(account|bank|card|kyc|sim|wallet)\\b", RegexOption.IGNORE_CASE).containsMatchIn(ruleText) &&
            Regex("\\b(block|blocked|blocking|suspend(?:ed|ing)?|expire[sd]?|close[sd]?|deactivat(?:ed|ion))\\b", RegexOption.IGNORE_CASE).containsMatchIn(ruleText)
        if (credentialThreat && (asksSecret || urlRegex.containsMatchIn(normalized))) {
            reasons += "Possible bank, KYC or service impersonation using an account threat"
            matchedRules += "account_threat_with_secret_or_link"
        }

        val hosts = urlRegex.findAll(normalized).mapNotNull { match ->
            val cleaned = match.value.trimEnd('.', ',', ';', ')', ']', '!', '?')
            runCatching {
                val uri = Uri.parse(if (cleaned.startsWith("www.", true)) "https://$cleaned" else cleaned)
                uri.host?.trimEnd('.')?.lowercase(Locale.ROOT)
            }.getOrNull()
        }.toList()
        for (host in hosts) {
            val asciiHost = runCatching { IDN.toASCII(host) }.getOrDefault(host)
            if (asciiHost.startsWith("xn--") || ".xn--" in asciiHost) reasons += "Link uses an internationalized domain that can disguise its name"
            if (host in shorteners) reasons += "Link uses a URL-shortening service that hides its destination"
            if (Regex("^\\d{1,3}(\\.\\d{1,3}){3}$").matches(host)) reasons += "Link points directly to a numeric IP address"
            for ((brand, domains) in brandDomains) {
                if (lower.contains(brand) && domains.none { host == it || host.endsWith(".$it") }) {
                    reasons += "Link domain does not match the $brand domain mentioned in the notification"
                    break
                }
            }
        }
        if (hosts.isNotEmpty() && Regex("\\b(login|log\\s+in|verify|verification|sign\\s+in|update|kyc|account|bank)\\b", RegexOption.IGNORE_CASE).containsMatchIn(ruleText)) {
            reasons += "Link is paired with a login, identity-verification or account-update request"
            matchedRules += "link_with_account_or_verification_language"
        }
        if (hosts.any { it in shorteners }) matchedRules += "shortened_link"
        if (hosts.any { Regex("^\\d{1,3}(\\.\\d{1,3}){3}$").matches(it) }) matchedRules += "numeric_ip_link"
        if (hosts.any { runCatching { IDN.toASCII(it).let { ascii -> ascii.startsWith("xn--") || ".xn--" in ascii } }.getOrDefault(false) }) matchedRules += "internationalized_domain"
        if (reasons.any { it.startsWith("Link domain does not match") }) matchedRules += "brand_domain_mismatch"
        return Result(reasons.isNotEmpty(), reasons.toList(), matchedRules.toList())
    }
}
