package kras.example.many

import android.content.pm.ActivityInfo
import android.content.Context
import android.content.res.Configuration
import android.os.Build
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

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration).apply {
            fontScale = 1f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) fontWeightAdjustment = 0
        }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

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
