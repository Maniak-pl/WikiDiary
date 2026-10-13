package pl.maniak.wikidiary.data.mapper

import pl.maniak.wikidiary.data.Todo
import pl.maniak.wikidiary.data.database.TodoEntity

class TodoMapper {
    fun mapToDomain(entity: TodoEntity): Todo {
        return Todo(
            id = entity.id,
            title = entity.title,
            isCompleted = entity.isCompleted
        )
    }

    fun mapToEntity(domain: Todo): TodoEntity {
        return TodoEntity(
            id = domain.id,
            title = domain.title,
            isCompleted = domain.isCompleted
        )
    }
}
