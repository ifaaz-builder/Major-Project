import { createServerFn } from "@tanstack/react-start";
import { z } from "zod";

const schema = z.object({
  text: z.string().min(1).max(3000),
  lang: z.string().min(2).max(10),
});

const LANG_NAMES: Record<string, string> = {
  en: "English",
  hi: "Hindi",
  ml: "Malayalam",
  kn: "Kannada",
  ta: "Tamil",
  te: "Telugu",
  bn: "Bengali",
  mr: "Marathi",
  gu: "Gujarati",
  pa: "Punjabi",
  ur: "Urdu",
  es: "Spanish",
};

export const synthesizeSpeech = createServerFn({ method: "POST" })
  .validator((data: unknown) => schema.parse(data))
  .handler(async ({ data }) => {
    const apiKey = process.env["LOVABLE_API_KEY"];
    if (!apiKey) throw new Error("Voice service is not configured");

    const language = LANG_NAMES[data.lang] ?? "English";

    const response = await fetch("https://ai.gateway.lovable.dev/v1/audio/speech", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: "openai/gpt-4o-mini-tts",
        input: data.text,
        voice: "alloy",
        response_format: "mp3",
        instructions: `Speak in ${language}. Read slowly, calmly and clearly, as if warning an elderly person about a scam.`,
      }),
    });

    if (!response.ok) {
      const body = await response.text().catch(() => "");
      throw new Error(`Voice generation failed [${response.status}]: ${body}`);
    }

    const buffer = await response.arrayBuffer();
    return { audioBase64: Buffer.from(buffer).toString("base64") };
  });
