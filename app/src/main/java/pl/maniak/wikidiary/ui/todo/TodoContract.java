package pl.maniak.wikidiary.ui.todo;

import java.util.List;

import pl.maniak.wikidiary.domain.todo.Task;
import pl.maniak.wikidiary.ui.BaseContract;

public interface TodoContract {

    interface View extends BaseContract.View {
        void showTasks(List<Task> list);
        void showOptionsDialog();
        void showNewTaskEditor();
        void showEditTaskEditor(Task task);
    }

    interface Router extends BaseContract.Router {
     }

    interface Presenter extends BaseContract.Presenter<View, Router> {
        void onResumed();
        void onPauseCalled();
        void onNewTaskButtonClicked();
        void onTaskClicked(long taksId);
        void onDoneChecked(long taskId);
        void onEditTaskOptionClicked();
        void onDeleteTaskOptionClicked();
        void onCommitNewTaskButtonClicked(String content);
        void onCommitEditTaskButtonClicked(String content);
    }
}
