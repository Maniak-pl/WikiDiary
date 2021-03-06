package pl.maniak.wikidiary.ui.settings.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.maniak.wikidiary.App;
import pl.maniak.wikidiary.R;
import pl.maniak.wikidiary.domain.tag.Tag;
import pl.maniak.wikidiary.repository.tag.TagRepository;
import pl.maniak.wikidiary.utils.views.FlowLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    @BindView(R.id.addTagEt)
    EditText addTagEt;
    @BindView(R.id.settingsContainerTags)
    FlowLayout mFlowLayout;

    private Set<Tag> setTag = new TreeSet<Tag>();

    @Inject
    public TagRepository repository;

    @Inject
    SharedPreferences preferences;

    public static SettingsFragment newInstance() {

        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_add_tag_modal, null);
        App.getAppComponent().inject(this);
        ButterKnife.bind(this, root);
        setTag.addAll(loadTag());

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        initTagContener();
    }


    @OnClick(R.id.addTagBtn)
    public void onClick() {
        if (!addTagEt.getText().toString().equals("")) {
            repository.addTag(addTagEt.getText().toString());
            addTagEt.setText("");
            reload();
        }
    }

    private void reload() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();

    }

    private void initTagContener() {
        mFlowLayout.removeAllViews();
        List<Tag> listTag = new ArrayList();
        listTag.addAll(setTag);
        for (int i = 0; i < listTag.size(); i++) {
            TextView tv = (TextView) getActivity().getLayoutInflater().inflate(R.layout.tag_item, null);
            tv.setText(listTag.get(i).getTag());
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            tv.setLayoutParams(params);
            tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String tag = ((TextView) v).getText().toString();
                    setTag.remove(new Tag(tag));
                    repository.deleteTag(tag);
                    reload();
                    return false;
                }
            });
            mFlowLayout.addView(tv);
        }
        mFlowLayout.invalidate();
    }

    public List<Tag> loadTag() {

            return repository.getAllTags();

    }
}
