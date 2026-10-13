package pl.maniak.wikidiary.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.android.ext.android.inject
import pl.maniak.wikidiary.core.config.Config
import pl.maniak.wikidiary.core.ui.theme.WikiTheme

// Default scrims of enableEdgeToEdge, reused when the theme is chosen in settings.
private val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

class MainActivity : ComponentActivity() {

    private val config: Config by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val chosenTheme by config.darkTheme.collectAsState()
            val darkTheme = chosenTheme ?: isSystemInDarkTheme()
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { darkTheme },
                )
                onDispose {}
            }
            WikiTheme(darkTheme = darkTheme) {
                WikiApp()
            }
        }
    }
}
