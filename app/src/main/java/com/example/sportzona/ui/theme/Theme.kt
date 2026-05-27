package com.example.sportzona.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEDC8),
    onPrimaryContainer = BrandPrimary,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFBE9E7),
    onSecondaryContainer = BrandSecondary,
    background = LightBg,
    onBackground = DeepText,
    surface = PureWhite,
    onSurface = DeepText,
    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = SubtitleText,
    outline = BorderLine,
    error = FailRed,
    onError = Color.White
)

@Composable
fun SportZonaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
