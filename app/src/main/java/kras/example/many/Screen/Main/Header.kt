package kras.example.many.Screen.Main

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.Color.MyColor.icoTheme
import kras.example.many.MySpacer
import kras.example.many.MyTxt
import kras.example.many.R

@Composable
 fun Header() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id =R.drawable.ico),
                contentDescription = null,
                modifier = Modifier .shadow(
                    4.dp,
                    RoundedCornerShape(6.dp),
                    ambientColor = MyColor.TITLE,
                    spotColor = MyColor.TITLE
                ).clip(RoundedCornerShape(6.dp))
                    .size(36.dp)

            )
            MySpacer(width = 10)
            Column {
                MyTxt("АлкоТестер", color = MyColor.SECTION_TITLE, fontWeight = 600,fontSize = 18)
                MyTxt(
                    "Калькулятор промилле",
                    modifier = Modifier,
                    color = MyColor.TITLE.copy(alpha = 0.5f),fontSize = 12,textAlign = TextAlign.Center
                )
            }

        }

        ThemeToggle()
    }
}

@Composable
fun ThemeToggle(){
    Image(
        painter = painterResource(id =icoTheme()),
        contentDescription = null,
        modifier = Modifier
            .size(36.dp)
            .clickable(interactionSource = null, indication = null ) {
                MyColor.toggleTheme()
            },
        colorFilter = ColorFilter.tint(MyColor.SECTION_TITLE)
    )
}