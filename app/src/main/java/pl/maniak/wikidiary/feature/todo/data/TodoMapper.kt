package pl.maniak.wikidiary.feature.todo.data

import pl.maniak.wikidiary.feature.todo.domain.Todo

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
