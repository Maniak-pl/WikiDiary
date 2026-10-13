package pl.maniak.wikidiary.feature.pictograms.data

import pl.maniak.wikidiary.feature.pictograms.domain.PictogramEntry
import java.util.Date

class PictogramEntryMapper {
    fun mapToDomain(record: PictogramEntryRecord): PictogramEntry = PictogramEntry(
        id = record.id,
        pictogramId = record.pictogramId,
        tag = record.tag,
        category = record.category,
        date = Date(record.date),
        name = record.name,
        icon = record.icon
    )

    fun mapToEntity(domain: PictogramEntry): PictogramEntryEntity = PictogramEntryEntity(
        id = domain.id,
        pictogramId = domain.pictogramId,
        tag = domain.tag,
        category = domain.category,
        date = domain.date.time
    )
}
