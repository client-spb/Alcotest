package kras.example.many.ui.calc

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import kras.example.many.core.Profile
import kras.example.many.ui.CardShape
import kras.example.many.ui.Ic
import kras.example.many.ui.Panel
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.Sheet
import kras.example.many.ui.Stepper
import kras.example.many.ui.T
import kras.example.many.ui.theme.Ui
import kotlin.math.roundToInt

private enum class Metric(val title: String, val unit: String, val min: Int, val max: Int) {
    WEIGHT("Вес", "кг", 35, 200),
    HEIGHT("Рост", "см", 130, 220),
    AGE("Возраст", "лет", 18, 100),
}

private fun Profile.valueOf(m: Metric) = when (m) {
    Metric.WEIGHT -> weightKg; Metric.HEIGHT -> heightCm; Metric.AGE -> age
}

private fun Profile.with(m: Metric, v: Int) = when (m) {
    Metric.WEIGHT -> copy(weightKg = v); Metric.HEIGHT -> copy(heightCm = v); Metric.AGE -> copy(age = v)
}

@Composable
fun StepProfile() {
    val c = Ui.c
    val p = AppStore.profile
    var editing by remember { mutableStateOf<Metric?>(null) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        SectionLabel("Пол")
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GenderTile("♂", "Мужчина", p.male, Modifier.weight(1f)) { AppStore.updateProfile(p.copy(male = true)) }
            GenderTile("♀", "Женщина", !p.male, Modifier.weight(1f)) { AppStore.updateProfile(p.copy(male = false)) }
        }
        Box(Modifier.size(14.dp))
        SectionLabel("Параметры — нажмите, чтобы изменить")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric.entries.forEach { m ->
                Panel(Modifier.weight(1f), onClick = { editing = m }, padding = PaddingValues(14.dp)) {
                    T(m.title, 12.sp, color = c.textDim)
                    T("${p.valueOf(m)}", 30.sp, FontWeight.Black)
                    T(m.unit, 12.sp, color = c.textDim)
                }
            }
        }
        T(
            "Данные запоминаются — в следующий раз просто нажмите «Рассчитать»",
            12.sp, color = c.textDim, align = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }

    editing?.let { m ->
        MetricSheet(m, p.valueOf(m), { AppStore.updateProfile(AppStore.profile.with(m, it)) }) { editing = null }
    }
}

@Composable
private fun GenderTile(symbol: String, title: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Ui.c
    val bg by animateColorAsState(if (active) c.accent else c.surface, label = "gender")
    val fg = if (active) c.onAccent else c.text
    Column(
        modifier.fillMaxHeight().clip(CardShape).background(bg).clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        T(symbol, 56.sp, FontWeight.Black, fg)
        T(title, 16.sp, FontWeight.Bold, fg)
    }
}

@Composable
private fun MetricSheet(m: Metric, initial: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    val c = Ui.c
    var v by remember { mutableIntStateOf(initial) }
    Sheet(onDismiss, m.title) {
        Stepper(
            "$v ${m.unit}",
            onMinus = { v = (v - 1).coerceAtLeast(m.min) },
            onPlus = { v = (v + 1).coerceAtMost(m.max) },
        )
        Box(Modifier.size(12.dp))
        Slider(
            value = v.toFloat(),
            onValueChange = { v = it.roundToInt() },
            valueRange = m.min.toFloat()..m.max.toFloat(),
            colors = SliderDefaults.colors(thumbColor = c.accent, activeTrackColor = c.accent, inactiveTrackColor = c.surfaceHigh),
        )
        Box(Modifier.size(16.dp))
        PrimaryButton("Сохранить", { onSave(v); onDismiss() }, Modifier.fillMaxWidth(), Ic.Check)
    }
}
