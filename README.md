# MEDHA

Premium Indian quiz game by **Paras Studios**.

Phase 1 is a playable cinematic prototype:

**Splash → name/avatar/language → Home → Classic game → suspense → reveal → result**

Questions currently load from a local original question bank so the game loop can be polished before Firebase.

## Open in Android Studio

1. Open this folder (`MEDHA`) in Android Studio 2026.1+.
2. Let Gradle sync (JDK is set to Android Studio’s JBR in `gradle.properties`).
3. Run the `app` configuration on a phone or emulator (API 26+).

From a terminal:

```bat
gradlew.bat :app:installDebug
```

## Modes in this build

- **Classic** — 15-question ascent
- **Daily Challenge** — same local set for a given calendar day
- **Quick Play** — 5 questions
- **Practice** — 8 questions, no ranking impact

Lifelines: 50:50, Audience, Expert (Archivist Ila), Switch.

Settings: Music / Sound effects.

## What is intentionally not in Phase 1

- Live Firestore question feeds and server-side answer validation
- Ranked leaderboards with anti-cheat Cloud Functions
- Admin dashboard
- AdMob

The package structure, `QuestionRepository`, `firestore.rules`, analytics façade and ad façade are ready for those phases.

## Originality

MEDHA uses an original mark, palette (ink / teal / copper), type (Cormorant + Outfit), UI language and internally generated audio. It does not reproduce KBC branding, music, layouts or catchphrases.
