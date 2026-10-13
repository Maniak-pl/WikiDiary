package pl.maniak.wikidiary.feature.routines.data

import pl.maniak.wikidiary.feature.routines.domain.Routine

class RoutineMapper {
    fun mapToDomain(entity: RoutineEntity): Routine {
        return Routine(entity.id, entity.name, entity.isCompleted)
    }

    fun mapToEntity(domain: Routine): RoutineEntity {
        return RoutineEntity(domain.id, domain.name, domain.isCompleted)
    }
}
