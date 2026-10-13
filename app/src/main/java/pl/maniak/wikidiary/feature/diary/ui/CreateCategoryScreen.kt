package pl.maniak.wikidiary.feature.diary.ui

import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.feature.diary.domain.Category


@Composable
fun CreateCategoryRoute(viewModel: TagEditorViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    CreateCategoryScreen(categories = state.categories, onIntent = viewModel::onIntent)
}

@Composable
fun CreateCategoryScreen(
    categories: List<Category>,
    onIntent: (TagEditorIntent) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\uD83D\uDCC1 Create Category",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = categoryName,
            onValueChange = {
                categoryName = it
            },
            label = { Text("Category Name") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onIntent(TagEditorIntent.SaveCategory(name = categoryName))
                categoryName = ""
            })
        )

        Spacer(modifier = Modifier.height(16.dp))

        DropdownMenuCategoriesView(categories, selectedCategory, onCategorySelected = {
            selectedCategory = it
            categoryName = it?.name ?: ""
        })

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    if (selectedCategory != null) {
                        onIntent(TagEditorIntent.SaveCategory(selectedCategory!!.id, categoryName))
                    } else {
                        onIntent(TagEditorIntent.SaveCategory(name = categoryName))
                    }
                    categoryName = ""
                    selectedCategory = null
                },
                enabled = categoryName.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (selectedCategory == null) "\uD83D\uDCBE Save" else "✏\uFE0F Update")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    selectedCategory?.let {
                        onIntent(TagEditorIntent.DeleteCategory(id = it.id))
                        categoryName = ""
                        selectedCategory = null
                    }
                },
                enabled = selectedCategory != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("❌ Delete")
            }
        }
    }
}