package pl.maniak.wikidiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = ColorPrimary,
    primaryVariant = ColorSecondary,
    secondary = ColorAccent
)

private val LightColorPalette = lightColors(
    primary = ColorPrimary,
    primaryVariant = ColorSecondary,
    secondary = ColorAccent
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