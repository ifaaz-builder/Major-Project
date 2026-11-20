import { useCallback, useEffect, useState } from "react";
import { Capacitor, registerPlugin } from "@capacitor/core";

type CallRecord = { id: number; number: string; classification: "SCAM" | "UNKNOWN" | "TRUSTED" | "VERIFIED"; source: string; time: number };
type Status = { supported: boolean; roleHeld: boolean; history: CallRecord[]; provider: string; lastScreeningAt: number; notificationsGranted: boolean; notificationsEnabled: boolean; callerAlertChannelImportance: number; spokenAlertsEnabled: boolean; speechEngineStatus: string; speechLastOutcome: string; speechSuppressionReason: string; notificationVolume: number; notificationVolumeMax: number; speechRingerMode: string; speechDndMode: number };
type CallScreeningApi = {
  getStatus(): Promise<Status>;
  requestRole(): Promise<{ roleHeld: boolean }>;
  getHistory(): Promise<{ history: CallRecord[] }>;
  setNumberLabel(options: { number: string; classification: "SCAM" | "TRUSTED" }): Promise<{ saved: boolean }>;
  requestNotificationPermission(): Promise<{ granted: boolean }>;
  openCallerAlertSettings(): Promise<void>;
  setSpokenAlertsEnabled(options: { enabled: boolean }): Promise<{ enabled: boolean }>;
  testSpokenAlert(): Promise<{ queued: boolean; status: string }>;
};
const CallScreening = registerPlugin<CallScreeningApi>("CallScreening");

export function CallProtectionPanel() {
  const [status, setStatus] = useState<Status | null>(null);
  const [number, setNumber] = useState("");
  const [label, setLabel] = useState<"SCAM" | "TRUSTED">("SCAM");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const channelImportance = status?.callerAlertChannelImportance ?? -1;

  const refresh = useCallback(async () => {
    if (!Capacitor.isNativePlatform()) return;
    try { setStatus(await CallScreening.getStatus()); setError(""); }
    catch { setError("Native call screening is unavailable in this build."); }
  }, []);
  useEffect(() => {
    void refresh();
    const onResume = () => { if (document.visibilityState === "visible") void refresh(); };
    window.addEventListener("focus", onResume);
    document.addEventListener("visibilitychange", onResume);
    return () => {
      window.removeEventListener("focus", onResume);
      document.removeEventListener("visibilitychange", onResume);
    };
  }, [refresh]);

  const enable = async () => {
    setBusy(true); setError("");
    try { await CallScreening.requestRole(); await refresh(); }
    catch (e) { setError(e instanceof Error ? e.message : "Android did not grant the call screening role."); }
    finally { setBusy(false); }
  };
  const saveLabel = async () => {
    setBusy(true); setError("");
    try { await CallScreening.setNumberLabel({ number, classification: label }); setNumber(""); await refresh(); }
    catch (e) { setError(e instanceof Error ? e.message : "Could not save number."); }
    finally { setBusy(false); }
  };
  const enableNotifications = async () => {
    setBusy(true); setError("");
    try {
      if (!status?.notificationsGranted) await CallScreening.requestNotificationPermission();
      else await CallScreening.openCallerAlertSettings();
      await refresh();
    }
    catch (e) { setError(e instanceof Error ? e.message : "Android did not grant notification permission."); }
    finally { setBusy(false); }
  };
  const toggleSpeech = async () => {
    if (!status) return;
    setBusy(true); setError("");
    try { await CallScreening.setSpokenAlertsEnabled({ enabled: !status.spokenAlertsEnabled }); await refresh(); }
    catch (e) { setError(e instanceof Error ? e.message : "Could not update spoken alerts."); }
    finally { setBusy(false); }
  };
  return <section className="mx-auto mt-10 max-w-7xl rounded-3xl border-2 border-primary/30 bg-card p-6 md:p-8">
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p className="text-xs font-bold uppercase tracking-[0.2em] text-muted-foreground">Native Android protection</p>
        <h2 className="mt-1 font-display text-2xl font-bold">Incoming call screening</h2>
        <p className="mt-1 text-sm text-muted-foreground">Screens eligible calls when the dashboard is closed. All calls are allowed.</p>
      </div>
      {Capacitor.isNativePlatform() && <button onClick={enable} disabled={busy || !!status?.roleHeld || status?.supported === false}
        className="rounded-xl bg-primary px-5 py-3 font-semibold text-primary-foreground disabled:opacity-60">
        {busy ? "Opening Android settings…" : status?.roleHeld ? "Call screening enabled" : status?.supported === false ? "Android 10+ required" : "Request call-screening role"}
      </button>}
    </div>

    {!Capacitor.isNativePlatform() && <p className="mt-4 rounded-xl bg-secondary p-4 text-sm">Install the Android app to enable system call screening. The web preview does not create call events.</p>}
    {Capacitor.isNativePlatform() && <>
      <div className="mt-4 rounded-xl bg-secondary p-4 text-sm">
        <p className="font-semibold">Android Call Screening role: {status?.roleHeld ? "Granted" : status?.supported === false ? "Unavailable (Android 10+ required)" : status ? "Not granted" : "Checking…"}</p>
        <p className="mt-1">{status?.roleHeld ? "Android can bind the native service for eligible incoming calls, including while this dashboard is closed." : "Grant the role using the button above. If you decline it, return here to retry."} Unknown numbers are never treated as safe or blocked automatically.</p>
      </div>
      <div className="mt-4 grid gap-3 rounded-xl border border-border p-4 sm:grid-cols-2">
        <div>
          <p className="font-semibold">Caller notifications: {status?.notificationsGranted && status.notificationsEnabled ? "Permission granted" : status ? "Permission needed" : "Checking…"}</p>
          <p className="mt-1 text-xs">Caller alert channel: {channelImportance === 4 ? "High importance" : channelImportance === 0 ? "Blocked" : channelImportance > 0 ? "Enabled, but below high importance" : "Not created yet"}</p>
          <p className="mt-1 text-xs text-muted-foreground">High importance caller alerts include the number and classification. Heads-up display depends on Android and HyperOS notification settings.</p>
          <button onClick={enableNotifications} disabled={busy} className="mt-3 rounded-lg border border-border bg-background px-4 py-2 font-semibold disabled:opacity-50">{status?.notificationsGranted ? "Manage caller alert settings" : "Allow caller notifications"}</button>
        </div>
        <div>
          <p className="font-semibold">Spoken safety warnings</p>
          <p className="mt-1 text-xs text-muted-foreground">This setting controls automatic warnings for SCAM or SPAM calls and suspicious SMS or app notifications. Speech is optional and follows Android audio, silent-mode and Do Not Disturb settings.</p>
          <button role="switch" aria-checked={!!status?.spokenAlertsEnabled} onClick={toggleSpeech} disabled={busy || !status} className="mt-3 rounded-lg border border-border bg-background px-4 py-2 font-semibold disabled:opacity-50">{status?.spokenAlertsEnabled ? "Turn spoken alerts off" : "Turn spoken alerts on"}</button>
          <p className="mt-2 text-xs">Engine: {status?.speechEngineStatus ?? "Checking…"}; notification volume: {status ? `${status.notificationVolume}/${status.notificationVolumeMax}` : "checking"}; ringer: {status?.speechRingerMode ?? "checking"}; DND: {status ? status.speechDndMode === 0 ? "off" : "active" : "checking"}</p>
          {status?.speechSuppressionReason && <p className="mt-1 text-xs">Speech may be paused: {status.speechSuppressionReason}</p>}
        </div>
      </div>
      <p className="mt-3 text-sm text-muted-foreground">Last real screening event: {status?.lastScreeningAt ? new Date(status.lastScreeningAt).toLocaleString() : "None recorded on this device yet"}</p>
      <div className="mt-5 flex flex-col gap-3 sm:flex-row">
        <input aria-label="Phone number" value={number} onChange={e => setNumber(e.target.value)} placeholder="Number to mark (8–15 digits)" className="min-w-0 flex-1 rounded-xl border border-border bg-background px-4 py-3" />
        <select aria-label="Classification" value={label} onChange={e => setLabel(e.target.value as "SCAM" | "TRUSTED")} className="rounded-xl border border-border bg-background px-4 py-3">
          <option value="SCAM">Mark likely SCAM</option><option value="TRUSTED">Mark trusted</option>
        </select>
        <button onClick={saveLabel} disabled={busy || !number.trim()} className="rounded-xl border border-border bg-secondary px-5 py-3 font-semibold disabled:opacity-50">Save label</button>
      </div>
      <p className="mt-2 text-xs text-muted-foreground">Labels are stored only on this device. SCAM and TRUSTED are your own marks, not provider-verified reputation. Unlisted callers remain UNKNOWN.</p>
      {error && <p role="alert" className="mt-3 text-sm text-danger">{error}</p>}
      <div className="mt-6">
        <div className="mb-3 flex items-center justify-between"><h3 className="font-semibold">Real incoming-call history</h3><button onClick={() => void refresh()} className="text-sm text-primary">Refresh</button></div>
        {!status?.history?.length ? <p className="rounded-xl border border-dashed border-border p-4 text-sm text-muted-foreground">No real incoming call screening events recorded yet.</p> :
          <ul className="space-y-2">{status.history.map(item => <li key={item.id} className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-border p-3 text-sm">
            <span className="break-all">{item.number}</span><span className="font-bold">{item.classification === "VERIFIED" ? "TRUSTED" : item.classification}</span><time className="text-muted-foreground">{new Date(item.time).toLocaleString()}</time>
          </li>)}</ul>}
      </div>
      <p className="mt-5 text-xs text-muted-foreground">Limitations: Android supplies eligible numbers only (commonly calls not in Contacts); private/withheld numbers and some carrier/device call paths are unavailable. No phone-number reputation provider is connected, so unlabelled calls are UNKNOWN. Role access may vary by Android version and manufacturer.</p>
    </>}
  </section>;
}
