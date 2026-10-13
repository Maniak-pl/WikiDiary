package pl.maniak.wikidiary.feature.people.domain


interface PersonRepository {
    suspend fun getPeople(): List<Person>

    suspend fun savePerson(person: Person)

    suspend fun deletePerson(id: Long)
}
