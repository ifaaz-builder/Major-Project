import { useCallback, useEffect, useState } from "react";
import { Capacitor, registerPlugin } from "@capacitor/core";

type AppEntry = { packageName: string; label: string; monitored: boolean };
type ScanRecord = { id: number; packageName: string; appName: string; status: "WARNING" | "NOT_SCANNED"; reasons: string[]; time: number };
type ScanStatus = { accessGranted: boolean; monitoringEnabled: boolean; retentionDays: number; apps: AppEntry[]; history: ScanRecord[]; smsSupported: boolean; smsScanningEnabled: boolean; smsPermissionGranted: boolean };
type ScanningApi = {
  getStatus(): Promise<ScanStatus>;
  openAccessSettings(): Promise<void>;
  setMonitoringEnabled(options: { enabled: boolean }): Promise<{ monitoringEnabled: boolean }>;
  setAppMonitored(options: { packageName: string; monitored: boolean }): Promise<void>;
  setRetentionDays(options: { days: number }): Promise<void>;
  clearHistory(): Promise<void>;
  requestSmsPermission(): Promise<{ granted: boolean }>;
  setSmsScanningEnabled(options: { enabled: boolean }): Promise<{ smsScanningEnabled: boolean }>;
};
type CallApi = { requestNotificationPermission(): Promise<{ granted: boolean }> };
const Scanning = registerPlugin<ScanningApi>("NotificationScanning");
const Calls = registerPlugin<CallApi>("CallScreening");

export function NotificationScanningPanel() {
  const [status, setStatus] = useState<ScanStatus | null>(null);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const refresh = useCallback(async () => {
    if (!Capacitor.isNativePlatform()) return;
    try { setStatus(await Scanning.getStatus()); setError(""); }
    catch { setError("Notification scanning controls are unavailable in this Android build."); }
  }, []);
  useEffect(() => {
    void refresh();
    const onResume = () => { if (document.visibilityState === "visible") void refresh(); };
    window.addEventListener("focus", onResume);
    document.addEventListener("visibilitychange", onResume);
    return () => { window.removeEventListener("focus", onResume); document.removeEventListener("visibilitychange", onResume); };
  }, [refresh]);

  const openSettings = async () => {
    setBusy(true); setError(""); setMessage("Enable ScamShield AI in Android's Notification access screen, then return here.");
    try { await Scanning.openAccessSettings(); }
    catch (e) { setError(e instanceof Error ? e.message : "Could not open Notification access settings."); }
    finally { setBusy(false); }
  };
  const toggle = async () => {
    if (!status) return;
    setBusy(true); setError(""); setMessage("");
    try {
      if (!status.accessGranted) { setError("Grant Notification access in Android settings before enabling monitoring."); return; }
      if (!status.monitoringEnabled) {
        const permission = await Calls.requestNotificationPermission();
        if (!permission.granted) { setError("Allow ScamShield notifications so it can show scan warnings."); return; }
      }
      await Scanning.setMonitoringEnabled({ enabled: !status.monitoringEnabled });
      await refresh();
    } catch (e) { setError(e instanceof Error ? e.message : "Could not update notification monitoring."); }
    finally { setBusy(false); }
  };
  const toggleApp = async (app: AppEntry) => {
    setBusy(true); setError("");
    try { await Scanning.setAppMonitored({ packageName: app.packageName, monitored: !app.monitored }); await refresh(); }
    catch (e) { setError(e instanceof Error ? e.message : "Could not update app monitoring."); }
    finally { setBusy(false); }
  };
  const toggleSms = async () => {
    if (!status) return;
    setBusy(true); setError(""); setMessage("");
    try {
      if (status.smsScanningEnabled) {
        await Scanning.setSmsScanningEnabled({ enabled: false });
      } else {
        const consent = window.confirm("Enable automatic SMS scam detection? ScamShield will inspect incoming SMS text locally to detect scam indicators. Message bodies, sender numbers, and raw SMS data are not saved or uploaded; history stores only detection time, SMS source, matched rules, and result. Android will ask for RECEIVE_SMS permission.");
        if (!consent) return;
        const permission = await Scanning.requestSmsPermission();
        if (!permission.granted) { setError("SMS permission was not granted. SMS scanning remains off; notification scanning is unaffected. On some APK installation routes Android may block this restricted permission."); return; }
        const warnings = await Calls.requestNotificationPermission();
        if (!warnings.granted) { setError("Allow ScamShield notifications to show SMS warnings. SMS scanning remains off."); return; }
        await Scanning.setSmsScanningEnabled({ enabled: true });
      }
      await refresh();
    } catch (e) { setError(e instanceof Error ? e.message : "Could not update SMS scanning."); }
    finally { setBusy(false); }
  };
  const changeRetention = async (days: number) => {
    setBusy(true); setError("");
    try { await Scanning.setRetentionDays({ days }); await refresh(); }
    catch (e) { setError(e instanceof Error ? e.message : "Could not update history retention."); }
    finally { setBusy(false); }
  };
  const clearHistory = async () => {
    setBusy(true); setError("");
    try { await Scanning.clearHistory(); await refresh(); }
    catch (e) { setError(e instanceof Error ? e.message : "Could not clear scan history."); }
    finally { setBusy(false); }
  };

  return <section className="mx-auto mt-8 max-w-7xl rounded-3xl border-2 border-primary/30 bg-card p-6 md:p-8">
    <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p className="text-xs font-bold uppercase tracking-[0.2em] text-muted-foreground">On-device protection</p>
        <h2 className="mt-1 font-display text-2xl font-bold">Notification scanning</h2>
        <p className="mt-2 max-w-3xl text-sm text-muted-foreground">ScamShield reads only text that another app exposes in its notifications, analyses it locally, and stores warning metadata without saving message text.</p>
      </div>
      {Capacitor.isNativePlatform() && <button onClick={openSettings} disabled={busy} className="rounded-xl border border-border bg-secondary px-5 py-3 font-semibold disabled:opacity-50">{status?.accessGranted ? "Manage Notification access" : "Grant Notification access"}</button>}
    </div>
    {!Capacitor.isNativePlatform() && <p className="mt-4 rounded-xl bg-secondary p-4 text-sm">Install the Android app to enable system notification scanning. Web preview does not read device notifications.</p>}
    {Capacitor.isNativePlatform() && <>
      <div className="mt-4 rounded-xl bg-secondary p-4 text-sm">
        <p className="font-semibold">Android Notification access: {status ? status.accessGranted ? "Granted" : "Not granted" : "Checking…"}</p>
        <p className="mt-1">Android requires this sensitive access to be enabled manually in Settings. It can expose notification text from many apps to ScamShield. Processing stays on this device; ScamShield does not upload notification text.</p>
      </div>
      <div className="mt-4 flex flex-col gap-3 rounded-xl border border-border p-4 sm:flex-row sm:items-center sm:justify-between">
        <div><p className="font-semibold">Monitoring: {status?.monitoringEnabled ? "On" : "Off"}</p><p className="mt-1 text-xs text-muted-foreground">Warnings require both Notification access and Android notification permission.</p></div>
        <button role="switch" aria-checked={!!status?.monitoringEnabled} onClick={toggle} disabled={busy || !status?.accessGranted} className="rounded-lg border border-border bg-background px-4 py-2 font-semibold disabled:opacity-50">{status?.monitoringEnabled ? "Turn monitoring off" : "Turn monitoring on"}</button>
      </div>
      <div className="mt-4 flex flex-col gap-3 rounded-xl border border-border p-4 sm:flex-row sm:items-center sm:justify-between">
        <div><p className="font-semibold">Automatic SMS scanning: {status?.smsScanningEnabled ? "On" : "Off"}</p><p className="mt-1 max-w-3xl text-xs text-muted-foreground">{status?.smsSupported === false ? "This device does not report telephony support." : `SMS permission: ${status?.smsPermissionGranted ? "Granted" : "Not granted"}. When enabled, incoming SMS is analysed on this device. ScamShield does not save or upload message bodies, sender numbers, or raw SMS data; history stores only time, source, matched rules, and result. SMS scanning can receive RECEIVE_SMS permission only after you enable it here. Your SMS app's notification scanning remains independent.`}</p></div>
        <button role="switch" aria-checked={!!status?.smsScanningEnabled} onClick={() => void toggleSms()} disabled={busy || status?.smsSupported === false} className="shrink-0 rounded-lg border border-border bg-background px-4 py-2 font-semibold disabled:opacity-50">{status?.smsScanningEnabled ? "Turn SMS scanning off" : "Enable SMS scanning"}</button>
      </div>
      {message && <p role="status" className="mt-3 text-sm text-muted-foreground">{message}</p>}
      {error && <p role="alert" className="mt-3 text-sm text-danger">{error}</p>}

      <div className="mt-6">
        <div className="mb-3"><h3 className="font-semibold">Apps monitored</h3><p className="text-xs text-muted-foreground">New apps are monitored by default. Turn off individual apps here. This list uses launcher-visible apps and apps that have posted notifications.</p></div>
        {!status?.apps?.length ? <p className="rounded-xl border border-dashed border-border p-4 text-sm text-muted-foreground">App list is loading. Open the dashboard again after notification access is granted.</p> :
          <ul className="grid gap-2 sm:grid-cols-2">{status.apps.map(app => <li key={app.packageName} className="flex items-center justify-between gap-3 rounded-xl border border-border p-3">
            <span className="min-w-0"><span className="block truncate text-sm font-semibold">{app.label}</span><span className="block truncate text-xs text-muted-foreground">{app.packageName}</span></span>
            <button role="switch" aria-checked={app.monitored} aria-label={`${app.monitored ? "Stop monitoring" : "Monitor"} ${app.label}`} onClick={() => void toggleApp(app)} disabled={busy} className="shrink-0 rounded-lg border border-border px-3 py-2 text-xs font-semibold disabled:opacity-50">{app.monitored ? "Monitored" : "Excluded"}</button>
          </li>)}</ul>}
      </div>

      <div className="mt-6 rounded-xl border border-border p-4">
        <div className="flex flex-wrap items-center justify-between gap-3"><div><h3 className="font-semibold">Detection history</h3><p className="mt-1 text-xs text-muted-foreground">Only real warnings and notifications with unavailable content are recorded. No notification body is saved.</p></div>
          <div className="flex items-center gap-2"><label htmlFor="scan-retention" className="text-xs">Keep for</label><select id="scan-retention" value={status?.retentionDays ?? 7} onChange={e => void changeRetention(Number(e.target.value))} disabled={busy} className="rounded-lg border border-border bg-background px-2 py-2 text-sm"><option value={1}>1 day</option><option value={7}>7 days</option><option value={30}>30 days</option></select><button onClick={() => void clearHistory()} disabled={busy || !status?.history?.length} className="rounded-lg border border-border bg-background px-3 py-2 text-xs font-semibold disabled:opacity-50">Clear history</button></div>
        </div>
        {!status?.history?.length ? <p className="mt-4 text-sm text-muted-foreground">No real notification warnings recorded yet.</p> : <ul className="mt-4 space-y-2">{status.history.map(row => <li key={row.id} className="rounded-xl border border-border p-3">
          <div className="flex flex-wrap items-center justify-between gap-2"><span className="font-semibold">{row.appName}</span><span className={`text-xs font-bold ${row.status === "WARNING" ? "text-danger" : "text-muted-foreground"}`}>{row.status === "WARNING" ? "WARNING" : "NOT SCANNED"}</span><time className="text-xs text-muted-foreground">{new Date(row.time).toLocaleString()}</time></div>
          <ul className="mt-2 list-inside list-disc text-sm">{row.reasons.map((reason, i) => <li key={`${row.id}-${i}`}>{reason}</li>)}</ul>
        </li>)}</ul>}
      </div>
      <p className="mt-4 text-xs text-muted-foreground">Scans only visible text supplied through notifications. Hidden/redacted content is marked “not scanned.” Rule-based warnings are indicators, not proof; no benign content or notification body is saved. HyperOS background restrictions can affect delivery and must be tested on this phone.</p>
    </>}
  </section>;
}
