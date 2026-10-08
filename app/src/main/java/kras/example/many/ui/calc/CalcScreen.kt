package kras.example.many.ui.calc

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.ads.Interstitial
import kras.example.many.core.AppStore
import kras.example.many.core.Forecast
import kras.example.many.ui.Ic
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.T
import kras.example.many.ui.theme.Ui

private data class StepInfo(val title: String, val hint: String)

private val steps = listOf(
    StepInfo("Что пили?", "Нажмите на напиток и укажите объём"),
    StepInfo("Когда пили?", "Время и закуска влияют на результат"),
    StepInfo("О вас", "Пол, вес и рост для точного расчёта"),
    StepInfo("Результат", "Обновляется каждые 30 секунд"),
)

@Composable
fun CalcScreen(activity: Activity, onShare: (Forecast) -> Unit) {
    val step = AppStore.step

    Column(Modifier.fillMaxSize()) {
        StepHeader(step)
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { dir * it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -dir * it / 3 } + fadeOut())
            },
            label = "steps",
        ) { s ->
            when (s) {
                AppStore.STEP_DRINKS -> StepDrinks()
                AppStore.STEP_TIME -> StepTime()
                AppStore.STEP_PROFILE -> StepProfile()
                else -> StepResult(onShare)
            }
        }
        if (step < AppStore.STEP_RESULT) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (step > AppStore.STEP_DRINKS) {
                    PrimaryButton("Назад", { AppStore.step = step - 1 }, Modifier.weight(0.7f), Ic.Back, secondary = true)
                }
                if (step < AppStore.STEP_PROFILE) {
                    PrimaryButton("Далее", {
                        if (step == AppStore.STEP_DRINKS && AppStore.draft.isEmpty()) {
                            AppStore.toast = "Сначала добавьте хотя бы один напиток"
                        } else {
                            AppStore.step = step + 1
                        }
                    }, Modifier.weight(1f), Ic.Next)
                } else {
                    PrimaryButton("Рассчитать", {
                        AppStore.calculating = true
                        Interstitial.showThen(activity) {
                            AppStore.calculating = false
                            AppStore.calculate(System.currentTimeMillis())
                        }
                    }, Modifier.weight(1f), Ic.Pulse)
                }
            }
        }
    }
}

@Composable
private fun StepHeader(step: Int) {
    val c = Ui.c
    val info = steps[step]
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 14.dp)) {
        if (step < AppStore.STEP_RESULT) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { i ->
                    val color by animateColorAsState(if (i <= step) c.accent else c.surfaceHigh, label = "progress")
                    Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
                }
                T("  ${step + 1} из 3", 12.sp, FontWeight.Bold, c.textDim)
            }
            Box(Modifier.size(10.dp))
        }
        T(info.title, 30.sp, FontWeight.Black)
        T(info.hint, 13.sp, color = c.textDim)
    }
}

/** Затемнение «Считаем…» на время загрузки/показа рекламы. */
@Composable
fun CalculatingOverlay() {
    val c = Ui.c
    Box(Modifier.fillMaxSize().background(c.bg.copy(alpha = 0.92f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = c.accent, strokeWidth = 5.dp, modifier = Modifier.size(56.dp))
            Box(Modifier.size(16.dp))
            T("Считаем…", 20.sp, FontWeight.Bold)
        }
    }
}
