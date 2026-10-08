package kras.example.many.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Собственный линейный набор иконок (24×24, штрих 2). */
object Ic {
    private fun line(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach {
                addPath(
                    pathData = addPathNodes(it),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    val Glass = line("glass", "M8 22h8", "M12 15v7", "M7 3h10l-0.6 6.5a4.4 4.4 0 0 1 -8.8 0z", "M7.4 8h9.2")
    val Pulse = line("pulse", "M3 12h4l3 -8 4 16 3 -8h4")
    val History = line("history", "M3 12a9 9 0 1 0 2.6 -6.4L3 8", "M3 3v5h5", "M12 7v5l3 2")
    val User = line("user", "M12 12a4 4 0 1 0 0 -8a4 4 0 0 0 0 8z", "M4 21a8 8 0 0 1 16 0")
    val Plus = line("plus", "M12 5v14", "M5 12h14")
    val Minus = line("minus", "M5 12h14")
    val Close = line("close", "M6 6l12 12", "M18 6L6 18")
    val Share = line("share", "M4 12v7a2 2 0 0 0 2 2h12a2 2 0 0 0 2 -2v-7", "M16 6l-4 -4 -4 4", "M12 2v13")
    val Info = line("info", "M12 21a9 9 0 1 0 0 -18a9 9 0 0 0 0 18z", "M12 16v-4", "M12 8h0.01")
    val Trash = line("trash", "M3 6h18", "M8 6V4h8v2", "M6 6l1 15h10l1 -15")
    val Car = line("car", "M5 16V11l2 -5h10l2 5v5z", "M5 11h14", "M7 16v3", "M17 16v3", "M8 13.5h0.01", "M16 13.5h0.01")
    val Moon = line("moon", "M21 13A9 9 0 1 1 11 3a7 7 0 0 0 10 10z")
    val Peak = line("peak", "M3 17l6 -6 4 4 8 -8", "M15 7h6v6")
    val Check = line("check", "M5 12l5 5L20 7")
    val List = line("list", "M9 6h11", "M9 12h11", "M9 18h11", "M4 6h0.01", "M4 12h0.01", "M4 18h0.01")
    val Flag = line("flag", "M5 21V4", "M5 4h11l-2 4 2 4H5")
    val Star = line("star", "M12 3l2.8 5.7 6.2 0.9 -4.5 4.4 1 6.2L12 17.3l-5.5 2.9 1 -6.2L3 9.6l6.2 -0.9z")
    val Chevron = line("chevron", "M9 6l6 6 -6 6")
}
