package pl.maniak.wikidiary.feature.pictograms.data

import pl.maniak.wikidiary.feature.pictograms.domain.Pictogram

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
