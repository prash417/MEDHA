package studio.paras.medha.ui.home

import studio.paras.medha.domain.engine.XpRules
import studio.paras.medha.domain.model.Achievement
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.PlayerProfile

enum class LobbyDest {
    HOME,
    PLAY,
    GAME_MODES,
    CATEGORIES,
    LEADERBOARD,
    MISSIONS,
    INVENTORY,
    ACHIEVEMENTS,
    PROFILE,
    SETTINGS,
}

enum class LobbyOverlay {
    MISSIONS,
    ACHIEVEMENTS,
    INVENTORY,
    FRIENDS,
    MESSAGES,
    VAULT,
}

enum class LobbyAccent { GOLD, BLUE, AMBER, TEAL, MAGENTA, GREEN }

data class LobbyPlayer(
    val name: String,
    val avatarId: Int,
    val level: Int,
    val xpProgress: Float,
    val xpInto: Int,
    val xpNeed: Int,
    val vip: Boolean,
    val coins: Int,
    val gems: Int,
)

data class LobbyMode(
    val mode: GameMode,
    val title: String,
    val subtitle: String,
    val accent: LobbyAccent,
)

data class LobbyCategory(
    val id: String,
    val name: String,
    val accent: LobbyAccent,
)

data class LobbyEvent(
    val id: String,
    val kicker: String,
    val title: String,
    val body: String,
    val cta: String?,
    val playMode: GameMode?,
    val endsAtEpochMs: Long?,
    val accent: LobbyAccent,
    val badge: String? = null,
)

data class LobbyMission(
    val id: String,
    val title: String,
    val detail: String,
    val progress: Float,
)

data class LobbyCatalog(
    val modes: List<LobbyMode>,
    val categories: List<LobbyCategory>,
    val events: List<LobbyEvent>,
    val missions: List<LobbyMission>,
    val achievements: List<Achievement>,
    val quote: String,
    val statusRight: String,
) {
    companion object {
        fun sample(nowMs: Long = System.currentTimeMillis()): LobbyCatalog {
            val leagueEnd = nowMs + 3L * 24 * 60 * 60 * 1000 + 12L * 60 * 60 * 1000
            return LobbyCatalog(
                modes = listOf(
                    LobbyMode(GameMode.CLASSIC, "CLASSIC", "Fifteen locks. One mind.", LobbyAccent.GOLD),
                    LobbyMode(GameMode.QUICK, "QUICK PLAY", "Five questions. Full tension.", LobbyAccent.TEAL),
                    LobbyMode(GameMode.PRACTICE, "PRACTICE", "No pressure. Just learn.", LobbyAccent.MAGENTA),
                ),
                categories = listOf(
                    LobbyCategory("gk", "General", LobbyAccent.GOLD),
                    LobbyCategory("sci", "Science", LobbyAccent.BLUE),
                    LobbyCategory("his", "History", LobbyAccent.AMBER),
                    LobbyCategory("spo", "Sports", LobbyAccent.TEAL),
                    LobbyCategory("mov", "Movies", LobbyAccent.GOLD),
                    LobbyCategory("tech", "Technology", LobbyAccent.BLUE),
                    LobbyCategory("fusion", "Fusion", LobbyAccent.BLUE),
                    LobbyCategory("nexus", "Nexus", LobbyAccent.TEAL),
                    LobbyCategory("art", "Arts & Culture", LobbyAccent.AMBER),
                    LobbyCategory("env", "Environment", LobbyAccent.TEAL),
                ),
                events = listOf(
                    LobbyEvent(
                        id = "daily",
                        kicker = "DAILY CHALLENGE",
                        title = "₹1,00,00,000",
                        body = "10 QUESTIONS  ·  WIN UP TO",
                        cta = "PLAY NOW",
                        playMode = GameMode.DAILY,
                        endsAtEpochMs = null,
                        accent = LobbyAccent.GOLD,
                        badge = "NEW",
                    ),
                    LobbyEvent(
                        id = "league",
                        kicker = "SPECIAL EVENT",
                        title = "LEGENDS LEAGUE",
                        body = "",
                        cta = null,
                        playMode = null,
                        endsAtEpochMs = leagueEnd,
                        accent = LobbyAccent.BLUE,
                    ),
                ),
                missions = listOf(
                    LobbyMission("m1", "Take the seat", "Finish one Classic run", 0.15f),
                    LobbyMission("m2", "Ten locks", "Seal 10 correct answers", 0.40f),
                    LobbyMission("m3", "Three dawns", "Hold a 3-day streak", 0.33f),
                ),
                achievements = listOf(
                    Achievement("a1", "First seat", "Enter the chamber", true),
                    Achievement("a2", "Ten thousand", "Clear the first lock", false),
                    Achievement("a3", "Unbroken", "Win a Classic run", false),
                    Achievement("a4", "Daily mind", "Complete a Daily Challenge", false),
                ),
                quote = "Knowledge unites people.",
                statusRight = "PLAY  •  LEARN  •  GROW",
            )
        }
    }
}

fun PlayerProfile.toLobbyPlayer(): LobbyPlayer {
    val lvl = maxOf(level, XpRules.levelFor(xp))
    val span = 250
    val into = xp % span
    return LobbyPlayer(
        name = displayName.ifBlank { "GUEST" },
        avatarId = avatarId,
        level = lvl,
        xpProgress = into / span.toFloat(),
        xpInto = into,
        xpNeed = span,
        vip = lvl >= 10,
        coins = coins,
        gems = gems,
    )
}

fun formatCount(n: Int): String {
    val v = n.coerceAtLeast(0)
    return when {
        v < 1_000 -> v.toString()
        v < 100_000 -> shortCount(v, 1_000, "k")
        v < 10_000_000 -> shortCount(v, 100_000, "L")
        else -> shortCount(v, 10_000_000, "Cr")
    }
}

private fun shortCount(v: Int, unit: Int, suffix: String): String {
    val whole = v / unit
    val tenth = (v % unit) * 10 / unit
    return if (whole >= 10 || tenth == 0) "${whole}$suffix" else "${whole}.${tenth}$suffix"
}

fun formatRemain(endsAt: Long, now: Long): String {
    val sec = ((endsAt - now) / 1000L).coerceAtLeast(0L)
    val d = sec / 86_400L
    val h = (sec % 86_400L) / 3_600L
    return "${d}D ${h}H"
}
