package hu.sherad.hos.ui.recyclerview.holders;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import hu.sherad.hos.R;

public class HolderListImageTextView extends RecyclerView.ViewHolder {

    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvDate;
    private ImageView img;

    public HolderListImageTextView(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.text_view_list_img_text_view_title);
        tvContent = itemView.findViewById(R.id.text_view_list_img_text_view_content);
        tvDate = itemView.findViewById(R.id.text_view_list_item_news_date);
        img = itemView.findViewById(R.id.img_list_img_text_view);
    }

    public TextView getTvDate() {
        return tvDate;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public TextView getTvContent() {
        return tvContent;
    }

    public ImageView getImg() {
        return img;
    }

}