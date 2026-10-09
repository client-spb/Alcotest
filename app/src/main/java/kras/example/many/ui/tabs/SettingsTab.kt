package kras.example.many.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kras.example.many.core.AppStore
import kras.example.many.core.BacEngine
import kras.example.many.core.Profile
import kras.example.many.ui.ChoiceRow
import kras.example.many.ui.InfoDialog
import kras.example.many.ui.NumberPicker
import kras.example.many.ui.Panel
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.ScreenHeader
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.Segmented
import kras.example.many.ui.SettingRow
import kras.example.many.ui.Sheet
import kras.example.many.ui.theme.ThemeId
import kras.example.many.ui.theme.Ui
import kotlin.math.roundToInt

private enum class Field(val title: String, val unit: String, val min: Int, val max: Int) {
    GENDER("Пол", "", 0, 0),
    AGE("Возраст", "лет", 18, 100),
    WEIGHT("Вес", "кг", 35, 200),
    HEIGHT("Рост", "см", 130, 220),
}

private fun Profile.number(f: Field) = when (f) {
    Field.AGE -> age; Field.WEIGHT -> weightKg; Field.HEIGHT -> heightCm; Field.GENDER -> 0
}

private fun Profile.withNumber(f: Field, v: Int) = when (f) {
    Field.AGE -> copy(age = v); Field.WEIGHT -> copy(weightKg = v); Field.HEIGHT -> copy(heightCm = v); Field.GENDER -> this
}

@Composable
fun SettingsTab(onShareApp: () -> Unit, onRate: () -> Unit) {
    val c = Ui.c
    val p = AppStore.profile
    var editing by remember { mutableStateOf<Field?>(null) }
    var about by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Настройки")
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column {
                SectionLabel("Ваши данные")
                Panel(Modifier.fillMaxWidth(), padding = PaddingValues(0.dp)) {
                    Field.entries.forEachIndexed { i, f ->
                        if (i > 0) HorizontalDivider(color = c.line)
                        val value = if (f == Field.GENDER) (if (p.male) "Мужской" else "Женский") else "${p.number(f)} ${f.unit}"
                        SettingRow(f.title, value) { editing = f }
                    }
                }
            }
            Column {
                SectionLabel("Лимит для вождения, ‰ (в России 0,3)")
                Segmented(listOf(0f, 0.3f, 0.5f, 0.8f), p.driveLimit, { if (it == 0f) "0,0" else "$it".replace('.', ',') }, {
                    AppStore.updateProfile(p.copy(driveLimit = it))
                }, Modifier.fillMaxWidth())
            }
            Column {
                SectionLabel("Тема")
                Segmented(ThemeId.entries, AppStore.theme, { it.title }, AppStore::updateTheme, Modifier.fillMaxWidth())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton("О расчёте", { about = true }, Modifier.weight(1f), secondary = true)
                PrimaryButton("Поделиться", onShareApp, Modifier.weight(1f), secondary = true)
                PrimaryButton("Оценить", onRate, Modifier.weight(1f), secondary = true)
            }
        }
    }

    editing?.let { f -> FieldSheet(f) { editing = null } }
    if (about) {
        val water = BacEngine.totalBodyWater(p)
        InfoDialog(
            "Как считается результат",
            "Формула Видмарка с поправкой Уотсона: учитываются пол, вес, рост и возраст " +
                "(у вас около ${water.roundToInt()} л воды в организме).\n\n" +
                "Порции распределяются равномерно по времени застолья, каждая всасывается 30–90 минут " +
                "в зависимости от закуски. Выведение — ${BacEngine.ELIMINATION_PER_HOUR} ‰ в час.\n\n" +
                "Результат ориентировочный и не является медицинским или юридическим заключением.",
        ) { about = false }
    }
}

@Composable
private fun FieldSheet(f: Field, onDismiss: () -> Unit) {
    val p = AppStore.profile
    var male by remember { mutableStateOf(p.male) }
    var v by remember { mutableIntStateOf(p.number(f)) }
    Sheet(onDismiss, f.title) {
        if (f == Field.GENDER) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceRow("Мужской", male) { male = true }
                ChoiceRow("Женский", !male) { male = false }
            }
        } else {
            NumberPicker(v, f.unit, f.min, f.max) { v = it }
        }
        Box(Modifier.size(24.dp))
        PrimaryButton("Сохранить", {
            AppStore.updateProfile(if (f == Field.GENDER) p.copy(male = male) else p.withNumber(f, v))
            onDismiss()
        }, Modifier.fillMaxWidth())
    }
}
