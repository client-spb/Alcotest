package kras.example.many.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.core.AppStore
import kras.example.many.core.BacEngine
import kras.example.many.core.SessionRecord
import kras.example.many.ui.EmptyState
import kras.example.many.ui.InfoDialog
import kras.example.many.ui.Panel
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.ScreenHeader
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.Sheet
import kras.example.many.ui.StatTile
import kras.example.many.ui.Sym
import kras.example.many.ui.T
import kras.example.many.ui.fmtDate
import kras.example.many.ui.fmtDayTime
import kras.example.many.ui.fmtPromille
import kras.example.many.ui.fmtVolume
import kras.example.many.ui.theme.Ui
import kotlin.math.roundToInt

private const val VISIBLE_ROWS = 3

@Composable
fun HistoryTab(onNewTest: () -> Unit) {
    val history = AppStore.history
    val c = Ui.c
    var detail by remember { mutableStateOf<SessionRecord?>(null) }
    var showAll by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("История", "Все ваши расчёты")
        if (history.isEmpty()) {
            EmptyState("Пока пусто", "Каждый расчёт автоматически сохраняется здесь.", "Начать тест", onNewTest)
        } else {
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Расчётов", "${history.size}", "всего") {}
                    val avg = history.map { it.peak }.average().toFloat()
                    StatTile("Средний пик", "${fmtPromille(avg)} ‰", BacEngine.stateOf(avg).title) {}
                    val max = history.maxOf { it.peak }
                    StatTile("Максимум", "${fmtPromille(max)} ‰", fmtDate(history.first { it.peak == max }.startMs)) {}
                }
                Panel(Modifier.fillMaxWidth().weight(1f), padding = PaddingValues(14.dp)) {
                    T("Пиковые значения", 14.sp, FontWeight.Medium)
                    Bars(history.take(8).reversed(), Modifier.fillMaxWidth().weight(1f).padding(top = 10.dp)) { detail = it }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("Последние", Modifier.weight(1f))
                        T("Все (${history.size})", 13.sp, FontWeight.Medium, c.accent, Modifier.padding(bottom = 8.dp).clickable { showAll = true })
                    }
                    Panel(Modifier.fillMaxWidth(), padding = PaddingValues(0.dp)) {
                        history.take(VISIBLE_ROWS).forEachIndexed { i, r ->
                            if (i > 0) HorizontalDivider(color = c.line)
                            HistoryRow(r) { detail = r }
                        }
                    }
                }
                Box(Modifier.size(2.dp))
            }
        }
    }

    detail?.let { DetailSheet(it) { detail = null } }
    if (showAll) {
        Sheet({ showAll = false }, "Все расчёты") {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                itemsIndexed(history) { i, r ->
                    if (i > 0) HorizontalDivider(color = c.line)
                    HistoryRow(r) { showAll = false; detail = r }
                }
            }
            Box(Modifier.size(16.dp))
            PrimaryButton("Очистить историю", { confirmClear = true }, Modifier.fillMaxWidth(), danger = true)
        }
    }
    if (confirmClear) {
        InfoDialog(
            "Очистить историю?", "Все сохранённые расчёты будут удалены.",
            confirm = "Удалить", dismissText = "Отмена",
            onConfirm = { AppStore.clearHistory(); showAll = false },
        ) { confirmClear = false }
    }
}

@Composable
private fun Bars(items: List<SessionRecord>, modifier: Modifier, onClick: (SessionRecord) -> Unit) {
    val c = Ui.c
    val max = maxOf(items.maxOf { it.peak }, 0.5f)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { r ->
            Column(Modifier.weight(1f).clickable { onClick(r) }, horizontalAlignment = Alignment.CenterHorizontally) {
                T(fmtPromille(r.peak), 10.sp, FontWeight.Medium, maxLines = 1)
                val color = c.level(BacEngine.stateOf(r.peak).level)
                Canvas(Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp)) {
                    val h = (r.peak / max).coerceIn(0.03f, 1f) * size.height
                    val w = size.width.coerceAtMost(20.dp.toPx())
                    drawRect(color, Offset((size.width - w) / 2, size.height - h), Size(w, h))
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
        Modifier.fillMaxWidth().height(56.dp).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            T(fmtDayTime(r.startMs), 14.sp, FontWeight.Medium, maxLines = 1)
            T("${r.kinds} · ${fmtVolume(r.totalMl)}", 12.sp, color = c.textDim, maxLines = 1)
        }
        T("${fmtPromille(r.peak)} ‰", 15.sp, FontWeight.SemiBold, c.level(BacEngine.stateOf(r.peak).level))
        Icon(Sym.Chevron, null, tint = c.textDim, modifier = Modifier.padding(start = 4.dp).size(20.dp))
    }
}

@Composable
private fun DetailSheet(r: SessionRecord, onDismiss: () -> Unit) {
    val c = Ui.c
    val state = BacEngine.stateOf(r.peak)
    Sheet(onDismiss, "Расчёт ${fmtDate(r.startMs)}") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Пик", "${fmtPromille(r.peak)} ‰", state.title, c.level(state.level)) {}
            StatTile("Порций", "${r.drinks}", fmtVolume(r.totalMl)) {}
            StatTile("Спирта", "${r.grams.roundToInt()} г", "чистого") {}
        }
        Box(Modifier.size(14.dp))
        T("Напитки: ${r.kinds}", 14.sp)
        T("Начали: ${fmtDayTime(r.startMs)}, закончили: ${fmtDayTime(r.endMs)}", 14.sp, color = c.textDim, modifier = Modifier.padding(top = 4.dp))
    }
}
