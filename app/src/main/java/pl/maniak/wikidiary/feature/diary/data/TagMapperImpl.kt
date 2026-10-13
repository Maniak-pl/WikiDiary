package pl.maniak.wikidiary.feature.diary.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import pl.maniak.wikidiary.feature.diary.domain.Tag

class TagMapperImpl : TagMapper {
    override fun mapToDomain(entity: TagEntity): Tag = Tag(
        entity.id,
        entity.tag,
        entity.category,
        entity.date,
        entity.color.toComposeColor()
    )

    override fun mapToEntity(domain: Tag): TagEntity = TagEntity(
        domain.id,
        domain.name,
        domain.category,
        domain.date,
        domain.color.toDatabaseColor()
    )

    private fun Color.toDatabaseColor(): Int = this.toArgb()

    private fun Int.toComposeColor(): Color = Color(this)
}