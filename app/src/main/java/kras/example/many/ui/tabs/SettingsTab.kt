package kras.example.many.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kras.example.many.ui.Ic
import kras.example.many.ui.InfoDialog
import kras.example.many.ui.PillGroup
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.ScreenHeader
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.T
import kras.example.many.ui.theme.ThemeId
import kras.example.many.ui.theme.Ui
import kotlin.math.roundToInt

@Composable
fun SettingsTab(onShareApp: () -> Unit, onRate: () -> Unit) {
    val c = Ui.c
    val p = AppStore.profile
    var about by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Настройки", "Оформление и параметры расчёта")
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column {
                SectionLabel("Оформление")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThemeId.entries.forEach { t -> ThemeSwatch(t, t == AppStore.theme, Modifier.weight(1f)) { AppStore.updateTheme(t) } }
                }
            }
            Column {
                SectionLabel("Лимит для вождения, ‰")
                PillGroup(listOf(0f, 0.3f, 0.5f, 0.8f), p.driveLimit, { if (it == 0f) "0.0" else "$it" }, {
                    AppStore.updateProfile(p.copy(driveLimit = it))
                }, Modifier.fillMaxWidth())
                T("В России — 0.3 ‰ (0.16 мг/л в выдохе)", 11.sp, color = c.textDim, modifier = Modifier.padding(top = 6.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Приложение")
                PrimaryButton("Как считается результат", { about = true }, Modifier.fillMaxWidth(), Ic.Info, secondary = true)
                PrimaryButton("Поделиться с друзьями", onShareApp, Modifier.fillMaxWidth(), Ic.Share, secondary = true)
                PrimaryButton("Оценить приложение", onRate, Modifier.fillMaxWidth(), Ic.Star)
            }
        }
    }

    if (about) {
        val water = BacEngine.totalBodyWater(p)
        InfoDialog(
            "Как считаем",
            "Формула Видмарка с поправкой Уотсона: учитываются пол, вес, рост и возраст " +
                "(у вас ≈ ${water.roundToInt()} л воды в организме).\n\n" +
                "Порции распределяются равномерно по времени застолья, каждая всасывается 30–90 минут " +
                "в зависимости от закуски. Выведение — ${BacEngine.ELIMINATION_PER_HOUR} ‰/ч.\n\n" +
                "Результат ориентировочный и не является медицинским или юридическим заключением.",
        ) { about = false }
    }
}

@Composable
private fun ThemeSwatch(t: ThemeId, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Ui.c
    val p = t.palette
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(52.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(p.bg, p.bgGlow, p.accent)))
                .border(if (active) 3.dp else 1.dp, if (active) c.accent else c.text.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (active) Icon(Ic.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        T(t.title, 11.sp, if (active) FontWeight.Bold else FontWeight.Normal, if (active) c.text else c.textDim, Modifier.padding(top = 4.dp), TextAlign.Center, 1)
    }
}
