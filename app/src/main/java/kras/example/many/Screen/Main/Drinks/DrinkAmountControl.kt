package kras.example.many.Screen.Main.Drinks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.MyTxt
import kras.example.many.R

@Composable
fun DrinkAmountControl(
    config: DrinkConfig,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MyColor.TITLE.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MyColor.BOX)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.do_not_disturb_on_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
            contentDescription = "Уменьшить",
            modifier = Modifier
                .size(36.dp)
                .clickable {
                    onValueChange((value - config.step).coerceAtLeast(config.minAmount))
                },
            colorFilter = ColorFilter.tint(MyColor.SUBTITLE)
        )

        MyTxt(
            text = config.formatAmount(value, withUnit = true),
            fontSize = 18,
            fontWeight = 600,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(id = R.drawable.add_circle_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
            contentDescription = "Увеличить",
            modifier = Modifier
                .size(36.dp)
                .clickable { onValueChange(value + config.step) },
            colorFilter = ColorFilter.tint(MyColor.SECTION_TITLE)
        )
    }
}
