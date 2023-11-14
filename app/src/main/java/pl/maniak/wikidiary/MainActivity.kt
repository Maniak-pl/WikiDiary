package pl.maniak.wikidiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import pl.maniak.wikidiary.data.WikiNote
import pl.maniak.wikidiary.ui.screens.PreparingNoteScreen
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val list = listOf(
            WikiNote(1, "ToDo", "Taking out the trash on Saturday", Date(), false),
            WikiNote(2, "Today", "I got up at 5:00", Date(), false)
        )

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    PreparingNoteScreen(list)
                }
            }
        }
    }
}