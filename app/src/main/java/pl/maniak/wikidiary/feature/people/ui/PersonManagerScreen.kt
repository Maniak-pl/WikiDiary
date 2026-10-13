package pl.maniak.wikidiary.feature.people.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender
import pl.maniak.wikidiary.ui.model.ActionClick

@Composable
fun PersonManagerScreen(
    people: List<Person>,
    error: String?,
    onClick: (ActionClick) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(PersonGender.NEUTRAL) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 4.dp)
                .background(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.38f),
                    shape = RoundedCornerShape(50)
                )
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Osoby",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Imię i nazwisko") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Ikona osoby", style = MaterialTheme.typography.subtitle2)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PersonGender.entries.forEach { option ->
                Button(
                    onClick = { gender = option },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (gender == option) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.surface
                        },
                        contentColor = if (gender == option) {
                            MaterialTheme.colors.onPrimary
                        } else {
                            MaterialTheme.colors.onSurface
                        }
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(option.icon)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                onClick(ActionClick.AddPerson(name = name, gender = gender))
                name = ""
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dodaj osobę")
        }

        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.body2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Zapisane osoby", style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(6.dp))
        if (people.isEmpty()) {
            Text(
                text = "Brak osób. Dodaj osobę, aby używać wzmianek @.",
                style = MaterialTheme.typography.body2
            )
        } else {
            people.forEach { person ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = person.gender.icon)
                    Text(
                        text = person.name,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    IconButton(onClick = { onClick(ActionClick.DeletePerson(person.id)) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń osobę"
                        )
                    }
                }
            }
        }
    }
}
