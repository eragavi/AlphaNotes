package github.FinalProject.alphanotes.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.activity.NoteActivity;
import github.FinalProject.alphanotes.adapter.CheckListAdapter;
import github.FinalProject.alphanotes.db.Controller;
import github.FinalProject.alphanotes.db.OpenHelper;
import github.FinalProject.alphanotes.fragment.template.NoteFragment;
import github.FinalProject.alphanotes.model.DatabaseModel;
import github.FinalProject.alphanotes.model.Note;


public class CheckListFragment extends NoteFragment implements CheckListAdapter.OnCheckListUpdateListener {

    private EditText titleEditText;

    private RecyclerView todoRecyclerView;

    private CheckListAdapter checkListAdapter;

    private long categoryId;

    private int noteType;

    public CheckListFragment() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        categoryId = intent.getLongExtra(OpenHelper.COLUMN_PARENT_ID, DatabaseModel.NEW_MODEL_ID);
        noteType = intent.getIntExtra(OpenHelper.COLUMN_TYPE, DatabaseModel.TYPE_NOTE_SIMPLE);

        setTodoRecyclerView();
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_check_list;
    }

    @Override
    public void init(View view) {
        initView(view);
    }

    private void initView(View view) {
        todoRecyclerView = view.findViewById(R.id.list_todo);
        titleEditText = view.findViewById(R.id.title_txt);

        titleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    saveNote(new SaveListener() {
                        @Override
                        public void onSave() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    titleEditText.setText("");
                                    note = null;
                                    loadList();
                                    note = new Note();
                                    activity.setNoteResult(NoteActivity.RESULT_NEW, false);
                                    note.categoryId = categoryId;
                                    note.title = "";
                                    note.body = "";
                                    note.isArchived = false;
                                    note.isLocked = false;
                                    note.type = DatabaseModel.TYPE_NOTE_CHECKLIST;
                                }
                            });
                        }
                    });
                }
                return false;
            }
        });
    }

    private void setTodoRecyclerView() {
        checkListAdapter = new CheckListAdapter();
        todoRecyclerView.setAdapter(checkListAdapter);
        todoRecyclerView.setHasFixedSize(true);
        todoRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        checkListAdapter.setOnCheckListUpdateListener(this);
        loadList();
    }

    private void saveNote() {
        String todo = titleEditText.getText().toString();
        todo = !TextUtils.isEmpty(todo) ? todo : "Untitled";
        Note note = new Note();
        note.isArchived = false;
        note.body = todo;
        note.categoryId = categoryId;
        note.type = noteType;
        note.createdAt = System.currentTimeMillis();
        note.isLocked = false;
        Controller.instance.saveNote(note, note.getContentValues());

        //loadList();
    }

    private void loadList() {
        ArrayList<Note> noteArrayList = Note.getCheckListNote(categoryId);
        if (noteArrayList != null && !noteArrayList.isEmpty()) {
            checkListAdapter.setData(noteArrayList);
        }
    }

    @Override
    public void saveNote(final SaveListener listener) {
        super.saveNote(listener);
        note.body = titleEditText.getText().toString();

        new Thread() {
            @Override
            public void run() {
                long id = note.save();
                if (note.id == DatabaseModel.NEW_MODEL_ID) {
                    note.id = id;
                }
                listener.onSave();
                interrupt();
            }
        }.start();
    }

    @Override
    public void onCheckedChange(Note note, int position) {
        note.isArchived = !note.isArchived;
        Controller.instance.saveNote(note, note.getContentValues());
        checkListAdapter.notifyItemChanged(position);
    }

}
