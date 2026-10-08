package kras.example.many.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

val CardShape = RoundedCornerShape(24.dp)

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
    textAlign = align, maxLines = maxLines, overflow = TextOverflow.Ellipsis, lineHeight = size * 1.2f,
)

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = Ui.c.surface,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(CardShape)
            .background(color)
            .border(1.dp, Ui.c.text.copy(alpha = 0.06f), CardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    bg: Color = Ui.c.surfaceHigh,
    tint: Color = Ui.c.text,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (enabled) tint else tint.copy(alpha = 0.3f), modifier = Modifier.size(size * 0.45f))
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    secondary: Boolean = false,
    danger: Boolean = false,
) {
    val c = Ui.c
    val bg = when { danger -> c.bad.copy(alpha = 0.15f); secondary -> c.surfaceHigh; else -> c.accent }
    val fg = when { danger -> c.bad; secondary -> c.text; else -> c.onAccent }
    Row(
        modifier
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
            Box(Modifier.size(8.dp))
        }
        T(text, 15.sp, FontWeight.SemiBold, fg, maxLines = 1)
    }
}

/** Набор «таблеток» для выбора одного значения. */
@Composable
fun <V> PillGroup(
    items: List<V>,
    selected: V?,
    label: (V) -> String,
    onSelect: (V) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Ui.c.surfaceHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { item ->
            val active = item == selected
            val bg by animateColorAsState(if (active) Ui.c.accent else Color.Transparent, label = "pill")
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .clickable { onSelect(item) },
                contentAlignment = Alignment.Center,
            ) {
                T(label(item), 13.sp, FontWeight.SemiBold, if (active) Ui.c.onAccent else Ui.c.text, maxLines = 1)
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
        RoundIconButton(Ic.Minus, onMinus, size = 52.dp)
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            T(value, 34.sp, FontWeight.Black, align = TextAlign.Center)
            if (caption != null) T(caption, 12.sp, color = Ui.c.textDim)
        }
        RoundIconButton(Ic.Plus, onPlus, size = 52.dp, bg = Ui.c.accent, tint = Ui.c.onAccent)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) =
    T(text.uppercase(), 11.sp, FontWeight.Bold, Ui.c.textDim, modifier.padding(bottom = 8.dp))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sheet(onDismiss: () -> Unit, title: String, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Ui.c.surface,
        contentColor = Ui.c.text,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(title, 22.sp, FontWeight.Bold, modifier = Modifier.weight(1f))
                RoundIconButton(Ic.Close, onDismiss, size = 36.dp)
            }
            Box(Modifier.height(16.dp))
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
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Ui.c.surface,
        shape = CardShape,
        title = { T(title, 20.sp, FontWeight.Bold) },
        text = { T(text, 15.sp, color = Ui.c.textDim) },
        confirmButton = {
            TextButton(onClick = { onConfirm?.invoke(); onDismiss() }) {
                T(confirm, 15.sp, FontWeight.SemiBold, Ui.c.accent)
            }
        },
        dismissButton = dismissText?.let { label ->
            @Composable { TextButton(onClick = onDismiss) { T(label, 15.sp, color = Ui.c.textDim) } }
        },
    )
}

@Composable
fun RowScope.StatTile(
    icon: ImageVector,
    label: String,
    value: String,
    sub: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Panel(Modifier.weight(1f), onClick = onClick, padding = PaddingValues(12.dp)) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }
        Box(Modifier.height(8.dp))
        T(label, 11.sp, color = Ui.c.textDim, maxLines = 1)
        T(value, 18.sp, FontWeight.Bold, maxLines = 1)
        T(sub, 11.sp, color = Ui.c.textDim, maxLines = 1)
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 14.dp)) {
        T(title, 30.sp, FontWeight.Black)
        T(subtitle, 13.sp, color = Ui.c.textDim)
    }
}

@Composable
fun EmptyState(emoji: String, title: String, text: String, action: String?, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        T(emoji, 64.sp)
        Box(Modifier.size(12.dp))
        T(title, 22.sp, FontWeight.Bold, align = TextAlign.Center)
        Box(Modifier.size(6.dp))
        T(text, 14.sp, color = Ui.c.textDim, align = TextAlign.Center)
        if (action != null) {
            Box(Modifier.size(20.dp))
            PrimaryButton(action, onAction, icon = Ic.Plus)
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
