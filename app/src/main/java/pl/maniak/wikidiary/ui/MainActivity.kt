package pl.maniak.wikidiary.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import pl.maniak.wikidiary.ui.screens.MainScreen
import pl.maniak.wikidiary.ui.theme.WikiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WikiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(
                        viewModel.notes.value,
                        viewModel.tags.value,
                        onAddWikiNote = { note ->
                            lifecycleScope.launch {
                                viewModel.saveWikiNote(note)
                            }
                        },
                        onAddTagClick = { tag ->
                            lifecycleScope.launch {
                                viewModel.saveTag(tag)
                            }
                        }
                    )

                }
            }
        }
    }
}