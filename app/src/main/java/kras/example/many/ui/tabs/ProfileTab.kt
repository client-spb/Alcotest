package kras.example.many.ui.tabs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.core.AppStore
import kras.example.many.core.BacEngine
import kras.example.many.core.Profile
import kras.example.many.ui.CardShape
import kras.example.many.ui.Ic
import kras.example.many.ui.InfoDialog
import kras.example.many.ui.Panel
import kras.example.many.ui.PillGroup
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.Sheet
import kras.example.many.ui.Stepper
import kras.example.many.ui.T
import kras.example.many.ui.theme.ThemeId
import kras.example.many.ui.theme.Ui
import kotlin.math.roundToInt

private enum class Metric(val title: String, val unit: String, val min: Int, val max: Int) {
    WEIGHT("Вес", "кг", 35, 200),
    HEIGHT("Рост", "см", 130, 220),
    AGE("Возраст", "лет", 18, 100),
}

private fun Profile.get(m: Metric) = when (m) { Metric.WEIGHT -> weightKg; Metric.HEIGHT -> heightCm; Metric.AGE -> age }
private fun Profile.set(m: Metric, v: Int) = when (m) {
    Metric.WEIGHT -> copy(weightKg = v); Metric.HEIGHT -> copy(heightCm = v); Metric.AGE -> copy(age = v)
}

@Composable
fun ProfileTab(onShareApp: () -> Unit, onRate: () -> Unit) {
    val c = Ui.c
    val p = AppStore.profile
    var editing by remember { mutableStateOf<Metric?>(null) }
    var about by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GenderTile("♂", "Мужчина", p.male, Modifier.weight(1f)) { AppStore.updateProfile(p.copy(male = true)) }
            GenderTile("♀", "Женщина", !p.male, Modifier.weight(1f)) { AppStore.updateProfile(p.copy(male = false)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric.entries.forEach { m ->
                Panel(Modifier.weight(1f), onClick = { editing = m }, padding = PaddingValues(14.dp)) {
                    T(m.title, 12.sp, color = c.textDim)
                    T("${p.get(m)}", 26.sp, FontWeight.Black)
                    T(m.unit, 12.sp, color = c.textDim)
                }
            }
        }
        Column {
            SectionLabel("Лимит для вождения, ‰")
            PillGroup(listOf(0f, 0.3f, 0.5f, 0.8f), p.driveLimit, { if (it == 0f) "0.0" else "$it" }, {
                AppStore.updateProfile(p.copy(driveLimit = it))
            }, Modifier.fillMaxWidth())
            T("В России — 0.3 ‰ (0.16 мг/л в выдохе)", 11.sp, color = c.textDim, modifier = Modifier.padding(top = 6.dp))
        }
        Column {
            SectionLabel("Оформление")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeId.entries.forEach { t -> ThemeSwatch(t, t == AppStore.theme, Modifier.weight(1f)) { AppStore.updateTheme(t) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton("Поделиться", onShareApp, Modifier.weight(1f), Ic.Share, secondary = true)
            PrimaryButton("Оценить", onRate, Modifier.weight(1f), Ic.Star, secondary = true)
            Box(Modifier.size(54.dp).clip(CircleShape).background(c.surfaceHigh).clickable { about = true }, contentAlignment = Alignment.Center) {
                Icon(Ic.Info, null, tint = c.text, modifier = Modifier.size(22.dp))
            }
        }
        Box(Modifier.size(2.dp))
    }

    editing?.let { m -> MetricSheet(m, p.get(m), { AppStore.updateProfile(p.set(m, it)) }) { editing = null } }
    if (about) {
        val water = BacEngine.totalBodyWater(p)
        InfoDialog(
            "Как считаем",
            "Формула Видмарка с поправкой Уотсона: учитываются пол, вес, рост и возраст " +
                "(у вас ≈ ${water.roundToInt()} л воды в организме).\n\n" +
                "Каждая порция всасывается 30–90 минут в зависимости от закуски, выведение — ${BacEngine.ELIMINATION_PER_HOUR} ‰/ч.\n\n" +
                "Результат ориентировочный и не является медицинским или юридическим заключением.",
        ) { about = false }
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
        T(symbol, 48.sp, FontWeight.Black, fg)
        T(title, 15.sp, FontWeight.Bold, fg)
    }
}

@Composable
private fun ThemeSwatch(t: ThemeId, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Ui.c
    val p = t.palette
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(48.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(p.bg, p.bgGlow, p.accent)))
                .border(if (active) 3.dp else 1.dp, if (active) c.accent else c.text.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (active) Icon(Ic.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        T(t.title, 11.sp, if (active) FontWeight.Bold else FontWeight.Normal, if (active) c.text else c.textDim, Modifier.padding(top = 4.dp), TextAlign.Center, 1)
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
