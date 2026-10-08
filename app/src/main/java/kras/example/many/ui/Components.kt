package kras.example.many.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.ui.theme.Ui
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

val CardShape = RoundedCornerShape(12.dp)
val ControlShape = RoundedCornerShape(10.dp)

@Composable
fun T(
    text: String,
    size: TextUnit = 14.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = Ui.c.text,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
) = Text(
    text = text, fontSize = size, fontWeight = weight, color = color, modifier = modifier,
    textAlign = align, maxLines = maxLines, overflow = TextOverflow.Ellipsis, lineHeight = size * 1.25f,
)

/** Карточка: фон, тонкая рамка, без теней. */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = Ui.c.surface,
    borderColor: Color = Ui.c.line,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(CardShape)
            .background(color)
            .border(1.dp, borderColor, CardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

@Composable
fun IconBtn(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    filled: Boolean = false,
    description: String? = null,
) {
    val c = Ui.c
    Box(
        modifier
            .size(size)
            .clip(ControlShape)
            .background(if (filled) c.accent else c.surface)
            .border(1.dp, if (filled) c.accent else c.line, ControlShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = if (filled) c.onAccent else c.text, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
    danger: Boolean = false,
) {
    val c = Ui.c
    val bg = if (secondary || danger) c.surface else c.accent
    val fg = when { danger -> c.bad; secondary -> c.text; else -> c.onAccent }
    val border = when { danger -> c.bad; secondary -> c.line; else -> c.accent }
    Box(
        modifier
            .height(52.dp)
            .clip(ControlShape)
            .background(bg)
            .border(1.dp, border, ControlShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        T(text, 15.sp, FontWeight.SemiBold, fg, maxLines = 1)
    }
}

/** Сегментированный переключатель. */
@Composable
fun <V> Segmented(
    items: List<V>,
    selected: V?,
    label: (V) -> String,
    onSelect: (V) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
) {
    val c = Ui.c
    Row(
        modifier
            .height(height)
            .clip(ControlShape)
            .border(1.dp, c.line, ControlShape)
            .background(c.surface),
    ) {
        items.forEachIndexed { i, item ->
            val active = item == selected
            if (i > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(c.line))
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (active) c.accent else Color.Transparent)
                    .clickable { onSelect(item) },
                contentAlignment = Alignment.Center,
            ) {
                T(label(item), 14.sp, FontWeight.Medium, if (active) c.onAccent else c.text, maxLines = 1)
            }
        }
    }
}

@Composable
fun Stepper(
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconBtn(Ic.Minus, onMinus, size = 48.dp, description = "Меньше")
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            T(value, 28.sp, FontWeight.SemiBold, align = TextAlign.Center)
            if (caption != null) T(caption, 12.sp, color = Ui.c.textDim)
        }
        IconBtn(Ic.Plus, onPlus, size = 48.dp, description = "Больше")
    }
}

/** Числовое значение: крупно + кнопки −/+ + ползунок. */
@Composable
fun NumberPicker(value: Int, unit: String, min: Int, max: Int, onChange: (Int) -> Unit) {
    val c = Ui.c
    Stepper(
        "$value $unit",
        onMinus = { onChange((value - 1).coerceAtLeast(min)) },
        onPlus = { onChange((value + 1).coerceAtMost(max)) },
    )
    Box(Modifier.size(8.dp))
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.roundToInt()) },
        valueRange = min.toFloat()..max.toFloat(),
        colors = SliderDefaults.colors(thumbColor = c.accent, activeTrackColor = c.accent, inactiveTrackColor = c.surfaceHigh),
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) =
    T(text, 13.sp, FontWeight.Medium, Ui.c.textDim, modifier.padding(bottom = 8.dp))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sheet(onDismiss: () -> Unit, title: String, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Ui.c.surface,
        contentColor = Ui.c.text,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(title, 20.sp, FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Icon(
                    Sym.Close, "Закрыть", tint = Ui.c.textDim,
                    modifier = Modifier.size(32.dp).clip(ControlShape).clickable(onClick = onDismiss).padding(4.dp),
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Ui.c.line)
            content()
        }
    }
}

@Composable
fun InfoDialog(
    title: String,
    text: String,
    confirm: String = "Понятно",
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismissClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Ui.c.surface,
        shape = CardShape,
        title = { T(title, 18.sp, FontWeight.SemiBold) },
        text = { T(text, 14.sp, color = Ui.c.textDim) },
        confirmButton = {
            TextButton(onClick = { onConfirm?.invoke(); onDismiss() }) {
                T(confirm, 14.sp, FontWeight.SemiBold, Ui.c.accent)
            }
        },
        dismissButton = dismissText?.let { label ->
            @Composable {
                TextButton(onClick = { onDismissClick?.invoke(); onDismiss() }) { T(label, 14.sp, FontWeight.Medium, Ui.c.textDim) }
            }
        },
    )
}

@Composable
fun RowScope.StatTile(
    label: String,
    value: String,
    sub: String,
    valueColor: Color = Ui.c.text,
    onClick: () -> Unit,
) {
    Panel(Modifier.weight(1f), onClick = onClick, padding = PaddingValues(12.dp)) {
        T(label, 12.sp, color = Ui.c.textDim, maxLines = 1)
        T(value, 18.sp, FontWeight.SemiBold, valueColor, maxLines = 1)
        T(sub, 11.sp, color = Ui.c.textDim, maxLines = 1)
    }
}

/** Строка настроек: название слева, значение справа, стрелка. */
@Composable
fun SettingRow(title: String, value: String, onClick: () -> Unit) {
    val c = Ui.c
    Row(
        Modifier.fillMaxWidth().height(52.dp).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T(title, 15.sp, modifier = Modifier.weight(1f))
        T(value, 15.sp, FontWeight.Medium, c.textDim)
        Icon(Sym.Chevron, null, tint = c.textDim, modifier = Modifier.padding(start = 4.dp).size(20.dp))
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)) {
        T(title, 24.sp, FontWeight.SemiBold)
        if (subtitle != null) T(subtitle, 13.sp, color = Ui.c.textDim)
    }
}

@Composable
fun EmptyState(title: String, text: String, action: String?, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        T(title, 18.sp, FontWeight.SemiBold, align = TextAlign.Center)
        Box(Modifier.size(6.dp))
        T(text, 14.sp, color = Ui.c.textDim, align = TextAlign.Center)
        if (action != null) {
            Box(Modifier.size(20.dp))
            PrimaryButton(action, onAction)
        }
    }
}

// ---- Форматирование ----

private val ru = Locale.forLanguageTag("ru")
private val hm = DateTimeFormatter.ofPattern("HH:mm", ru)
private val dayMonth = DateTimeFormatter.ofPattern("d MMM", ru)

fun fmtPromille(v: Float): String = String.format(ru, "%.2f", v)

fun fmtTime(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(hm)

fun fmtDayTime(ms: Long): String {
    val dt = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())
    val today = LocalDate.now()
    val prefix = when (dt.toLocalDate()) {
        today -> ""
        today.plusDays(1) -> "завтра "
        today.minusDays(1) -> "вчера "
        else -> dt.format(dayMonth) + " "
    }
    return prefix + dt.format(hm)
}

fun fmtMinutes(min: Int): String = String.format(ru, "%02d:%02d", min / 60, min % 60)

fun fmtDate(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(dayMonth)

fun fmtDuration(ms: Long): String {
    val min = (ms / 60_000L).coerceAtLeast(0)
    return when {
        min < 60 -> "$min мин"
        min % 60 == 0L -> "${min / 60} ч"
        else -> "${min / 60} ч ${min % 60} мин"
    }
}

fun fmtVolume(ml: Int): String = if (ml >= 1000) String.format(ru, "%.1f л", ml / 1000f).replace(",0 ", " ") else "$ml мл"

fun abvText(abv: Float) = String.format(ru, "%.1f%%", abv).replace(",0%", "%")
