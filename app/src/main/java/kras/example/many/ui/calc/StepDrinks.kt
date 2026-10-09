package kras.example.many.ui.calc

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.core.AppStore
import kras.example.many.core.DrinkType
import kras.example.many.ui.ControlShape
import kras.example.many.ui.IconBtn
import kras.example.many.ui.InfoDialog
import kras.example.many.ui.Panel
import kras.example.many.ui.PrimaryButton
import kras.example.many.ui.SectionLabel
import kras.example.many.ui.Segmented
import kras.example.many.ui.Sheet
import kras.example.many.ui.Stepper
import kras.example.many.ui.Sym
import kras.example.many.ui.T
import kras.example.many.ui.abvText
import kras.example.many.ui.fmtVolume
import kras.example.many.ui.theme.Ui
import kotlin.math.roundToInt

@Composable
fun StepDrinks() {
    val c = Ui.c
    var addType by remember { mutableStateOf<DrinkType?>(null) }
    var askMore by remember { mutableStateOf(false) }
    var drinkPage by remember { mutableIntStateOf(0) }
    val draft = AppStore.draft

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DrinkType.entries.chunked(2).forEach { row ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { type ->
                        DrinkTile(
                            type, draft.filter { it.type == type }.sumOf { it.count },
                            Modifier.weight(1f).fillMaxHeight(),
                        ) { addType = type }
                    }
                }
            }
        }
        Box(Modifier.size(12.dp))
        SectionLabel(if (draft.isEmpty()) "Вы выпили" else "Вы выпили · нажмите, чтобы убрать")
        Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.CenterStart) {
            if (draft.isEmpty()) {
                T("Пока ничего — выберите напиток выше", 14.sp, color = c.textDim)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val index = drinkPage.coerceIn(0, draft.lastIndex)
                    val d = draft[index]
                    Row(
                        Modifier.weight(1f).height(40.dp).clip(ControlShape).background(c.surfaceHigh)
                            .clickable { AppStore.removeDraft(d.id) }.padding(start = 12.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        T("${d.type.title} ${d.count} × ${fmtVolume(d.volumeMl)}", 13.sp, FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1)
                        Icon(Sym.Close, "Убрать", tint = c.textDim, modifier = Modifier.padding(start = 4.dp).size(16.dp))
                    }
                    if (draft.size > 1) {
                        IconBtn(Sym.Back, { drinkPage = (index - 1 + draft.size) % draft.size }, size = 40.dp, description = "Предыдущий напиток")
                        T("${index + 1}/${draft.size}", 12.sp, color = c.textDim)
                        IconBtn(Sym.Next, { drinkPage = (index + 1) % draft.size }, size = 40.dp, description = "Следующий напиток")
                    }
                }
            }
        }
    }

    addType?.let { type ->
        AddDrinkSheet(type, onDismiss = { addType = null }) { askMore = true }
    }
    if (askMore) {
        InfoDialog(
            title = "Напиток добавлен",
            text = "Пили что-то ещё?",
            confirm = "Продолжить",
            onConfirm = { AppStore.step = AppStore.STEP_TIME },
            dismissText = "Добавить ещё",
            onDismiss = { askMore = false },
        )
    }
}

@Composable
private fun DrinkTile(type: DrinkType, count: Int, modifier: Modifier, onClick: () -> Unit) {
    val c = Ui.c
    val (ml, abv) = AppStore.lastFor(type)
    Panel(
        modifier, onClick = onClick, padding = PaddingValues(14.dp),
        color = if (count > 0) c.accent.copy(alpha = 0.08f) else c.surface,
        borderColor = if (count > 0) c.accent else c.line,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            T(type.title, 17.sp, FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
            if (count > 0) {
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(c.accent).padding(horizontal = 7.dp, vertical = 2.dp)
                ) { T("$count", 12.sp, FontWeight.Bold, c.onAccent) }
            }
        }
        Box(Modifier.weight(1f))
        T("${fmtVolume(ml)}, ${abvText(abv)}", 13.sp, color = c.textDim, maxLines = 1)
    }
}

@Composable
private fun AddDrinkSheet(type: DrinkType, onDismiss: () -> Unit, onAdded: () -> Unit) {
    val c = Ui.c
    val last = AppStore.lastFor(type)
    var ml by remember { mutableIntStateOf(last.first) }
    var abv by remember { mutableFloatStateOf(last.second) }
    var count by remember { mutableIntStateOf(1) }

    Sheet(onDismiss, type.title) {
        SectionLabel("Объём одной порции")
        Segmented(type.presetsMl, ml, { fmtVolume(it) }, { ml = it }, Modifier.fillMaxWidth())
        Box(Modifier.size(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("Крепость", Modifier.weight(1f))
            T(abvText(abv), 15.sp, FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        }
        Slider(
            value = abv,
            onValueChange = { abv = (it * 2).roundToInt() / 2f },
            valueRange = type.minAbv..type.maxAbv,
            colors = SliderDefaults.colors(thumbColor = c.accent, activeTrackColor = c.accent, inactiveTrackColor = c.surfaceHigh),
        )
        Box(Modifier.size(16.dp))
        SectionLabel("Количество порций")
        Stepper(
            "$count",
            caption = "всего ${fmtVolume(ml * count)}",
            onMinus = { count = (count - 1).coerceAtLeast(1) },
            onPlus = { count = (count + 1).coerceAtMost(20) },
        )
        Box(Modifier.size(24.dp))
        PrimaryButton(
            "Добавить",
            {
                AppStore.addDraft(type, ml, abv, count)
                onDismiss()
                onAdded()
            },
            Modifier.fillMaxWidth(),
        )
    }
}
