package studio.paras.medha.core.ads

/**
 * AdMob is wired in Phase 5. Rewarded ads may grant a bonus lifeline.
 * Interstitials only at natural breaks — never during a live question.
 */
interface AdController {
    fun preload()
    fun showRewarded(onReward: () -> Unit)
    fun showInterstitialIfNaturalBreak()
}

class NoOpAds : AdController {
    override fun preload() = Unit
    override fun showRewarded(onReward: () -> Unit) = Unit
    override fun showInterstitialIfNaturalBreak() = Unit
}
