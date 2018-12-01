package hu.sherad.hos.ui.recyclerview.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import hu.sherad.hos.R;
import hu.sherad.hos.ui.recyclerview.holders.HolderListPages;


public class AdapterPages extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int tabsSize;
    private PagesActions pagesActions;

    public AdapterPages(PagesActions pagesActions, int tabsSize) {
        this.tabsSize = tabsSize;
        this.pagesActions = pagesActions;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderListPages(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pages, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HolderListPages holderListPages = (HolderListPages) holder;

        holderListPages.getTextView().setText(new StringBuilder((position + 1) + " / " + tabsSize));
        holderListPages.getTextView().setOnClickListener(view -> pagesActions.onPageSelected(position));

    }

    @Override
    public int getItemCount() {
        return tabsSize;
    }

    public interface PagesActions {
        void onPageSelected(int page);
    }
}
