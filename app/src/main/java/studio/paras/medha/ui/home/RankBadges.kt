package studio.paras.medha.ui.home

import studio.paras.medha.R

object RankBadges {
    fun drawableFor(level: Int): Int {
        val tier = (level / 10).coerceIn(0, 10)
        return when (tier) {
            0 -> R.drawable.vip
            1 -> R.drawable.rank_seeker
            2 -> R.drawable.rank_learner
            3 -> R.drawable.rank_thinker
            4 -> R.drawable.rank_scholar
            5 -> R.drawable.rank_mastermind
            6 -> R.drawable.rank_strategist
            7 -> R.drawable.rank_genius
            8 -> R.drawable.rank_sage
            9 -> R.drawable.rank_legend
            else -> R.drawable.rank_max
        }
    }

    fun messageFor(level: Int): String = when (level) {
        1 -> "First spark"
        2 -> "Hunger wakes"
        3 -> "Lock clicks"
        4 -> "Mind on fire"
        5 -> "Seat taken"
        6 -> "No turning"
        7 -> "Gold in sight"
        8 -> "Quiet thunder"
        9 -> "Nine lives in"
        else -> when ((level / 10).coerceIn(1, 10)) {
            1 -> "Path of light"
            2 -> "Lessons burn"
            3 -> "Thoughts deepen"
            4 -> "Ink and gold"
            5 -> "Puzzle king"
            6 -> "Every move"
            7 -> "A rare mind"
            8 -> "Halls go still"
            9 -> "Myth walks in"
            else -> "Last lock"
        }
    }
    fun labelFor(level: Int): String {
        val tier = (level / 10).coerceIn(0, 10)
        return when (tier) {
            0 -> "VIP"
            1 -> "Seeker"
            2 -> "Learner"
            3 -> "Thinker"
            4 -> "Scholar"
            5 -> "Mastermind"
            6 -> "Strategist"
            7 -> "Genius"
            8 -> "Sage"
            9 -> "Legend"
            else -> "Max"
        }
    }
}
