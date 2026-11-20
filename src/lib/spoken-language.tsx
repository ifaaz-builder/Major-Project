import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { Capacitor, registerPlugin } from "@capacitor/core";
import { LANGUAGES, type LangCode } from "@/lib/i18n";

type SpokenLanguageApi = {
  getPreferredLanguage(): Promise<{ language: LangCode | null }>;
  setPreferredLanguage(options: { language: LangCode }): Promise<{ language: LangCode }>;
};

type SpokenLanguageContextValue = {
  language: LangCode;
  setLanguage: (language: LangCode) => Promise<void>;
};

const STORAGE_KEY = "scamshield.spokenLanguage";
const NativeSpokenLanguage = registerPlugin<SpokenLanguageApi>("SpokenLanguage");
type VoiceStatus = { code: LangCode; tag: string; status: "READY" | "MISSING VOICE DATA" | "UNSUPPORTED"; voiceLocale: string | null };
type SpeechStatusApi = {
  getSpeechLanguageStatus(): Promise<{ selectedLanguage: LangCode | null; engineStatus: string; engineFailure: string; languages: VoiceStatus[]; additionalOutputCapabilities?: { language: string; status: string; backend: string; reason: string }[] }>;
  testSpokenAlert(): Promise<{ queued: boolean; status: string }>;
};
const NativeSpeechStatus = registerPlugin<SpeechStatusApi>("CallScreening");
const SpokenLanguageContext = createContext<SpokenLanguageContextValue | null>(null);

function isLangCode(value: unknown): value is LangCode {
  return typeof value === "string" && LANGUAGES.some((language) => language.code === value);
}

async function readSavedLanguage(): Promise<LangCode | null> {
  if (Capacitor.isNativePlatform()) {
    const { language } = await NativeSpokenLanguage.getPreferredLanguage();
    return isLangCode(language) ? language : null;
  }
  const saved = window.localStorage.getItem(STORAGE_KEY);
  return isLangCode(saved) ? saved : null;
}

async function writeSavedLanguage(language: LangCode): Promise<void> {
  if (Capacitor.isNativePlatform()) {
    await NativeSpokenLanguage.setPreferredLanguage({ language });
    return;
  }
  window.localStorage.setItem(STORAGE_KEY, language);
}

export function SpokenLanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguageState] = useState<LangCode | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadFailed, setLoadFailed] = useState(false);
  const [saving, setSaving] = useState(false);

  const loadLanguage = useCallback(async () => {
    setLoading(true);
    setLoadFailed(false);
    try {
      setLanguageState(await readSavedLanguage());
    } catch {
      setLoadFailed(true);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void loadLanguage(); }, [loadLanguage]);

  const setLanguage = useCallback(async (nextLanguage: LangCode) => {
    setSaving(true);
    try {
      await writeSavedLanguage(nextLanguage);
      setLanguageState(nextLanguage);
      setLoadFailed(false);
    } finally {
      setSaving(false);
    }
  }, []);

  if (loading || loadFailed) {
    return <main className="flex min-h-screen items-center justify-center bg-background px-5 text-foreground">
      <div className="w-full max-w-xl rounded-3xl border-2 border-border bg-card p-8 text-center">
        <h1 className="font-display text-3xl font-bold">ScamShield AI</h1>
        {loading ? <p className="mt-4 text-lg">Loading your spoken-language setting…</p> : <>
          <p className="mt-4 text-lg">Your spoken-language setting could not be loaded. Try again to continue.</p>
          <button onClick={() => void loadLanguage()} className="mt-6 min-h-14 rounded-2xl bg-primary px-8 py-4 text-lg font-bold text-primary-foreground">Try again</button>
        </>}
      </div>
    </main>;
  }

  if (!language) return <LanguageOnboarding onContinue={setLanguage} saving={saving} />;

  return <SpokenLanguageContext.Provider value={{ language, setLanguage }}>{children}</SpokenLanguageContext.Provider>;
}

function LanguageOnboarding({ onContinue, saving }: { onContinue: (language: LangCode) => Promise<void>; saving: boolean }) {
  const [selected, setSelected] = useState<LangCode | "">("");
  const [error, setError] = useState("");
  const submit = async () => {
    if (!selected) return;
    setError("");
    try { await onContinue(selected); }
    catch { setError("The selection could not be saved. Please try again."); }
  };

  return <main className="flex min-h-screen items-center justify-center bg-background px-5 py-10 text-foreground">
    <section className="w-full max-w-2xl rounded-3xl border-2 border-primary/30 bg-card p-7 text-center shadow-lg md:p-10">
      <h1 className="font-display text-3xl font-bold md:text-4xl">Welcome to ScamShield AI</h1>
      <p className="mx-auto mt-5 max-w-xl text-lg leading-relaxed">Choose the language you want ScamShield to use when reading safety alerts aloud. All written app content will remain in English. You can change this setting later.</p>
      <label className="mt-8 block text-left">
        <span className="mb-3 block text-lg font-bold">Preferred spoken language</span>
        <select value={selected} onChange={(event) => setSelected(event.target.value as LangCode)} className="min-h-16 w-full rounded-2xl border-2 border-border bg-background px-4 py-4 text-lg font-semibold focus:border-primary focus:outline-none">
          <option value="" disabled>Select a language</option>
          {LANGUAGES.map((language) => <option key={language.code} value={language.code}>{language.native} — {language.label}</option>)}
        </select>
      </label>
      {error && <p role="alert" className="mt-4 text-base font-semibold text-danger">{error}</p>}
      <button onClick={() => void submit()} disabled={!selected || saving} className="mt-7 min-h-16 w-full rounded-2xl bg-primary px-6 py-4 text-xl font-bold text-primary-foreground disabled:opacity-50">
        {saving ? "Saving…" : "Continue"}
      </button>
    </section>
  </main>;
}

export function useSpokenLanguage() {
  const value = useContext(SpokenLanguageContext);
  if (!value) throw new Error("useSpokenLanguage must be used inside SpokenLanguageProvider");
  return value;
}

export function SpokenLanguageSettings() {
  const { language, setLanguage } = useSpokenLanguage();
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [voiceStatuses, setVoiceStatuses] = useState<VoiceStatus[]>([]);
  const [additionalOutputCapabilities, setAdditionalOutputCapabilities] = useState<{ language: string; status: string; backend: string; reason: string }[]>([]);
  const [checkingVoices, setCheckingVoices] = useState(false);
  const [speechTest, setSpeechTest] = useState("");
  const [testingSpeech, setTestingSpeech] = useState(false);
  const refreshVoices = useCallback(async () => {
    if (!Capacitor.isNativePlatform()) return;
    setCheckingVoices(true);
    try {
      const result = await NativeSpeechStatus.getSpeechLanguageStatus();
      setVoiceStatuses(result.languages);
      setAdditionalOutputCapabilities(result.additionalOutputCapabilities ?? []);
    } catch {
      setVoiceStatuses([]);
      setAdditionalOutputCapabilities([]);
    } finally {
      setCheckingVoices(false);
    }
  }, []);
  useEffect(() => {
    void refreshVoices();
    const onResume = () => { if (document.visibilityState === "visible") void refreshVoices(); };
    window.addEventListener("focus", onResume);
    document.addEventListener("visibilitychange", onResume);
    return () => {
      window.removeEventListener("focus", onResume);
      document.removeEventListener("visibilitychange", onResume);
    };
  }, [refreshVoices]);
  const changeLanguage = async (next: LangCode) => {
    setSaving(true);
    setError("");
    try { await setLanguage(next); setSpeechTest(""); await refreshVoices(); }
    catch { setError("The spoken language could not be saved. Please try again."); }
    finally { setSaving(false); }
  };
  const testSpeech = async () => {
    setTestingSpeech(true);
    setSpeechTest("Preparing the selected offline voice…");
    try {
      const result = await NativeSpeechStatus.testSpokenAlert();
      setSpeechTest(result.status);
    } catch {
      setSpeechTest("The speech test could not be started. Check the selected voice status below.");
    } finally {
      setTestingSpeech(false);
      await refreshVoices();
    }
  };
  const selectedVoice = voiceStatuses.find((item) => item.code === language);

  return <section aria-labelledby="spoken-language-heading" className="mx-auto mt-8 max-w-7xl rounded-3xl border-2 border-border bg-card p-6 md:p-8">
    <h2 id="spoken-language-heading" className="font-display text-2xl font-bold">Settings</h2>
    <label className="mt-4 block max-w-xl">
      <span className="mb-2 block text-lg font-semibold">Preferred spoken language</span>
      <select value={language} onChange={(event) => void changeLanguage(event.target.value as LangCode)} disabled={saving} className="min-h-14 w-full rounded-2xl border-2 border-border bg-background px-4 py-3 text-lg font-semibold disabled:opacity-60">
        {LANGUAGES.map((option) => <option key={option.code} value={option.code}>{option.native} — {option.label}</option>)}
      </select>
    </label>
    <p className="mt-3 max-w-2xl text-base text-muted-foreground">This setting changes read-aloud language only. The app interface and written explanations stay in English.</p>
    {Capacitor.isNativePlatform() ? <div className="mt-5 max-w-3xl rounded-2xl border border-border p-4">
      <h3 className="font-semibold">Android speech voice status</h3>
      <p className="mt-1 text-sm">Selected voice: <strong>{selectedVoice?.status ?? (checkingVoices ? "Checking…" : "Unavailable")}</strong>{selectedVoice?.voiceLocale ? ` (${selectedVoice.voiceLocale})` : ""}</p>
      {selectedVoice?.status === "MISSING VOICE DATA" && <p className="mt-2 text-sm">Install the selected language voice in Android Settings &gt; Accessibility (or Additional settings &gt; Languages &amp; input) &gt; Text-to-speech output &gt; your engine settings &gt; Install voice data. Then return here and refresh this screen. ScamShield will not speak in another language.</p>}
      {selectedVoice?.status === "UNSUPPORTED" && <p className="mt-2 text-sm">The installed Android text-to-speech engine does not support this language. Choose another available voice or install a compatible offline TTS engine in Android settings. ScamShield will not substitute another language.</p>}
      <div className="mt-3 grid gap-1 sm:grid-cols-2">
        {LANGUAGES.map((option) => {
          const status = voiceStatuses.find((item) => item.code === option.code)?.status;
          return <p key={option.code} className="text-xs">{option.label}: {status ?? (checkingVoices ? "Checking…" : "Not checked")}</p>;
        })}
      </div>
      <div className="mt-4 border-t border-border pt-3">
        <h4 className="text-sm font-semibold">Additional spoken-language output readiness</h4>
        {additionalOutputCapabilities.map((item) => <p key={item.language} className="mt-1 text-xs">
          {item.language === "tulu" ? "Tulu" : item.language === "beary" ? "Beary" : item.language}: {item.status}
          {item.status !== "READY" ? " — approved exact-language recordings have not been supplied." : " — recorded prompt backend ready."}
        </p>)}
        <p className="mt-2 text-xs text-muted-foreground">Tulu and Beary are not selectable until approved recordings provide the complete safety-phrase set. ScamShield will not substitute Kannada.</p>
      </div>
      <button onClick={() => void testSpeech()} disabled={testingSpeech || checkingVoices} className="mt-4 rounded-lg border border-border bg-background px-4 py-2 font-semibold disabled:opacity-50">{testingSpeech ? "Speaking test…" : "Test selected language"}</button>
      <button onClick={() => void refreshVoices()} disabled={checkingVoices} className="ml-2 mt-4 rounded-lg border border-border bg-background px-4 py-2 font-semibold disabled:opacity-50">Refresh voice status</button>
      <p className="mt-2 text-xs text-muted-foreground">The test is separate from automatic warning speech and does not create a call or history entry.</p>
      {speechTest && <p role="status" className="mt-2 text-sm">Speech test: {speechTest}</p>}
    </div> : <p className="mt-4 max-w-2xl text-sm text-muted-foreground">Android voice availability is checked in the installed app. This web preview uses only an exact matching browser voice and does not use a cloud speech service.</p>}
    {error && <p role="alert" className="mt-3 text-base font-semibold text-danger">{error}</p>}
  </section>;
}
