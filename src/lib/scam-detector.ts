export type ThreatLevel = "safe" | "danger";

export interface DetectionResult {
  level: ThreatLevel;
  title: string;
  reason: string;
  coaching: string;
  signals: string[];
}

export type ArtifactKind = "link" | "file";
export type ArtifactVerdict = "safe" | "caution" | "danger";

export interface ArtifactResult {
  value: string;
  kind: ArtifactKind;
  verdict: ArtifactVerdict;
  headline: string;
  reasons: string[];
  advice: string;
}


const SUSPICIOUS_KEYWORDS = [
  "urgent", "verify", "locked", "suspended", "click here", "act now",
  "winner", "prize", "claim", "gift card", "wire transfer", "bitcoin",
  "social security", "irs", "arrest", "warrant", "password", "otp",
  "one-time code", "compromised", "unusual activity", "confirm your",
];

const SUSPICIOUS_DOMAINS = [
  "bit.ly", "tinyurl", "t.co/", "goo.gl", "ow.ly",
  "secure-", "-verify", "-update", "-bank", "login-",
];

const TRUSTED_PHRASES = ["delivery scheduled", "your order", "appointment confirmed"];

export function detectScam(text: string): DetectionResult {
  const lower = text.toLowerCase();
  const signals: string[] = [];

  for (const k of SUSPICIOUS_KEYWORDS) {
    if (lower.includes(k)) signals.push(`Pressure word: "${k}"`);
  }
  for (const d of SUSPICIOUS_DOMAINS) {
    if (lower.includes(d)) signals.push(`Suspicious link pattern: "${d}"`);
  }
  if (/https?:\/\/\S+/.test(lower) && /(bank|account|verify|login|password)/.test(lower)) {
    signals.push("Link combined with banking or login words");
  }
  if (/\b\d{6}\b/.test(lower) && /(code|otp|verification)/.test(lower)) {
    signals.push("Asks you to share a verification code");
  }

  const trusted = TRUSTED_PHRASES.some((p) => lower.includes(p));

  if (signals.length >= 1 && !trusted) {
    return {
      level: "danger",
      title: "DANGER: SCAM DETECTED",
      reason: "This message uses tricks scammers commonly use.",
      coaching:
        "Banks, the IRS, and delivery companies will never ask you to click a link to unlock an account, share a code, or send money quickly. Do not tap the link. Do not type your password. If you are worried, call the company using the number on the back of your card.",
      signals: signals.slice(0, 4),
    };
  }

  return {
    level: "safe",
    title: "Safe Message",
    reason: "No common scam patterns were detected.",
    coaching:
      "This message looks normal. Still, never share passwords or one-time codes with anyone — not even someone who says they are from your bank.",
    signals: ["No pressure language", "No suspicious links"],
  };
}

/* ---------------- Link & file (APK) scanner ---------------- */

const SHORTENERS = ["bit.ly", "tinyurl", "t.co", "goo.gl", "ow.ly", "cutt.ly", "rb.gy", "is.gd", "shorturl", "tiny.cc"];
const RISKY_TLDS = [".apk", ".xyz", ".top", ".click", ".zip", ".mov", ".tk", ".ml", ".gq", ".cf", ".rest", ".buzz"];
const BRAND_WORDS = ["bank", "paytm", "phonepe", "gpay", "sbi", "hdfc", "icici", "amazon", "flipkart", "paypal", "netflix", "kyc", "aadhaar", "upi"];
const DANGEROUS_EXTENSIONS = [".apk", ".exe", ".scr", ".bat", ".msi", ".vbs", ".jar", ".apks", ".xapk"];
const SAFE_EXTENSIONS = [".pdf", ".jpg", ".jpeg", ".png", ".txt", ".docx", ".mp3", ".mp4"];

export const MESSAGE_SOURCES = [
  "WhatsApp",
  "SMS",
  "Email",
  "Telegram",
  "Facebook / Instagram",
  "Browser download",
  "Unknown number",
] as const;
export type MessageSource = (typeof MESSAGE_SOURCES)[number];

const HIGH_RISK_SOURCES: string[] = ["WhatsApp", "Telegram", "Unknown number", "SMS", "Facebook / Instagram"];

export function extractArtifacts(text: string): { value: string; kind: ArtifactKind }[] {
  const found: { value: string; kind: ArtifactKind }[] = [];
  const urlRe = /((https?:\/\/)?[a-z0-9-]+(\.[a-z0-9-]+)+(\/[^\s]*)?)/gi;
  const fileRe = /[\w-]+\.(apk|apks|xapk|exe|scr|bat|msi|vbs|jar|pdf|docx|zip)\b/gi;

  for (const m of text.matchAll(fileRe)) {
    found.push({ value: m[0], kind: "file" });
  }
  for (const m of text.matchAll(urlRe)) {
    const v = m[0];
    if (found.some((f) => v.toLowerCase().includes(f.value.toLowerCase()))) continue;
    if (!/\.[a-z]{2,}/i.test(v)) continue;
    found.push({ value: v, kind: "link" });
  }
  return found.slice(0, 6);
}

export function analyzeArtifact(
  raw: string,
  source?: MessageSource,
): ArtifactResult {
  const value = raw.trim();
  const lower = value.toLowerCase();
  const kind: ArtifactKind =
    DANGEROUS_EXTENSIONS.some((e) => lower.endsWith(e)) ||
    SAFE_EXTENSIONS.some((e) => lower.endsWith(e)) ||
    (/\.[a-z0-9]{2,5}$/i.test(lower) && !lower.includes("/") && !lower.startsWith("http") && !/\.(com|net|org|in|io|co|gov|edu)$/i.test(lower))
      ? "file"
      : "link";

  const reasons: string[] = [];
  let score = 0;

  if (kind === "file") {
    const isApk = /\.(apk|apks|xapk)$/i.test(lower);
    if (isApk) {
      score += 5;
      reasons.push("This is an Android app installer (APK) sent outside the Play Store");
    } else if (DANGEROUS_EXTENSIONS.some((e) => lower.endsWith(e))) {
      score += 5;
      reasons.push("This file can install a program on your device");
    } else if (SAFE_EXTENSIONS.some((e) => lower.endsWith(e))) {
      reasons.push("Common document or media file type");
    } else {
      score += 1;
      reasons.push("Unknown file type");
    }
    if (BRAND_WORDS.some((b) => lower.includes(b))) {
      score += 2;
      reasons.push("File name copies a bank or popular brand name");
    }
    if (/(update|kyc|reward|offer|verify|gift|lucky)/.test(lower)) {
      score += 2;
      reasons.push("File name uses a pressure or reward word");
    }
  } else {
    if (SHORTENERS.some((s) => lower.includes(s))) {
      score += 3;
      reasons.push("Shortened link hides the real website address");
    }
    if (RISKY_TLDS.some((tld) => lower.includes(tld))) {
      score += 3;
      reasons.push("Uses a web address ending that scammers often buy cheaply");
    }
    if (/\d{1,3}(\.\d{1,3}){3}/.test(lower)) {
      score += 3;
      reasons.push("Link points to a bare IP address instead of a real site");
    }
    if (/(secure-|-verify|login-|-update|-support|account-)/.test(lower)) {
      score += 3;
      reasons.push("Web address is dressed up to look official");
    }
    if (BRAND_WORDS.some((b) => lower.includes(b)) && !/^https:\/\/[a-z0-9-]+\.(com|in|org|gov)\//.test(lower)) {
      score += 2;
      reasons.push("Uses a bank or brand name in a look-alike address");
    }
    if (lower.startsWith("http://")) {
      score += 2;
      reasons.push("Not a secure (https) connection");
    }
    if ((lower.match(/-/g) || []).length >= 3 || (lower.match(/\./g) || []).length >= 4) {
      score += 1;
      reasons.push("Unusually long or complicated web address");
    }
    if (reasons.length === 0) reasons.push("No known scam patterns in this web address");
  }

  if (source && HIGH_RISK_SOURCES.includes(source) && score > 0) {
    score += 1;
    reasons.push(`Arrived through ${source}, a channel scammers use often`);
  }

  const verdict: ArtifactVerdict = score >= 4 ? "danger" : score >= 2 ? "caution" : "safe";

  const headline =
    verdict === "danger"
      ? kind === "file"
        ? "DANGER — Do not install this file"
        : "DANGER — Do not open this link"
      : verdict === "caution"
        ? "BE CAREFUL — This looks suspicious"
        : kind === "file"
          ? "Looks safe — but still check the sender"
          : "Looks safe — no scam patterns found";

  const advice =
    verdict === "danger"
      ? kind === "file"
        ? "Delete this file. Apps sent by message can steal your bank OTPs and money. Only install apps from the Google Play Store or Apple App Store."
        : "Do not tap this link and never type your password or OTP on the page it opens. If it claims to be your bank, call the number printed on your card instead."
      : verdict === "caution"
        ? "Do not act on this yet. Confirm with the sender by calling a number you already know, and never share a code or password."
        : "Nothing suspicious found, but if you did not expect this from the sender, confirm with them first.";

  return { value, kind, verdict, headline, reasons: reasons.slice(0, 4), advice };
}
