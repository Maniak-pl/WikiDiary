package pl.maniak.wikidiary.feature.routines.data

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.routines.domain.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.feature.routines.domain.Routine

class RoutineRepositoryImpl(
    private val mapper: RoutineMapper,
    private val dao: RoutineDao
) : RoutineRepository {
    override fun observeRoutines(): Flow<List<Routine>> =
        dao.observeAll().map { routines -> routines.map(mapper::mapToDomain) }

    override suspend fun getRoutines(): List<Routine> = withContext(Dispatchers.IO) {
        dao.getAllRoutines().map { mapper.mapToDomain(it) }
    }

    override suspend fun saveRoutine(routine: Routine) = withContext(Dispatchers.IO) {
        dao.insert(mapper.mapToEntity(routine))
    }

    override suspend fun deleteRoutine(id: Long)  = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }
    override suspend fun deleteAllRoutines() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }

    override suspend fun updateAll(updatedRoutines: List<Routine>) = withContext(Dispatchers.IO) {
        dao.updateAll(updatedRoutines.map { mapper.mapToEntity(it) })
    }
}
