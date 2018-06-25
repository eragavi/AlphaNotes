package github.FinalProject.alphanotes.model;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Locale;

import github.FinalProject.alphanotes.App;
import github.FinalProject.alphanotes.db.Controller;
import github.FinalProject.alphanotes.db.OpenHelper;

public class Note extends DatabaseModel {
    public long categoryId;
    public String body;

    public Note() {
    }

    /**
     * Instantiates a new object of Note class using the data retrieved from database.
     *
     * @param c cursor object returned from a database query
     */
    public Note(Cursor c) {
        super(c);
        this.categoryId = c.getLong(c.getColumnIndex(OpenHelper.COLUMN_PARENT_ID));
        try {
            this.body = c.getString(c.getColumnIndex(OpenHelper.COLUMN_BODY));
        } catch (Exception ignored) {
        }
    }

    /**
     * @return ContentValue object to be saved or updated
     */
    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        if (id == DatabaseModel.NEW_MODEL_ID) {
            values.put(OpenHelper.COLUMN_TYPE, type);
            values.put(OpenHelper.COLUMN_DATE, createdAt);
            values.put(OpenHelper.COLUMN_ARCHIVED, isArchived);
            values.put(OpenHelper.COLUMN_PARENT_ID, categoryId);
            values.put(OpenHelper.COLUMN_LOCK, isLocked);
        }

        values.put(OpenHelper.COLUMN_TITLE, title);
        values.put(OpenHelper.COLUMN_BODY, body);
        values.put(OpenHelper.COLUMN_TYPE, type);
        values.put(OpenHelper.COLUMN_ARCHIVED, isArchived);

        return values;
    }

    /**
     * Reads a note by its id
     *
     * @param id primary key of note
     * @return the note object or null if it was not found
     */
    public static Note find(long id) {
        return Controller.instance.findNote(Note.class, id);
    }

    /**
     * Reads all notes
     *
     * @param categoryId the id of parent category
     * @return a list of notes which is populated by database
     */
    public static ArrayList<Note> all(long categoryId) {
        return Controller.instance.findNotes(
                Note.class,
                new String[]{
                        OpenHelper.COLUMN_ID,
                        OpenHelper.COLUMN_TITLE,
                        OpenHelper.COLUMN_DATE,
                        OpenHelper.COLUMN_TYPE,
                        OpenHelper.COLUMN_ARCHIVED,
                        OpenHelper.COLUMN_PARENT_ID,
                        OpenHelper.COLUMN_LOCK
                },
                OpenHelper.COLUMN_TYPE + " != ? AND " + OpenHelper.COLUMN_TYPE + " != ? AND " + OpenHelper.COLUMN_PARENT_ID + " = ? AND " + OpenHelper.COLUMN_ARCHIVED + " = ?",
                new String[]{
                        String.format(Locale.US, "%d", DatabaseModel.TYPE_CATEGORY),
                        String.format(Locale.US, "%d", DatabaseModel.TYPE_NOTE_CHECKLIST),
                        String.format(Locale.US, "%d", categoryId),
                        "0"
                },
                App.sortNotesBy
        );
    }

    /**
     * Reads all notes
     *
     * @param categoryId the id of parent category
     * @return a list of notes which is populated by database
     */
    public static ArrayList<Note> getCheckListNote(long categoryId) {
        return Controller.instance.findCheckList(
                Note.class,
                new String[]{
                        OpenHelper.COLUMN_ID,
                        OpenHelper.COLUMN_TITLE,
                        OpenHelper.COLUMN_DATE,
                        OpenHelper.COLUMN_TYPE,
                        OpenHelper.COLUMN_ARCHIVED,
                        OpenHelper.COLUMN_PARENT_ID,
                        OpenHelper.COLUMN_LOCK
                },
                OpenHelper.COLUMN_TYPE + " = ? AND " + OpenHelper.COLUMN_PARENT_ID + " = ?",
                new String[]{
                        String.format(Locale.US, "%d", DatabaseModel.TYPE_NOTE_CHECKLIST),
                        String.format(Locale.US, "%d", categoryId)
                },
                App.sortNotesBy
        );
    }


    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Note && id == (((Note) o).id);
    }
}
