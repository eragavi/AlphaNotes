package github.FinalProject.alphanotes.fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import github.FinalProject.alphanotes.App;
import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.activity.CategoryActivity;
import github.FinalProject.alphanotes.adapter.CategoryAdapter;
import github.FinalProject.alphanotes.adapter.template.ModelAdapter;
import github.FinalProject.alphanotes.db.OpenHelper;
import github.FinalProject.alphanotes.fragment.template.RecyclerFragment;
import github.FinalProject.alphanotes.model.Category;
import github.FinalProject.alphanotes.model.DatabaseModel;

public class MainFragment extends RecyclerFragment<Category, CategoryAdapter> {
    private int categoryDialogTheme = Category.THEME_GREEN;

    private ModelAdapter.ClickListener listener = new ModelAdapter.ClickListener() {
        @Override
        public void onClick(DatabaseModel item, int position) {
            if (item.isLocked) {
                displayPasswordVerifyDialog(
                        R.string.open_note_book,
                        R.string.unlock,
                        item,
                        position
                );
            } else {
                Intent intent = new Intent(getContext(), CategoryActivity.class);
                intent.putExtra("position", position);
                intent.putExtra(OpenHelper.COLUMN_ID, item.id);
                intent.putExtra(OpenHelper.COLUMN_TITLE, item.title);
                intent.putExtra(OpenHelper.COLUMN_THEME, ((Category) item).theme);
                startActivityForResult(intent, CategoryActivity.REQUEST_CODE);
            }

        }

        @Override
        public void onChangeSelection(boolean haveSelected) {
            toggleSelection(haveSelected);
        }

        @Override
        public void onCountSelection(int count) {
            onChangeCounter(count);
            activity.toggleOneSelection(count <= 1);
        }
    };

    public MainFragment() {
    }

    @Override
    public void onClickFab() {
        categoryDialogTheme = Category.THEME_GREEN;
        displayCategoryDialog(
                R.string.new_category,
                R.string.create,
                "",
                DatabaseModel.NEW_MODEL_ID,
                0
        );
    }

    public void onLockSelected() {
        if (!selected.isEmpty()) {
            //ArrayList a = new ArrayList();
            //a.addAll(selected);
            Category item = selected.remove(0);
            int position = items.indexOf(item);
            refreshItem(position);
            toggleSelection(false);
            categoryDialogTheme = item.theme;
            displayLockDialog(
                    R.string.edit_lock,
                    R.string.lock,
                    item.title,
                    item.id,
                    position
            );

        }
    }

    public void onEditSelected() {
        if (!selected.isEmpty()) {
            Category item = selected.remove(0);
            int position = items.indexOf(item);
            refreshItem(position);
            toggleSelection(false);
            categoryDialogTheme = item.theme;
            displayCategoryDialog(
                    R.string.edit_category,
                    R.string.edit,
                    item.title,
                    item.id,
                    position
            );
        }
    }

    private void displayLockDialog(@StringRes int title, @StringRes int positiveText, final String categoryTitle, final long categoryId, final int position) {
        MaterialDialog dialog = new MaterialDialog.Builder((getContext()))
                .title(title)
                .positiveText(positiveText)
                .negativeText(R.string.cancel)
                .negativeColor(ContextCompat.getColor(getContext(), R.color.secondary_text))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String inputPassword = ((EditText) dialog.getCustomView().findViewById(R.id.title_txt)).getText().toString();
                        if (TextUtils.isEmpty(inputPassword)) {
                            Toast.makeText(getContext(), "Please enter password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        App.getInstance().putPrefs("" + categoryId, inputPassword);
                        final Category category = new Category();
                        category.id = categoryId;

                        final boolean isEditing = categoryId != DatabaseModel.NEW_MODEL_ID;

                        if (!isEditing) {
                            category.counter = 0;
                            category.type = DatabaseModel.TYPE_CATEGORY;
                            category.createdAt = System.currentTimeMillis();
                            category.isArchived = false;
                        }
                        category.title = categoryTitle;
                        category.isLocked = true;
                        category.theme = categoryDialogTheme;

                        new Thread() {
                            @Override
                            public void run() {
                                final long id = category.save();
                                if (id != DatabaseModel.NEW_MODEL_ID) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isEditing) {
                                                Category categoryInItems = items.get(position);
                                                categoryInItems.theme = category.theme;
                                                categoryInItems.title = category.title;
                                                categoryInItems.isLocked = category.isLocked;
                                                refreshItem(position);
                                            } else {
                                                category.id = id;
                                                addItem(category, position);
                                            }
                                        }
                                    });
                                }

                                interrupt();
                            }
                        }.start();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .customView(R.layout.dialog_password, true)
                .build();
        dialog.show();
        //noinspection ConstantConditions
    }

    private void displayPasswordVerifyDialog(@StringRes int title, @StringRes int positiveText, final DatabaseModel item, final int position) {
        MaterialDialog dialog = new MaterialDialog.Builder((getContext()))
                .title(title)
                .positiveText(positiveText)
                .negativeText(R.string.cancel)
                .negativeColor(ContextCompat.getColor(getContext(), R.color.secondary_text))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String inputPassword = ((EditText) dialog.getCustomView().findViewById(R.id.title_txt)).getText().toString();
                        if (TextUtils.isEmpty(inputPassword)) {
                            Toast.makeText(getContext(), "Please enter password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (inputPassword.equalsIgnoreCase(App.getInstance().getString(item.id + "", ""))) {
                            Intent intent = new Intent(getContext(), CategoryActivity.class);
                            intent.putExtra("position", position);
                            intent.putExtra(OpenHelper.COLUMN_ID, item.id);
                            intent.putExtra(OpenHelper.COLUMN_TITLE, item.title);
                            intent.putExtra(OpenHelper.COLUMN_THEME, ((Category) item).theme);
                            startActivityForResult(intent, CategoryActivity.REQUEST_CODE);
                        } else {
                            Toast.makeText(getContext(), "Wrong password", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .customView(R.layout.dialog_password, true)
                .build();
        dialog.show();
    }

    private void displayCategoryDialog(@StringRes int title, @StringRes int positiveText, final String categoryTitle, final long categoryId, final int position) {
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(title)
                .positiveText(positiveText)
                .negativeText(R.string.cancel)
                .negativeColor(ContextCompat.getColor(getContext(), R.color.secondary_text))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //noinspection ConstantConditions
                        String inputTitle = ((EditText) dialog.getCustomView().findViewById(R.id.title_txt)).getText().toString();
                        if (inputTitle.isEmpty()) {
                            inputTitle = "Untitled";
                        }

                        final Category category = new Category();
                        category.id = categoryId;

                        final boolean isEditing = categoryId != DatabaseModel.NEW_MODEL_ID;

                        if (!isEditing) {
                            category.counter = 0;
                            category.type = DatabaseModel.TYPE_CATEGORY;
                            category.createdAt = System.currentTimeMillis();
                            category.isArchived = false;
                        }

                        category.title = inputTitle;
                        category.theme = categoryDialogTheme;

                        new Thread() {
                            @Override
                            public void run() {
                                final long id = category.save();
                                if (id != DatabaseModel.NEW_MODEL_ID) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isEditing) {
                                                Category categoryInItems = items.get(position);
                                                categoryInItems.theme = category.theme;
                                                categoryInItems.title = category.title;
                                                refreshItem(position);
                                            } else {
                                                category.id = id;
                                                addItem(category, position);
                                            }
                                        }
                                    });
                                }

                                interrupt();
                            }
                        }.start();

                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .customView(R.layout.dialog_category, true)
                .build();

        dialog.show();

        final View view = dialog.getCustomView();

        //noinspection ConstantConditions
        ((EditText) view.findViewById(R.id.title_txt)).setText(categoryTitle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CategoryActivity.REQUEST_CODE && resultCode == CategoryActivity.RESULT_CHANGE) {
            int position = data.getIntExtra("position", 0);
            items.get(position).counter = data.getIntExtra(OpenHelper.COLUMN_COUNTER, 0);
            refreshItem(position);
        }
    }

    @Override
    public int getLayout() {
        return (R.layout.fragment_main);
    }

    @Override
    public String getItemName() {
        return "category";
    }

    @Override
    public Class<CategoryAdapter> getAdapterClass() {
        return CategoryAdapter.class;
    }

    @Override
    public ModelAdapter.ClickListener getListener() {
        return listener;
    }
}
