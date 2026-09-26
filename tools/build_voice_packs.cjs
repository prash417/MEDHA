/**
 * Build a host voice pack for every category and mode question.
 * MP3s go to Cloud Storage voice/{hash}.mp3
 * Each question document gets voicePack = that hash.
 */
const fs = require("fs");
const path = require("path");
const crypto = require("crypto");
const tools = path.join(process.env.APPDATA, "npm/node_modules/firebase-tools/lib");
const { configstore } = require(path.join(tools, "configstore"));
const { getAccessToken } = require(path.join(tools, "auth"));
const scopes = require(path.join(tools, "scopes"));

const PROJECT = "paras-medha";
const BUCKET = "paras-medha.firebasestorage.app";
const DATABASE = "default";
const COMMIT = `https://firestore.googleapis.com/v1/projects/${PROJECT}/databases/${DATABASE}/documents:commit`;
const HOST_PROMPT =
  "You are Medha, one Indian English quiz-show host. " +
  "Speak the script exactly, word for word. Do not add, drop, or rephrase any word. " +
  "Same voice, same pitch, same pace, every line. Proper English. Warm and clear. " +
  "No extra commentary and no hints.";

function wrongScript(q) {
  const letter = String(q.correct || q.correctAnswer || "A").trim().slice(0, 1).toUpperCase();
  const text = String(q[`option${letter}`] || "").trim().replace(/[.\s]+$/, "");
  const detail = text ? `option ${letter}, ${text}` : `option ${letter}`;
  return `Your answer is wrong. The correct answer is ${detail}.`;
}
function boardScript(q) {
  const prompt = String(q.prompt || q.question || "").trim().replace(/[?]+$/, "");
  const parts = [`${prompt}?`];
  for (const letter of ["A", "B", "C", "D"]) {
    const text = String(q[`option${letter}`] || "").trim().replace(/[.]+$/, "");
    if (text) parts.push(`Option ${letter}. ${text}.`);
  }
  return parts.join(" ");
}

function hashScript(script) {
  return crypto.createHash("sha256").update(`v10|${script}`).digest("hex").slice(0, 24);
}

const session = { access: "", at: 0 };

async function token() {
  const saved = configstore.get("tokens");
  if (!saved?.refresh_token) throw new Error("Firebase CLI is not logged in.");
  return (await getAccessToken(saved.refresh_token, [scopes.CLOUD_PLATFORM, scopes.FIREBASE_PLATFORM])).access_token;
}

async function auth(force = false) {
  if (force || !session.access || Date.now() - session.at > 20 * 60 * 1000) {
    session.access = await token();
    session.at = Date.now();
  }
  return session.access;
}

function headers(access, json) {
  const h = { Authorization: `Bearer ${access}`, "x-goog-user-project": PROJECT };
  if (json) h["Content-Type"] = "application/json";
  return h;
}

async function exists(objectPath) {
  const access = await auth();
  const url = `https://storage.googleapis.com/storage/v1/b/${BUCKET}/o/${encodeURIComponent(objectPath)}`;
  const res = await fetch(url, { headers: headers(access) });
  if (res.status === 401) {
    const again = await fetch(url, { headers: headers(await auth(true)) });
    return again.status === 200;
  }
  return res.status === 200;
}

async function synthesize(text) {
  const voices = [
    {
      input: { text, prompt: HOST_PROMPT },
      voice: { languageCode: "en-IN", name: "Charon", modelName: "gemini-2.5-flash-tts" },
      audioConfig: { audioEncoding: "MP3" },
    },
    {
      input: { text },
      voice: { languageCode: "en-IN", name: "en-IN-Chirp3-HD-Charon" },
      audioConfig: { audioEncoding: "MP3" },
    },
  ];
  let last = "tts failed";
  for (const body of voices) {
    for (let attempt = 0; attempt < 5; attempt++) {
      const res = await fetch("https://texttospeech.googleapis.com/v1/text:synthesize", {
        method: "POST",
        headers: headers(await auth(attempt > 0 && last === "401"), true),
        body: JSON.stringify(body),
      });
      if (res.status === 401) {
        last = "401";
        await auth(true);
        continue;
      }
      if (res.status === 429 || res.status >= 500) {
        await new Promise((r) => setTimeout(r, 1500 * (attempt + 1)));
        last = `${res.status}`;
        continue;
      }
      const raw = await res.text();
      if (!res.ok) {
        last = raw.slice(0, 240);
        break;
      }
      const audio = Buffer.from(JSON.parse(raw).audioContent, "base64");
      if (audio.length > 0) return audio;
      last = "empty audio";
      break;
    }
  }
  throw new Error(last);
}

async function upload(objectPath, audio) {
  const url = `https://storage.googleapis.com/upload/storage/v1/b/${BUCKET}/o?uploadType=media&name=${encodeURIComponent(objectPath)}`;
  const res = await fetch(url, {
    method: "POST",
    headers: { ...headers(await auth()), "Content-Type": "audio/mpeg" },
    body: audio,
  });
  if (!res.ok) throw new Error(`upload ${res.status}: ${(await res.text()).slice(0, 240)}`);
}

async function mark(items) {
  const writes = items.map((item) => ({
    update: {
      name: `projects/${PROJECT}/databases/${DATABASE}/documents/questions/${item.id}`,
      fields: { voicePack: { stringValue: item.hash } },
    },
    updateMask: { fieldPaths: ["voicePack"] },
  }));
  const res = await fetch(COMMIT, {
    method: "POST",
    headers: headers(await auth(), true),
    body: JSON.stringify({ writes }),
  });
  if (!res.ok) throw new Error(`commit ${res.status}: ${(await res.text()).slice(0, 240)}`);
}

async function ensure(script) {
  const hash = hashScript(script);
  const objectPath = `voice/${hash}.mp3`;
  if (!(await exists(objectPath))) {
    const audio = await synthesize(script);
    await upload(objectPath, audio);
  }
  return hash;
}

function loadQuestions() {
  const files = ["questions.json", "category_more.json", "mode_questions.json"];
  const all = [];
  for (const file of files) {
    const rows = JSON.parse(fs.readFileSync(path.join(__dirname, file), "utf8"));
    all.push(...rows);
  }
  return all;
}

async function pool(items, limit, worker) {
  let index = 0;
  async function run() {
    while (index < items.length) {
      const current = index;
      index += 1;
      await worker(items[current], current);
    }
  }
  await Promise.all(Array.from({ length: limit }, run));
}

async function main() {
  const shared = [];
  for (let n = 1; n <= 15; n++) shared.push(`Question ${n}.`);
  for (const letter of ["A", "B", "C", "D"]) shared.push(`You selected option ${letter}.`);
  shared.push("Answer locked.", "Your answer is correct.", "Your answer is wrong.");
  for (const line of shared) await ensure(line);
  console.log(`shared lines ${shared.length}`);

  const questions = loadQuestions();
  console.log(`questions ${questions.length}`);
  let done = 0;
  const pending = [];
  let chain = Promise.resolve();
  const lock = (fn) => {
    const run = chain.then(fn, fn);
    chain = run.then(() => {}, () => {});
    return run;
  };
  await pool(questions, 16, async (q) => {
    const hash = await ensure(boardScript(q));
    await ensure(wrongScript(q));
    await lock(async () => {
      pending.push({ id: q.id, hash });
      done += 1;
      if (pending.length >= 20) {
        const batch = pending.splice(0, 20);
        await mark(batch);
        console.log(`packs ${done}/${questions.length}`);
      }
    });
  });
  if (pending.length) await mark(pending.splice(0, pending.length));
  console.log(`done: ${questions.length}`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
