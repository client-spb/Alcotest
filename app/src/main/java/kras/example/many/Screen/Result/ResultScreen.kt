package kras.example.many.Screen.Result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.Color.MyColor
import kras.example.many.MySpacer
import kras.example.many.MyTxt
import kras.example.many.Nav
import kras.example.many.NavState
import kras.example.many.calculation.PromilleCalculator
import kras.example.many.session.AppSession

@Composable
fun ResultScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val result by AppSession.result.collectAsState()

    BackHandler {
        Nav.navigateTo(NavState.MAIN)
    }

    if (result == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MyColor.BACKGROUND),
            contentAlignment = Alignment.Center
        ) {
            MyTxt("Нет данных расчёта", color = MyColor.SUBTITLE)
        }
        return
    }

    val data = result!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyColor.BACKGROUND)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Диагноз крупными буквами
            val diagnosisData = getDiagnosis(data.initialPromille)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyTxt(
                    text = diagnosisData.title,
                    fontSize = 22,
                    fontWeight = 700,
                    color = diagnosisData.color,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                MyTxt(
                    text = String.format("%.1f ‰", (data.initialPromille * 10f).toInt() / 10f),
                    fontSize = 28,
                    fontWeight = 800,
                    color = diagnosisData.color
                )
            }

            MySpacer(height = 12)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(12.dp), ambientColor = MyColor.TITLE)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MyColor.BOX)
                    .padding(14.dp)
            ) {
                Column {
                    MyTxt(
                        text = "Динамика выведения",
                        fontSize = 22,
                        fontWeight = 600,
                        color = MyColor.SECTION_TITLE
                    )
                    MySpacer(height = 8)
                    MyTxt(
                        text = "Расчет произведен по формуле Эрика Видмарка",
                        fontSize = 12,
                        color = MyColor.TITLE.copy(alpha = 0.75f)
                    )
                    MySpacer(height = 14)

                    data.points.forEachIndexed { index, point ->
                        if (index > 0) MySpacer(height = 8)
                        EliminationRow(point.timeLabel, point.title, point.promille)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MyColor.BACKGROUND)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResultActionButton(
                text = "Поделиться",
                filled = true,
                onClick = { ResultShare.shareResult(context, data.shareText) }
            )
            ResultActionButton(
                text = "Ещё раз",
                filled = false,
                onClick = {
                    AppSession.requestDrinksReset()
                    AppSession.clearResult()
                    Nav.navigateTo(NavState.MAIN)
                }
            )

            MySpacer(height = 12)

            // Предупреждение крупным шрифтом
            MyTxt(
                text = "Внимание! Это не медицинский анализ! Результаты — приблизительные данные, рассчитанные по формуле Видмарка для среднестатистического человека. Индивидуальная реакция организма может существенно отличаться.\"",
                fontSize = 16,
                fontWeight = 600,
                color = MyColor.RESULT,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

        }
    }
}

@Composable
private fun EliminationRow(timeLabel: String, title: String, promille: Float) {
    val textColor = calculatePromilleColor(promille)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MyColor.BACKGROUND)
            .border(1.dp, MyColor.TITLE.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MyTxt(text = timeLabel, fontSize = 16, fontWeight = 600)
        Column(horizontalAlignment = Alignment.End) {
            MyTxt(text = title, fontSize = 12, color = MyColor.TITLE.copy(alpha = 0.45f))
            MyTxt(text = formatPromille(promille), fontSize = 17, fontWeight = 600, color = textColor)
        }
    }
}

@Composable
private fun ResultActionButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (filled) MyColor.SECTION_TITLE else MyColor.BOX
    val textColor = if (filled) MyColor.BACKGROUND else MyColor.SECTION_TITLE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                if (filled) 4.dp else 0.dp,
                RoundedCornerShape(14.dp),
                ambientColor = MyColor.SECTION_TITLE
            )
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(
                width = if (filled) 0.dp else 1.dp,
                color = MyColor.TITLE.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        MyTxt(text = text, color = textColor, fontWeight = 600, fontSize = 16)
    }
}

/**
 * Вычисляет цвет в зависимости от уровня промилле.
 * 0 ‰ → зеленый, высокое значение → красный, плавный градиент между ними.
 */
@Composable
private fun calculatePromilleColor(promille: Float): androidx.compose.ui.graphics.Color {
    // Ограничиваем диапазон от 0 до 3.0‰ для градиента
    val clampedPromille = promille.coerceIn(0f, 3.0f)
    val ratio = (clampedPromille / 3.0f).coerceIn(0f, 1f)

    // Зеленый (0‰) - RGB(76, 175, 80)
    // Красный (3‰+) - RGB(244, 67, 54)
    val greenR = 76f
    val greenG = 175f
    val greenB = 80f

    val redR = 244f
    val redG = 67f
    val redB = 54f

    // Интерполяция: чем больше промилле, тем ближе к красному
    val r = greenR + (redR - greenR) * ratio
    val g = greenG + (redG - greenG) * ratio
    val b = greenB + (redB - greenB) * ratio

    return androidx.compose.ui.graphics.Color(r.toInt(), g.toInt(), b.toInt())
}

/**
 * Возвращает диагноз и цвет в зависимости от уровня промилле.
 */
@Composable
private fun getDiagnosis(promille: Float): DiagnosisData {
    return when {
        promille <= 0.0f -> DiagnosisData("Трезв", Color(0xFF4CAF50)) // Зеленый
        promille <= 0.3f -> DiagnosisData("Легкое опьянение", Color(0xFF8BC34A)) // Светло-зеленый
        promille <= 0.5f -> DiagnosisData("Опьянение", Color(0xFFCDDC39)) // Желто-зеленый
        promille <= 0.8f -> DiagnosisData("Среднее опьянение", Color(0xFFFFEB3B)) // Желтый
        promille <= 1.2f -> DiagnosisData("Сильное опьянение", Color(0xFFFFC107)) // Оранжевый
        promille <= 2.0f -> DiagnosisData("Очень сильное опьянение", Color(0xFFFF9800)) // Темно-оранжевый
        promille <= 3.0f -> DiagnosisData("Тяжелое отравление", Color(0xFFF44336)) // Красный
        promille <= 4.0f -> DiagnosisData("Опасное состояние", Color(0xFFD32F2F)) // Темно-красный
        else -> DiagnosisData("Алкогольная кома!", Color(0xFFB71C1C)) // Очень темный красный
    }
}

data class DiagnosisData(val title: String, val color: androidx.compose.ui.graphics.Color)

private fun formatPromille(value: Float): String {
    val rounded = (value * 10f).toInt() / 10f
    return String.format("%.1f ‰", rounded)
}