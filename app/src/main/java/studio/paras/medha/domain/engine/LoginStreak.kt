package studio.paras.medha.domain.engine

import studio.paras.medha.domain.model.PlayerProfile
import java.time.LocalDate

object LoginStreak {
    val dayNames = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val dayCards = arrayOf("mon.png", "tue.png", "wed.png", "thu.png", "fri.png", "sat.png", "sun.png")
    const val GrandSundayCard = "Sunday continue 7 days.png"

    /** Amounts printed on the weekday cards. Index 0 is Monday. */
    val dayCoins = intArrayOf(500, 0, 750, 1_000, 0, 0, 2_000)
    val dayGems = intArrayOf(0, 5, 0, 0, 10, 15, 0)
    const val PerfectSundayGems = 50
    private const val ClaimIntervalMillis = 24L * 60 * 60 * 1_000
    private val dailyBonuses = intArrayOf(100, 150, 200, 250, 300, 400, 600)

    data class Week(
        val anchor: Long,
        val mask: Int,
        val broken: Boolean,
        val todayIndex: Int,
        val joinIndex: Int,
        val lastClaimAtMillis: Long = 0,
    )

    fun mondayOf(today: Long): Long {
        val dow = LocalDate.ofEpochDay(today).dayOfWeek.value
        return today - (dow - 1)
    }

    fun indexOf(today: Long): Int = LocalDate.ofEpochDay(today).dayOfWeek.value - 1

    fun week(profile: PlayerProfile, today: Long): Week {
        val monday = mondayOf(today)
        val todayIndex = indexOf(today)
        if (profile.streakAnchorDay != monday) {
            return Week(monday, 0, false, todayIndex, todayIndex, profile.streakLastClaimAtMillis)
        }
        val join = profile.streakJoinIndex.takeIf { it in 0..6 } ?: todayIndex
        val missed = (join until todayIndex).any { profile.streakMask and (1 shl it) == 0 }
        return Week(monday, profile.streakMask, missed, todayIndex, join, profile.streakLastClaimAtMillis)
    }

    fun ensure(profile: PlayerProfile, today: Long): PlayerProfile {
        val live = week(profile, today)
        if (profile.streakAnchorDay != live.anchor) {
            return profile.copy(
                streakAnchorDay = live.anchor,
                streakJoinIndex = live.joinIndex,
                streakMask = 0,
                streakBroken = false,
                dailyStreak = 0,
            )
        }
        if (profile.streakJoinIndex !in 0..6) {
            return profile.copy(streakJoinIndex = live.joinIndex)
        }
        return profile
    }

    fun claimed(week: Week, day: Int): Boolean = week.mask and (1 shl day) != 0

    fun nextClaimAt(week: Week): Long =
        if (week.lastClaimAtMillis > 0) week.lastClaimAtMillis + ClaimIntervalMillis else 0

    fun canClaim(week: Week, nowMillis: Long): Boolean =
        week.todayIndex >= week.joinIndex &&
            !claimed(week, week.todayIndex) &&
            nowMillis >= nextClaimAt(week)

    fun hasReducedRewards(week: Week): Boolean = week.joinIndex > 0

    /** Seven increasing daily bonuses add up to 2,000 coins. */
    fun bonusFor(week: Week): Int =
        if (hasReducedRewards(week)) 0
        else dailyBonuses[Integer.bitCount(week.mask and 0x7f) % dailyBonuses.size]

    /** Sunday stays special until a daily claim is missed. */
    fun showGrandSunday(week: Week): Boolean = !week.broken

    /** Award the grand reward only after all six weekday rewards have been claimed. */
    fun grandSunday(week: Week): Boolean =
        week.joinIndex == 0 &&
            !week.broken &&
            (week.mask and ((1 shl 6) - 1)) == (1 shl 6) - 1

    fun cardFor(week: Week, day: Int): String {
        if (day == 6 && showGrandSunday(week)) return GrandSundayCard
        return dayCards[day]
    }

    fun coinsFor(week: Week, day: Int): Int {
        if (day !in week.joinIndex..6) return 0
        if (hasReducedRewards(week)) return if (dayCoins[day] > 0) 100 else 0
        return dayCoins[day]
    }

    fun gemsFor(week: Week, day: Int): Int {
        if (day !in week.joinIndex..6) return 0
        if (hasReducedRewards(week)) return if (dayGems[day] > 0) 1 else 0
        if (day == 6 && grandSunday(week)) return PerfectSundayGems
        return dayGems[day]
    }

    @JvmOverloads
    fun claim(profile: PlayerProfile, today: Long, nowMillis: Long = System.currentTimeMillis()): PlayerProfile? {
        val live = week(profile, today)
        val day = live.todayIndex
        if (!canClaim(live, nowMillis)) return null
        val streakDays = if (live.broken) 0 else day - live.joinIndex + 1
        return profile.copy(
            coins = profile.coins + coinsFor(live, day) + bonusFor(live),
            gems = profile.gems + gemsFor(live, day),
            streakAnchorDay = live.anchor,
            streakJoinIndex = live.joinIndex,
            streakMask = live.mask or (1 shl day),
            streakBroken = live.broken,
            streakLastClaimAtMillis = nowMillis,
            dailyStreak = streakDays,
        )
    }
}
