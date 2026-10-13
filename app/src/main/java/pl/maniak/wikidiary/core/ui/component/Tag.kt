package pl.maniak.wikidiary.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tag(
    modifier: Modifier = Modifier,
    text: String = "Tag",
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    shape: Shape = CircleShape,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    colors: TagColors = TagDefaults.tagColors(),
    content: @Composable () -> Unit = {
        Text(
            text = text,
            style = TextStyle(fontSize = 12.sp)
        )
    },
) {
    Surface(
        modifier = modifier
            .wrapContentSize()
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .clip(shape)
            .semantics { role = Role.Button }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = shape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        border = border,
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = TagDefaults.MinHeight)
                .padding(vertical = 6.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Immutable
data class TagColors(val containerColor: Color, val contentColor: Color)

object TagDefaults {

    val MinHeight = 32.dp

    @Composable
    fun tagColors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
    ): TagColors = TagColors(containerColor, contentColor)

    /** Colours for a user-picked tag colour, keeping the label readable on any background. */
    fun filledTagColors(containerColor: Color): TagColors = TagColors(
        containerColor = containerColor,
        contentColor = if (containerColor.luminance() > 0.5f) Color.Black else Color.White
    )
}

@Composable
@Preview(showBackground = true)
fun TagPreview() {
    Tag(
        onClick = { },
        colors = TagDefaults.filledTagColors(Color.Black),
        content = { Text(text = "ToDo") }
    )
}
