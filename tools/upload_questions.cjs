/**
 * Upload tools/questions.json into Firestore database "default".
 * Uses the logged-in Firebase CLI account.
 */
const fs = require("fs");
const path = require("path");
const tools = path.join(process.env.APPDATA, "npm/node_modules/firebase-tools/lib");
const { configstore } = require(path.join(tools, "configstore"));
const { getAccessToken } = require(path.join(tools, "auth"));
const scopes = require(path.join(tools, "scopes"));

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
  return {
    arrayValue: { values: values.map((x) => str(x)) },
  };
}

const { hintsFor } = require("./hint_bank.cjs");

function fields(q) {
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
    source: str(q.source || "medha-seed"),
    hints: arr(hintsFor(q)),
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
  const questions = JSON.parse(
    fs.readFileSync(path.join(__dirname, "questions.json"), "utf8"),
  );
  const saved = configstore.get("tokens");
  if (!saved?.refresh_token) {
    throw new Error("Firebase CLI is not logged in. Run firebase login.");
  }
  const token = (
    await getAccessToken(saved.refresh_token, [scopes.CLOUD_PLATFORM, scopes.FIREBASE_PLATFORM])
  ).access_token;
  const categories = [
    ["gk", "General Knowledge"],
    ["sci", "Science"],
    ["his", "History"],
    ["spo", "Sports"],
    ["mov", "Movies"],
    ["tech", "Technology"],
    ["fusion", "Fusion"],
    ["nexus", "Nexus"],
  ];
  const catWrites = categories.map(([id, name]) => ({
    update: {
      name: `projects/${PROJECT}/databases/${DATABASE}/documents/categories/${id}`,
      fields: {
        id: str(id),
        name: str(name),
        questionCount: int(200),
        language: str("en"),
        active: bool(true),
      },
    },
  }));
  await commit(token, catWrites);
  console.log(`categories: ${categories.length}`);

  const chunk = 400;
  for (let i = 0; i < questions.length; i += chunk) {
    const slice = questions.slice(i, i + chunk);
    const writes = slice.map((q) => ({
      update: {
        name: `projects/${PROJECT}/databases/${DATABASE}/documents/questions/${q.id}`,
        fields: fields(q),
      },
    }));
    await commit(token, writes);
    console.log(`questions ${i + 1}-${i + slice.length}`);
  }
  console.log(`done: ${questions.length}`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
