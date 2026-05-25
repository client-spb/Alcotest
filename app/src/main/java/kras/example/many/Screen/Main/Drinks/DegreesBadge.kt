package kras.example.many.Screen.Main.Drinks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.MyTxt

@Composable
fun DegreesBadge(value: Float, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(40.dp)
            .background(MyColor.SUBTITLE.copy(alpha = 0.2f))
            .border(2.dp, MyColor.SUBTITLE.copy(alpha = 0.3f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        val displayValue = String.format(java.util.Locale.US, "%.1f", value).removeSuffix(".0")
        MyTxt(text = "$displayValue%", color = MyColor.TITLE, fontSize = 11)
    }
}
