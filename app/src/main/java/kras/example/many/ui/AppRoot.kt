package kras.example.many.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    CALC("Расчёт", Ic.Glass),
    HISTORY("История", Ic.History),
    SETTINGS("Настройки", Ic.Sliders),
}

@Composable
fun AppRoot(activity: Activity) {
    val c = Ui.c
    var tab by rememberSaveable { mutableStateOf(Tab.CALC) }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to c.bgGlow, 0.45f to c.bg, 1f to c.bg))) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            AnimatedContent(
                targetState = tab,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (fadeIn() + slideInHorizontally { dir * it / 5 }) togetherWith fadeOut()
                },
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
            StickyBanner(Modifier.padding(top = 6.dp))
            BottomBar(tab) { tab = it }
        }
        if (AppStore.calculating) CalculatingOverlay()
        ToastHost(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 8.dp))
    }

    if (!AppStore.disclaimerAccepted) {
        InfoDialog(
            title = "Добро пожаловать 🥂",
            text = "Приложение рассчитывает примерную концентрацию алкоголя в крови по формуле Видмарка.\n\n" +
                "Это не медицинский прибор и не алкотестер. Не используйте результат как разрешение садиться за руль.",
            confirm = "Принимаю",
            onConfirm = AppStore::acceptDisclaimer,
            onDismiss = {},
        )
    }
}

@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    val c = Ui.c
    val shape = RoundedCornerShape(28.dp)
    Row(
        Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(68.dp)
            .shadow(16.dp, shape, ambientColor = c.accent, spotColor = c.accent)
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.text.copy(alpha = 0.06f), shape)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tab.entries.forEach { t ->
            val active = t == selected
            val bg by animateColorAsState(if (active) c.accent else c.surface, label = "nav")
            val fg = if (active) c.onAccent else c.textDim
            Column(
                Modifier
                    .weight(if (active) 1.6f else 1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(bg)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(t) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                Icon(t.icon, t.title, tint = fg, modifier = Modifier.size(22.dp))
                if (active) T(t.title, 11.sp, FontWeight.Bold, fg, maxLines = 1)
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
            delay(1800)
            AppStore.toast = null
        }
    }
    AnimatedVisibility(
        visible = msg != null,
        modifier = modifier,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
    ) {
        Box(
            Modifier
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(c.surfaceHigh)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) { T(last, 14.sp, FontWeight.SemiBold) }
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
        "${type.emoji} ${type.title}: ${list.size} шт, ${fmtVolume(list.sumOf { it.volumeMl })}"
    }
    val text = buildString {
        appendLine("🥂 Алкотестер — мой прогноз")
        appendLine()
        appendLine(drinks)
        appendLine()
        appendLine("Сейчас: ${fmtPromille(f.current)} ‰ (${BacEngine.stateOf(f.current).title})")
        appendLine("Пик: ${fmtPromille(f.peak)} ‰ в ${fmtDayTime(f.peakMs)}")
        f.driveMs?.let { appendLine("Ниже лимита: ${fmtDayTime(it)}") }
        f.soberMs?.let { appendLine("Полная трезвость: ${fmtDayTime(it)}") }
        appendLine()
        append("Скачать: $STORE_URL")
    }
    shareText(activity, text)
}
