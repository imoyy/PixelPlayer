package com.theveloper.pixelplay.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors

private val WearColors = Colors(
    primary = androidx.compose.ui.graphics.Color(0xFFBB86FC),
    primaryVariant = androidx.compose.ui.graphics.Color(0xFF3700B3),
    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6),
    secondaryVariant = androidx.compose.ui.graphics.Color(0xFF018786),
    error = androidx.compose.ui.graphics.Color(0xFFCF6679),
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onError = androidx.compose.ui.graphics.Color.Black,
)

@Composable
fun WearPixelPlayTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = WearColors,
        content = content,
    )
}
