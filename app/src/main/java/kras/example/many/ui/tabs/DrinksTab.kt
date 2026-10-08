package kras.example.many.ui.tabs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.core.AppStore
import kras.example.many.core.BacEngine
import kras.example.many.core.DrinkEntry
import kras.example.many.core.DrinkType
import kras.example.many.core.Forecast
import kras.example.many.core.Stomach
import kras.example.many.ui.Ic
import kras.example.many.ui.InfoDialog
import kras.example.many.ui.Panel
import kras.example.many.ui.PillGroup
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.RoundIconButton
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.Sheet
import kras.example.many.ui.Stepper
import kras.example.many.ui.T
import kras.example.many.ui.fmtDayTime
import kras.example.many.ui.fmtPromille
import kras.example.many.ui.fmtVolume
import kras.example.many.ui.theme.Ui
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DrinksTab(forecast: Forecast, nowMs: Long, onOpenForecast: () -> Unit) {
    var addType by remember { mutableStateOf<DrinkType?>(null) }
    var showSession by remember { mutableStateOf(false) }
    var confirmFinish by remember { mutableStateOf(false) }
    val drinks = AppStore.drinks

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        LiveCard(forecast, drinks, onOpenForecast)
        Box(Modifier.size(14.dp))
        SectionLabel("Что пьём?")
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DrinkType.entries.chunked(2).forEach { row ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { type ->
                        DrinkTile(type, drinks.count { it.type == type }, Modifier.weight(1f).fillMaxHeight()) {
                            addType = type
                        }
                    }
                }
            }
        }
        Box(Modifier.size(14.dp))
        SectionLabel("Закуска")
        PillGroup(
            items = Stomach.entries,
            selected = AppStore.stomach,
            label = { "${it.emoji} ${it.title}" },
            onSelect = AppStore::updateStomach,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton(
                "Сессия · ${drinks.size}", { showSession = true }, Modifier.weight(1f), Ic.List, secondary = true,
            )
            PrimaryButton(
                "Завершить", { if (drinks.isEmpty()) AppStore.toast = "Сессия пуста" else confirmFinish = true },
                Modifier.weight(1f), Ic.Flag,
            )
        }
        Box(Modifier.size(12.dp))
    }

    addType?.let { type -> AddDrinkSheet(type, nowMs) { addType = null } }
    if (showSession) SessionSheet(forecast) { showSession = false }
    if (confirmFinish) {
        InfoDialog(
            title = "Завершить сессию?",
            text = "Напитки будут сохранены в историю, а счётчик начнётся заново.",
            confirm = "Завершить",
            dismissText = "Отмена",
            onConfirm = { AppStore.finishSession(System.currentTimeMillis()) },
            onDismiss = { confirmFinish = false },
        )
    }
}

@Composable
private fun LiveCard(forecast: Forecast, drinks: List<DrinkEntry>, onClick: () -> Unit) {
    val c = Ui.c
    val state = BacEngine.stateOf(forecast.current)
    val color = c.level(state.level)
    val progress by animateFloatAsState((forecast.current / 3f).coerceIn(0f, 1f), label = "ring")
    Panel(Modifier.fillMaxWidth(), onClick = onClick, padding = PaddingValues(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                T("Сейчас в крови", 12.sp, color = c.textDim)
                Row(verticalAlignment = Alignment.Bottom) {
                    T(fmtPromille(forecast.current), 46.sp, FontWeight.Black)
                    T(" ‰", 20.sp, FontWeight.Bold, c.textDim, Modifier.padding(bottom = 8.dp))
                }
                Box(
                    Modifier.clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) { T(state.title, 12.sp, FontWeight.Bold, color) }
            }
            Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                val track = c.surfaceHigh
                Canvas(Modifier.fillMaxSize()) {
                    val sw = 10.dp.toPx()
                    val arc = Size(size.width - sw, size.height - sw)
                    val tl = Offset(sw / 2, sw / 2)
                    drawArc(track, 135f, 270f, false, tl, arc, style = Stroke(sw, cap = StrokeCap.Round))
                    drawArc(color, 135f, 270f * progress, false, tl, arc, style = Stroke(sw, cap = StrokeCap.Round))
                }
                T(if (drinks.isEmpty()) "🫗" else drinks.last().type.emoji, 30.sp)
            }
        }
        Box(Modifier.size(10.dp))
        val grams = drinks.sumOf { it.grams.toDouble() }.roundToInt()
        T(
            if (drinks.isEmpty()) "Выберите напиток ниже — прогноз построится сам"
            else "${drinks.size} шт · ${fmtVolume(drinks.sumOf { it.volumeMl })} · $grams г спирта  ›  прогноз",
            12.sp, color = c.textDim, maxLines = 1,
        )
    }
}

@Composable
private fun DrinkTile(type: DrinkType, count: Int, modifier: Modifier, onClick: () -> Unit) {
    val c = Ui.c
    val (ml, abv) = AppStore.lastFor(type)
    Box(modifier) {
        Panel(Modifier.fillMaxSize(), onClick = onClick, padding = PaddingValues(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(type.emoji, 30.sp)
                Box(Modifier.size(10.dp))
                Column {
                    T(type.title, 16.sp, FontWeight.Bold, maxLines = 1)
                    T("${fmtVolume(ml)} · ${abvText(abv)}", 12.sp, color = c.textDim, maxLines = 1)
                }
            }
        }
        if (count > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp).size(22.dp).clip(CircleShape).background(c.accent),
                contentAlignment = Alignment.Center,
            ) { T("$count", 11.sp, FontWeight.Black, c.onAccent) }
        }
    }
}

private fun abvText(abv: Float) = String.format(Locale.forLanguageTag("ru"), "%.1f%%", abv).replace(",0%", "%")

private val timeOffsets = listOf(0, 15, 30, 60, 120)

@Composable
private fun AddDrinkSheet(type: DrinkType, nowMs: Long, onDismiss: () -> Unit) {
    val c = Ui.c
    val last = AppStore.lastFor(type)
    var ml by remember { mutableIntStateOf(last.first) }
    var abv by remember { mutableFloatStateOf(last.second) }
    var offset by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(1) }
    val gramsOne = DrinkEntry(0, type, ml, abv, 0).grams

    Sheet(onDismiss, "${type.emoji}  ${type.title}") {
        SectionLabel("Объём порции")
        PillGroup(type.presetsMl, ml, { fmtVolume(it) }, { ml = it }, Modifier.fillMaxWidth())
        Box(Modifier.size(12.dp))
        Stepper(
            value = fmtVolume(ml),
            caption = "${gramsOne.roundToInt()} г чистого спирта",
            onMinus = { ml = (ml - type.stepMl).coerceAtLeast(type.stepMl) },
            onPlus = { ml = (ml + type.stepMl).coerceAtMost(3000) },
        )
        Box(Modifier.size(18.dp))
        SectionLabel("Крепость · ${abvText(abv)}")
        Slider(
            value = abv,
            onValueChange = { abv = (it * 2).roundToInt() / 2f },
            valueRange = type.minAbv..type.maxAbv,
            colors = SliderDefaults.colors(
                thumbColor = c.accent, activeTrackColor = c.accent, inactiveTrackColor = c.surfaceHigh,
            ),
        )
        Box(Modifier.size(12.dp))
        SectionLabel("Когда выпито")
        PillGroup(timeOffsets, offset, { if (it == 0) "Сейчас" else if (it < 60) "−$it м" else "−${it / 60} ч" }, { offset = it }, Modifier.fillMaxWidth())
        Box(Modifier.size(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundIconButton(Ic.Minus, { count = (count - 1).coerceAtLeast(1) })
            T("×$count", 20.sp, FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp))
            RoundIconButton(Ic.Plus, { count = (count + 1).coerceAtMost(10) })
            Box(Modifier.size(12.dp))
            PrimaryButton(
                "Добавить",
                {
                    AppStore.addDrinks(type, ml, abv, nowMs - offset * 60_000L, count)
                    onDismiss()
                },
                Modifier.weight(1f), Ic.Plus,
            )
        }
    }
}

@Composable
private fun SessionSheet(forecast: Forecast, onDismiss: () -> Unit) {
    val c = Ui.c
    val drinks = AppStore.drinks.sortedByDescending { it.timeMs }
    Sheet(onDismiss, "Текущая сессия") {
        if (drinks.isEmpty()) {
            T("Пока ничего не выпито 🙂", 15.sp, color = c.textDim, modifier = Modifier.padding(vertical = 24.dp))
        } else {
        T("Пик ${fmtPromille(forecast.peak)} ‰ · трезвость ${forecast.soberMs?.let { fmtDayTime(it) } ?: "—"}", 13.sp, color = c.textDim)
        Box(Modifier.size(12.dp))
        LazyColumn(Modifier.heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(drinks, key = { it.id }) { d ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surfaceHigh)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    T(d.type.emoji, 24.sp)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        T("${d.type.title} · ${fmtVolume(d.volumeMl)}", 15.sp, FontWeight.SemiBold)
                        T("${abvText(d.abv)} · ${d.grams.roundToInt()} г · ${fmtDayTime(d.timeMs)}", 12.sp, color = c.textDim)
                    }
                    RoundIconButton(Ic.Trash, { AppStore.removeDrink(d.id) }, size = 36.dp, bg = c.bad.copy(alpha = 0.12f), tint = c.bad)
                }
            }
        }
        }
    }
}
