package pl.maniak.wikidiary.domain.repository

import pl.maniak.wikidiary.data.Routine

interface RoutineRepository {
    suspend fun getRoutines(): List<Routine>

    suspend fun saveRoutine(routine: Routine)

    suspend fun deleteRoutine(id: Long)

    suspend fun deleteAllRoutines()

    suspend fun updateAll(updatedRoutines: List<Routine>)
}
