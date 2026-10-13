package pl.maniak.wikidiary.feature.people

import org.koin.dsl.module
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.people.data.PersonMapper
import pl.maniak.wikidiary.feature.people.data.PersonRepositoryImpl
import pl.maniak.wikidiary.feature.people.domain.PersonRepository

val peopleModule = module {
    single { get<WikiNoteDatabase>().personDao() }
    factory { PersonMapper() }
    single<PersonRepository> { PersonRepositoryImpl(get(), get()) }
}
