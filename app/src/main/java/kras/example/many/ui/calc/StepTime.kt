package kras.example.many.ui.calc

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.core.AppStore
import kras.example.many.core.Stomach
import kras.example.many.ui.CardShape
import kras.example.many.ui.Ic
import kras.example.many.ui.Panel
import kras.example.many.ui.PillGroup
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.Sheet
import kras.example.many.ui.Stepper
import kras.example.many.ui.T
import kras.example.many.ui.fmtDuration
import kras.example.many.ui.fmtMinutes
import kras.example.many.ui.theme.Ui
import java.time.LocalTime

private enum class Edge { START, END }

@Composable
fun StepTime() {
    val c = Ui.c
    var editing by remember { mutableStateOf<Edge?>(null) }
    val duration = ((AppStore.endMin - AppStore.startMin) + 24 * 60) % (24 * 60)

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeTile("Начали пить", AppStore.startMin, Modifier.weight(1f)) { editing = Edge.START }
            TimeTile("Закончили", AppStore.endMin, Modifier.weight(1f)) { editing = Edge.END }
        }
        T(
            if (duration == 0) "Всё выпито за раз" else "Длительность застолья: ${fmtDuration(duration * 60_000L)}",
            13.sp, color = c.textDim, align = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        )
        SectionLabel("Чем закусывали?")
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Stomach.entries.forEach { s ->
                StomachTile(s, s == AppStore.stomach, Modifier.weight(1f)) { AppStore.updateStomach(s) }
            }
        }
        Box(Modifier.size(4.dp))
    }

    editing?.let { edge ->
        TimeSheet(
            title = if (edge == Edge.START) "Начали пить" else "Закончили пить",
            initial = if (edge == Edge.START) AppStore.startMin else AppStore.endMin,
            onSave = { if (edge == Edge.START) AppStore.updateStart(it) else AppStore.updateEnd(it) },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun TimeTile(label: String, min: Int, modifier: Modifier, onClick: () -> Unit) {
    val c = Ui.c
    Panel(modifier, onClick = onClick, padding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Ic.Clock, null, tint = c.accent, modifier = Modifier.size(18.dp))
            T(label, 12.sp, color = c.textDim, modifier = Modifier.padding(start = 6.dp))
        }
        T(fmtMinutes(min), 40.sp, FontWeight.Black)
        T("изменить ›", 12.sp, FontWeight.SemiBold, c.accent)
    }
}

private val stomachHints = mapOf(
    Stomach.EMPTY to "Почти ничего не ели — алкоголь всасывается быстро",
    Stomach.SNACK to "Лёгкие закуски, бутерброды, салаты",
    Stomach.FULL to "Сытный ужин — пик ниже и наступает позже",
)

@Composable
private fun StomachTile(s: Stomach, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Ui.c
    val bg by animateColorAsState(if (active) c.accent else c.surface, label = "stomach")
    val fg = if (active) c.onAccent else c.text
    Row(
        modifier.fillMaxWidth().clip(CardShape).background(bg).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T(s.emoji, 30.sp)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            T(s.title, 17.sp, FontWeight.Bold, fg)
            T(stomachHints.getValue(s), 12.sp, color = if (active) fg.copy(alpha = 0.8f) else c.textDim, maxLines = 2)
        }
        if (active) Icon(Ic.Check, null, tint = fg, modifier = Modifier.size(22.dp))
    }
}

private val quick = listOf(0, 60, 120, 180)

@Composable
private fun TimeSheet(title: String, initial: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    var v by remember { mutableIntStateOf(initial) }
    fun set(x: Int) { v = ((x % 1440) + 1440) % 1440 }
    val nowMin = LocalTime.now().let { (it.hour * 60 + it.minute) / 5 * 5 }

    Sheet(onDismiss, title) {
        T(fmtMinutes(v), 64.sp, FontWeight.Black, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Box(Modifier.size(12.dp))
        SectionLabel("Часы")
        Stepper(fmtMinutes(v).take(2), onMinus = { set(v - 60) }, onPlus = { set(v + 60) })
        Box(Modifier.size(12.dp))
        SectionLabel("Минуты")
        Stepper(fmtMinutes(v).takeLast(2), onMinus = { set(v - 5) }, onPlus = { set(v + 5) })
        Box(Modifier.size(16.dp))
        SectionLabel("Быстрый выбор")
        PillGroup(quick, null, { if (it == 0) "Сейчас" else "−${it / 60} ч" }, { set(nowMin - it) }, Modifier.fillMaxWidth())
        Box(Modifier.size(20.dp))
        PrimaryButton("Готово", { onSave(v); onDismiss() }, Modifier.fillMaxWidth(), Ic.Check)
    }
}
