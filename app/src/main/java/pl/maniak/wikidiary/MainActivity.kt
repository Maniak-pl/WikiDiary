package pl.maniak.wikidiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import org.koin.android.ext.android.inject
import pl.maniak.wikidiary.domain.WikiNoteRepository
import pl.maniak.wikidiary.ui.screens.MainScreen
import pl.maniak.wikidiary.ui.theme.WikiTheme

class MainActivity : ComponentActivity() {

    private val repository: WikiNoteRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WikiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(repository.getNotes(), onAddWikiNote = { note ->
                        repository.saveNote(note)
                    })
                }
            }
        }
    }
}