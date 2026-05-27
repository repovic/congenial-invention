package com.example.sportzona.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SportZonaColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = DarkBlue,
    secondary = GymBlack,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = GymBlack,
    background = BackgroundWhite,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = TextGray,
    outline = DividerGray,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun SportZonaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SportZonaColorScheme,
        typography = Typography,
        content = content
    )
}
