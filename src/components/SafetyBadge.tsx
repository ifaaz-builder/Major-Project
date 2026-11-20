import { useEffect, useState } from "react";
import { AlertTriangle, CheckCircle2, Volume2, Square, Loader2 } from "lucide-react";
import type { DetectionResult } from "@/lib/scam-detector";
import { t } from "@/lib/i18n";
import { speakSafetyPhrase, stopSpeaking } from "@/lib/speech";
import { useSpokenLanguage } from "@/lib/spoken-language";

interface Props {
  result: DetectionResult;
  sourceLabel?: string;
}

export function SafetyBadge({ result, sourceLabel }: Props) {
  const [shakeKey, setShakeKey] = useState(0);
  const [speaking, setSpeaking] = useState(false);
  const [loadingVoice, setLoadingVoice] = useState(false);
  const [voiceError, setVoiceError] = useState<string | null>(null);
  const { language } = useSpokenLanguage();

  const isDanger = result.level === "danger";
  const s = t("en");
  const spoken = t(language);
  const title = isDanger ? s.dangerTitle : s.safeTitle;
  const coaching = isDanger ? s.dangerCoaching : s.safeCoaching;

  useEffect(() => {
    setShakeKey((k) => k + 1);
  }, [result]);

  useEffect(() => {
    return () => stopSpeaking();
  }, []);

  const speak = async () => {
    if (speaking || loadingVoice) {
      stopSpeaking();
      setSpeaking(false);
      setLoadingVoice(false);
      return;
    }
    setVoiceError(null);
    setLoadingVoice(true);
    try {
      const playback = speakSafetyPhrase(
        isDanger ? "manual_danger" : "manual_safe",
        language,
        `${isDanger ? spoken.dangerTitle : spoken.safeTitle}. ${isDanger ? spoken.dangerCoaching : spoken.safeCoaching}`,
        () => setSpeaking(false),
      );
      setSpeaking(true);
      setLoadingVoice(false);
      await playback;
    } catch {
      setVoiceError("Voice is unavailable right now. Please try again.");
    } finally {
      setLoadingVoice(false);
      setSpeaking(false);
    }
  };


  return (
    <div className="flex flex-col gap-6">
      {sourceLabel && (
        <p className="text-base font-medium uppercase tracking-[0.2em] text-muted-foreground">
          {sourceLabel}
        </p>
      )}

      <div
        key={shakeKey}
        className={`relative overflow-hidden rounded-3xl p-8 text-center ${
          isDanger ? "animate-badge-shake" : ""
        }`}
        style={{
          background: isDanger ? "var(--gradient-danger)" : "var(--gradient-safe)",
          boxShadow: isDanger ? "var(--shadow-glow-danger)" : "var(--shadow-glow-safe)",
          color: isDanger ? "var(--danger-foreground)" : "var(--safe-foreground)",
        }}
      >
        <div className="flex flex-col items-center gap-4">
          {isDanger ? (
            <AlertTriangle className="h-20 w-20" strokeWidth={2.5} />
          ) : (
            <CheckCircle2 className="h-20 w-20" strokeWidth={2.5} />
          )}
          <h2 className="font-display text-5xl font-black leading-none md:text-6xl">
            {isDanger ? s.dangerHeading : s.safeHeading}
          </h2>
          <p className="text-2xl font-semibold md:text-3xl">{title}</p>
        </div>
      </div>

      <div className="rounded-2xl border-2 border-border bg-card p-6">
        <p className="mb-3 text-base font-bold uppercase tracking-wider text-muted-foreground">
          {s.whatThisMeans}
        </p>
        <p className="text-2xl font-semibold leading-relaxed text-foreground md:text-[26px]">
          {coaching}
        </p>

        <button
          onClick={speak}
          className="mt-4 inline-flex w-full items-center justify-center gap-3 rounded-2xl bg-foreground px-6 py-5 text-2xl font-bold text-background transition-transform hover:scale-[1.02] active:scale-[0.98]"
        >
          {loadingVoice ? (
            <>
              <Loader2 className="h-7 w-7 animate-spin" /> …
            </>
          ) : speaking ? (
            <>
              <Square className="h-7 w-7 fill-current" /> {s.stopReading}
            </>
          ) : (
            <>
              <Volume2 className="h-7 w-7" /> {s.readAloud}
            </>
          )}
        </button>
        {voiceError && (
          <p className="mt-2 text-base font-medium text-danger">{voiceError}</p>
        )}

      </div>

      {result.signals.length > 0 && (
        <div className="rounded-2xl border border-border bg-card/50 p-5">
          <p className="mb-3 text-sm font-bold uppercase tracking-wider text-muted-foreground">
            {s.whyFlagged}
          </p>
          <ul className="space-y-2">
            {result.signals.map((sig, i) => (
              <li key={i} className="flex items-start gap-2 text-lg text-foreground">
                <span
                  className="mt-2 inline-block h-2 w-2 flex-shrink-0 rounded-full"
                  style={{
                    background: isDanger ? "var(--danger)" : "var(--safe)",
                  }}
                />
                {sig}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
