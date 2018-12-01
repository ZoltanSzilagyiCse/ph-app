package hu.sherad.hos.ui.recyclerview.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import hu.sherad.hos.R;

public class HolderCardButton extends RecyclerView.ViewHolder {

    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvButton;

    public HolderCardButton(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.text_view_card_button_title);
        tvContent = itemView.findViewById(R.id.text_view_card_button_content);
        tvButton = itemView.findViewById(R.id.text_view_card_button);
    }

    public TextView getTvButton() {
        return tvButton;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public TextView getTvContent() {
        return tvContent;
    }
}
