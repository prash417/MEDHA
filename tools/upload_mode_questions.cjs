/**
 * Upload tools/mode_questions.json (classic, quick, practice) with 50 hints each.
 */
const fs = require("fs");
const path = require("path");
const tools = path.join(process.env.APPDATA, "npm/node_modules/firebase-tools/lib");
const { configstore } = require(path.join(tools, "configstore"));
const { getAccessToken } = require(path.join(tools, "auth"));
const scopes = require(path.join(tools, "scopes"));
const { hintsFor } = require("./hint_bank.cjs");

const PROJECT = "paras-medha";
const DATABASE = "default";
const COMMIT = `https://firestore.googleapis.com/v1/projects/${PROJECT}/databases/${DATABASE}/documents:commit`;

function str(v) {
  return { stringValue: String(v) };
}
function bool(v) {
  return { booleanValue: Boolean(v) };
}
function int(v) {
  return { integerValue: String(v) };
}
function arr(values) {
  return { arrayValue: { values: values.map((x) => str(x)) } };
}

function fields(q, hints) {
  return {
    question: str(q.question),
    prompt: str(q.prompt),
    optionA: str(q.optionA),
    optionB: str(q.optionB),
    optionC: str(q.optionC),
    optionD: str(q.optionD),
    correctAnswer: str(q.correctAnswer),
    correct: str(q.correct),
    category: str(q.category),
    difficulty: str(q.difficulty),
    explanation: str(q.explanation),
    prizeLevel: int(q.prizeLevel),
    language: str(q.language),
    active: bool(q.active),
    tags: arr(q.tags || [q.category]),
    source: str(q.source || "medha-mode"),
    hints: arr(hints),
  };
}

async function commit(token, writes) {
  const res = await fetch(COMMIT, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ writes }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`commit ${res.status}: ${text.slice(0, 800)}`);
  }
}

async function main() {
  const file = process.argv[2] || "mode_questions.json";
  const questions = JSON.parse(fs.readFileSync(path.join(__dirname, file), "utf8"));
  const counts = {};
  for (const q of questions) {
    const hints = hintsFor(q);
    if (hints.length !== 50) throw new Error(`${q.id} has ${hints.length} hints`);
    q._hints = hints;
    counts[q.category] = (counts[q.category] || 0) + 1;
  }
  console.log(counts);
  const saved = configstore.get("tokens");
  if (!saved?.refresh_token) throw new Error("Firebase CLI is not logged in.");
  const token = (
    await getAccessToken(saved.refresh_token, [scopes.CLOUD_PLATFORM, scopes.FIREBASE_PLATFORM])
  ).access_token;

  const chunk = 20;
  for (let i = 0; i < questions.length; i += chunk) {
    const slice = questions.slice(i, i + chunk);
    const writes = slice.map((q) => ({
      update: {
        name: `projects/${PROJECT}/databases/${DATABASE}/documents/questions/${q.id}`,
        fields: fields(q, q._hints),
      },
    }));
    await commit(token, writes);
    console.log(`uploaded ${i + 1}-${i + slice.length}`);
  }
  console.log(`done: ${questions.length}`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
