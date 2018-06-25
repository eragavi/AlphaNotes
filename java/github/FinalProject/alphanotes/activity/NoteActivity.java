package github.FinalProject.alphanotes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.db.OpenHelper;
import github.FinalProject.alphanotes.fragment.AudioFragment;
import github.FinalProject.alphanotes.fragment.AudioNoteFragment;
import github.FinalProject.alphanotes.fragment.CameraNoteFragment;
import github.FinalProject.alphanotes.fragment.CheckListFragment;
import github.FinalProject.alphanotes.fragment.SimpleNoteFragment;
import github.FinalProject.alphanotes.fragment.template.NoteFragment;
import github.FinalProject.alphanotes.model.Category;
import github.FinalProject.alphanotes.model.DatabaseModel;

public class NoteActivity extends AppCompatActivity implements NoteFragment.Callbacks {
    public static final int REQUEST_CODE = 2;
    public static final int RESULT_NEW = 101;
    public static final int RESULT_EDIT = 102;
    public static final int RESULT_DELETE = 103;

    private int noteResult = 0;
    private int position;

    private NoteFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent data = getIntent();
        setTheme(Category.getStyle(data.getIntExtra(OpenHelper.COLUMN_THEME, Category.THEME_GREEN)));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        position = data.getIntExtra("position", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (Exception ignored) {
        }

        toolbar.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (savedInstanceState == null) {
            if (data.getIntExtra(OpenHelper.COLUMN_TYPE, DatabaseModel.TYPE_NOTE_SIMPLE) == DatabaseModel.TYPE_NOTE_SIMPLE) {
                fragment = new SimpleNoteFragment();
            } else if (data.getIntExtra(OpenHelper.COLUMN_TYPE, DatabaseModel.TYPE_NOTE_CAMERA) == DatabaseModel.TYPE_NOTE_CAMERA) {
                fragment = new CameraNoteFragment();
            } else if (data.getIntExtra(OpenHelper.COLUMN_TYPE, DatabaseModel.TYPE_NOTE_AUDIO) == DatabaseModel.TYPE_NOTE_AUDIO) {
                fragment = AudioFragment.newInstance();
            } else if (data.getIntExtra(OpenHelper.COLUMN_TYPE, DatabaseModel.TYPE_NOTE_CHECKLIST) == DatabaseModel.TYPE_NOTE_CHECKLIST) {
                fragment = new CheckListFragment();
            }
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragment)
                        .commit();
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (fragment instanceof CheckListFragment) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setResult(RESULT_OK, null);
                    finish();
                }
            });
        } else {
            fragment.saveNote(new NoteFragment.SaveListener() {
                @Override
                public void onSave() {
                    final Intent data = new Intent();
                    data.putExtra("position", position);
                    data.putExtra(OpenHelper.COLUMN_ID, fragment.note.id);

                    switch (noteResult) {
                        case RESULT_NEW:
                            data.putExtra(OpenHelper.COLUMN_TYPE, fragment.note.type);
                            data.putExtra(OpenHelper.COLUMN_DATE, fragment.note.createdAt);
                        case RESULT_EDIT:
                            data.putExtra(OpenHelper.COLUMN_TITLE, fragment.note.title);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setResult(noteResult, data);
                            finish();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void setNoteResult(int result, boolean closeActivity) {
        noteResult = result;
        if (closeActivity) {
            Intent data = new Intent();
            data.putExtra("position", position);
            data.putExtra(OpenHelper.COLUMN_ID, fragment.note.id);
            setResult(result, data);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (fragment != null && fragment instanceof CameraNoteFragment) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
