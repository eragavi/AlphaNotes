package github.FinalProject.alphanotes.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.inner.Formatter;
import github.FinalProject.alphanotes.model.DatabaseModel;
import github.FinalProject.alphanotes.model.Note;
import github.FinalProject.alphanotes.widget.template.ModelViewHolder;

public class NoteViewHolder extends ModelViewHolder<Note> {
    public ImageView badge;
    public TextView title;
    public TextView date;

    public NoteViewHolder(View itemView) {
        super(itemView);
        badge = (ImageView) itemView.findViewById(R.id.badge_icon);
        title = (TextView) itemView.findViewById(R.id.title_txt);
        date = (TextView) itemView.findViewById(R.id.date_txt);
    }

    @Override
    public void populate(Note item) {
        switch (item.type) {
            case DatabaseModel.TYPE_NOTE_AUDIO:
                badge.setImageResource(R.drawable.ic_settings_voice_black_24dp);
                break;
            case DatabaseModel.TYPE_NOTE_CAMERA:
                badge.setImageResource(R.drawable.ic_photo_camera_black_24dp);
                break;
            case DatabaseModel.TYPE_NOTE_CHECKLIST:
                badge.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                break;
            case DatabaseModel.TYPE_NOTE_SIMPLE:
                badge.setImageResource(R.drawable.fab_type);
                break;
        }
        if (item.type == DatabaseModel.TYPE_NOTE_CHECKLIST) {
            title.setText("Todo List");
        } else {
            title.setText(item.title);
        }

        date.setText(Formatter.formatShortDate(item.createdAt));
    }
}
