package kras.example.many.ui.calc

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kras.example.many.core.AppStore
import kras.example.many.core.BacEngine
import kras.example.many.core.Forecast
import kras.example.many.ui.Ic
import kras.example.many.ui.InfoDialog
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

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Gauge(forecast.current, Modifier.fillMaxWidth().weight(0.9f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Ic.Peak, "Пик", "${fmtPromille(forecast.peak)} ‰", fmtTime(forecast.peakMs), c.accent2) { tip = Tip.PEAK }
            val drive = forecast.driveMs
            val canDrive = drive == null || drive <= nowMs
            StatTile(
                Ic.Car, "За руль",
                if (canDrive) "Можно*" else fmtDayTime(drive!!),
                if (canDrive) "ниже $limit ‰" else "через ${fmtDuration(drive!! - nowMs)}",
                if (canDrive) c.good else c.warn,
            ) { tip = Tip.DRIVE }
            val sober = forecast.soberMs
            StatTile(
                Ic.Moon, "Трезвость",
                if (sober == null) "—" else if (sober <= nowMs) "Уже" else fmtDayTime(sober),
                if (sober == null || sober <= nowMs) "0.00 ‰" else "через ${fmtDuration(sober - nowMs)}",
                c.accent,
            ) { tip = Tip.SOBER }
        }
        Panel(Modifier.fillMaxWidth().weight(1f), padding = PaddingValues(14.dp)) {
            T("Как будет снижаться", 15.sp, FontWeight.Bold)
            T("пунктир — лимит для вождения $limit ‰", 11.sp, color = c.textDim)
            if (!forecast.isEmpty) Chart(forecast, nowMs, limit, Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton("Поделиться", { onShare(forecast) }, Modifier.weight(1f), Ic.Share, secondary = true)
            PrimaryButton("Новый расчёт", AppStore::newCalculation, Modifier.weight(1f), Ic.Refresh)
        }
        T(
            "* Расчёт ориентировочный и не заменяет алкотестер",
            10.sp, color = c.textDim, align = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
    }

    when (tip) {
        Tip.PEAK -> InfoDialog("Пиковая концентрация", "Максимум ${fmtPromille(forecast.peak)} ‰ — около ${fmtDayTime(forecast.peakMs)}. Чем плотнее закуска, тем ниже и позже пик.") { tip = null }
        Tip.DRIVE -> InfoDialog("Когда за руль", "Время, когда уровень опустится ниже лимита $limit ‰. Лимит меняется в «Настройках». Всегда проверяйтесь алкотестером.") { tip = null }
        Tip.SOBER -> InfoDialog("Полная трезвость", "Средняя скорость выведения — ${BacEngine.ELIMINATION_PER_HOUR} ‰ в час.") { tip = null }
        null -> Unit
    }
}

@Composable
private fun Gauge(value: Float, modifier: Modifier) {
    val c = Ui.c
    val state = BacEngine.stateOf(value)
    val color = c.level(state.level)
    val anim by animateFloatAsState((value / 3f).coerceIn(0f, 1f), tween(900), label = "gauge")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val sw = 18.dp.toPx()
            val d = minOf(size.width, size.height * 1.25f) - sw
            val tl = Offset((size.width - d) / 2, sw / 2)
            val sz = Size(d, d)
            drawArc(c.surfaceHigh, 160f, 220f, false, tl, sz, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(
                Brush.sweepGradient(listOf(c.good, c.warn, c.bad, c.good), Offset(tl.x + d / 2, tl.y + d / 2)),
                160f, 220f * anim, false, tl, sz, style = Stroke(sw, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T("сейчас в крови", 12.sp, color = c.textDim)
            T(fmtPromille(value), 56.sp, FontWeight.Black)
            T("промилле", 12.sp, color = c.textDim)
            Box(Modifier.size(6.dp))
            T(state.title, 18.sp, FontWeight.Bold, color)
            T(state.hint, 12.sp, color = c.textDim, align = TextAlign.Center)
        }
    }
}

@Composable
private fun Chart(f: Forecast, nowMs: Long, limit: Float, modifier: Modifier) {
    val c = Ui.c
    val reveal = remember(f.points.size) { Animatable(0f) }
    LaunchedEffect(reveal) { reveal.animateTo(1f, tween(800)) }
    val start = f.points.first().timeMs
    val end = f.points.last().timeMs
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val maxY = maxOf(f.peak, limit) * 1.2f + 0.05f
            val span = (end - start).coerceAtLeast(1L).toFloat()
            fun x(t: Long) = (t - start) / span * size.width
            fun y(v: Float) = size.height - v / maxY * size.height * reveal.value

            val line = Path()
            f.points.forEachIndexed { i, p -> if (i == 0) line.moveTo(x(p.timeMs), y(p.promille)) else line.lineTo(x(p.timeMs), y(p.promille)) }
            val fill = Path().apply {
                addPath(line)
                lineTo(size.width, size.height); lineTo(0f, size.height); close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(c.accent.copy(alpha = 0.35f), c.accent.copy(alpha = 0f))))
            drawPath(line, c.accent, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))

            val ly = y(limit)
            drawLine(c.warn, Offset(0f, ly), Offset(size.width, ly), 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)))

            if (nowMs in start..end) {
                val nx = x(nowMs)
                drawLine(c.text.copy(alpha = 0.4f), Offset(nx, 0f), Offset(nx, size.height), 1.dp.toPx())
                drawCircle(c.accent2, 6.dp.toPx(), Offset(nx, y(f.current)))
                drawCircle(c.surface, 3.dp.toPx(), Offset(nx, y(f.current)))
            }
        }
        Box(Modifier.height(6.dp))
        Row {
            T(fmtTime(start), 11.sp, color = c.textDim, modifier = Modifier.weight(1f))
            T("сейчас ${fmtTime(nowMs)}", 11.sp, FontWeight.SemiBold, c.accent2, Modifier.weight(1f), TextAlign.Center)
            T(fmtTime(end), 11.sp, color = c.textDim, modifier = Modifier.weight(1f), align = TextAlign.End)
        }
    }
}
