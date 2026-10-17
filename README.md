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


## October 2026 update — what changed

**Game**
- Every mode (Classic, Quick Play, Practice) plays on the Challenge screen.
- The game-over card says how the run ended (won / prize secured / game over), shows the prize and coins earned, and spins a **3D trophy** (win) or **3D coin**.
- If questions fail to load, a "Try again / Home" dialog appears instead of a stuck screen.
- Settings in the left menu now opens the settings popup.

**Economy**
- Each lifeline is free once per run; after that it costs coins (prices in `domain/engine/Economy.kt` → `LifelineShop`). Hints: 3 free per run, then 50 coins.
- Coin and diamond shops are rewarded ads only (with daily limits) plus a 5 diamonds → 1,000 coins trade.
- Finished runs pay coins (Practice pays none).

**Missions, achievements, profile, leaderboard**
- Missions (left menu) are real, tiered and claimable (`Missions` in `Economy.kt`); achievements read real stats.
- First launch asks for the player's name; Profile lets you change name and avatar.
- Leaderboard is online (Firestore `leaderboard` collection, top 50 by XP or best prize).

**Size and speed**
- Host-voice clips download on demand from Firebase Storage (`voice/<hash>.mp3`), so `assets/offline/voice-packs.zip` (778 MB) is no longer needed. **Move it out of `app/src/main/assets/offline/`** (for example to `tools/`), otherwise it is still packed into the app.
- Asset PNGs are shrunk while loading and cached once, which cuts memory a lot on cheap phones.

### Before you publish
1. **AdMob:** in `app/build.gradle.kts` replace the two TEST ids (`admobAppId`, `ADMOB_REWARDED_ID`) with your own from admob.google.com.
2. **Firestore rules:** run `firebase deploy --only firestore:rules` (the leaderboard rule was tightened).
3. **3D models:** `python tools/models/make_models.py` rebuilds `assets/models/coin.glb` and `trophy.glb` if you change their shapes.
