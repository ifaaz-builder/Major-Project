import { Capacitor, registerPlugin } from "@capacitor/core";
import { LANGUAGES, type LangCode } from "@/lib/i18n";

type NativeSpeechApi = {
  speakManualPhrase(options: { phraseId: string }): Promise<{ queued: boolean; status: string }>;
  stopManualSpeech(): Promise<void>;
};

const NativeSpeech = registerPlugin<NativeSpeechApi>("CallScreening");
let currentUtterance: SpeechSynthesisUtterance | null = null;

export function stopSpeaking() {
  if (typeof window !== "undefined" && "speechSynthesis" in window) {
    window.speechSynthesis.cancel();
  }
  currentUtterance = null;
  if (Capacitor.isNativePlatform()) void NativeSpeech.stopManualSpeech().catch(() => undefined);
}

/** Uses native offline Android TTS for fixed phrase IDs; web uses only an exact language voice. */
export async function speakSafetyPhrase(
  phraseId: string,
  lang: LangCode,
  fallbackText: string,
  onEnd?: () => void,
): Promise<void> {
  stopSpeaking();
  if (Capacitor.isNativePlatform()) {
    const result = await NativeSpeech.speakManualPhrase({ phraseId });
    if (!result.queued) throw new Error(result.status || "The selected voice is unavailable.");
    onEnd?.();
    return;
  }
  if (typeof window === "undefined" || !("speechSynthesis" in window)) {
    throw new Error("Speech is unavailable in this browser.");
  }
  const option = LANGUAGES.find((item) => item.code === lang);
  const voices = window.speechSynthesis.getVoices();
  const voice = voices.find((item) => item.lang.replace("_", "-").toLowerCase() === option?.bcp47.toLowerCase());
  if (!voice) throw new Error("A voice for the selected language is unavailable in this browser.");

  await new Promise<void>((resolve, reject) => {
    const utterance = new SpeechSynthesisUtterance(fallbackText);
    currentUtterance = utterance;
    utterance.voice = voice;
    utterance.lang = option?.bcp47 ?? voice.lang;
    utterance.rate = 0.92;
    utterance.onend = () => { currentUtterance = null; onEnd?.(); resolve(); };
    utterance.onerror = () => { currentUtterance = null; onEnd?.(); reject(new Error("Speech could not be played.")); };
    window.speechSynthesis.speak(utterance);
  });
}
