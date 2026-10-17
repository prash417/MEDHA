package studio.paras.medha.domain.engine

import studio.paras.medha.domain.model.Achievement
import studio.paras.medha.domain.model.LifelineKind
import studio.paras.medha.domain.model.PlayerProfile

/**
 * Lifeline pricing. Every lifeline is free the first time in a run;
 * after that it costs coins. Dare stays once per run because it is a gamble.
 */
object LifelineShop {
    /** Hints are cheap, so a run gets a few for free. */
    const val FreeHints = 3
    const val HintPrice = 50

    fun price(kind: LifelineKind): Int = when (kind) {
        LifelineKind.FIFTY_FIFTY -> 200
        LifelineKind.SWITCH -> 250
        LifelineKind.EXTRA_TIME -> 150
        LifelineKind.PROTECT -> 300
        LifelineKind.AUDIENCE -> 150
        LifelineKind.EXPERT -> 200
        LifelineKind.DARE -> 0
    }

    fun repeatable(kind: LifelineKind): Boolean = kind != LifelineKind.DARE

    /** Coins the next use costs, or 0 when it is still free. */
    fun nextCost(kind: LifelineKind, usesThisRun: Int): Int =
        if (usesThisRun <= 0) 0 else price(kind)

    fun nextHintCost(hintsThisRun: Int): Int =
        if (hintsThisRun < FreeHints) 0 else HintPrice
}

/** Rewarded-ad payouts. All shop rows are "watch an ad, get a reward". */
enum class AdReward(
    val id: String,
    val title: String,
    val coins: Int,
    val gems: Int,
    /** How many times a day; 1 means a once-a-day bonus. */
    val dailyLimit: Int,
) {
    COINS("coins", "Watch ad", coins = 100, gems = 0, dailyLimit = 20),
    COIN_BONUS("coin_bonus", "Daily bonus ad", coins = 500, gems = 0, dailyLimit = 1),
    GEMS("gems", "Watch ad", coins = 0, gems = 1, dailyLimit = 10),
    GEM_BONUS("gem_bonus", "Daily bonus ad", coins = 0, gems = 5, dailyLimit = 1),
}

object AdLedger {
    /** Profile with ad counters reset if the stored day is not [today]. */
    fun fresh(profile: PlayerProfile, today: Long): PlayerProfile =
        if (profile.adDay == today) profile
        else profile.copy(adDay = today, adCoinsToday = 0, adGemsToday = 0)

    fun usedToday(profile: PlayerProfile, reward: AdReward, today: Long): Int {
        val p = fresh(profile, today)
        return when (reward) {
            AdReward.COINS -> p.adCoinsToday
            AdReward.GEMS -> p.adGemsToday
            AdReward.COIN_BONUS -> if (p.adCoinBonusDay == today) 1 else 0
            AdReward.GEM_BONUS -> if (p.adGemBonusDay == today) 1 else 0
        }
    }

    fun left(profile: PlayerProfile, reward: AdReward, today: Long): Int =
        (reward.dailyLimit - usedToday(profile, reward, today)).coerceAtLeast(0)

    /** Pays [reward] once, or returns null when today's limit is used up. */
    fun grant(profile: PlayerProfile, reward: AdReward, today: Long): PlayerProfile? {
        if (left(profile, reward, today) <= 0) return null
        val p = fresh(profile, today)
        val paid = p.copy(coins = p.coins + reward.coins, gems = p.gems + reward.gems)
        return when (reward) {
            AdReward.COINS -> paid.copy(adCoinsToday = p.adCoinsToday + 1)
            AdReward.GEMS -> paid.copy(adGemsToday = p.adGemsToday + 1)
            AdReward.COIN_BONUS -> paid.copy(adCoinBonusDay = today)
            AdReward.GEM_BONUS -> paid.copy(adGemBonusDay = today)
        }
    }
}

/** Diamonds can be traded for coins. */
object GemExchange {
    const val Gems = 5
    const val Coins = 1_000

    fun trade(profile: PlayerProfile): PlayerProfile? =
        if (profile.gems < Gems) null
        else profile.copy(gems = profile.gems - Gems, coins = profile.coins + Coins)
}

enum class MissionStat { GAMES, CORRECT, WINS, STREAK, DAILY_WINS, LEVEL }

data class MissionTrack(
    val key: String,
    val stat: MissionStat,
    val title: String,
    /** Detail line; `%d` is replaced with the tier target. */
    val detail: String,
    val targets: List<Int>,
    val coins: List<Int>,
    val gems: List<Int>,
)

data class Mission(
    val id: String,
    val title: String,
    val detail: String,
    val current: Int,
    val target: Int,
    val coins: Int,
    val gems: Int,
    val claimable: Boolean,
    /** Every tier of this track is claimed. */
    val finished: Boolean,
) {
    val progress: Float get() = if (target <= 0) 1f else (current.toFloat() / target).coerceIn(0f, 1f)
}

/**
 * Tiered missions read from lifetime stats. Each track shows its next
 * unclaimed tier; claiming pays out and moves the track to the next tier.
 */
object Missions {
    val tracks: List<MissionTrack> = listOf(
        MissionTrack(
            "games", MissionStat.GAMES, "Take the seat", "Play %d games",
            targets = listOf(1, 5, 15, 40, 100, 250),
            coins = listOf(100, 300, 600, 1_200, 2_500, 5_000),
            gems = listOf(0, 0, 2, 5, 10, 20),
        ),
        MissionTrack(
            "correct", MissionStat.CORRECT, "Sharp mind", "Answer %d questions correctly",
            targets = listOf(10, 50, 150, 400, 1_000, 2_500),
            coins = listOf(150, 400, 800, 1_500, 3_000, 6_000),
            gems = listOf(0, 1, 2, 5, 10, 25),
        ),
        MissionTrack(
            "wins", MissionStat.WINS, "Full ladder", "Clear every question in %d runs",
            targets = listOf(1, 5, 15, 40),
            coins = listOf(500, 1_500, 3_000, 6_000),
            gems = listOf(2, 5, 10, 25),
        ),
        MissionTrack(
            "streak", MissionStat.STREAK, "Daily habit", "Hold a %d-day login streak",
            targets = listOf(3, 7),
            coins = listOf(300, 1_000),
            gems = listOf(1, 5),
        ),
        MissionTrack(
            "daily", MissionStat.DAILY_WINS, "Daily champion", "Clear the Daily Challenge %d times",
            targets = listOf(1, 7, 30),
            coins = listOf(300, 1_500, 5_000),
            gems = listOf(1, 5, 20),
        ),
        MissionTrack(
            "level", MissionStat.LEVEL, "Climb the ranks", "Reach level %d",
            targets = listOf(2, 5, 10, 20),
            coins = listOf(200, 800, 2_000, 5_000),
            gems = listOf(1, 3, 10, 25),
        ),
    )

    fun statValue(profile: PlayerProfile, stat: MissionStat): Int = when (stat) {
        MissionStat.GAMES -> profile.gamesPlayed
        MissionStat.CORRECT -> profile.correctAnswers
        MissionStat.WINS -> profile.wins
        MissionStat.STREAK -> profile.dailyStreak
        MissionStat.DAILY_WINS -> profile.dailyWins
        MissionStat.LEVEL -> XpRules.levelFor(profile.xp)
    }

    fun id(track: MissionTrack, tier: Int): String = "${track.key}:$tier"

    fun current(profile: PlayerProfile): List<Mission> = tracks.map { track ->
        val claimed = profile.claimedMissions.toSet()
        val tier = track.targets.indices.firstOrNull { id(track, it) !in claimed }
        val value = statValue(profile, track.stat)
        if (tier == null) {
            val last = track.targets.lastIndex
            Mission(
                id = id(track, last),
                title = track.title,
                detail = track.detail.replace("%d", track.targets[last].toString()),
                current = value,
                target = track.targets[last],
                coins = 0,
                gems = 0,
                claimable = false,
                finished = true,
            )
        } else {
            val target = track.targets[tier]
            Mission(
                id = id(track, tier),
                title = track.title,
                detail = track.detail.replace("%d", target.toString()),
                current = value.coerceAtMost(target),
                target = target,
                coins = track.coins[tier],
                gems = track.gems[tier],
                claimable = value >= target,
                finished = false,
            )
        }
    }

    fun claimableCount(profile: PlayerProfile): Int = current(profile).count { it.claimable }

    /** Pays out mission [id] if it is ready, or returns null. */
    fun claim(profile: PlayerProfile, id: String): PlayerProfile? {
        val mission = current(profile).find { it.id == id && it.claimable } ?: return null
        return profile.copy(
            coins = profile.coins + mission.coins,
            gems = profile.gems + mission.gems,
            claimedMissions = profile.claimedMissions + mission.id,
        )
    }
}

object Achievements {
    fun all(profile: PlayerProfile): List<Achievement> = listOf(
        Achievement("first_seat", "First seat", "Play your first game.", profile.gamesPlayed >= 1),
        Achievement("ten", "Ten locks", "Answer 10 questions correctly.", profile.correctAnswers >= 10),
        Achievement("first_win", "Unbroken", "Clear every question in a run.", profile.wins >= 1),
        Achievement("hundred", "Knowledge master", "Answer 100 questions correctly.", profile.correctAnswers >= 100),
        Achievement("daily", "Daily mind", "Clear a Daily Challenge.", profile.dailyWins >= 1),
        Achievement("week", "Seven dawns", "Hold a 7-day login streak.", profile.dailyStreak >= 7),
        Achievement("crore", "₹1 Crore club", "Win the full Classic ladder.", profile.bestPrize >= 1_00_00_000L),
        Achievement("level10", "VIP", "Reach level 10.", XpRules.levelFor(profile.xp) >= 10),
    )
}

/** Coins a finished run pays. Practice pays nothing so it cannot be farmed. */
object RunRewards {
    const val DailyBonus = 500

    fun coins(
        mode: studio.paras.medha.domain.model.GameMode,
        prize: Long,
        completed: Boolean,
        profile: PlayerProfile,
        today: Long,
    ): Int {
        if (mode == studio.paras.medha.domain.model.GameMode.PRACTICE) return 0
        val base = XpRules.coinsFor(prize, completed)
        val daily = if (
            mode == studio.paras.medha.domain.model.GameMode.DAILY && completed && profile.lastDailyDay != today
        ) DailyBonus else 0
        return base + daily
    }
}
