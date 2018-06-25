package github.FinalProject.alphanotes.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.model.Note;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.CheckListViewHolder> {

    private List<Note> noteList;

    private OnCheckListUpdateListener onCheckListUpdateListener;

    public CheckListAdapter() {
        noteList = new ArrayList<>();
    }

    public void setData(List<Note> noteList) {
        this.noteList = noteList;
        notifyDataSetChanged();
    }

    public void setOnCheckListUpdateListener(OnCheckListUpdateListener onCheckListUpdateListener) {
        this.onCheckListUpdateListener = onCheckListUpdateListener;
    }

    @Override
    public CheckListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new CheckListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CheckListViewHolder holder, int position) {
        final Note note = noteList.get(position);
        holder.tvTodoItem.setText(note.title);

        holder.cbTodoItem.setOnCheckedChangeListener(null);
        holder.cbTodoItem.setChecked(note.isArchived);


        holder.cbTodoItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onCheckListUpdateListener.onCheckedChange(note,holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (noteList != null && !noteList.isEmpty()) ? noteList.size() : 0;
    }

    static class CheckListViewHolder extends RecyclerView.ViewHolder {

        private CheckBox cbTodoItem;
        private TextView tvTodoItem;

        CheckListViewHolder(View itemView) {
            super(itemView);
            cbTodoItem = itemView.findViewById(R.id.cb_todo_item);
            tvTodoItem = itemView.findViewById(R.id.tv_todo_item);
        }
    }

    public interface OnCheckListUpdateListener {

        void onCheckedChange(Note note, int position);
    }
}
