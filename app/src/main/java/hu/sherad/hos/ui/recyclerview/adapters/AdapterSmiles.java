package hu.sherad.hos.ui.recyclerview.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import hu.sherad.hos.R;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.ui.recyclerview.holders.HolderSmile;

public class AdapterSmiles extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnSelect onSelect;

    public AdapterSmiles(OnSelect onSelect) {
        this.onSelect = onSelect;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderSmile(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_smile, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HolderSmile holderSmile = (HolderSmile) holder;
        int i = 0;
        for (String key : TopicComment.Utils.allSmiles.keySet()) {
            if (i == position) {
                // TODO: 2017. 09. 28. Small icons after view recycled? Figure out why
                Glide.with(holderSmile.getSmile().getContext()).asGif().apply(new RequestOptions().fitCenter()).load(TopicComment.Utils.allSmiles.get(key)).into(holderSmile.getSmile());
                holderSmile.getSmile().setOnClickListener(view -> onSelect.onSmileSelect(key));
                return;
            }
            i++;
        }

    }

    @Override
    public int getItemCount() {
        return TopicComment.Utils.allSmiles.size();
    }

    public interface OnSelect {
        void onSmileSelect(String key);
    }
}
