package pl.maniak.wikidiary.feature.backup

import androidx.room.withTransaction
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.diary.data.CategoryEntity
import pl.maniak.wikidiary.feature.diary.data.TagEntity
import pl.maniak.wikidiary.feature.people.data.PersonEntity
import pl.maniak.wikidiary.feature.pictograms.data.PictogramEntity
import pl.maniak.wikidiary.feature.routines.data.RoutineEntity
import pl.maniak.wikidiary.feature.todo.data.TodoEntity
import java.util.Date

class ConfigurationStore(private val database: WikiNoteDatabase) {
    suspend fun snapshot(): ConfigurationBackup = database.withTransaction {
        ConfigurationBackup(
            tags = database.tagDao().getAll().sortedBy { it.id }.map {
                BackupTag(it.id, it.tag, it.category, it.date.time, it.color)
            },
            categories = database.categoryDao().getAllCategories().sortedBy { it.id }.map {
                BackupCategory(it.id, it.name, it.parentId)
            },
            todos = database.todoDao().getAll().filterNot { it.isCompleted }.sortedBy { it.id }.map {
                BackupItem(it.id, it.title)
            },
            routines = database.routineDao().getAllRoutines().sortedBy { it.id }.map {
                BackupItem(it.id, it.name)
            },
            pictograms = database.pictogramDao().getAll().sortedBy { it.id }.map {
                BackupPictogram(it.id, it.name, it.icon)
            },
            people = database.personDao().getAll().sortedBy { it.id }.map {
                BackupPerson(it.id, it.name, it.gender)
            },
        ).also { it.validate() }
    }

    suspend fun restore(backup: ConfigurationBackup) {
        backup.validate()
        database.withTransaction {
            require(database.backupDao().journalEntryCount() == 0) {
                "Przywracanie jest możliwe tylko bez lokalnych notatek i przypisań pictogramów. Najpierw wyeksportuj wpisy."
            }
            database.tagDao().deleteAll()
            database.categoryDao().deleteAll()
            database.routineDao().deleteAll()
            database.backupDao().clearTodos()
            database.backupDao().clearPeople()
            database.backupDao().clearPictograms()
            backup.categories.forEach {
                database.categoryDao().insert(CategoryEntity(it.id, it.name, it.parentId))
            }
            backup.tags.forEach {
                database.tagDao().insert(TagEntity(it.id, it.name, it.category, Date(it.date), it.color))
            }
            backup.routines.forEach { database.routineDao().insert(RoutineEntity(it.id, it.name, false)) }
            backup.todos.forEach { database.todoDao().insert(TodoEntity(it.id, it.name, false)) }
            backup.people.forEach { database.personDao().insert(PersonEntity(it.id, it.name, it.gender)) }
            backup.pictograms.forEach {
                database.pictogramDao().insert(PictogramEntity(it.id, it.name, it.icon))
            }
        }
    }
}
