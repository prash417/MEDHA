const path = require("path");
const tools = path.join(process.env.APPDATA, "npm/node_modules/firebase-tools/lib");
const { configstore } = require(path.join(tools, "configstore"));
const { getAccessToken } = require(path.join(tools, "auth"));
const scopes = require(path.join(tools, "scopes"));

async function main() {
  const saved = configstore.get("tokens");
  const token = (
    await getAccessToken(saved.refresh_token, [scopes.CLOUD_PLATFORM, scopes.FIREBASE_PLATFORM])
  ).access_token;
  const headers = {
    Authorization: `Bearer ${token}`,
    "x-goog-user-project": "paras-medha",
  };
  const buckets = await fetch("https://storage.googleapis.com/storage/v1/b?project=paras-medha", { headers });
  const bucketJson = await buckets.json();
  console.log("buckets", buckets.status, (bucketJson.items || []).map((b) => b.name));
  const body = {
    input: { text: "What is two plus two? Option A. Four.", prompt: "Speak the script exactly, word for word." },
    voice: { languageCode: "en-IN", name: "Charon", modelName: "gemini-2.5-flash-tts" },
    audioConfig: { audioEncoding: "MP3" },
  };
  const tts = await fetch("https://texttospeech.googleapis.com/v1/text:synthesize", {
    method: "POST",
    headers: { ...headers, "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const raw = await tts.text();
  console.log("tts", tts.status, raw.slice(0, 300));
}
main().catch((e) => { console.error(e); process.exit(1); });
