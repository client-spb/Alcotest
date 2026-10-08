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
import kras.example.many.ui.Panel
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.T
import kras.example.many.ui.theme.Ui

@Composable
fun CalcScreen(activity: Activity, onShare: (Forecast) -> Unit) {
    val step = AppStore.step

    Column(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { dir * it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -dir * it / 3 } + fadeOut())
            },
            label = "steps",
        ) { s ->
            Column(Modifier.fillMaxSize()) {
                when (s) {
                    AppStore.STEP_START -> StartScreen()
                    AppStore.STEP_DRINKS -> {
                        StepHeader(1, "Что пили?", "Нажмите на напиток, укажите крепость и количество")
                        Box(Modifier.weight(1f)) { StepDrinks() }
                    }
                    AppStore.STEP_TIME -> {
                        StepHeader(2, "Когда пили?", "Время и закуска влияют на результат")
                        Box(Modifier.weight(1f)) { StepTime() }
                    }
                    else -> {
                        StepHeader(null, "Результат", "Обновляется каждые 30 секунд")
                        Box(Modifier.weight(1f)) { StepResult(onShare) }
                    }
                }
            }
        }
        if (step == AppStore.STEP_DRINKS || step == AppStore.STEP_TIME) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PrimaryButton("Назад", { AppStore.step = step - 1 }, Modifier.weight(1f), secondary = true)
                if (step == AppStore.STEP_DRINKS) {
                    PrimaryButton("Продолжить", {
                        if (AppStore.draft.isEmpty()) AppStore.toast = "Сначала добавьте хотя бы один напиток"
                        else AppStore.step = AppStore.STEP_TIME
                    }, Modifier.weight(1f))
                } else {
                    PrimaryButton("Рассчитать", {
                        AppStore.calculating = true
                        Interstitial.showThen(activity) {
                            AppStore.calculating = false
                            AppStore.calculate(System.currentTimeMillis())
                        }
                    }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StartScreen() {
    val c = Ui.c
    val p = AppStore.profile
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        T("Алкотестер", 32.sp, FontWeight.SemiBold)
        T(
            "Узнайте, сколько промилле в крови сейчас, когда можно за руль и когда алкоголь полностью выведется.",
            15.sp, color = c.textDim, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        Panel(Modifier.fillMaxWidth()) {
            T("Ваши данные", 13.sp, color = c.textDim)
            T(
                "${if (p.male) "Мужчина" else "Женщина"}, ${p.age} лет, ${p.weightKg} кг, ${p.heightCm} см",
                15.sp, FontWeight.Medium, modifier = Modifier.padding(top = 2.dp),
            )
            T("Изменить можно в настройках", 12.sp, color = c.textDim, modifier = Modifier.padding(top = 2.dp))
        }
        Box(Modifier.size(24.dp))
        PrimaryButton("Начать тест", AppStore::newCalculation, Modifier.fillMaxWidth())
        if (AppStore.result.isNotEmpty()) {
            Box(Modifier.size(10.dp))
            PrimaryButton("Последний результат", { AppStore.step = AppStore.STEP_RESULT }, Modifier.fillMaxWidth(), secondary = true)
        }
    }
}

@Composable
private fun StepHeader(number: Int?, title: String, hint: String) {
    val c = Ui.c
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)) {
        if (number != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(2) { i ->
                    val color by animateColorAsState(if (i < number) c.accent else c.line, label = "progress")
                    Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(color))
                }
            }
            T("Шаг $number из 2", 12.sp, color = c.textDim, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        }
        T(title, 24.sp, FontWeight.SemiBold)
        T(hint, 13.sp, color = c.textDim)
    }
}

/** Затемнение «Считаем…» на время загрузки и показа рекламы. */
@Composable
fun CalculatingOverlay() {
    val c = Ui.c
    Box(Modifier.fillMaxSize().background(c.bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = c.accent, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
            Box(Modifier.size(16.dp))
            T("Считаем…", 16.sp, FontWeight.Medium)
        }
    }
}
