package kras.example.many.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.core.AppStore
import kras.example.many.ui.theme.Ui

private data class Question(val title: String, val hint: String)

private val questions = listOf(
    Question("Ваш пол", "От пола зависит, как распределяется алкоголь в организме"),
    Question("Сколько вам лет?", "Возраст влияет на долю воды в организме"),
    Question("Ваш вес", "Чем больше вес, тем ниже концентрация"),
    Question("Ваш рост", "Последний вопрос — и можно начинать"),
)

/** Анкета первого запуска: пол → возраст → вес → рост. */
@Composable
fun Onboarding() {
    val c = Ui.c
    val start = AppStore.profile
    var page by rememberSaveable { mutableIntStateOf(0) }
    var male by rememberSaveable { mutableStateOf(start.male) }
    var age by rememberSaveable { mutableIntStateOf(start.age) }
    var weight by rememberSaveable { mutableIntStateOf(start.weightKg) }
    var height by rememberSaveable { mutableIntStateOf(start.heightCm) }

    BackHandler(enabled = page > 0) { page-- }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            questions.indices.forEach { i ->
                val color by animateColorAsState(if (i <= page) c.accent else c.line, label = "progress")
                Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(color))
            }
        }
        T("Шаг ${page + 1} из ${questions.size}", 12.sp, color = c.textDim, modifier = Modifier.padding(top = 8.dp))

        AnimatedContent(
            targetState = page,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { dir * it } + fadeIn()) togetherWith (slideOutHorizontally { -dir * it } + fadeOut())
            },
            label = "onboarding",
        ) { p ->
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                T(questions[p].title, 28.sp, FontWeight.SemiBold)
                T(questions[p].hint, 14.sp, color = c.textDim, modifier = Modifier.padding(top = 6.dp, bottom = 32.dp))
                when (p) {
                    0 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChoiceRow("Мужской", male) { male = true }
                        ChoiceRow("Женский", !male) { male = false }
                    }
                    1 -> NumberPicker(age, "лет", 18, 100) { age = it }
                    2 -> NumberPicker(weight, "кг", 35, 200) { weight = it }
                    else -> NumberPicker(height, "см", 130, 220) { height = it }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (page > 0) PrimaryButton("Назад", { page-- }, Modifier.weight(1f), secondary = true)
            val last = page == questions.lastIndex
            PrimaryButton(if (last) "Готово" else "Далее", {
                if (last) {
                    AppStore.finishOnboarding(
                        AppStore.profile.copy(male = male, age = age, weightKg = weight, heightCm = height)
                    )
                } else {
                    page++
                }
            }, Modifier.weight(1f))
        }
    }
}

@Composable
fun ChoiceRow(title: String, selected: Boolean, onClick: () -> Unit) {
    val c = Ui.c
    Panel(
        Modifier.fillMaxWidth(), onClick = onClick, padding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        color = if (selected) c.accent.copy(alpha = 0.08f) else c.surface,
        borderColor = if (selected) c.accent else c.line,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            T(title, 17.sp, FontWeight.Medium, modifier = Modifier.weight(1f))
            Box(
                Modifier.size(22.dp).clip(RoundedCornerShape(11.dp))
                    .background(if (selected) c.accent else c.line),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(c.onAccent))
            }
        }
    }
}
