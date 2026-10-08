package kras.example.many.ui.calc

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.core.AppStore
import kras.example.many.core.Stomach
import kras.example.many.ui.Panel
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.Segmented
import kras.example.many.ui.Sheet
import kras.example.many.ui.Stepper
import kras.example.many.ui.Sym
import kras.example.many.ui.T
import kras.example.many.ui.fmtDuration
import kras.example.many.ui.fmtMinutes
import kras.example.many.ui.theme.Ui
import java.time.LocalTime

private enum class Edge { START, END }

private val stomachHints = mapOf(
    Stomach.EMPTY to "Почти ничего не ели — алкоголь всасывается быстрее",
    Stomach.SNACK to "Лёгкие закуски, бутерброды, салаты",
    Stomach.FULL to "Сытный ужин — пик ниже и наступает позже",
)

@Composable
fun StepTime() {
    val c = Ui.c
    var editing by remember { mutableStateOf<Edge?>(null) }
    val duration = ((AppStore.endMin - AppStore.startMin) + 24 * 60) % (24 * 60)

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeTile("Начали", AppStore.startMin, Modifier.weight(1f)) { editing = Edge.START }
            TimeTile("Закончили", AppStore.endMin, Modifier.weight(1f)) { editing = Edge.END }
        }
        T(
            if (duration == 0) "Всё выпито за один раз" else "Продолжительность: ${fmtDuration(duration * 60_000L)}",
            13.sp, color = c.textDim, align = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
        Column {
            SectionLabel("Закуска")
            Segmented(Stomach.entries, AppStore.stomach, { it.title }, AppStore::updateStomach, Modifier.fillMaxWidth(), height = 48.dp)
            T(stomachHints.getValue(AppStore.stomach), 13.sp, color = c.textDim, modifier = Modifier.padding(top = 8.dp))
        }
    }

    editing?.let { edge ->
        TimeSheet(
            title = if (edge == Edge.START) "Когда начали пить" else "Когда закончили пить",
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
            T(label, 13.sp, color = c.textDim, modifier = Modifier.weight(1f))
            Icon(Sym.Clock, null, tint = c.textDim, modifier = Modifier.size(18.dp))
        }
        T(fmtMinutes(min), 36.sp, FontWeight.SemiBold)
        T("Изменить", 13.sp, FontWeight.Medium, c.accent)
    }
}

private val quick = listOf(0, 60, 120, 180)

@Composable
private fun TimeSheet(title: String, initial: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    var v by remember { mutableIntStateOf(initial) }
    fun set(x: Int) { v = ((x % 1440) + 1440) % 1440 }
    val nowMin = LocalTime.now().let { (it.hour * 60 + it.minute) / 5 * 5 }

    Sheet(onDismiss, title) {
        T(fmtMinutes(v), 56.sp, FontWeight.SemiBold, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Box(Modifier.size(12.dp))
        SectionLabel("Часы")
        Stepper(fmtMinutes(v).take(2), onMinus = { set(v - 60) }, onPlus = { set(v + 60) })
        Box(Modifier.size(12.dp))
        SectionLabel("Минуты")
        Stepper(fmtMinutes(v).takeLast(2), onMinus = { set(v - 5) }, onPlus = { set(v + 5) })
        Box(Modifier.size(16.dp))
        SectionLabel("Быстрый выбор")
        Segmented(quick, null, { if (it == 0) "Сейчас" else "−${it / 60} ч" }, { set(nowMin - it) }, Modifier.fillMaxWidth())
        Box(Modifier.size(24.dp))
        PrimaryButton("Готово", { onSave(v); onDismiss() }, Modifier.fillMaxWidth())
    }
}
