import { useCallback, useEffect, useRef, useState } from "react";
import { Link2, FileWarning, ShieldCheck, AlertTriangle, ScanLine, Volume2, Square } from "lucide-react";
import {
  analyzeArtifact,
  extractArtifacts,
  MESSAGE_SOURCES,
  type ArtifactResult,
  type MessageSource,
} from "@/lib/scam-detector";
import { speakSafetyPhrase, stopSpeaking } from "@/lib/speech";
import { useSpokenLanguage } from "@/lib/spoken-language";
import { t } from "@/lib/i18n";

const EXAMPLES = [
  "http://secure-sbi-kyc-update.xyz/login",
  "Bank_Reward_KYC_update.apk",
  "https://www.amazon.in/orders",
];

export function LinkFileScanner() {
  const [input, setInput] = useState("");
  const [source, setSource] = useState<MessageSource>("WhatsApp");
  const [results, setResults] = useState<ArtifactResult[] | null>(null);
  const { language } = useSpokenLanguage();
  const [autoRead, setAutoRead] = useState(true);
  const [speaking, setSpeaking] = useState(false);
  const lastSpoken = useRef<string>("");

  const scan = useCallback(
    (raw?: string) => {
      const text = (raw ?? input).trim();
      if (!text) {
        setResults(null);
        return;
      }
      const found = extractArtifacts(text);
      const list =
        found.length > 0
          ? found.map((f) => analyzeArtifact(f.value, source))
          : [analyzeArtifact(text, source)];
      setResults(list);
    },
    [input, source],
  );

  // Automatic detection: scan links and app files as they are pasted or typed.
  useEffect(() => {
    const id = setTimeout(() => scan(), 600);
    return () => clearTimeout(id);
  }, [input, source, scan]);

  const readResults = useCallback(
    async (list: ArtifactResult[]) => {
      const worst =
        list.find((r) => r.verdict === "danger") ??
        list.find((r) => r.verdict === "caution") ??
        list[0];
      if (!worst) return;
      const spoken = t(language);
      const phraseId = worst.verdict === "danger" ? "link_danger" : worst.verdict === "caution" ? "link_caution" : "link_safe";
      const text = worst.verdict === "danger"
        ? `${spoken.dangerTitle}. ${spoken.dangerCoaching}`
        : worst.verdict === "caution"
          ? `${spoken.cautionTitle}. ${spoken.cautionCoaching}`
          : `${spoken.safeTitle}. ${spoken.safeCoaching}`;
      try {
        setSpeaking(true);
        await speakSafetyPhrase(phraseId, language, text, () => setSpeaking(false));
      } catch {
        setSpeaking(false);
      } finally {
        setSpeaking(false);
      }
    },
    [language],
  );

  // Automatically read the verdict aloud when a new result appears.
  useEffect(() => {
    if (!autoRead || !results?.length) return;
    const key = `${language}|${results.map((r) => r.value + r.verdict).join(",")}`;
    if (key === lastSpoken.current) return;
    lastSpoken.current = key;
    void readResults(results);
  }, [results, autoRead, language, readResults]);

  useEffect(() => () => stopSpeaking(), []);

  const toggleSpeak = () => {
    if (speaking) {
      stopSpeaking();
      setSpeaking(false);
      return;
    }
    if (results?.length) void readResults(results);
  };


  return (
    <section className="mx-auto mt-10 max-w-7xl rounded-3xl border border-border bg-card/50 p-6 md:p-8">
      <p className="text-xs font-bold uppercase tracking-[0.2em] text-muted-foreground">
        Link &amp; app file checker
      </p>
      <h2 className="mt-1 font-display text-2xl font-bold">
        Is this link or APK safe to open?
      </h2>
      <p className="mt-1 text-sm text-muted-foreground">
        Paste any link, or the name of a file (like <code>update.apk</code>) someone sent you.
      </p>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="flex flex-col gap-3">
          <label className="text-sm font-bold uppercase tracking-wider text-muted-foreground">
            Where did it come from?
          </label>
          <select
            value={source}
            onChange={(e) => setSource(e.target.value as MessageSource)}
            className="w-full rounded-2xl border-2 border-border bg-input px-4 py-4 text-lg font-semibold text-foreground focus:border-primary focus:outline-none"
          >
            {MESSAGE_SOURCES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>

          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="https://…  or  Bank_Update.apk"
            className="min-h-[120px] w-full rounded-2xl border-2 border-border bg-input p-4 text-lg text-foreground placeholder:text-muted-foreground focus:border-primary focus:outline-none"
          />

          <div className="flex flex-wrap gap-2">
            {EXAMPLES.map((e) => (
              <button
                key={e}
                onClick={() => {
                  setInput(e);
                  scan(e);
                }}
                className="rounded-full border border-border bg-secondary px-3 py-1.5 text-xs font-medium text-secondary-foreground"
              >
                {e}
              </button>
            ))}
          </div>

          <label className="flex items-center gap-3 text-base font-medium text-foreground">
            <input
              type="checkbox"
              checked={autoRead}
              onChange={(e) => {
                setAutoRead(e.target.checked);
                if (!e.target.checked) {
                  stopSpeaking();
                  setSpeaking(false);
                }
              }}
              className="h-5 w-5 accent-[var(--primary)]"
            />
            Detect and read the result aloud automatically
          </label>

          <div className="flex gap-3">
            <button
              onClick={() => scan()}
              disabled={!input.trim()}
              className="inline-flex flex-1 items-center justify-center gap-2 rounded-2xl bg-primary px-6 py-4 text-lg font-bold text-primary-foreground transition-transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50"
            >
              <ScanLine className="h-5 w-5" /> Scan link or file
            </button>
            <button
              onClick={toggleSpeak}
              disabled={!results?.length}
              className="inline-flex items-center justify-center gap-2 rounded-2xl bg-foreground px-5 py-4 text-lg font-bold text-background disabled:opacity-50"
            >
              {speaking ? <Square className="h-5 w-5 fill-current" /> : <Volume2 className="h-5 w-5" />}
            </button>
          </div>

        </div>

        <div className="flex flex-col gap-4">
          {!results && (
            <div className="flex h-full min-h-[180px] flex-col items-center justify-center rounded-2xl border-2 border-dashed border-border p-8 text-center text-muted-foreground">
              <ShieldCheck className="mb-3 h-10 w-10 opacity-50" />
              <p className="text-base">Scan result will appear here</p>
            </div>
          )}
          {results?.map((r, i) => {
            const danger = r.verdict === "danger";
            const caution = r.verdict === "caution";
            return (
              <div
                key={i}
                className="overflow-hidden rounded-2xl border-2"
                style={{
                  borderColor: danger
                    ? "var(--danger)"
                    : caution
                      ? "var(--warning, var(--danger))"
                      : "var(--safe)",
                }}
              >
                <div
                  className="flex items-center gap-3 p-5"
                  style={{
                    background: danger
                      ? "var(--gradient-danger)"
                      : caution
                        ? "var(--gradient-danger)"
                        : "var(--gradient-safe)",
                    color: danger || caution
                      ? "var(--danger-foreground)"
                      : "var(--safe-foreground)",
                    opacity: caution ? 0.9 : 1,
                  }}
                >
                  {danger || caution ? (
                    <AlertTriangle className="h-10 w-10 flex-shrink-0" strokeWidth={2.5} />
                  ) : (
                    <ShieldCheck className="h-10 w-10 flex-shrink-0" strokeWidth={2.5} />
                  )}
                  <div>
                    <p className="text-xl font-black leading-tight">{r.headline}</p>
                    <p className="mt-1 flex items-center gap-1.5 break-all text-xs opacity-90">
                      {r.kind === "file" ? (
                        <FileWarning className="h-3.5 w-3.5" />
                      ) : (
                        <Link2 className="h-3.5 w-3.5" />
                      )}
                      {r.value}
                    </p>
                  </div>
                </div>
                <div className="bg-card p-5">
                  <ul className="space-y-2">
                    {r.reasons.map((reason, j) => (
                      <li key={j} className="flex items-start gap-2 text-base text-foreground">
                        <span
                          className="mt-2 inline-block h-2 w-2 flex-shrink-0 rounded-full"
                          style={{
                            background: r.verdict === "safe" ? "var(--safe)" : "var(--danger)",
                          }}
                        />
                        {reason}
                      </li>
                    ))}
                  </ul>
                  <p className="mt-4 text-lg font-semibold text-foreground">{r.advice}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
