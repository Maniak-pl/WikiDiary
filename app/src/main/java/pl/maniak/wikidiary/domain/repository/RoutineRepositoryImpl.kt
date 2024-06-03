package pl.maniak.wikidiary.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.data.Routine
import pl.maniak.wikidiary.data.database.RoutineDao
import pl.maniak.wikidiary.data.mapper.RoutineMapper

class RoutineRepositoryImpl(
    private val mapper: RoutineMapper,
    private val dao: RoutineDao
) : RoutineRepository {
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
