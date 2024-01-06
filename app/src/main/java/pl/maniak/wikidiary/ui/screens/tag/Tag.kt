package pl.maniak.wikidiary.ui.screens.tag

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tag(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    border: BorderStroke? = BorderStroke(2.dp, Color.Black),
    colors: TagColors = TagDefaults.tagColors(),
    content: @Composable () -> Unit,
) {
    val contentColor by colors.contentColor(enabled)
    Surface(
        modifier = modifier
            .wrapContentSize()
            .semantics { role = Role.Button }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .combinedClickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = rememberRipple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = shape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(1.0f),
        border = border,
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(
                    minHeight = TagDefaults.MinHeight
                )
                .wrapContentSize()
                .padding(vertical = 4.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

object TagDefaults {

    val MinHeight = 32.dp

    @Composable
    fun tagColors(
        backgroundColor: Color = Color.White
            .compositeOver(MaterialTheme.colors.surface),
        contentColor: Color = Color.Black,
    ): TagColors = DefaultTagColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        disabledBackgroundColor = backgroundColor.copy(alpha = ContentAlpha.disabled),
        disabledContentColor = contentColor.copy(alpha = ContentAlpha.disabled),
    )
}


private class DefaultTagColors(
    private val backgroundColor: Color,
    private val contentColor: Color,
    private val disabledBackgroundColor: Color,
    private val disabledContentColor: Color,
) : TagColors {
    @Composable
    override fun backgroundColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) backgroundColor else disabledBackgroundColor)
    }

    @Composable
    override fun contentColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) contentColor else disabledContentColor)
    }
}

interface TagColors {
    @Composable
    fun backgroundColor(enabled: Boolean): State<Color>

    @Composable
    fun contentColor(enabled: Boolean): State<Color>
}

@Composable
@Preview(showBackground = true)
fun TagPreview() {
    Tag(
        onClick = { },
        onLongClick = { },
        colors = TagDefaults.tagColors(
            backgroundColor = Color.Black,
            contentColor = Color.White
        ),
        content = {
            Text(text = "ToDo")
        }
    )
}