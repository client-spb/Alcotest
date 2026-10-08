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
    const val BANNER = "demo-banner-yandex"
}

/** Межстраничная реклама: заранее грузится, показывается не чаще раза в [COOLDOWN_MS]. */
object Interstitial {
    private const val COOLDOWN_MS = 3 * 60_000L
    private const val RETRY_MS = 60_000L
    private var loader: InterstitialAdLoader? = null
    private var ad: InterstitialAd? = null
    private var loading = false
    private var lastShownMs = 0L
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

    /** Показывает рекламу, если она готова и прошло достаточно времени. */
    fun maybeShow(activity: Activity) {
        val now = System.currentTimeMillis()
        val current = ad ?: return preload(activity)
        if (now - lastShownMs < COOLDOWN_MS || activity.isFinishing || activity.isDestroyed) return
        ad = null
        lastShownMs = now
        current.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() = Unit
            override fun onAdFailedToShow(adError: AdError) { preload(activity) }
            override fun onAdDismissed() {
                current.setAdEventListener(null)
                preload(activity)
            }
            override fun onAdClicked() = Unit
            override fun onAdImpression(impressionData: ImpressionData?) = Unit
        })
        runCatching { current.show(activity) }.onFailure { preload(activity) }
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
