package com.scamshield.ai

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Minimal native caller ID surface launched only from a real screening callback. */
class CallerIdentificationActivity : Activity() {
    companion object {
        const val EXTRA_NUMBER = "caller_number"
        const val EXTRA_CLASSIFICATION = "caller_classification"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        render(intent.getStringExtra(EXTRA_NUMBER), intent.getStringExtra(EXTRA_CLASSIFICATION))
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        render(intent.getStringExtra(EXTRA_NUMBER), intent.getStringExtra(EXTRA_CLASSIFICATION))
    }

    private fun render(rawNumber: String?, rawClassification: String?) {
        val classification = rawClassification?.let { if (it == "VERIFIED") "TRUSTED" else it }
            ?.takeIf { it in setOf("SCAM", "UNKNOWN", "TRUSTED") } ?: "UNKNOWN"
        val heading = when (classification) {
            "SCAM" -> "Marked SCAM"
            "TRUSTED" -> "Marked trusted"
            else -> "Unknown caller"
        }
        val detail = when (classification) {
            "SCAM" -> "This number is on your device's ScamShield SCAM list."
            "TRUSTED" -> "You marked this number trusted on this device. This is not independent business verification."
            else -> "This number is not on your local list. Its identity is not verified."
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 36)
            setBackgroundColor(Color.rgb(12, 18, 32))
        }
        val badgeColor = when (classification) {
            "SCAM" -> Color.rgb(220, 68, 78)
            "TRUSTED" -> Color.rgb(42, 150, 117)
            else -> Color.rgb(142, 112, 52)
        }
        val badge = TextView(this).apply {
            text = heading
            textSize = 18f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(22, 14, 22, 14)
            background = GradientDrawable().apply { setColor(badgeColor); cornerRadius = 48f }
        }
        val number = TextView(this).apply {
            text = rawNumber?.takeIf { it.isNotBlank() } ?: "Unknown number"
            textSize = 30f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 14)
        }
        val note = TextView(this).apply {
            text = "$detail\n\nScamShield is allowing this call. It will not block calls automatically."
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(4, 4, 4, 28)
        }
        val dismiss = Button(this).apply {
            text = "Dismiss"
            setOnClickListener { finish() }
        }
        root.addView(badge)
        root.addView(number, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(note, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(dismiss)
        setContentView(root)
    }
}
