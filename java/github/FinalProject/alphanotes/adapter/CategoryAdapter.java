package github.FinalProject.alphanotes.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.adapter.template.ModelAdapter;
import github.FinalProject.alphanotes.model.Category;
import github.FinalProject.alphanotes.widget.CategoryViewHolder;

public class CategoryAdapter extends ModelAdapter<Category, CategoryViewHolder> {
	public CategoryAdapter(ArrayList<Category> items, ArrayList<Category> selected, ClickListener<Category> listener) {
		super(items, selected, listener);
	}

	@Override
	public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false));
	}
}
