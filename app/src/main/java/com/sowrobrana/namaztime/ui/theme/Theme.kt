package com.sowrobrana.namaztime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EmeraldDark = Color(0xFF0B3D2E)
private val Emerald = Color(0xFF0E7A5B)
private val EmeraldLight = Color(0xFFD9EDE4)
private val Gold = Color(0xFFC9962E)
private val GoldLight = Color(0xFFE8B84B)
private val Paper = Color(0xFFF7F5EE)
private val Ink = Color(0xFF1A2E26)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = EmeraldDark,
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E7C3),
    onSecondaryContainer = Color(0xFF5C430A),
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEFEAE0),
    onSurfaceVariant = Color(0xFF5A6B62),
    outline = Color(0xFFC9D4CC),
)

@Composable
fun NamazTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
