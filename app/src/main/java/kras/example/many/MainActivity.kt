package kras.example.many

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import kras.example.many.ads.Interstitial
import kras.example.many.core.AppStore
import kras.example.many.ui.AppRoot
import kras.example.many.ui.theme.AppTheme
import kras.example.many.ui.theme.isDark

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        AppStore.init(this)
        Interstitial.preload(this)
        enableEdgeToEdge()

        setContent {
            val dark = AppStore.theme.isDark()
            LaunchedEffect(dark) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            AppTheme(dark) { AppRoot(this) }
        }
    }
}
