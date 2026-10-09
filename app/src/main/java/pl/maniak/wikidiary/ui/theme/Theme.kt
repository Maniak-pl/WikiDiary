package pl.maniak.wikidiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = DarkColorPrimary,
    primaryVariant = DarkColorPrimaryVariant,
    secondary = DarkColorAccent,
    secondaryVariant = DarkColorAccent,
    background = DarkColorBackground,
    surface = DarkColorSurface,
    onPrimary = DarkColorOnPrimary,
    onSecondary = DarkColorOnPrimary,
    onBackground = DarkColorOnBackground,
    onSurface = DarkColorOnSurface
)

private val LightColorPalette = lightColors(
    primary = ColorPrimary,
    primaryVariant = ColorPrimaryVariant,
    secondary = ColorAccent,
    secondaryVariant = ColorAccent,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1B1B1B),
    onSurface = Color(0xFF1B1B1B)
)

@Composable
fun WikiTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}