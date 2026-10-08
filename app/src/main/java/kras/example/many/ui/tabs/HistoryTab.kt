package kras.example.many.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.core.AppStore
import kras.example.many.core.BacEngine
import kras.example.many.core.SessionRecord
import kras.example.many.ui.Ic
import kras.example.many.ui.InfoDialog
import kras.example.many.ui.Panel
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.Sheet
import kras.example.many.ui.StatTile
import kras.example.many.ui.T
import kras.example.many.ui.fmtDate
import kras.example.many.ui.fmtDayTime
import kras.example.many.ui.fmtPromille
import kras.example.many.ui.fmtVolume
import kras.example.many.ui.theme.Ui
import kotlin.math.roundToInt

private const val VISIBLE_ROWS = 3

@Composable
fun HistoryTab(onAddDrink: () -> Unit) {
    val history = AppStore.history
    if (history.isEmpty()) {
        EmptyState("🗂️", "История пуста", "Завершите сессию на вкладке «Бар» — она сохранится здесь со статистикой.", "К напиткам", onAddDrink)
        return
    }
    val c = Ui.c
    var detail by remember { mutableStateOf<SessionRecord?>(null) }
    var showAll by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var tip by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Ic.History, "Сессий", "${history.size}", "всего", c.accent) { tip = "Сохраняются последние 30 сессий." }
            val avg = history.map { it.peak }.average().toFloat()
            StatTile(Ic.Pulse, "Средний пик", "${fmtPromille(avg)} ‰", BacEngine.stateOf(avg).title, c.level(BacEngine.stateOf(avg).level)) {
                tip = "Средняя пиковая концентрация по всем сохранённым сессиям."
            }
            val max = history.maxOf { it.peak }
            StatTile(Ic.Peak, "Рекорд", "${fmtPromille(max)} ‰", fmtDate(history.first { it.peak == max }.startMs), c.accent2) {
                tip = "Самая высокая пиковая концентрация. Береги себя 🙏"
            }
        }
        Panel(Modifier.fillMaxWidth().weight(1f), padding = PaddingValues(14.dp)) {
            T("Пики последних сессий", 15.sp, FontWeight.Bold)
            Bars(history.take(8).reversed(), Modifier.fillMaxWidth().weight(1f).padding(top = 10.dp)) { detail = it }
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Недавние", Modifier.weight(1f))
                if (history.size > VISIBLE_ROWS) {
                    T("Все (${history.size}) ›", 12.sp, FontWeight.Bold, c.accent, Modifier.padding(bottom = 8.dp).clickable { showAll = true })
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                history.take(VISIBLE_ROWS).forEach { HistoryRow(it) { detail = it } }
            }
        }
        Box(Modifier.size(4.dp))
    }

    detail?.let { DetailSheet(it) { detail = null } }
    if (showAll) {
        Sheet({ showAll = false }, "Все сессии") {
            LazyColumn(Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { r -> HistoryRow(r) { showAll = false; detail = r } }
            }
            Box(Modifier.size(12.dp))
            PrimaryButton("Очистить историю", { confirmClear = true }, Modifier.fillMaxWidth(), Ic.Trash, danger = true)
        }
    }
    if (confirmClear) {
        InfoDialog(
            "Очистить историю?", "Все сохранённые сессии будут удалены без возможности восстановления.",
            onDismiss = { confirmClear = false }, confirm = "Удалить", dismissText = "Отмена",
            onConfirm = { AppStore.clearHistory(); showAll = false },
        )
    }
    tip?.let { InfoDialog("Статистика", it) { tip = null } }
}

@Composable
private fun Bars(items: List<SessionRecord>, modifier: Modifier, onClick: (SessionRecord) -> Unit) {
    val c = Ui.c
    val max = maxOf(items.maxOf { it.peak }, 0.5f)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { r ->
            Column(Modifier.weight(1f).clickable { onClick(r) }, horizontalAlignment = Alignment.CenterHorizontally) {
                T(fmtPromille(r.peak), 10.sp, FontWeight.Bold, maxLines = 1)
                val color = c.level(BacEngine.stateOf(r.peak).level)
                Canvas(Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp)) {
                    val h = (r.peak / max).coerceIn(0.03f, 1f) * size.height
                    val w = size.width.coerceAtMost(28.dp.toPx())
                    drawRoundRect(
                        color, Offset((size.width - w) / 2, size.height - h), Size(w, h), CornerRadius(8.dp.toPx()),
                    )
                }
                T(fmtDate(r.startMs).take(6), 9.sp, color = c.textDim, maxLines = 1, align = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun HistoryRow(r: SessionRecord, onClick: () -> Unit) {
    val c = Ui.c
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surface).clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T(r.emojis.ifEmpty { "🍸" }, 20.sp, maxLines = 1)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            T(fmtDayTime(r.startMs), 14.sp, FontWeight.SemiBold, maxLines = 1)
            T("${r.drinks} шт · ${fmtVolume(r.totalMl)}", 12.sp, color = c.textDim, maxLines = 1)
        }
        T("${fmtPromille(r.peak)} ‰", 15.sp, FontWeight.Bold, c.level(BacEngine.stateOf(r.peak).level))
        Icon(Ic.Chevron, null, tint = c.textDim, modifier = Modifier.padding(start = 6.dp).size(16.dp))
    }
}

@Composable
private fun DetailSheet(r: SessionRecord, onDismiss: () -> Unit) {
    val c = Ui.c
    val state = BacEngine.stateOf(r.peak)
    Sheet(onDismiss, "${r.emojis}  ${fmtDate(r.startMs)}") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Ic.Peak, "Пик", "${fmtPromille(r.peak)} ‰", state.title, c.level(state.level)) {}
            StatTile(Ic.Glass, "Напитков", "${r.drinks}", fmtVolume(r.totalMl), c.accent) {}
            StatTile(Ic.Pulse, "Спирта", "${r.grams.roundToInt()} г", "чистого", c.accent2) {}
        }
        Box(Modifier.size(14.dp))
        T("Первый напиток: ${fmtDayTime(r.startMs)}\nПоследний: ${fmtDayTime(r.endMs)}", 14.sp, color = c.textDim)
        Box(Modifier.size(8.dp))
        T(state.hint, 14.sp, color = c.textDim)
    }
}
