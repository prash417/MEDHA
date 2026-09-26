/**
 * Patch every Firestore question with 50 accurate hints derived from
 * the current correct option + explanation. Does not replace other fields.
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

function arr(values) {
  return { arrayValue: { values: values.map((x) => str(x)) } };
}

const { hintsFor } = require("./hint_bank.cjs");

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

  const chunk = 40;
  for (let i = 0; i < questions.length; i += chunk) {
    const slice = questions.slice(i, i + chunk);
    const writes = slice.map((q) => {
      const hints = hintsFor(q);
      if (hints.length < 50) {
        throw new Error(`${q.id} only produced ${hints.length} hints`);
      }
      return {
        update: {
          name: `projects/${PROJECT}/databases/${DATABASE}/documents/questions/${q.id}`,
          fields: { hints: arr(hints) },
        },
        updateMask: { fieldPaths: ["hints"] },
      };
    });
    await commit(token, writes);
    console.log(`hints ${i + 1}-${i + slice.length}`);
  }
  console.log(`done: ${questions.length} questions × 50 hints`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
