package kras.example.many.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kras.example.many.ads.StickyBanner
import kras.example.many.core.AppStore
import kras.example.many.core.BacEngine
import kras.example.many.core.Forecast
import kras.example.many.ui.calc.CalcScreen
import kras.example.many.ui.calc.CalculatingOverlay
import kras.example.many.ui.tabs.HistoryTab
import kras.example.many.ui.tabs.SettingsTab
import kras.example.many.ui.theme.Ui

const val STORE_URL = "https://www.rustore.ru/catalog/app/kras.example.many"

enum class Tab(val title: String, val icon: ImageVector) {
    CALC("Тест", Ic.Glass),
    HISTORY("История", Ic.History),
    SETTINGS("Настройки", Ic.Sliders),
}

@Composable
fun AppRoot(activity: Activity) {
    val c = Ui.c
    var tab by rememberSaveable { mutableStateOf(Tab.CALC) }

    BackHandler(enabled = AppStore.onboarded &&
        (AppStore.calculating || tab != Tab.CALC || AppStore.step != AppStore.STEP_START)) {
        when {
            AppStore.calculating -> Unit
            tab != Tab.CALC -> tab = Tab.CALC
            AppStore.step == AppStore.STEP_RESULT -> AppStore.step = AppStore.STEP_START
            AppStore.step > AppStore.STEP_START -> AppStore.step--
        }
    }

    Box(Modifier.fillMaxSize().background(c.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            AnimatedContent(
                targetState = AppStore.onboarded,
                modifier = Modifier.weight(1f),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding",
            ) { done ->
                if (!done) {
                    Onboarding()
                } else {
                    AnimatedContent(
                        targetState = tab,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tabs",
                    ) { t ->
                        when (t) {
                            Tab.CALC -> CalcScreen(activity) { shareForecast(activity, it) }
                            Tab.HISTORY -> HistoryTab {
                                AppStore.newCalculation()
                                tab = Tab.CALC
                            }
                            Tab.SETTINGS -> SettingsTab(onShareApp = { shareText(activity, "Алкотестер — считает промилле и время до трезвости:\n$STORE_URL") }) {
                                runCatching { activity.startActivity(Intent(Intent.ACTION_VIEW, STORE_URL.toUri())) }
                            }
                        }
                    }
                }
            }
            StickyBanner(Modifier.padding(top = 8.dp))
            if (AppStore.onboarded) BottomBar(tab) { tab = it } else Box(Modifier.navigationBarsPadding())
        }
        if (AppStore.calculating) CalculatingOverlay()
        ToastHost(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 8.dp))
    }

    if (AppStore.justOnboarded) {
        InfoDialog(
            title = "Готово",
            text = "Данные сохранены. Изменить их можно позже во вкладке «Настройки».\n\n" +
                "Расчёт ориентировочный и не заменяет алкотестер. Не используйте его как разрешение садиться за руль.",
            confirm = "Начать",
        ) { AppStore.justOnboarded = false }
    }
}

@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    val c = Ui.c
    Column(Modifier.fillMaxWidth().background(c.surface).navigationBarsPadding()) {
        HorizontalDivider(color = c.line)
        Row(Modifier.fillMaxWidth().height(60.dp)) {
            Tab.entries.forEach { t ->
                val active = t == selected
                val color = if (active) c.accent else c.textDim
                Column(
                    Modifier.weight(1f).height(60.dp).padding(horizontal = 6.dp, vertical = 6.dp)
                        .clip(ControlShape).background(if (active) c.surfaceHigh else c.surface)
                        .clickable { onSelect(t) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(t.icon, null, tint = color, modifier = Modifier.size(22.dp))
                    T(t.title, 12.sp, if (active) FontWeight.SemiBold else FontWeight.Normal, color, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun ToastHost(modifier: Modifier) {
    val c = Ui.c
    val msg = AppStore.toast
    var last by remember { mutableStateOf("") }
    if (msg != null) last = msg
    LaunchedEffect(msg) {
        if (msg != null) {
            delay(2000)
            AppStore.toast = null
        }
    }
    AnimatedVisibility(visible = msg != null, modifier = modifier, enter = fadeIn(), exit = fadeOut()) {
        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .clip(ControlShape)
                .background(c.text)
                .border(1.dp, c.text, ControlShape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) { T(last, 14.sp, FontWeight.Medium, c.bg) }
    }
}

private fun shareText(activity: Activity, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching { activity.startActivity(Intent.createChooser(intent, "Поделиться")) }
}

private fun shareForecast(activity: Activity, f: Forecast) {
    val drinks = AppStore.result.groupBy { it.type }.entries.joinToString("\n") { (type, list) ->
        "${type.title}: ${list.size} шт., ${fmtVolume(list.sumOf { it.volumeMl })}"
    }
    val text = buildString {
        appendLine("Алкотестер — мой результат")
        appendLine()
        appendLine(drinks)
        appendLine()
        appendLine("Сейчас: ${fmtPromille(f.current)} ‰ (${BacEngine.stateOf(f.current).title})")
        appendLine("Пик: ${fmtPromille(f.peak)} ‰ около ${fmtDayTime(f.peakMs)}")
        f.driveMs?.let { appendLine("Ниже лимита: ${fmtDayTime(it)}") }
        f.soberMs?.let { appendLine("Полная трезвость: ${fmtDayTime(it)}") }
        appendLine()
        append("Скачать: $STORE_URL")
    }
    shareText(activity, text)
}
