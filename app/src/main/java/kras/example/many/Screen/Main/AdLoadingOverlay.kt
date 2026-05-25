package kras.example.many.Screen.Main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.MySpacer
import kras.example.many.MyTxt
import kotlinx.coroutines.delay

@Composable
fun AdLoadingOverlay(
    modifier: Modifier = Modifier,
    timeoutMillis: Long = 10000L,
    onTimeout: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "ad_loading")
    val pulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    var remainingSeconds by remember { mutableStateOf((timeoutMillis / 1000).toInt()) }
    
    LaunchedEffect(timeoutMillis) {
        val totalSeconds = (timeoutMillis / 1000).toInt()
        for (second in totalSeconds downTo 0) {
            delay(1000L)
            remainingSeconds = second
        }
        onTimeout()
    }

    Box(
        modifier = modifier
            .background(MyColor.BACKGROUND.copy(alpha = 0.96f))
            .pointerInteropFilter { true },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = MyColor.SECTION_TITLE)
                .clip(RoundedCornerShape(20.dp))
                .background(MyColor.BOX)
                .padding(horizontal = 32.dp, vertical = 28.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(52.dp),
                color = MyColor.SECTION_TITLE,
                strokeWidth = 4.dp
            )
            MySpacer(height = 18)
            MyTxt(
                text = "Загрузка рекламы",
                fontSize = 20,
                fontWeight = 600,
                modifier = Modifier.alpha(pulse)
            )
            MySpacer(height = 6)
            MyTxt(
                text = "Подождите немного…",
                fontSize = 13,
                color = MyColor.TITLE.copy(alpha = 0.5f)
            )
            MySpacer(height = 12)
            MyTxt(
                text = "${remainingSeconds} сек",
                fontSize = 14,
                fontWeight = 500,
                color = MyColor.SECTION_TITLE.copy(alpha = 0.7f)
            )
        }
    }
}
