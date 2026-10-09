package pl.maniak.wikidiary.ui.screens.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.domain.model.FilmMediaType
import pl.maniak.wikidiary.domain.model.FilmMetadata
import pl.maniak.wikidiary.domain.model.FilmSearchResult
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.model.FilmwebSaveState
import pl.maniak.wikidiary.ui.model.FilmwebSearchUiState
import pl.maniak.wikidiary.utils.helpers.formatDateString
import pl.maniak.wikidiary.utils.helpers.WikiParser
import java.util.Date

@Composable
fun FilmwebSearchScreen(
    tags: List<Tag>,
    selectedDate: Date,
    saveState: FilmwebSaveState,
    searchState: FilmwebSearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClick: (ActionClick) -> Unit
) {
    var selectedResult by remember { mutableStateOf<FilmSearchResult?>(null) }
    var title by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf(FilmMediaType.FILM) }
    var posterUrl by remember { mutableStateOf("") }
    var sentencePrefix by remember { mutableStateOf("Obejrzałem") }
    var selectedTag by remember { mutableStateOf<Tag?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(selectedResult) {
        selectedResult?.let { result ->
            title = result.title
            year = result.year?.toString().orEmpty()
            mediaType = result.mediaType
            posterUrl = result.posterUrl.orEmpty()
        }
        listState.animateScrollToItem(0)
    }

    val selectedUrl = selectedResult?.canonicalUrl
    val metadata = selectedUrl?.let { url ->
        year.toIntOrNull()?.let { parsedYear ->
            FilmMetadata(
                canonicalUrl = url,
                title = title.trim(),
                year = parsedYear,
                mediaType = mediaType,
                posterUrl = posterUrl.trim().ifBlank { null }
            )
        }
    }
    val canSave = selectedTag != null &&
        metadata != null &&
        metadata.title.isNotBlank() &&
        year.matches(Regex("""\d{4}"""))
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(680.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "🎬 Filmweb",
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchState.query,
                    onValueChange = onQueryChange,
                    label = { Text("Szukaj filmu lub serialu") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        selectedResult = null
                        onSearch()
                    },
                    enabled = searchState.query.isNotBlank() && !searchState.isLoading
                ) {
                    Text("Szukaj")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (searchState.isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "Pobieranie wyników...",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        searchState.error?.let { error ->
            item {
                Text(text = error, modifier = Modifier.padding(vertical = 8.dp))
                Button(
                    onClick = {
                        selectedResult = null
                        onSearch()
                    },
                    enabled = !searchState.isLoading
                ) {
                    Text("Ponów")
                }
            }
        }

        if (
            searchState.hasSearched &&
            !searchState.isLoading &&
            searchState.error == null &&
            searchState.results.isEmpty()
        ) {
            item {
                Text(
                    text = "Nie znaleziono filmu ani serialu.",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        if (selectedResult == null && searchState.results.isNotEmpty()) {
            item {
                Text(
                    text = "Wyniki wyszukiwania (${searchState.results.size})",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(
                items = searchState.results,
                key = { result -> "${result.mediaType.name}-${result.id}" }
            ) { result ->
                FilmwebSearchResultCard(
                    result = result,
                    onClick = { selectedResult = result }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        selectedResult?.let { result ->
            item {
                TextButton(
                    onClick = { selectedResult = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Wybierz inną pozycję")
                }

                FilmwebMetadataCard(
                    url = result.canonicalUrl,
                    title = title,
                    year = year,
                    mediaType = mediaType,
                    posterUrl = posterUrl,
                    onTitleChange = { title = it },
                    onYearChange = { year = it.filter(Char::isDigit).take(4) },
                    onMediaTypeChange = { mediaType = it },
                    onOpenUrl = { uriHandler.openUri(result.canonicalUrl) }
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sentencePrefix,
                    onValueChange = { sentencePrefix = it },
                    label = { Text("Początek zdania (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tag",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                if (tags.isEmpty()) {
                    Text(
                        text = "Brak dostępnych tagów.",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = tags.sortedBy { it.name },
                            key = { tag -> tag.id }
                        ) { tag ->
                            FilmTagChip(
                                tag = tag,
                                isSelected = tag.id == selectedTag?.id,
                                onClick = { selectedTag = tag }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onClick(ActionClick.TagChangeDate) }) {
                    Text("Data: ${formatDateString(selectedDate)}")
                }

                Text(
                    text = "Podgląd notatki",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.06f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    ),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (canSave && metadata != null) {
                            WikiParser.addFilmNote(sentencePrefix, metadata)
                        } else {
                            "Wybierz tag i uzupełnij tytuł oraz czterocyfrowy rok."
                        },
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (saveState is FilmwebSaveState.Error) {
                    Text(text = saveState.message, modifier = Modifier.padding(vertical = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val tag = selectedTag
                            val filmMetadata = metadata
                            if (tag != null && filmMetadata != null) {
                                onClick(
                                    ActionClick.SaveFilmNote(
                                        tag = tag,
                                        date = selectedDate,
                                        metadata = filmMetadata,
                                        sentencePrefix = sentencePrefix
                                    )
                                )
                            }
                        },
                        enabled = canSave && saveState !is FilmwebSaveState.Saving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (saveState is FilmwebSaveState.Saving) {
                                "Zapisywanie..."
                            } else {
                                "Użyj tego filmu"
                            }
                        )
                    }
                    TextButton(
                        onClick = { onClick(ActionClick.CloseBottomSheet) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Zamknij")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FilmwebSearchResultCard(
    result: FilmSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (result.posterUrl == null) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Brak\nokładki", fontSize = 12.sp)
                }
            } else {
                AsyncImage(
                    model = result.posterUrl,
                    contentDescription = "Okładka: ${result.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${result.mediaType.displayName()} · ${result.year ?: "rok nieznany"}",
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FilmwebMetadataCard(
    url: String,
    title: String,
    year: String,
    mediaType: FilmMediaType,
    posterUrl: String,
    onTitleChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onMediaTypeChange: (FilmMediaType) -> Unit,
    onOpenUrl: () -> Unit
) {
    var typeMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(
            text = "Wybrana pozycja",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = posterUrl.ifBlank { null },
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Text(
                text = url,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenUrl),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Tytuł") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = year,
                onValueChange = onYearChange,
                label = { Text("Rok") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = mediaType.displayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Typ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { typeMenuExpanded = true }
                )
                DropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    FilmMediaType.values().forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                onMediaTypeChange(item)
                                typeMenuExpanded = false
                            }
                        ) {
                            Text(item.displayName())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilmTagChip(
    tag: Tag,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        backgroundColor = if (isSelected) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.surface
        },
        contentColor = if (isSelected) {
            MaterialTheme.colors.onPrimary
        } else {
            MaterialTheme.colors.onSurface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.24f)
            }
        ),
        elevation = 0.dp,
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = tag.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun FilmMediaType.displayName(): String {
    return when (this) {
        FilmMediaType.FILM -> "Film"
        FilmMediaType.SERIAL -> "Serial"
    }
}
