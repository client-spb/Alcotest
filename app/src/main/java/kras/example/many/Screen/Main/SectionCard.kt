package kras.example.many.Screen.Main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.MySpacer
import kras.example.many.MyTxt

@Composable
fun SectionCard(
    iconRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
        Column(
            modifier = Modifier
                .shadow(
                    10.dp,
                    RoundedCornerShape(12.dp),
                    ambientColor = MyColor.TITLE,
                    spotColor = MyColor.TITLE
                )
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth()
                .background(MyColor.BOX)
                .padding(12.dp)
        ) {
            SectionHeader(iconRes = iconRes, title = title)
            content()
        }
    }
}

@Composable
fun SectionHeader(iconRes: Int, title: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(MyColor.TITLE)
        )
        MySpacer(width = 6)
        MyTxt(
            text = title,
            color = MyColor.SECTION_TITLE.copy(alpha = 0.7f),
            fontWeight = 600,
            fontSize = 18
        )
    }
}
