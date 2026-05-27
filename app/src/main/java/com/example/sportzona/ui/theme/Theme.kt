package com.example.sportzona.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MinimalistColorScheme = lightColorScheme(
    primary = SportBlue,
    onPrimary = White,
    secondary = Black,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    error = ErrorRed,
    onError = White,
    // Postavljamo i kontejnere na isto radi minimalizma
    primaryContainer = White,
    onPrimaryContainer = SportBlue,
    secondaryContainer = White,
    onSecondaryContainer = Black,
    surfaceVariant = White,
    onSurfaceVariant = GrayText,
    outline = LightGray
)

@Composable
fun SportZonaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinimalistColorScheme,
        typography = Typography,
        content = content
    )
}
