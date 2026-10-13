package pl.maniak.wikidiary.feature.routines.domain

import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun observeRoutines(): Flow<List<Routine>>

    suspend fun getRoutines(): List<Routine>

    suspend fun saveRoutine(routine: Routine)

    suspend fun deleteRoutine(id: Long)

    suspend fun deleteAllRoutines()

    suspend fun updateAll(updatedRoutines: List<Routine>)
}
