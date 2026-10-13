package pl.maniak.wikidiary.feature.people.domain

import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun observePeople(): Flow<List<Person>>

    suspend fun getPeople(): List<Person>

    suspend fun savePerson(person: Person)

    suspend fun deletePerson(id: Long)
}
