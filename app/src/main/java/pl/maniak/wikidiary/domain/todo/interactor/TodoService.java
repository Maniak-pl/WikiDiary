package pl.maniak.wikidiary.domain.todo.interactor;

import java.util.List;

import pl.maniak.wikidiary.domain.todo.Task;

public interface TodoService {
    Task getTask(Long id);
    List<Task> getAllTasks();
    void save(Task task);
    void update(Task task);
    void delete(Long id);
    void done(Task task);
}
