package pl.maniak.wikidiary.feature.pictograms.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.pictograms.domain.Pictogram
import pl.maniak.wikidiary.feature.pictograms.domain.PictogramEntry
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.core.wiki.WikiParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val pictogramIcons = listOf(
    "❤️",
    "🩺",
    "🤝",
    "🏥",
    "🎓",
    "💰",
    "🚶",
    "🎧",
    "🔨",
    "👋",
    "🌳",
    "🧱",
    "🎴",
    "⭐",
    "✅"
)

private val additionalPictogramIcons = listOf(
    "📅",
    "✈️",
    "🍎",
    "🧹",
    "🛒",
    "🎵",
    "📚",
    "🏃",
    "🧘",
    "💡",
    "🛠️",
    "🎯",
    "💊",
    "🍽️",
    "🐶"
)

@Composable
fun PictogramManagerScreen(
    pictograms: List<Pictogram>,
    pictogramEntries: List<PictogramEntry>,
    tags: List<Tag>,
    selectedDate: Date,
    error: String?,
    onClick: (ActionClick) -> Unit
) {
    var selectedTag by remember(tags) { mutableStateOf(tags.firstOrNull()) }
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(pictogramIcons.first()) }
    var editingId by remember { mutableStateOf(0L) }
    var showAdditionalIcons by remember { mutableStateOf(false) }
    val selectedDay = selectedDate.toDayKey()
    val selectedEntries = pictogramEntries.filter {
        it.tag == selectedTag?.name && it.date.toDayKey() == selectedDay
    }

    LaunchedEffect(tags) {
        if (selectedTag != null && tags.none { it.id == selectedTag?.id }) {
            selectedTag = tags.firstOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SheetHandle(modifier = Modifier.align(Alignment.CenterHorizontally))

        Text(
            text = "Pictogramy",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tag dla pictogramu",
            style = MaterialTheme.typography.subtitle2
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                TagChoice(
                    tag = tag,
                    selected = tag.id == selectedTag?.id,
                    onClick = { selectedTag = tag }
                )
            }
        }
        if (tags.isEmpty()) {
            Text(
                text = "Najpierw utwórz tag, aby przypisać pictogram.",
                style = MaterialTheme.typography.body2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (editingId == 0L) "Nowy pictogram" else "Edytuj pictogram",
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nazwa") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val availableIcons = if (showAdditionalIcons) {
                pictogramIcons + additionalPictogramIcons
            } else {
                pictogramIcons
            }
            availableIcons.forEach { candidate ->
                IconChoice(
                    icon = candidate,
                    selected = candidate == icon,
                    onClick = { icon = candidate }
                )
            }
            if (!showAdditionalIcons) {
                IconChoice(
                    icon = "+",
                    selected = false,
                    onClick = { showAdditionalIcons = true }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onClick(ActionClick.SavePictogram(editingId, name, icon))
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (editingId == 0L) "Zapisz" else "Aktualizuj")
            }
            if (editingId != 0L) {
                TextButton(
                    onClick = {
                        editingId = 0L
                        name = ""
                        icon = pictogramIcons.first()
                    }
                ) {
                    Text("Anuluj")
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lista pictogramów",
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (pictograms.isEmpty()) {
            Text(
                text = "Brak pictogramów. Utwórz pierwszy powyżej.",
                style = MaterialTheme.typography.body2
            )
        } else {
            pictograms.forEach { pictogram ->
                PictogramRow(
                    pictogram = pictogram,
                    attached = selectedEntries.any { it.pictogramId == pictogram.id },
                    onAttach = {
                        selectedTag?.let { tag ->
                            onClick(
                                ActionClick.AddPictogramToTag(
                                    pictogramId = pictogram.id,
                                    tag = tag,
                                    date = selectedDate
                                )
                            )
                        }
                    },
                    onEdit = {
                        editingId = pictogram.id
                        name = pictogram.name
                        icon = pictogram.icon
                        showAdditionalIcons = pictogram.icon in additionalPictogramIcons
                    },
                    onDelete = { onClick(ActionClick.DeletePictogram(pictogram.id)) }
                )
            }
        }
    }
}

@Composable
private fun SheetHandle(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(bottom = 8.dp)
            .size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(50)),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
    ) {}
}

@Composable
private fun TagChoice(
    tag: Tag,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        backgroundColor = if (selected) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colors.onPrimary
        } else {
            MaterialTheme.colors.onSurface
        },
        elevation = 0.dp
    ) {
        Text(
            text = tag.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun IconChoice(
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (selected) {
            MaterialTheme.colors.primary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colors.surface
        },
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.24f)
            }
        )
    ) {
        Text(
            text = icon,
            modifier = Modifier.padding(8.dp),
            fontSize = 24.sp
        )
    }
}

@Composable
private fun PictogramRow(
    pictogram: Pictogram,
    attached: Boolean,
    onAttach: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = pictogram.icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = pictogram.name)
            Text(
                text = WikiParser.pictogramFileName(pictogram.name),
                style = MaterialTheme.typography.caption
            )
        }
        Button(
            onClick = onAttach,
            enabled = !attached
        ) {
            Text(if (attached) "Dodano" else "Dodaj")
        }
        IconButton(onClick = onEdit) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edytuj pictogram")
        }
        IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń pictogram")
        }
    }
}

private fun Date.toDayKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(this)
