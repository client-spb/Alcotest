package kras.example.many

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import kras.example.many.Color.MyColor
import kras.example.many.Screen.Main.Drinks.DrinkParamsStorage
import kras.example.many.Screen.Main.UserParamsStorage

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)

        MyColor.init(this)
        UserParamsStorage.init(this)
        DrinkParamsStorage.init(this)

        enableEdgeToEdge()

        setContent {
            val isLightTheme = MyColor.isLightTheme()

            LaunchedEffect(isLightTheme) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = isLightTheme
                controller.isAppearanceLightNavigationBars = isLightTheme
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MyColor.BACKGROUND),
                containerColor = Color.Transparent
            ) { innerPadding ->
                App(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun App(modifier: Modifier) {
    NavBar(modifier = modifier)
}
