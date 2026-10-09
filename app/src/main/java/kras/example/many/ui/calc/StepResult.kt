package kras.example.many.ui.calc

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kras.example.many.core.AppStore
import kras.example.many.core.BacEngine
import kras.example.many.core.Forecast
import kras.example.many.ui.InfoDialog
import kras.example.many.ui.ControlShape
import kras.example.many.ui.Panel
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.StatTile
import kras.example.many.ui.T
import kras.example.many.ui.fmtDayTime
import kras.example.many.ui.fmtDuration
import kras.example.many.ui.fmtPromille
import kras.example.many.ui.fmtTime
import kras.example.many.ui.theme.Ui

private enum class Tip { PEAK, DRIVE, SOBER }

@Composable
fun StepResult(onShare: (Forecast) -> Unit) {
    val c = Ui.c
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            nowMs = System.currentTimeMillis()
        }
    }
    val forecast = remember(AppStore.result, AppStore.profile, AppStore.stomach, nowMs) {
        BacEngine.compute(AppStore.result, AppStore.profile, AppStore.stomach, nowMs)
    }
    var tip by remember { mutableStateOf<Tip?>(null) }
    val limit = AppStore.profile.driveLimit
    val state = BacEngine.stateOf(forecast.current)
    val stateColor = c.level(state.level)

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Panel(Modifier.fillMaxWidth()) {
            T("КОНЦЕНТРАЦИЯ · ${fmtTime(nowMs)}", 12.sp, FontWeight.Medium, c.textDim)
            Row(verticalAlignment = Alignment.Bottom) {
                T(fmtPromille(forecast.current), 36.sp, FontWeight.SemiBold)
                T(" ‰", 18.sp, color = c.textDim, modifier = Modifier.padding(bottom = 8.dp))
            }
            LevelBar(forecast.current)
            T("${state.title}. ${state.hint}", 13.sp, FontWeight.Medium, stateColor, modifier = Modifier.padding(top = 8.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Пик", "${fmtPromille(forecast.peak)} ‰", "около ${fmtTime(forecast.peakMs)}") { tip = Tip.PEAK }
            val drive = forecast.driveMs
            val canDrive = drive == null || drive <= nowMs
            StatTile(
                "За руль",
                if (canDrive) "Можно*" else fmtTime(drive!!),
                if (canDrive) "ниже $limit ‰" else "через ${fmtDuration(drive!! - nowMs)}",
                if (canDrive) c.good else c.warn,
            ) { tip = Tip.DRIVE }
            val sober = forecast.soberMs
            StatTile(
                "Трезвость",
                if (sober == null) "—" else if (sober <= nowMs) "Уже" else fmtTime(sober),
                if (sober == null || sober <= nowMs) "0,00 ‰" else "через ${fmtDuration(sober - nowMs)}",
            ) { tip = Tip.SOBER }
        }
        Panel(Modifier.fillMaxWidth().weight(1f), padding = PaddingValues(14.dp)) {
            T("Почасовой прогноз", 14.sp, FontWeight.SemiBold)
            T("От текущего времени до полного выведения", 12.sp, color = c.textDim)
            HourlyList(forecast, nowMs, Modifier.fillMaxWidth().weight(1f).padding(top = 10.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton("Поделиться", { onShare(forecast) }, Modifier.weight(1f), secondary = true)
            PrimaryButton("Новый расчёт", AppStore::newCalculation, Modifier.weight(1f))
        }
        T("* Расчёт ориентировочный и не заменяет алкотестер", 11.sp, color = c.textDim, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }

    when (tip) {
        Tip.PEAK -> InfoDialog("Пиковая концентрация", "Максимум ${fmtPromille(forecast.peak)} ‰ — около ${fmtDayTime(forecast.peakMs)}. Чем плотнее закуска, тем ниже и позже пик.") { tip = null }
        Tip.DRIVE -> InfoDialog("Когда за руль", "Время, когда уровень опустится ниже лимита $limit ‰. Лимит меняется в настройках. Всегда проверяйтесь алкотестером.") { tip = null }
        Tip.SOBER -> InfoDialog("Полная трезвость", "Средняя скорость выведения — ${BacEngine.ELIMINATION_PER_HOUR} ‰ в час.") { tip = null }
        null -> Unit
    }
}

/** Горизонтальная шкала 0–3 ‰ с отметками уровней. */
@Composable
private fun LevelBar(value: Float) {
    val c = Ui.c
    val fraction by animateFloatAsState((value / 3f).coerceIn(0f, 1f), label = "level")
    val color = c.level(BacEngine.stateOf(value).level)
    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(c.surfaceHigh)) {
        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(color))
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        listOf("0", "1", "2", "3 ‰").forEachIndexed { i, s ->
            T(s, 11.sp, color = c.textDim, modifier = Modifier.weight(1f), align = if (i == 3) TextAlign.End else TextAlign.Start)
        }
    }
}

@Composable
private fun HourlyList(f: Forecast, nowMs: Long, modifier: Modifier) {
    val c = Ui.c
    val rows = remember(f, nowMs) { f.hourlyFrom(nowMs) }
    val scroll = rememberLazyListState()
    Row(modifier) {
        LazyColumn(Modifier.weight(1f).clip(ControlShape), state = scroll) {
            itemsIndexed(rows, key = { index, _ -> index }) { index, point ->
                Row(
                    Modifier.fillMaxWidth().background(if (index == 0) c.surfaceHigh else c.surface)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        T(fmtDayTime(point.timeMs), 14.sp, FontWeight.Medium, maxLines = 1)
                        T(if (index == 0) "Сейчас" else if (point.timeMs == f.soberMs) "Полное выведение" else "+${index} ч",
                            11.sp, color = c.textDim, maxLines = 1)
                    }
                    T("${fmtPromille(point.promille)} ‰", 17.sp, FontWeight.SemiBold)
                }
                if (index < rows.lastIndex) HorizontalDivider(color = c.line)
            }
        }
        // Постоянная полоса прокрутки только внутри окна прогноза.
        Canvas(Modifier.padding(start = 8.dp).width(3.dp).fillMaxHeight()
            .semantics { contentDescription = "Полоса прокрутки почасового прогноза" }) {
            val info = scroll.layoutInfo
            if ((scroll.canScrollForward || scroll.canScrollBackward) && info.visibleItemsInfo.isNotEmpty()) {
                drawRoundRect(c.line, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width))
                val rowHeight = info.visibleItemsInfo.first().size.coerceAtLeast(1)
                val totalHeight = info.totalItemsCount * rowHeight.toFloat()
                val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
                val thumb = (size.height * viewport / totalHeight).coerceIn(18.dp.toPx().coerceAtMost(size.height), size.height)
                val offset = (scroll.firstVisibleItemIndex * rowHeight + scroll.firstVisibleItemScrollOffset).toFloat()
                val top = (size.height - thumb) * (offset / (totalHeight - viewport).coerceAtLeast(1f)).coerceIn(0f, 1f)
                drawRoundRect(c.textDim, topLeft = Offset(0f, top), size = androidx.compose.ui.geometry.Size(size.width, thumb),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width))
            }
        }
    }
}
