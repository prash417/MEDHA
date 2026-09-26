/** Non-spoiler hints: a way in, never the lock itself. */
function escapeRe(s) {
  return String(s).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function redact(hay, needle) {
  const text = String(hay || "");
  const n = String(needle || "").trim();
  if (!n) return text;
  const pat =
    n.length <= 2
      ? new RegExp(`\\b${escapeRe(n)}\\b`, "ig")
      : new RegExp(escapeRe(n), "ig");
  return text
    .replace(pat, "")
    .replace(/\(\s*\)/g, "")
    .replace(/\s{2,}/g, " ")
    .replace(/\s+([,.;:])/g, "$1")
    .replace(/^[—\-–,:;.]\s*/, "")
    .trim();
}

function leaksAnswer(hint, answer) {
  const raw = String(answer || "").trim();
  const h = hint.toLowerCase();
  const a = raw.toLowerCase();
  if (raw.length >= 4 && h.includes(a)) return true;
  if (raw.length >= 1 && raw.length < 4) {
    if (new RegExp(`\\b${escapeRe(raw)}\\b`).test(hint)) return true;
    if (new RegExp(`"${escapeRe(raw)}"`, "i").test(hint)) return true;
  }
  if (/\b(option|lock)\s+[abcd]\b/.test(h)) return true;
  if (/\bthe accurate (answer|lock) is\b/.test(h)) return true;
  if (/\bcorrect (answer|lock) is\b/.test(h)) return true;
  return false;
}

const CAT = {
  gk: "general knowledge",
  sci: "science",
  his: "history",
  spo: "sports",
  mov: "films",
  tech: "technology",
  fusion: "this mixed field",
  nexus: "this advanced topic",
};

function hintsFor(q) {
  const letter = String(q.correct || q.correctAnswer || "A")
    .trim()
    .slice(0, 1)
    .toUpperCase();
  const answer = String(q[`option${letter}`] || "").trim();
  const wrongs = ["A", "B", "C", "D"]
    .filter((l) => l !== letter)
    .map((l) => String(q[`option${l}`] || "").trim())
    .filter(Boolean);
  const expl = redact(q.explanation || "", answer);
  const field = CAT[q.category] || "this topic";
  const stems = [];

  if (expl.length > 18 && !leaksAnswer(expl, answer)) {
    stems.push(`A useful clue: ${expl}`);
    stems.push(`Hold this fact: ${expl}`);
    stems.push(`The record leans this way: ${expl}`);
    stems.push(`Don't chase a lookalike. ${expl}`);
  }

  wrongs.forEach((w) => {
    stems.push(`You can drop "${w}".`);
    stems.push(`"${w}" is a nearby fact, not the lock.`);
    stems.push(`If you were between this and "${w}", skip "${w}".`);
  });

  stems.push("Read the question once more — the key word is already in the prompt.");
  stems.push("Two options are neighbouring facts. Stay with the exact match.");
  stems.push(`Stay inside ${field}. Discard anything from another field.`);
  stems.push("Pick the most specific option, not the broadest famous name.");
  stems.push("If two options feel close, choose the one the question is actually asking.");
  stems.push("Ignore the famous-but-wrong neighbour.");
  stems.push("The distractors are real facts from nearby topics. Don't follow them.");
  stems.push("Think of what the question names, not what sounds similar.");
  stems.push("Cut the option that answers a different question.");
  stems.push("A careful read beats a fast guess here.");
  stems.push("One option matches the stem exactly. The others only rhyme with it.");
  stems.push("Ask: which option would still be true if the others were removed?");
  stems.push("Don't lock the first familiar word you see.");
  stems.push("The safe path is the option tied to the year, place, or name in the question.");
  stems.push("If it feels like a cousin of the right fact, it is probably a trap.");

  const out = [];
  const seen = new Set();
  const push = (raw) => {
    const t = String(raw).replace(/\s+/g, " ").trim();
    if (!t || seen.has(t) || leaksAnswer(t, answer)) return;
    seen.add(t);
    out.push(t);
  };
  stems.forEach(push);
  const pads = [
    "Read the stem twice before you pick.",
    "Cut the option that belongs to a different fact.",
    "Stay with the exact ask, not a famous neighbour.",
    "One word in the question is doing the real work.",
    "The trap is the option that almost fits.",
    "Name, year, and place in the prompt are the trail.",
    "Don't reward the first familiar word.",
    "The right path is narrower than it looks.",
  ];
  pads.forEach(push);
  let n = 1;
  while (out.length < 50) {
    push(`Another way in (${n}): stay with the exact ask, not a famous neighbour.`);
    n += 1;
    if (n > 60) break;
  }
  if (out.length < 50) {
    throw new Error(`${q.id} produced only ${out.length} clean hints`);
  }
  return out.slice(0, 50);
}

module.exports = { hintsFor };
