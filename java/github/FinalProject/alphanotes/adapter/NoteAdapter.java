package github.FinalProject.alphanotes.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.adapter.template.ModelAdapter;
import github.FinalProject.alphanotes.model.DatabaseModel;
import github.FinalProject.alphanotes.model.Note;
import github.FinalProject.alphanotes.widget.NoteViewHolder;
import github.FinalProject.alphanotes.widget.NoteViewListHolder;
import github.FinalProject.alphanotes.widget.template.ModelViewHolder;

public class NoteAdapter extends ModelAdapter<Note, ModelViewHolder<Note>> {

    private ArrayList<Note> items;

    public NoteAdapter(ArrayList<Note> items, ArrayList<Note> selected, ClickListener<Note> listener) {
        super(items, selected, listener);
        this.items = items;
    }

    @Override
    public ModelViewHolder<Note> onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == DatabaseModel.TYPE_NOTE_CHECKLIST) {
            return new NoteViewListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false));
        } else {
            return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        Note note = items.get(position);
        if (note.type == DatabaseModel.TYPE_NOTE_CHECKLIST) {
            return DatabaseModel.TYPE_NOTE_CHECKLIST;
        }
        return super.getItemViewType(position);
    }
}