package kras.example.many.Screen.Main.Drinks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kras.example.many.Screen.Main.NumberPickerSheet

@Composable
fun DrinksItem(
    config: DrinkConfig,
    amount: Float,
    degrees: Int,
    onAmountChange: (Float) -> Unit,
    onDegreesChange: (Int) -> Unit,
) {
    val displayDegrees = degrees.coerceIn(config.minDegrees, config.maxDegrees)
    var showDegreesPicker by remember { mutableStateOf(false) }

    if (showDegreesPicker) {
        NumberPickerSheet(
            title = "Крепость (%) — ${config.name}",
            initialValue = displayDegrees,
            minValue = config.minDegrees,
            maxValue = config.maxDegrees,
            step = if (config.id == "beer") 0.1f else 1f,
            onDismiss = { showDegreesPicker = false },
            onConfirm = { newValue ->
                onDegreesChange(newValue)
                DrinkParamsStorage.saveDegrees(config.id, newValue)
                showDegreesPicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MyColor.TITLE.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MyColor.BACKGROUND)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                MyTxt(config.emoji, fontSize = 36)
                MySpacer(width = 12)
                Column {
                    MyTxt(config.name, fontWeight = 600, fontSize = 18)
                    MyTxt(
                        "Порция: ${config.portionLabel()}",
                        fontSize = 12,
                        color = MyColor.TITLE.copy(alpha = 0.45f)
                    )
                }
            }
            DegreesBadge(
                value = displayDegrees,
                onClick = { showDegreesPicker = true }
            )
        }

        DrinkAmountControl(
            config = config,
            value = amount,
            onValueChange = onAmountChange
        )
    }
}
