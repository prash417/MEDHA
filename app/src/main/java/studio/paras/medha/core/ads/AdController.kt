package studio.paras.medha.core.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.lang.ref.WeakReference

/**
 * Rewarded ads pay out coins and diamonds in the shop.
 * Interstitials only at natural breaks — never during a live question.
 */
interface AdController {
    fun preload()

    /**
     * Shows a rewarded ad. [onReward] runs only when the player watched it through.
     * [onUnavailable] runs when no ad could be shown (offline, still loading, no fill).
     */
    fun showRewarded(onReward: () -> Unit, onUnavailable: () -> Unit = {})
    fun showInterstitialIfNaturalBreak()

    /** The activity ads are shown over; pass null when it pauses. */
    fun attach(activity: Activity?) = Unit
}

/** Grants rewards straight away. Handy on emulators without Play services. */
class NoOpAds : AdController {
    override fun preload() = Unit
    override fun showRewarded(onReward: () -> Unit, onUnavailable: () -> Unit) = onReward()
    override fun showInterstitialIfNaturalBreak() = Unit
}

/**
 * Google AdMob rewarded ads.
 *
 * The ids come from BuildConfig and default to Google's public TEST ids, which always
 * serve a test ad. Before release, put your own AdMob app id and rewarded unit id in
 * app/build.gradle.kts (see ADMOB_APP_ID and ADMOB_REWARDED_ID there).
 */
class AdMobAds(
    context: Context,
    private val rewardedUnitId: String,
) : AdController {
    private val app = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var activity: WeakReference<Activity>? = null
    private var rewarded: RewardedAd? = null
    private var loading = false
    @Volatile private var initialized = false

    init {
        Thread {
            runCatching {
                MobileAds.initialize(app) {
                    initialized = true
                    main.post { preload() }
                }
            }.onFailure { Log.e(TAG, "MobileAds init failed", it) }
        }.start()
    }

    override fun attach(activity: Activity?) {
        this.activity = activity?.let { WeakReference(it) }
        if (activity != null) preload()
    }

    override fun preload() {
        if (!initialized || loading || rewarded != null) return
        loading = true
        main.post {
            runCatching {
                RewardedAd.load(
                    app,
                    rewardedUnitId,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            loading = false
                            rewarded = ad
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            loading = false
                            rewarded = null
                            Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                        }
                    },
                )
            }.onFailure {
                loading = false
                Log.e(TAG, "Rewarded load crashed", it)
            }
        }
    }

    override fun showRewarded(onReward: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewarded
        val host = activity?.get()
        if (ad == null || host == null || host.isFinishing) {
            preload()
            onUnavailable()
            return
        }
        rewarded = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Rewarded ad failed to show: ${error.message}")
                preload()
                onUnavailable()
            }
        }
        ad.show(host) { _ -> onReward() }
    }

    override fun showInterstitialIfNaturalBreak() = Unit

    private companion object {
        const val TAG = "MedhaAds"
    }
}
