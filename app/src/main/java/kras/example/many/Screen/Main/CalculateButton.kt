package kras.example.many.Screen.Main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.MyTxt

@Composable
fun CalculateButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (enabled) MyColor.SECTION_TITLE else MyColor.SUBTITLE.copy(alpha = 0.35f)
    val textColor = if (enabled) MyColor.BACKGROUND else MyColor.TITLE.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MyColor.BACKGROUND)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (enabled) {
                        Modifier.shadow(
                            6.dp,
                            RoundedCornerShape(14.dp),
                            ambientColor = MyColor.SECTION_TITLE
                        )
                    } else {
                        Modifier
                    }
                )
                .alpha(if (enabled) 1f else 0.7f)
                .clip(RoundedCornerShape(14.dp))
                .background(backgroundColor)
                .then(
                    if (enabled) Modifier.clickable(onClick = onClick) else Modifier
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            MyTxt(
                text = "Рассчитать",
                color = textColor,
                fontWeight = 600,
                fontSize = 17
            )
        }
    }
}
