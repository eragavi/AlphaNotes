package github.FinalProject.alphanotes.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.Locale;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.activity.NoteActivity;
import github.FinalProject.alphanotes.adapter.NoteAdapter;
import github.FinalProject.alphanotes.adapter.template.ModelAdapter;
import github.FinalProject.alphanotes.db.Controller;
import github.FinalProject.alphanotes.db.OpenHelper;
import github.FinalProject.alphanotes.fragment.template.RecyclerFragment;
import github.FinalProject.alphanotes.inner.Animator;
import github.FinalProject.alphanotes.model.DatabaseModel;
import github.FinalProject.alphanotes.model.Note;

public class CategoryFragment extends RecyclerFragment<Note, NoteAdapter> {
    public View protector;
    public View fab_text;
    public View fab_camera;
    public View fab_audio;
    public View fab_checklist;
    public boolean isFabOpen = false;

    private ModelAdapter.ClickListener listener = new ModelAdapter.ClickListener() {
        @Override
        public void onClick(DatabaseModel item, int position) {
            startNoteActivity(item.type, item.id, position);
        }

        @Override
        public void onChangeSelection(boolean haveSelected) {
            toggleSelection(haveSelected);
        }

        @Override
        public void onCountSelection(int count) {
            onChangeCounter(count);
        }
    };

    public CategoryFragment() {
    }

    @Override
    public void init(View view) {
        protector = view.findViewById(R.id.protector);
        fab_text = view.findViewById(R.id.fab_text);
        fab_camera = view.findViewById(R.id.fab_camera);
        fab_audio = view.findViewById(R.id.fab_audio);
        fab_checklist = view.findViewById(R.id.fab_checklist);

        protector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFab(true);
            }
        });

        fab_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNoteActivity(DatabaseModel.TYPE_NOTE_SIMPLE, DatabaseModel.NEW_MODEL_ID, 0);
            }
        });

        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNoteActivity(DatabaseModel.TYPE_NOTE_CAMERA, DatabaseModel.NEW_MODEL_ID, 0);
            }
        });
        fab_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNoteActivity(DatabaseModel.TYPE_NOTE_AUDIO, DatabaseModel.NEW_MODEL_ID, 0);
            }
        });
        fab_checklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNoteActivity(DatabaseModel.TYPE_NOTE_CHECKLIST, DatabaseModel.NEW_MODEL_ID, 0);
            }
        });
    }

    private void startNoteActivity(final int type, final long noteId, final int position) {
        toggleFab(true);

        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(150);
                } catch (InterruptedException ignored) {
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getContext(), NoteActivity.class);
                        intent.putExtra(OpenHelper.COLUMN_TYPE, type);
                        intent.putExtra("position", position);
                        intent.putExtra(OpenHelper.COLUMN_ID, noteId);
                        intent.putExtra(OpenHelper.COLUMN_PARENT_ID, categoryId);
                        intent.putExtra(OpenHelper.COLUMN_THEME, categoryTheme);
                        startActivityForResult(intent, NoteActivity.REQUEST_CODE);
                    }
                });

                interrupt();
            }
        }.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == NoteActivity.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                loadItems();
                return;
            }
            if (data ==null || !data.hasExtra("position")){

                return;
            }
            final int position = data.getIntExtra("position", 0);

            switch (resultCode) {
                case NoteActivity.RESULT_NEW:
                    Note note = new Note();
                    note.title = data.getStringExtra(OpenHelper.COLUMN_TITLE);
                    note.type = data.getIntExtra(OpenHelper.COLUMN_TYPE, DatabaseModel.TYPE_NOTE_SIMPLE);
                    note.createdAt = data.getLongExtra(OpenHelper.COLUMN_DATE, System.currentTimeMillis());
                    note.id = data.getLongExtra(OpenHelper.COLUMN_ID, DatabaseModel.NEW_MODEL_ID);
                    addItem(note, position);
                    break;
                case NoteActivity.RESULT_EDIT:
                    Note item = items.get(position);
                    item.title = data.getStringExtra(OpenHelper.COLUMN_TITLE);
                    refreshItem(position);
                    break;
                case NoteActivity.RESULT_DELETE:
                    new Thread() {
                        @Override
                        public void run() {
                            Controller.instance.deleteNotes(
                                    new String[]{
                                            String.format(Locale.US, "%d", data.getLongExtra(OpenHelper.COLUMN_ID, DatabaseModel.NEW_MODEL_ID))
                                    },
                                    categoryId
                            );

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final Note deletedItem = deleteItem(position);
                                    Snackbar.make(fab != null ? fab : selectionToolbar, "1 note was deleted", 7000)
                                            .setAction(R.string.undo, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    new Thread() {
                                                        @Override
                                                        public void run() {
                                                            Controller.instance.undoDeletion();
                                                            Controller.instance.addCategoryCounter(deletedItem.categoryId, 1);

                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    addItem(deletedItem, position);
                                                                }
                                                            });

                                                            interrupt();
                                                        }
                                                    }.start();
                                                }
                                            })
                                            .show();
                                }
                            });

                            interrupt();
                        }
                    }.start();
                    break;
            }
        }
    }

    @Override
    public void onClickFab() {
        toggleFab(false);
    }

    public void toggleFab(boolean forceClose) {
        if (isFabOpen) {
            isFabOpen = false;

            Animator.create(getContext())
                    .on(protector)
                    .setEndVisibility(View.GONE)
                    .animate(R.anim.fade_out);

            Animator.create(getContext())
                    .on(fab)
                    .animate(R.anim.fab_rotate_back);

            Animator.create(getContext())
                    .on(fab_text)
                    .setEndVisibility(View.GONE)
                    .animate(R.anim.fab_out);

            Animator.create(getContext())
                    .on(fab_audio)
                    .setEndVisibility(View.GONE)
                    .animate(R.anim.fab_out);

            Animator.create(getContext())
                    .on(fab_checklist)
                    .setEndVisibility(View.GONE)
                    .animate(R.anim.fab_out);

            Animator.create(getContext())
                    .on(fab_camera)
                    .setEndVisibility(View.GONE)
                    .animate(R.anim.fab_out);
        } else if (!forceClose) {
            isFabOpen = true;

            Animator.create(getContext())
                    .on(protector)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fade_in);

            Animator.create(getContext())
                    .on(fab)
                    .animate(R.anim.fab_rotate);

            Animator.create(getContext())
                    .on(fab_text)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fab_in);

            Animator.create(getContext())
                    .on(fab_audio)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fab_in);

            Animator.create(getContext())
                    .on(fab_checklist)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fab_in);

            Animator.create(getContext())
                    .on(fab_camera)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fab_in);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_category;
    }

    @Override
    public String getItemName() {
        return "note";
    }

    @Override
    public Class<NoteAdapter> getAdapterClass() {
        return NoteAdapter.class;
    }

    @Override
    public ModelAdapter.ClickListener getListener() {
        return listener;
    }
}
