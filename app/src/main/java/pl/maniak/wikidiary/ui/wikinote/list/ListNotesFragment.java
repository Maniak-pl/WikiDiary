package pl.maniak.wikidiary.ui.wikinote.list;

import java.util.List;
import pl.maniak.wikidiary.domain.wikinote.WikiNote;
import pl.maniak.wikidiary.ui.BaseFragment;

public interface ListNotesFragment {

    void showNotes(List<WikiNote> list);

}
