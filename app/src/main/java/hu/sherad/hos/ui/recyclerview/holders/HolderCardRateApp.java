package hu.sherad.hos.ui.recyclerview.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import hu.sherad.hos.R;


public class HolderCardRateApp extends RecyclerView.ViewHolder {

    private View stars;

    public HolderCardRateApp(View itemView) {
        super(itemView);
        stars = itemView.findViewById(R.id.linear_layout_card_rate_app);
    }

    public View getStars() {
        return stars;
    }

}
