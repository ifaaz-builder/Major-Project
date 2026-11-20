# Phase 4 call screening: behavior and limitations

## Current behavior

- Android's system `CallScreeningService` runs for incoming calls while the dashboard is closed, after the user grants ScamShield the Android Call screening role.
- Each received event is written to app-private persistent storage, newest first (up to 200 entries). Entries are local to the device and disappear if app data is cleared or the app is uninstalled.
- A user-entered on-device list can label a number `SCAM` or `TRUSTED`. Existing `VERIFIED` labels are retained and interpreted as the user's explicit local trust mark; they are not provider or business verification.
- Any number absent from that list is `UNKNOWN`. All calls are allowed; the app does not reject, silence, or block any call.
- For each eligible real incoming call, the native service posts a high-importance `CATEGORY_CALL` notification after responding to Android. It contains the number and local classification; tapping opens the native caller-identification screen with those details. The service no longer attempts a background activity launch.
- Android 13+ notification permission is requested from the dashboard. Android and HyperOS can suppress heads-up presentation or sound based on the app/channel settings, lock-screen policy, DND, battery management, and user choices. Full-screen intent is intentionally not used because Android 14 restricts it to qualifying calling/alarm apps.
- Optional text-to-speech is off by default and can be enabled from the dashboard. It uses Android notification-event audio attributes, checks silent/vibrate mode and notification-stream volume, and never changes volume or requests DND access. Android's audio policy remains authoritative; device-level behavior needs physical verification.
- The dashboard has an explicit **Test speech** action that says “ScamShield speech test. This is only a test. No call was screened.” It does not write call history and can be used while call speech is disabled. The dashboard reports TTS initialization/playback state, notification volume, ringer mode, and DND suppression status.
- The service uses no network request, so network failure cannot delay incoming ringing. No provider status is represented as a positive result.

## Android limits

- Android requires the selected call screening app to answer within five seconds. This implementation uses local storage and an in-memory lookup only.
- Android 10+ is required for the role request. The user must choose ScamShield in Android's system role prompt. Android/OEM call handling can vary.
- Android normally passes only numbers that are not in Contacts unless the app also receives Contacts access. This app does not request Contacts access.
- Restricted, unknown-presentation, unavailable, payphone, and some non-TEL call handles are not delivered to the service by Android. Calls routed by carrier/OEM paths may also differ.
- The history records what Android delivered to the service, not a complete carrier call log. It has no SMS or simulated-call data.
- The caller-identification activity is informational and has a dismiss button; the phone's system dialer still controls answering/rejecting. If notifications are disabled or suppressed, check the real history in ScamShield instead.

## Reputation provider investigation

No live reputation API is configured. Candidate providers were reviewed; none is approved or configured for this build:

- Hiya Protect publishes spam/fraud reputation endpoints and a cache, uses global call data, and requires partner access/KYC and confidential API credentials. Public documents do not establish +91 inbound mobile coverage, public pricing, or a latency/SLA commitment. Hiya requires its data-use policy to be made available to app users.
- tellows India offers Live and Scorelist APIs for caller protection using reports, scores, comments, caller types, and user-assigned names. A tellows.in presence and broad country availability do not prove complete +91 mobile coverage. Access requires a purchased API key/list or a partner agreement; business/API prices are not public in the reviewed materials. Live API implies online lookups but no latency SLA is published. Its data is community reported and not independently verified.
- YouMail Phone Lookup offers spam/fraud/unlawful risk with account credits (public docs list a 200-credit charge for risk datasets, while rates/allowances vary by account). Its embedded spammer lists explicitly focus on the US/Canada; public material does not confirm +91 mobile lookup coverage, nor response-time SLA.
- Kaspersky Who Calls SDK was also considered; public country-presence information did not confirm India availability.

No API endpoint, key, coverage claim, or result has been fabricated. Current local SCAM/TRUSTED marks remain user-provided evidence only. An absent provider match is never treated as trusted. A future integration should first obtain written India coverage, data-use/commercial terms, price and latency guarantees, then use a protected server-side credential and a bounded local cache; timeout/offline remains UNKNOWN and the call is allowed. Never ship provider secrets in the APK.

## Physical test

Install `android/app/build/outputs/apk/debug/app-debug.apk` on an Android 10+ phone, open ScamShield, grant the Call Screening role if needed, and grant notification permission. In system settings, allow ScamShield notifications and set **Incoming caller alerts** to High/Pop on screen; on HyperOS, also allow floating notifications and lock-screen notifications. Optionally enable spoken warnings in ScamShield. Close the dashboard and place a real test call from another phone. Confirm the call still rings, notification shows the right number/classification, tapping opens the caller detail with the same data, and speech follows the enabled setting. Test an unlabelled caller (`UNKNOWN`) and, if desired, a manually marked caller. The call must still ring. The service writes a non-PII `ScamShieldScreening` Logcat entry when Android invokes it.

Validation on POCO X5 Pro 5G (Android 14 / HyperOS 2) before this notification update: the user confirmed a real incoming call rang normally and was written to real screening history, establishing service invocation. The earlier background caller activity did not appear. This updated notification and speech implementation still requires a physical incoming-call test; code/build validation alone does not prove heads-up, tap-through, or speech behavior on the handset.

## Spoken-alert investigation (POCO X5 Pro 5G, Android 14 / HyperOS 2)

Retained Logcat from the user's latest speech test showed two service invocations. On one call the previous implementation explicitly suppressed speech because either ringer mode was not normal or notification volume was zero; the old log message did not distinguish which condition. On the subsequent call, Google TTS bound and connected successfully, and Android created an `AudioTrack` using `USAGE_NOTIFICATION_EVENT` / `CONTENT_TYPE_SPEECH` about four seconds after screening. The old implementation had no utterance progress listener, so Logcat could not confirm that playback completed. These records support a ringer/volume suppression and cold-start delay; they do not prove whether HyperOS rendered the generated audio audibly. There was no evidence that `CallScreeningService` destruction directly stopped an already submitted TTS utterance.

The updated implementation warms TTS when the user enables speech or opens the dashboard, checks the selected English voice, requests transient `MAY_DUCK` audio focus and proceeds only if granted, and logs accepted/started/completed/error/stopped outcomes. It uses notification audio attributes, respects ringer mode, notification volume and DND, and never changes volume or obtains DND bypass access. A fresh physical call test is still required to confirm audible output while the phone is ringing.
