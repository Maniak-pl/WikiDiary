package pl.maniak.wikidiary.ui.screens.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import pl.maniak.wikidiary.data.Category
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.screens.view.DropdownMenuCategoriesView


@Composable
fun CreateCategoryScreen(
    categories: List<Category>,
    onClick: (ActionClick) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .width(40.dp)
                .height(4.dp)
                .background(Color.Gray, shape = RoundedCornerShape(50))
        )

        Text(
            text = "\uD83D\uDCC1 Create Category",
            style = MaterialTheme.typography.h6,
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
                onClick(ActionClick.AddCategory(name = categoryName))
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
                        onClick(ActionClick.UpdateCategory(selectedCategory!!.id, categoryName))
                    } else {
                        onClick(ActionClick.AddCategory(name = categoryName))
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

            Button(
                onClick = {
                    selectedCategory?.let {
                        onClick(ActionClick.DeleteCategory(id = it.id))
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