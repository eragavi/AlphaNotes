package github.FinalProject.alphanotes.fragment;

import android.support.v4.content.ContextCompat;
import android.view.View;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.fragment.template.NoteFragment;
import github.FinalProject.alphanotes.model.DatabaseModel;
import jp.wasabeef.richeditor.RichEditor;

public class SimpleNoteFragment extends NoteFragment {
	private RichEditor body;

	public SimpleNoteFragment() {}

	@Override
	public int getLayout() {
		return R.layout.fragment_simple_note;
	}

	@Override
	public void saveNote(final SaveListener listener) {
		super.saveNote(listener);
		note.body = body.getHtml();

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
	public void init(View view) {
		body = (RichEditor) view.findViewById(R.id.editor);
		body.setPlaceholder("TextNote");
		body.setEditorBackgroundColor(ContextCompat.getColor(getContext(), R.color.bg));
		body.setHtml(note.body);
	}
}
