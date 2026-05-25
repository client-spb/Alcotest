package kras.example.many.Screen.Main.Drinks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kras.example.many.Color.MyColor
import kras.example.many.MySpacer
import kras.example.many.R
import kras.example.many.Screen.Main.SectionCard

@Composable
fun DrinksSection(
    amounts: Map<String, Float>,
    degrees: Map<String, Float>,
    onAmountChange: (drinkId: String, amount: Float) -> Unit,
    onDegreesChange: (drinkId: String, degrees: Float) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    SectionCard(
        iconRes = R.drawable.local_drink_24dp_1f1f1f_fill0_wght400_grad0_opsz24,
        title = "Напитки"
    ) {
        MySpacer(height = 10)

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MyColor.TITLE,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth(),
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MyColor.SECTION_TITLE,
                        height = 3.dp
                    )
                }
            },
            divider = {}
        ) {
            DrinksCatalog.all.forEachIndexed { index, config ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = "${config.emoji} ${config.name}",
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) MyColor.SECTION_TITLE else MyColor.SUBTITLE
                        )
                    }
                )
            }
        }

        MySpacer(height = 12)

        val config = DrinksCatalog.all[selectedTab]
        DrinksItem(
            config = config,
            amount = amounts[config.id] ?: config.defaultAmount,
            degrees = degrees[config.id] ?: config.defaultDegrees,
            onAmountChange = { onAmountChange(config.id, it) },
            onDegreesChange = { onDegreesChange(config.id, it) }
        )
    }
}
