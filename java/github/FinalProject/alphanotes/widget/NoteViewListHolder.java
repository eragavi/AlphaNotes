package github.FinalProject.alphanotes.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.inner.Formatter;
import github.FinalProject.alphanotes.model.DatabaseModel;
import github.FinalProject.alphanotes.model.Note;
import github.FinalProject.alphanotes.widget.template.ModelViewHolder;


public class NoteViewListHolder extends ModelViewHolder<Note> {

    public ImageView badge;
    public TextView title;
    public TextView date;

    public NoteViewListHolder(View itemView) {
        super(itemView);
        badge = (ImageView) itemView.findViewById(R.id.badge_icon);
        title = (TextView) itemView.findViewById(R.id.title_txt);
        date = (TextView) itemView.findViewById(R.id.date_txt);
    }

    @Override
    public void populate(Note item) {
        switch (item.type) {
            case DatabaseModel.TYPE_NOTE_CHECKLIST:
                badge.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                break;
        }
        title.setText("Todo List");
        date.setText(Formatter.formatShortDate(item.createdAt));
    }
}
