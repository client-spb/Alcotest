package kras.example.many

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.Color.MyColor

@Composable
fun MyTxt(
    text: String = "Hello World",
    color: Color = MyColor.TITLE,
    fontSize: Int = 14,
    fontWeight: Int = 400,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start

) {
    Text(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = fontSize.sp,
            fontWeight = FontWeight(fontWeight),
            textAlign = textAlign
        ),modifier = modifier
    )
}

@Composable
fun MySpacer(
    width: Int = 10, height: Int = 10, color: Color = Color.Transparent
) {
    Spacer(
        modifier = Modifier
            .width(width.dp)
            .height(height.dp)
            .background(color)
    )
}