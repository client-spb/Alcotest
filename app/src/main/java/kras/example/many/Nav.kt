package kras.example.many

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kras.example.many.Screen.Main.MainScreen
import kras.example.many.Screen.Result.ResultScreen

object Nav {
    private val _screen = MutableStateFlow(NavState.MAIN)
    val screen: StateFlow<NavState> = _screen.asStateFlow()

    fun navigateTo(state: NavState) {
        _screen.value = state
    }
}

@Composable
fun NavBar(modifier: Modifier) {
    val navState by Nav.screen.collectAsState()

    AnimatedContent(
        targetState = navState,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "screen_transition"
    ) { state ->
        when (state) {
            NavState.MAIN -> MainScreen(modifier = modifier)
            NavState.RESULT -> ResultScreen(modifier = modifier)
        }
    }
}

enum class NavState {
    MAIN,
    RESULT,
}
