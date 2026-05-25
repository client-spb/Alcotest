package kras.example.many.session

import kras.example.many.calculation.CalculationInput
import kras.example.many.calculation.CalculationResult
import kras.example.many.calculation.PromilleCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppSession {
    private val _result = MutableStateFlow<CalculationResult?>(null)
    val result: StateFlow<CalculationResult?> = _result.asStateFlow()

    private val _drinksResetVersion = MutableStateFlow(0)
    val drinksResetVersion: StateFlow<Int> = _drinksResetVersion.asStateFlow()

    fun submitAndCalculate(input: CalculationInput) {
        _result.value = PromilleCalculator.calculate(input)
    }

    fun requestDrinksReset() {
        _drinksResetVersion.value++
    }

    fun clearResult() {
        _result.value = null
    }
}
