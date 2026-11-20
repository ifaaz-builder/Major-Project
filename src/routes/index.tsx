import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { Send, Shield } from "lucide-react";
import { SafetyBadge } from "@/components/SafetyBadge";
import { LinkFileScanner } from "@/components/LinkFileScanner";
import { CallProtectionPanel } from "@/components/CallProtectionPanel";
import { NotificationScanningPanel } from "@/components/NotificationScanningPanel";
import { detectScam, type DetectionResult } from "@/lib/scam-detector";
import { SpokenLanguageSettings } from "@/lib/spoken-language";

export const Route = createFileRoute("/")({ component: Index });

function Index() {
  const [manualText, setManualText] = useState("");
  const [manualResult, setManualResult] = useState<DetectionResult | null>(null);

  const handleManualScan = () => {
    if (manualText.trim()) setManualResult(detectScam(manualText));
  };

  return (
    <main className="min-h-screen px-4 py-8 md:px-8 md:py-12">
      <header className="mx-auto mb-8 flex max-w-7xl items-center gap-3">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-primary text-primary-foreground">
          <Shield className="h-6 w-6" strokeWidth={2.5} />
        </div>
        <div>
          <h1 className="font-display text-2xl font-bold leading-tight md:text-3xl">ScamShield AI</h1>
          <p className="text-sm text-muted-foreground">Call protection and scam checks</p>
        </div>
      </header>

      <CallProtectionPanel />
      <NotificationScanningPanel />
      <SpokenLanguageSettings />

      <section className="mx-auto mt-8 max-w-7xl rounded-3xl border border-border bg-card/50 p-6 md:p-8">
        <div className="mb-5">
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-muted-foreground">Manual check</p>
          <h2 className="mt-1 font-display text-2xl font-bold">Check a message or link</h2>
          <p className="mt-1 text-sm text-muted-foreground">Paste text for a local scam-pattern check. Optional notification scanning only analyses text exposed by app notifications; it does not read message inboxes.</p>
        </div>
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div className="flex flex-col gap-3">
            <textarea value={manualText} onChange={e => setManualText(e.target.value)}
              placeholder="Paste suspicious text or a link here…"
              className="min-h-[180px] w-full rounded-2xl border-2 border-border bg-input p-4 text-lg text-foreground placeholder:text-muted-foreground focus:border-primary focus:outline-none" />
            <div className="flex gap-3">
              <button onClick={handleManualScan} disabled={!manualText.trim()}
                className="inline-flex flex-1 items-center justify-center gap-2 rounded-2xl bg-primary px-6 py-4 text-lg font-bold text-primary-foreground disabled:opacity-50">
                <Send className="h-5 w-5" /> Check text
              </button>
              <button onClick={() => { setManualText(""); setManualResult(null); }}
                className="rounded-2xl border-2 border-border bg-secondary px-5 py-4 font-semibold text-secondary-foreground">Clear</button>
            </div>
          </div>
          <div>{manualResult ? <SafetyBadge result={manualResult} sourceLabel="Manual check" /> :
            <div className="flex h-full min-h-[180px] flex-col items-center justify-center rounded-2xl border-2 border-dashed border-border p-8 text-center text-muted-foreground">
              <Shield className="mb-3 h-10 w-10 opacity-50" /><p className="text-base">Your result will appear here</p>
            </div>}</div>
        </div>
      </section>

      <div className="mx-auto mt-8 max-w-7xl"><LinkFileScanner /></div>

      <footer className="mx-auto mt-12 max-w-7xl text-center text-xs text-muted-foreground">
        Call screening classifies only calls Android provides to the service. Unlisted callers remain unknown, and calls are allowed.
      </footer>
    </main>
  );
}
