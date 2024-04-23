package pl.maniak.wikidiary.ui.screens.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.data.Category
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.screens.bottomsheet.ScreenValues.SpacerHeight
import pl.maniak.wikidiary.ui.screens.view.DropdownMenuCategoriesView

private object ScreenValues {
    val SpacerHeight = 16.dp
}

@Composable
fun CreateProjectScreen(
    categories: List<Category>,
    onClick: (ActionClick) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            text = stringResource(id = R.string.label_create_project),
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(SpacerHeight))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = stringResource(id = R.string.label_name)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(SpacerHeight))

        DropdownMenuCategoriesView(categories = categories, selectedCategory = selectedCategory, onCategorySelected = {
            selectedCategory = it
            category = it?.name ?: ""
        })

        Spacer(modifier = Modifier.height(SpacerHeight))

        Button(
            onClick = {
                if (name.isNotBlank() && category.isNotBlank()) {
                    onClick(ActionClick.ConfirmCreateProject(name = name, category = category))
                    name = ""
                    category = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = stringResource(id = R.string.ok))
        }
    }
}

