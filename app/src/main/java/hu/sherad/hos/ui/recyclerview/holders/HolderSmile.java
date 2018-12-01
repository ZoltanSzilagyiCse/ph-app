package hu.sherad.hos.ui.recyclerview.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import hu.sherad.hos.R;


public class HolderSmile extends RecyclerView.ViewHolder {

    private ImageView smile;

    public HolderSmile(View itemView) {
        super(itemView);

        smile = itemView.findViewById(R.id.image_view_list_item_smile);
    }

    public ImageView getSmile() {
        return smile;
    }
}
