package kras.example.many.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

object AdIds {
    /** Полноэкранный блок (уже существующий в кабинете). */
    const val INTERSTITIAL = "R-M-2581205-1"
    /** TODO: создайте блок «Баннер» в кабинете Яндекс Рекламной сети и подставьте его ID. */
    const val BANNER = "R-M-2581205-3"
}

/** Межстраничная реклама: грузится заранее и показывается перед результатом расчёта. */
object Interstitial {
    private const val RETRY_MS = 30_000L
    private const val WAIT_MS = 5_000L
    private const val POLL_MS = 250L
    private var loader: InterstitialAdLoader? = null
    private var ad: InterstitialAd? = null
    private var loading = false
    private val handler = Handler(Looper.getMainLooper())

    fun preload(context: Context) {
        if (loading || ad != null) return
        loading = true
        val app = context.applicationContext
        val l = loader ?: InterstitialAdLoader(app).also { loader = it }
        l.loadAd(AdRequest.Builder(AdIds.INTERSTITIAL).build(), object : InterstitialAdLoadListener {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                loading = false
                ad = interstitialAd
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                loading = false
                handler.postDelayed({ preload(app) }, RETRY_MS)
            }
        })
    }

    /**
     * Показывает рекламу и вызывает [onDone] после её закрытия.
     * Если реклама не успела загрузиться за [WAIT_MS] или не показалась — [onDone] вызывается сразу.
     */
    fun showThen(activity: Activity, onDone: () -> Unit) {
        var finished = false
        val finish = {
            if (!finished) {
                finished = true
                onDone()
            }
        }
        preload(activity)
        val deadline = System.currentTimeMillis() + WAIT_MS
        val poll = object : Runnable {
            override fun run() {
                val ready = ad
                when {
                    activity.isFinishing || activity.isDestroyed -> finish()
                    ready != null -> display(activity, ready, finish)
                    System.currentTimeMillis() > deadline -> finish()
                    else -> handler.postDelayed(this, POLL_MS)
                }
            }
        }
        poll.run()
    }

    private fun display(activity: Activity, current: InterstitialAd, finish: () -> Unit) {
        ad = null
        current.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() = Unit
            override fun onAdFailedToShow(adError: AdError) {
                finish()
                preload(activity)
            }
            override fun onAdDismissed() {
                current.setAdEventListener(null)
                finish()
                preload(activity)
            }
            override fun onAdClicked() = Unit
            override fun onAdImpression(impressionData: ImpressionData?) = Unit
        })
        runCatching { current.show(activity) }.onFailure { finish(); preload(activity) }
    }
}

/** Липкий адаптивный баннер на всю ширину экрана. */
@Composable
fun StickyBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    val view = remember(widthDp) {
        BannerAdView(context).apply {
            setAdSize(BannerAdSize.sticky(context, widthDp))
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() = Unit
                override fun onAdFailedToLoad(error: AdRequestError) = Unit
                override fun onAdClicked() = Unit
                override fun onImpression(impressionData: ImpressionData?) = Unit
            })
            loadAd(AdRequest.Builder(AdIds.BANNER).build())
        }
    }
    DisposableEffect(view) { onDispose { view.destroy() } }
    AndroidView(factory = { view }, modifier = modifier.fillMaxWidth())
}
