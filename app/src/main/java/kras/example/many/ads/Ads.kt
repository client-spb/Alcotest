package kras.example.many.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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

/** Размер слота известен до загрузки: появление рекламы не меняет высоту экрана. */
@Composable
fun StickyBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(1)
        key(context, widthDp) {
            val size = remember { BannerAdSize.sticky(context, widthDp) }
            val slotHeight = with(density) { size.getHeightInPixels(context).toDp() }
            var loaded by remember { mutableStateOf(false) }
            val opacity by animateFloatAsState(
                targetValue = if (loaded) 1f else 0f,
                animationSpec = tween(450),
                label = "bannerAppearance",
            )
            val view = remember { BannerAdView(context).apply { setAdSize(size) } }
            DisposableEffect(view) {
                view.setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() { loaded = true }
                    override fun onAdFailedToLoad(error: AdRequestError) { loaded = false }
                    override fun onAdClicked() = Unit
                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                view.loadAd(AdRequest.Builder(AdIds.BANNER).build())
                onDispose {
                    view.setBannerAdEventListener(null)
                    view.destroy()
                }
            }
            // Слот остаётся и при ошибке загрузки, чтобы контент не прыгал обратно.
            Box(Modifier.fillMaxWidth().height(slotHeight), contentAlignment = Alignment.Center) {
                AndroidView(
                    factory = { view },
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = opacity },
                )
            }
        }
    }
}
