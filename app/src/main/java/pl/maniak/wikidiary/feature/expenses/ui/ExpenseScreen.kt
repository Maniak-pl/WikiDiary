package pl.maniak.wikidiary.feature.expenses.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import java.util.UUID
import org.koin.androidx.compose.koinViewModel
import pl.maniak.wikidiary.core.util.formatDateString
import pl.maniak.wikidiary.feature.expenses.domain.PaymentMethod

@Composable
fun ExpenseRoute(
    initialInput: String,
    initialTagId: Long?,
    onPickDate: () -> Unit,
    onClose: () -> Unit,
    viewModel: ExpenseViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val sessionId = rememberSaveable { UUID.randomUUID().toString() }
    LaunchedEffect(sessionId) {
        viewModel.open(initialInput, initialTagId, sessionId)
        viewModel.uiState.first { it.saved }
        onClose()
    }
    ExpenseScreen(state, viewModel::onIntent, onPickDate)
}

@Composable
private fun ExpenseScreen(state: ExpenseUiState, onIntent: (ExpenseIntent) -> Unit, onPickDate: () -> Unit) {
    var tagsExpanded by remember { mutableStateOf(false) }
    val enabled = !state.isLoading && !state.isSaving && !state.saved
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dodaj wydatek", style = MaterialTheme.typography.headlineSmall)
        TextButton(onClick = onPickDate, enabled = enabled) {
            Text("Data: ${formatDateString(state.date)}")
        }
        OutlinedTextField(
            value = state.amount,
            onValueChange = { onIntent(ExpenseIntent.Amount(it)) },
            label = { Text("Kwota (zł)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = { onIntent(ExpenseIntent.Description(it)) },
            label = { Text("Opis, np. OLX – Kurtka") },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Sposób płatności", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentMethod.entries.forEach { method ->
                FilterChip(
                    selected = state.paymentMethod == method,
                    onClick = { onIntent(ExpenseIntent.PaymentSelected(method)) },
                    label = { Text("${method.icon} ${method.label}") },
                    enabled = enabled
                )
            }
        }
        Box {
            OutlinedButton(onClick = { tagsExpanded = true }, enabled = enabled) {
                Text("Tag: ${state.tag.name}")
            }
            DropdownMenu(expanded = tagsExpanded, onDismissRequest = { tagsExpanded = false }) {
                state.tags.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag.name) },
                        onClick = {
                            onIntent(ExpenseIntent.TagSelected(tag))
                            tagsExpanded = false
                        }
                    )
                }
            }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { onIntent(ExpenseIntent.Save) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Zapisywanie…" else "Zapisz wydatek")
        }
    }
}
