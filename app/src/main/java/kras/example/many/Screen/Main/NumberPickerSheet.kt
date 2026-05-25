package kras.example.many.Screen.Main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.MySpacer
import kras.example.many.MyTxt

@Composable
fun NumberPickerSheet(
    title: String,
    initialValue: Int,
    minValue: Int,
    maxValue: Int,
    unit: String? = null,
    step: Float = 1f,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var selectedValue by remember { mutableStateOf(initialValue.coerceIn(minValue, maxValue).toFloat()) }
    val listState = rememberLazyListState()
    val showQuickGrid = maxValue - minValue <= 20 && step >= 1f

    LaunchedEffect(initialValue) {
        val scrollIndex = (selectedValue - minValue).toInt()
        if (scrollIndex >= 0) {
            listState.scrollToItem(maxOf(0, scrollIndex - 2))
        }
    }

    PickerBottomSheet(
        title = title,
        onDismiss = onDismiss,
        onConfirm = { onConfirm(selectedValue.toInt()) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepButton(text = "−") {
                selectedValue = (selectedValue - step).coerceAtLeast(minValue.toFloat())
            }

            ColumnCenterValue(value = selectedValue, unit = unit, step = step)

            StepButton(text = "+") {
                selectedValue = (selectedValue + step).coerceAtMost(maxValue.toFloat())
            }
        }

        if (showQuickGrid) {
            MySpacer(height = 16)
            QuickValueGrid(
                minValue = minValue,
                maxValue = maxValue,
                selectedValue = selectedValue.toInt(),
                onSelect = { selectedValue = it.toFloat() }
            )
        } else if (step >= 1f) {
            MySpacer(height = 12)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = listState
            ) {
                items(maxValue - minValue + 1) { index ->
                    val value = minValue + index
                    ValueListItem(
                        value = value,
                        unit = unit,
                        selected = value == selectedValue.toInt(),
                        onClick = { selectedValue = value.toFloat() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnCenterValue(value: Float, unit: String?, step: Float) {
    Box(
        modifier = Modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val displayValue = if (step < 1f) {
            String.format(java.util.Locale.US, "%.1f", value).toFloat()
        } else {
            value.toInt().toFloat()
        }
        val display = if (unit != null) "$displayValue $unit" else displayValue.toString()
        MyTxt(
            text = display,
            fontSize = 40,
            fontWeight = 600,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StepButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(MyColor.BACKGROUND)
            .border(1.dp, MyColor.TITLE.copy(alpha = 0.15f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        MyTxt(text = text, fontSize = 28, fontWeight = 600, color = MyColor.SECTION_TITLE)
    }
}

@Composable
private fun QuickValueGrid(
    minValue: Int,
    maxValue: Int,
    selectedValue: Int,
    onSelect: (Int) -> Unit,
) {
    val columns = 5
    val values = (minValue..maxValue).toList()
    values.chunked(columns).forEach { rowValues ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowValues.forEach { value ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (value == selectedValue) MyColor.SECTION_TITLE.copy(alpha = 0.15f)
                            else MyColor.BACKGROUND
                        )
                        .border(
                            width = if (value == selectedValue) 1.5.dp else 1.dp,
                            color = if (value == selectedValue) MyColor.SECTION_TITLE
                            else MyColor.TITLE.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(value) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MyTxt(
                        text = value.toString(),
                        fontSize = 15,
                        fontWeight = if (value == selectedValue) 600 else 400,
                        color = if (value == selectedValue) MyColor.SECTION_TITLE else MyColor.TITLE
                    )
                }
            }
            repeat(columns - rowValues.size) {
                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ValueListItem(
    value: Int,
    unit: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val label = if (unit != null) "$value $unit" else value.toString()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MyColor.SECTION_TITLE.copy(alpha = 0.12f) else Color.Transparent
            )
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) MyColor.SECTION_TITLE else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        MyTxt(
            text = label,
            fontSize = 17,
            fontWeight = if (selected) 600 else 400,
            color = if (selected) MyColor.SECTION_TITLE else MyColor.TITLE
        )
    }
}
