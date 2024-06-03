package pl.maniak.wikidiary.data.mapper

import pl.maniak.wikidiary.data.Routine
import pl.maniak.wikidiary.data.database.RoutineEntity

class RoutineMapper {
    fun mapToDomain(entity: RoutineEntity): Routine {
        return Routine(entity.id, entity.name, entity.isCompleted)
    }

    fun mapToEntity(domain: Routine): RoutineEntity {
        return RoutineEntity(domain.id, domain.name, domain.isCompleted)
    }
}
