package kras.example.many.ads

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

const val INTERSTITIAL_AD_UNIT_ID = "R-M-2581205-1"

/**
 * Полноэкранная реклама Yandex (Interstitial).
 * Загружается при изменении [key] (например, по нажатию «Рассчитать»).
 * [onFinished] вызывается после закрытия рекламы или при любой ошибке.
 */
@Composable
fun YandexFullscreenAd(
    key: Any,
    adUnitId: String = INTERSTITIAL_AD_UNIT_ID,
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity

    if (activity == null || activity.isDestroyed || activity.isFinishing) {
        LaunchedEffect(key) { onFinished() }
        return
    }

    var interstitialAd by remember(key) { mutableStateOf<InterstitialAd?>(null) }
    var isLoading by remember(key) { mutableStateOf(false) }
    var isLoaded by remember(key) { mutableStateOf(false) }
    var isFinished by remember(key) { mutableStateOf(false) }

    fun finish() {
        if (isFinished) return
        isFinished = true
        interstitialAd?.setAdEventListener(null)
        interstitialAd = null
        onFinished()
    }

    fun loadAd() {
        if (isLoading || isLoaded || isFinished) return
        isLoading = true

        val loader = InterstitialAdLoader(context)
        loader.loadAd(
            AdRequest.Builder(adUnitId).build(),
            object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    isLoaded = true
                    interstitialAd = ad

                    ad.setAdEventListener(object : InterstitialAdEventListener {
                        override fun onAdShown() = Unit

                        override fun onAdFailedToShow(
                            adError: com.yandex.mobile.ads.common.AdError
                        ) {
                            isLoaded = false
                            finish()
                        }

                        override fun onAdDismissed() {
                            isLoaded = false
                            finish()
                        }

                        override fun onAdClicked() = Unit

                        override fun onAdImpression(impressionData: ImpressionData?) = Unit
                    })
                }

                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    isLoading = false
                    finish()
                }
            }
        )
    }

    DisposableEffect(key) {
        loadAd()
        onDispose {
            interstitialAd?.setAdEventListener(null)
            interstitialAd = null
        }
    }

    LaunchedEffect(isLoaded, key) {
        if (
            isLoaded &&
            !isFinished &&
            !activity.isFinishing &&
            !activity.isDestroyed
        ) {
            try {
                interstitialAd?.show(activity)
            } catch (_: Exception) {
                finish()
            }
        }
    }
}
