package pl.maniak.wikidiary.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AddScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    )
    {
        Text(modifier = Modifier.wrapContentSize().align(Alignment.Center), text = "Add screen")
    }
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    AddScreen()
}