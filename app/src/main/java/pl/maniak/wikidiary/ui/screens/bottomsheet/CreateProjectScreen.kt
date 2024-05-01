package pl.maniak.wikidiary.ui.screens.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.data.Category
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.ui.model.ActionClick
import pl.maniak.wikidiary.ui.screens.bottomsheet.ScreenValues.SpacerHeight
import pl.maniak.wikidiary.ui.screens.view.DropdownMenuCategoriesView

private object ScreenValues {
    val SpacerHeight = 16.dp
}

@Composable
fun CreateProjectScreen(
    tag: Tag?,
    categories: List<Category>,
    onClick: (ActionClick) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var color by remember { mutableStateOf(Color.Red) }

    LaunchedEffect(tag) {
        tag?.let {
            name = it.name
            category = it.category ?: ""
            selectedCategory = categories.find { category -> category.name == it.category }
            color = it.color
        } ?: run {
            name = ""
            category = ""
            selectedCategory = null
            color = Color.Red
        }
    }

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

        ColorPicker(
            selectedColor = color,
            onColorSelected = { color = it }
        )

        Spacer(modifier = Modifier.height(SpacerHeight))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = stringResource(id = R.string.label_name)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(SpacerHeight))

        DropdownMenuCategoriesView(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = {
                selectedCategory = it
                category = it?.name ?: ""
            })

        Spacer(modifier = Modifier.height(SpacerHeight))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    onClick(
                        ActionClick.ConfirmProject(
                            tag?.id ?: 0,
                            name = name,
                            category = category,
                            color = color
                        )
                    )
                    name = ""
                    selectedCategory = null
                },
                enabled = tag != null || name.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (tag == null) "\uD83D\uDCBE Save" else "✏\uFE0F Update")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    tag?.let {
                        onClick(ActionClick.DeleteTag(it.id))
                        name = ""
                        selectedCategory = null
                    }
                },
                enabled = tag != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("❌ Delete")
            }
        }
    }
}


@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFF0000FF),
        Color(0xFF673AB7),
        Color(0xFFE91E1E),
        Color(0xFFFF5722),
        Color(0xFF808000),
        Color(0xFF4CAF50),
        Color(0xFF964B00),
        Color(0xFF808080),
        Color(0xFF000000),
    )

    LazyRow {
        items(colors) { color ->
            ColorCircle(color = color, isSelected = color == selectedColor) {
                onColorSelected(color)
            }
        }
    }
}

@Composable
fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color.Black else Color.Transparent

    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(20.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(color = color, shape = CircleShape)
            .border(2.dp, borderColor, shape = CircleShape)
    )
}

