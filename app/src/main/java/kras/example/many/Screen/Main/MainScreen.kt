package kras.example.many.Screen.Main



import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import kras.example.many.Nav

import kras.example.many.NavState

import kras.example.many.ads.YandexFullscreenAd

import kras.example.many.calculation.CalculationInput

import kras.example.many.session.AppSession

import kras.example.many.Screen.Main.Drinks.DrinkParamsStorage

import kras.example.many.Screen.Main.Drinks.DrinksCatalog

import kras.example.many.Screen.Main.Drinks.DrinksSection

import java.time.LocalTime



@Composable

fun MainScreen(modifier: Modifier) {

    var adRequestKey by remember { mutableStateOf(0) }

    var age by remember { mutableStateOf(UserParamsStorage.getAge()) }

    var weight by remember { mutableStateOf(UserParamsStorage.getWeight()) }

    var gender by remember { mutableStateOf(UserParamsStorage.getGender()) }

    var startTime by remember { mutableStateOf(LocalTime.now()) }

    var endTime by remember { mutableStateOf(LocalTime.now()) }

    var drinkAmounts by remember {

        mutableStateOf(DrinksCatalog.all.associate { it.id to it.defaultAmount })

    }

    var drinkDegrees by remember {

        mutableStateOf(

            DrinksCatalog.all.associate { config ->

                val default = config.defaultDegrees.coerceIn(config.minDegrees, config.maxDegrees)

                val stored = DrinkParamsStorage.getDegrees(config.id, default)

                config.id to stored.coerceIn(config.minDegrees, config.maxDegrees)

            }

        )

    }



    val drinksResetVersion by AppSession.drinksResetVersion.collectAsState()

    LaunchedEffect(drinksResetVersion) {

        if (drinksResetVersion > 0) {

            drinkAmounts = DrinksCatalog.all.associate { it.id to it.defaultAmount }

            startTime = LocalTime.now()

            endTime = LocalTime.now()

        }

    }



    var showAgePicker by remember { mutableStateOf(false) }

    var showWeightPicker by remember { mutableStateOf(false) }



    if (showAgePicker) {

        NumberPickerSheet(

            title = "Возраст",

            initialValue = age,

            minValue = 18,

            maxValue = 120,

            onDismiss = { showAgePicker = false },

            onConfirm = { newValue ->

                age = newValue

                UserParamsStorage.saveAge(newValue)

                showAgePicker = false

            }

        )

    }



    if (showWeightPicker) {

        NumberPickerSheet(

            title = "Вес",

            initialValue = weight,

            minValue = 20,

            maxValue = 300,

            unit = "кг",

            onDismiss = { showWeightPicker = false },

            onConfirm = { newValue ->

                weight = newValue

                UserParamsStorage.saveWeight(newValue)

                showWeightPicker = false

            }

        )

    }



    Box(modifier = modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            Column(

                modifier = Modifier

                    .weight(1f)

                    .verticalScroll(rememberScrollState())

            ) {

                Header()

                Parametr(

                    age = age,

                    weight = weight,

                    gender = gender,

                    onAgeClick = { showAgePicker = true },

                    onWeightClick = { showWeightPicker = true },

                    onGenderClick = {

                        gender = if (gender == "М") "Ж" else "М"

                        UserParamsStorage.saveGender(gender)

                    }

                )

                TimeSection(

                    startTime = startTime,

                    endTime = endTime,

                    onStartTimeChange = { startTime = it },

                    onEndTimeChange = { endTime = it }

                )

                DrinksSection(

                    amounts = drinkAmounts,

                    degrees = drinkDegrees,

                    onAmountChange = { id, amount ->

                        drinkAmounts = drinkAmounts + (id to amount)

                    },

                    onDegreesChange = { id, value ->

                        drinkDegrees = drinkDegrees + (id to value)

                    }

                )

                Spacer(modifier = Modifier.height(8.dp))

            }



            val canCalculate = drinkAmounts.values.any { it > 0f }

            CalculateButton(

                enabled = canCalculate && adRequestKey == 0,

                onClick = {

                    AppSession.submitAndCalculate(

                        CalculationInput(

                            age = age,

                            weightKg = weight,

                            gender = gender,

                            startTime = startTime,

                            endTime = endTime,

                            drinkAmounts = drinkAmounts,

                            drinkDegrees = drinkDegrees,

                        )

                    )

                    adRequestKey++

                }

            )

        }



        if (adRequestKey > 0) {
            var adFinished by remember { mutableStateOf(false) }
            
            AdLoadingOverlay(
                modifier = Modifier.fillMaxSize(),
                onTimeout = {
                    if (!adFinished) {
                        adFinished = true
                        adRequestKey = 0
                        Nav.navigateTo(NavState.RESULT)
                    }
                }
            )
            YandexFullscreenAd(
                key = adRequestKey,
                onFinished = {
                    adFinished = true
                    adRequestKey = 0
                    Nav.navigateTo(NavState.RESULT)
                }
            )
        }

    }

}


