package pl.maniak.wikidiary.data

import androidx.compose.ui.graphics.Color
import java.util.Date

data class Tag(
    var id: Long,
    var name: String,
    var category: String? = null,
    val date: Date = Date(),
    val color: Color = Color.Black
)