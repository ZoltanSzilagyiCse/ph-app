package hu.sherad.hos.ui.recyclerview.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import hu.sherad.hos.R;
import hu.sherad.hos.ui.widget.RichTextView;

public class HolderListPages extends RecyclerView.ViewHolder {

    private RichTextView textView;

    public HolderListPages(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.text_view_list_item_pages);
    }

    public RichTextView getTextView() {
        return textView;
    }
}
