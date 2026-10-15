package pl.maniak.wikidiary.feature.books.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.UUID
import kotlinx.coroutines.flow.first
import org.koin.androidx.compose.koinViewModel
import pl.maniak.wikidiary.core.util.formatDateString
import pl.maniak.wikidiary.core.wiki.WikiParser

@Composable
fun BooksRoute(
    initialQuery: String,
    onPickDate: () -> Unit,
    onClose: () -> Unit,
    viewModel: BooksViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val sessionId = rememberSaveable { UUID.randomUUID().toString() }
    LaunchedEffect(sessionId) {
        viewModel.open(initialQuery, sessionId)
        viewModel.uiState.first { it.saved }
        onClose()
    }
    BooksScreen(state, viewModel::onIntent, onPickDate, onClose)
}

@Composable
private fun BooksScreen(
    state: BooksUiState,
    onIntent: (BooksIntent) -> Unit,
    onPickDate: () -> Unit,
    onClose: () -> Unit,
) {
    val list = rememberLazyListState()
    LaunchedEffect(state.selected?.canonicalUrl) { list.scrollToItem(0) }
    val uriHandler = LocalUriHandler.current
    val enabled = !state.isSaving && !state.saved
    val selected = state.selected
    val preview = remember(selected, state.prefix, state.year) {
        selected?.let { WikiParser.addBookNote(state.prefix, it.copy(year = state.year.toIntOrNull())) }
    }
    LazyColumn(
        state = list,
        modifier = Modifier.fillMaxWidth().heightIn(max = 680.dp).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("📚 Lubimyczytać.pl", style = MaterialTheme.typography.headlineSmall) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onIntent(BooksIntent.Query(it)) },
                    label = { Text("Tytuł książki lub autor") },
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { onIntent(BooksIntent.Search) }, enabled = enabled && !state.isLoading) {
                    Text("Szukaj")
                }
            }
        }
        if (state.isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
        if (selected == null) {
            if (state.hasSearched && state.results.isEmpty() && !state.isLoading && state.error == null) {
                item { Text("Nie znaleziono książek.") }
            }
            items(state.results, key = { it.canonicalUrl }) { book ->
                ElevatedCard(
                    onClick = { onIntent(BooksIntent.Select(book)) },
                    enabled = enabled && !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (book.coverUrl != null) {
                            AsyncImage(
                                model = book.coverUrl,
                                contentDescription = "Okładka: ${book.title}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(64.dp, 92.dp)
                            )
                        } else {
                            Text("Brak\nokładki", modifier = Modifier.size(64.dp, 92.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(book.title, style = MaterialTheme.typography.titleMedium)
                            Text(book.author)
                        }
                    }
                }
            }
        } else {
            item {
                TextButton(onClick = { onIntent(BooksIntent.BackToResults) }, enabled = enabled) {
                    Text("Wybierz inną książkę")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AsyncImage(
                        model = selected.coverUrl,
                        contentDescription = "Okładka: ${selected.title}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(80.dp, 116.dp)
                    )
                    TextButton(onClick = { uriHandler.openUri(selected.canonicalUrl) }) {
                        Text("Otwórz w Lubimyczytać.pl")
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = selected.title,
                    onValueChange = { onIntent(BooksIntent.Title(it)) },
                    label = { Text("Tytuł") },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = selected.author,
                    onValueChange = { onIntent(BooksIntent.Author(it)) },
                    label = { Text("Autor / autorzy") },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = state.year,
                    onValueChange = { onIntent(BooksIntent.Year(it)) },
                    label = { Text("Rok wydania (opcjonalnie)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = state.prefix,
                    onValueChange = { onIntent(BooksIntent.Prefix(it)) },
                    label = { Text("Początek zdania") },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { Text("Tag", style = MaterialTheme.typography.titleMedium) }
            item {
                if (state.tags.isEmpty()) Text("Brak tagów. Utwórz tag w Dzienniku.")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.tags, key = { it.id }) { tag ->
                        FilterChip(
                            selected = tag.id == state.tag?.id,
                            onClick = { onIntent(BooksIntent.TagSelected(tag)) },
                            label = { Text(tag.name) },
                            enabled = enabled
                        )
                    }
                }
            }
            item {
                TextButton(onClick = onPickDate, enabled = enabled) {
                    Text("Data: ${formatDateString(state.date)}")
                }
                Text("Podgląd notatki", style = MaterialTheme.typography.titleMedium)
                Text(preview.orEmpty())
                Text(
                    "Eksport dodaje znacznik okładki w przestrzeni books. Grafikę należy wgrać do Wiki osobno.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                Button(
                    onClick = { onIntent(BooksIntent.Save) },
                    enabled = enabled && !state.isLoading && state.tag != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isSaving) "Zapisywanie…" else "Zapisz przeczytaną książkę")
                }
            }
        }
        item { TextButton(onClick = onClose, enabled = !state.isSaving) { Text("Zamknij") } }
    }
}
