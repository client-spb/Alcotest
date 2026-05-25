package kras.example.many.Screen.Main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.MySpacer
import kras.example.many.MyTxt
import kras.example.many.R

@Composable
fun Parametr(
    age: Int,
    weight: Int,
    gender: String,
    onAgeClick: () -> Unit = {},
    onWeightClick: () -> Unit = {},
    onGenderClick: () -> Unit = {},
) {
    SectionCard(iconRes = R.drawable.people, title = "Параметры") {
        MySpacer(height = 10)
        Row(modifier = Modifier.fillMaxWidth()) {
            ParametrItem(
                modifier = Modifier.weight(1f),
                icon = R.drawable.cake,
                label = "Возраст",
                value = age.toString(),
                onClick = onAgeClick
            )
            MySpacer(width = 8)
            ParametrItem(
                modifier = Modifier.weight(1f),
                icon = R.drawable.balance_24dp_1f1f1f_fill0_wght400_grad0_opsz24,
                label = "Вес",
                value = weight.toString(),
                onClick = onWeightClick
            )
            MySpacer(width = 8)
            ParametrItemGender(
                modifier = Modifier.weight(1f),
                gender = gender,
                onClick = onGenderClick
            )
        }
    }
}

@Composable
private fun ParametrItem(
    modifier: Modifier,
    icon: Int,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(MyColor.SECTION_TITLE.copy(alpha = 0.7f))
            )
            MySpacer(width = 4)
            MyTxt(text = label, color = MyColor.SUBTITLE, fontSize = 13)
        }
        MySpacer(height = 6)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MyColor.BACKGROUND)
                .border(1.dp, MyColor.TITLE.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            MyTxt(text = value, color = MyColor.TITLE, fontSize = 18, fontWeight = 600)
        }
    }
}

@Composable
private fun ParametrItemGender(
    modifier: Modifier,
    gender: String,
    onClick: () -> Unit,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.wc_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(MyColor.SECTION_TITLE.copy(alpha = 0.7f))
            )
            MySpacer(width = 4)
            MyTxt(text = "Пол", color = MyColor.SUBTITLE, fontSize = 13)
        }
        MySpacer(height = 6)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MyColor.BACKGROUND)
                .border(1.dp, MyColor.TITLE.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MyColor.SECTION_TITLE),
                contentAlignment = Alignment.Center
            ) {
                MyTxt(text = gender, color = MyColor.BACKGROUND, fontSize = 16, fontWeight = 600)
            }
        }
    }
}
