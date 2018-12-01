package hu.sherad.hos.ui.recyclerview.adapters.section;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.models.TopicRear;
import hu.sherad.hos.ui.recyclerview.holders.HolderListImageTextView;
import hu.sherad.hos.ui.recyclerview.holders.HolderListSimpleView;
import hu.sherad.hos.ui.recyclerview.interfaces.TopicRearActions;
import hu.sherad.hos.utils.ViewUtils;

public class AdapterNews extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SimpleDateFormat simpleDateFormat;
    private final boolean isTimeAbsolute;

    private TopicRearActions actions;

    private List<TopicRear> topics = new ArrayList<>();

    private PH.Data data = PH.Data.DATA_LOADING;

    public AdapterNews(TopicRearActions actions) {
        this.actions = actions;
        simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
        isTimeAbsolute = PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME, false);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.layout.list_item_news:
                return new HolderListImageTextView(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
            case R.layout.list_item_loading:
            case R.layout.list_item_text_view:
                return new HolderListSimpleView(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case R.layout.list_item_text_view:
                bindViewAsText((HolderListSimpleView) holder, position);
                return;
            case R.layout.list_item_news:
                bindViewAsNew((HolderListImageTextView) holder, position);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == topics.size()) {
            if (data == PH.Data.DATA_LOADING) {
                return R.layout.list_item_loading;
            }
            return R.layout.list_item_text_view;
        }
        return R.layout.list_item_news;
    }

    @Override
    public int getItemCount() {
        return topics.size() + (data == PH.Data.OK ? 0 : 1);
    }

    public List<TopicRear> getTopics() {
        return topics;
    }

    public PH.Data getData() {
        return data;
    }

    public void setData(PH.Data data) {
        this.data = data;
        notifyItemChanged(getItemCount());
    }

    public void addData(List<TopicRear> topics, PH.Data data) {
        this.data = data;
        int lastSize = getItemCount();
        this.topics.addAll(topics);
        notifyItemRangeInserted(lastSize, this.topics.size() + 1);
    }

    public void setLoadingAndClearData() {
        data = PH.Data.DATA_LOADING;
        topics.clear();
        notifyDataSetChanged();
    }

    private void bindViewAsNew(HolderListImageTextView holder, int position) {
        TopicRear topicRear = topics.get(position);

        holder.getTvTitle().setText(topicRear.getTitle());
        holder.getTvContent().setText(topicRear.getDescription());
        if (topicRear.getPubDate() == null) {
            holder.getTvDate().setVisibility(View.GONE);
        } else {
            holder.getTvDate().setVisibility(View.VISIBLE);
            holder.getTvDate().setText(isTimeAbsolute ? simpleDateFormat.format(topicRear.getPubDate()) :
                    DateUtils.getRelativeTimeSpanString(topicRear.getPubDate().getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS)
                            .toString().toLowerCase());
        }
        holder.itemView.setOnClickListener(v -> actions.onSelected(topicRear));
        if (topicRear.getPhotoURL() == null) {
            holder.getImg().setVisibility(View.GONE);
        } else {
            Glide.with(PHApplication.getInstance())
                    .load(topicRear.getPhotoURL())
                    .apply(ViewUtils.getDefaultGlideOptions(R.drawable.vd_image).fitCenter())
                    .into(holder.getImg());
        }
    }

    private void bindViewAsText(HolderListSimpleView holder, int position) {
        TextView info = holder.itemView.findViewById(R.id.text_view_util);
        info.setText(PH.getErrorFromCode(data));
        switch (data) {
            case DATA_CAN_LOAD:
                info.setOnClickListener(v -> {
                    data = PH.Data.DATA_LOADING;
                    notifyItemChanged(position);
                    actions.loadMore();
                });
                break;
            case ERROR_LOAD:
            case ERROR_NO_INTERNET:
                info.setOnClickListener(v -> {
                    data = PH.Data.DATA_LOADING;
                    notifyItemChanged(position);
                    actions.reload();
                });
                break;
            default:
                break;
        }
    }

}
