package pl.maniak.wikidiary.feature.routines.domain


interface RoutineRepository {
    suspend fun getRoutines(): List<Routine>

    suspend fun saveRoutine(routine: Routine)

    suspend fun deleteRoutine(id: Long)

    suspend fun deleteAllRoutines()

    suspend fun updateAll(updatedRoutines: List<Routine>)
}
