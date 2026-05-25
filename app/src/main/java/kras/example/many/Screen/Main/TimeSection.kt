package kras.example.many.Screen.Main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.MySpacer
import kras.example.many.MyTxt
import kras.example.many.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private enum class EditingTimeField {
    START,
    END,
}

@Composable
fun TimeSection(
    startTime: LocalTime,
    endTime: LocalTime,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
) {
    var editingField by remember { mutableStateOf<EditingTimeField?>(null) }

    when (editingField) {
        EditingTimeField.START -> TimePickerSheet(
            title = "Когда начали",
            initialHour = startTime.hour,
            initialMinute = startTime.minute,
            onDismiss = { editingField = null },
            onConfirm = { hour, minute ->
                onStartTimeChange(LocalTime.of(hour, minute))
                editingField = null
            }
        )

        EditingTimeField.END -> TimePickerSheet(
            title = "Когда закончили",
            initialHour = endTime.hour,
            initialMinute = endTime.minute,
            onDismiss = { editingField = null },
            onConfirm = { hour, minute ->
                onEndTimeChange(LocalTime.of(hour, minute))
                editingField = null
            }
        )

        null -> Unit
    }

    SectionCard(iconRes = R.drawable.time, title = "Время") {
        MySpacer(height = 10)
        TimePickerRow(
            label = "Начало",
            time = startTime,
            onClick = { editingField = EditingTimeField.START }
        )
        MySpacer(height = 8)
        TimePickerRow(
            label = "Когда закончили",
            time = endTime,
            onClick = { editingField = EditingTimeField.END }
        )
    }
}

@Composable
private fun TimePickerRow(
    label: String,
    time: LocalTime,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MyColor.BACKGROUND)
            .border(1.dp, MyColor.TITLE.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            MyTxt(text = label, fontSize = 12, color = MyColor.TITLE.copy(alpha = 0.45f))
            MySpacer(height = 2)
            MyTxt(text = time.format(timeFormatter), fontSize = 24, fontWeight = 600)
        }
        MyTxt(text = "Изменить", fontSize = 13, color = MyColor.SECTION_TITLE, fontWeight = 600)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerSheet(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )

    PickerBottomSheet(
        title = title,
        onDismiss = onDismiss,
        onConfirm = { onConfirm(timePickerState.hour, timePickerState.minute) }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
