package pl.maniak.wikidiary.testing

import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.diary.data.TagMapperImpl
import pl.maniak.wikidiary.feature.diary.data.TagRepositoryImpl
import pl.maniak.wikidiary.feature.diary.data.WikiNoteMapperImpl
import pl.maniak.wikidiary.feature.diary.data.WikiNoteRepositoryImpl
import pl.maniak.wikidiary.feature.diary.domain.TagRepository
import pl.maniak.wikidiary.feature.diary.domain.WikiNoteRepository
import pl.maniak.wikidiary.feature.people.data.PersonMapper
import pl.maniak.wikidiary.feature.people.data.PersonRepositoryImpl
import pl.maniak.wikidiary.feature.people.domain.PersonRepository
import pl.maniak.wikidiary.feature.routines.data.RoutineMapper
import pl.maniak.wikidiary.feature.routines.data.RoutineRepositoryImpl
import pl.maniak.wikidiary.feature.routines.domain.RoutineRepository
import pl.maniak.wikidiary.feature.todo.data.TodoMapper
import pl.maniak.wikidiary.feature.todo.data.TodoRepositoryImpl
import pl.maniak.wikidiary.feature.todo.domain.TodoRepository

/** Production repositories backed by one in-memory database. */
class TestRepositories(database: WikiNoteDatabase) {
    val notes: WikiNoteRepository = WikiNoteRepositoryImpl(
        database = database,
        wikiNoteDao = database.wikiNoteDao(),
        mapper = WikiNoteMapperImpl(),
        notePersonDao = database.notePersonDao(),
        personMapper = PersonMapper()
    )
    val people: PersonRepository = PersonRepositoryImpl(PersonMapper(), database.personDao())
    val tags: TagRepository = TagRepositoryImpl(TagMapperImpl(), database.tagDao())
    val todos: TodoRepository = TodoRepositoryImpl(TodoMapper(), database.todoDao())
    val routines: RoutineRepository = RoutineRepositoryImpl(RoutineMapper(), database.routineDao())
}
