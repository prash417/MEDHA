/**
 * MEDHA Host Voice API
 * Callable: hostSpeak
 *
 * Proper English quiz host. Board is split so the UI can reveal
 * the question and each option in sync with the voice.
 */
const crypto = require("crypto");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");
const textToSpeech = require("@google-cloud/text-to-speech");

setGlobalOptions({ region: "asia-south1", maxInstances: 20 });
admin.initializeApp();

const ttsHolder = { client: null };

function ttsClient() {
  if (!ttsHolder.client) ttsHolder.client = new textToSpeech.TextToSpeechClient();
  return ttsHolder.client;
}

const HOST_PROMPT =
  "You are Medha, one Indian English quiz-show host. " +
  "Speak the script exactly, word for word. Do not add, drop, or rephrase any word. " +
  "Same voice, same pitch, same pace, every line. Proper English. Warm and clear. " +
  "No extra commentary and no hints.";

const HOST_VOICE = { languageCode: "en-IN", name: "Charon", modelName: "gemini-2.5-flash-tts" };
const FALLBACK_VOICE = { languageCode: "en-IN", name: "en-IN-Chirp3-HD-Charon", chirp: true };

function boardParts(_number, prompt, options) {
  const q = String(prompt || "").trim().replace(/[?]+$/, "");
  const parts = [{ id: "q", text: `${q}?` }];
  const order = ["A", "B", "C", "D"];
  const byLetter = {};
  (options || []).forEach((o) => {
    const letter = String(o.letter || o.id || "").toUpperCase().slice(0, 1);
    const text = String(o.text || o.value || "").trim();
    if (letter && text) byLetter[letter] = text.replace(/[.]+$/, "");
  });
  order.forEach((letter) => {
    if (byLetter[letter]) {
      parts.push({ id: letter, text: `Option ${letter}. ${byLetter[letter]}.` });
    }
  });
  return parts;
}

function lineFor(kind, detail) {
  const d = String(detail || "").trim();
  switch (String(kind || "")) {
    case "OPTION_SELECTED":
      return `You selected option ${d}.`;
    case "FINAL_ANSWER":
      return "Answer locked.";
    case "CORRECT":
      return "Your answer is correct.";
    case "INCORRECT":
      return d
        ? `Your answer is wrong. The correct answer is ${d}.`
        : "Your answer is wrong.";
    default:
      return d;
  }
}

async function geminiWith(text) {
  const [res] = await ttsClient().synthesizeSpeech({
    input: { text, prompt: HOST_PROMPT },
    voice: HOST_VOICE,
    audioConfig: {
      audioEncoding: "MP3",
    },
  });
  if (!res.audioContent || !res.audioContent.length) throw new Error("empty gemini audio");
  return { audio: res.audioContent, voice: `${HOST_VOICE.modelName}:${HOST_VOICE.name}` };
}

async function classicWith(text) {
  const [res] = await ttsClient().synthesizeSpeech({
    input: { text },
    voice: { languageCode: FALLBACK_VOICE.languageCode, name: FALLBACK_VOICE.name },
    audioConfig: {
      audioEncoding: "MP3",
      speakingRate: 1.0,
    },
  });
  if (!res.audioContent || !res.audioContent.length) throw new Error("empty classic audio");
  return { audio: res.audioContent, voice: FALLBACK_VOICE.name };
}

async function speak(text) {
  try {
    return await geminiWith(text);
  } catch (err) {
    console.error("gemini host failed", err && err.message ? err.message : err);
    return classicWith(text);
  }
}

async function clipFor(script) {
  const hash = crypto.createHash("sha256").update(`v10|${script}`).digest("hex").slice(0, 24);
  const objectPath = `voice/${hash}.mp3`;
  const file = admin.storage().bucket("paras-medha.firebasestorage.app").file(objectPath);
  try {
    const [exists] = await file.exists();
    if (exists) {
      const [buf] = await file.download();
      return { hash, audio: buf, voice: "cached" };
    }
  } catch (_err) {
    // Storage optional.
  }
  const synth = await speak(script);
  try {
    await file.save(Buffer.from(synth.audio), {
      contentType: "audio/mpeg",
      metadata: {
        cacheControl: "public,max-age=31536000",
        metadata: { voice: synth.voice, kind: "medha-host" },
      },
    });
  } catch (_err) {
    // Ignore cache write failures.
  }
  return { hash, audio: Buffer.from(synth.audio), voice: synth.voice };
}

exports.hostSpeak = onCall(
  { cors: true, invoker: "public", timeoutSeconds: 60, memory: "512MiB", minInstances: 1, concurrency: 20 },
  async (req) => {
    const kind = String(req.data?.kind || "board");
    if (kind === "line" || kind === "OPTION_SELECTED" || kind === "FINAL_ANSWER" || kind === "CORRECT" || kind === "INCORRECT") {
      const script = String(req.data?.line || "").trim() || lineFor(kind === "line" ? req.data?.cue : kind, req.data?.detail);
      if (!script) {
        throw new HttpsError("invalid-argument", "Need a host line.");
      }
      const clip = await clipFor(script);
      return {
        hash: clip.hash,
        voice: clip.voice,
        contentType: "audio/mpeg",
        audioBase64: clip.audio.toString("base64"),
      };
    }
    const number = req.data?.number;
    const prompt = req.data?.prompt;
    const options = req.data?.options;
    if (!prompt || !Array.isArray(options) || options.length < 2) {
      throw new HttpsError("invalid-argument", "Need a question and options.");
    }
    const parts = boardParts(number, prompt, options);
    const script = parts.map((p) => p.text).join(" ");
    const clip = await clipFor(script);
    return {
      voice: clip.voice,
      contentType: "audio/mpeg",
      clips: [
        {
          id: "board",
          hash: clip.hash,
          audioBase64: clip.audio.toString("base64"),
        },
      ],
    };
  },
);
