package pl.maniak.wikidiary.data.mapper

import pl.maniak.wikidiary.data.database.PictogramEntity
import pl.maniak.wikidiary.domain.model.Pictogram

class PictogramMapper {
    fun mapToDomain(entity: PictogramEntity): Pictogram = Pictogram(
        id = entity.id,
        name = entity.name,
        icon = entity.icon
    )

    fun mapToEntity(domain: Pictogram): PictogramEntity = PictogramEntity(
        id = domain.id,
        name = domain.name,
        icon = domain.icon
    )
}
