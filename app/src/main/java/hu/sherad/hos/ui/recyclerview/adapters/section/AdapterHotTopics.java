package hu.sherad.hos.ui.recyclerview.adapters.section;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.ui.recyclerview.holders.HolderListSimpleView;
import hu.sherad.hos.ui.recyclerview.holders.HolderListTopic;
import hu.sherad.hos.ui.recyclerview.interfaces.TopicActions;
import hu.sherad.hos.utils.HtmlUtils;

public class AdapterHotTopics extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Topic> topics = new ArrayList<>();
    private TopicActions actions;
    private PH.Data data = PH.Data.DATA_LOADING;

    public AdapterHotTopics(TopicActions actions) {
        this.actions = actions;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.layout.list_item_topic:
                return new HolderListTopic(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
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
            case R.layout.list_item_topic:
                bindViewAsTopic((HolderListTopic) holder, position);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if ((topics.size() == position && data == PH.Data.OK) || position == topics.size()) {
            if (data == PH.Data.DATA_LOADING) {
                return R.layout.list_item_loading;
            }
            return R.layout.list_item_text_view;
        }
        return R.layout.list_item_topic;

    }

    @Override
    public int getItemCount() {
        if (topics.size() == 0 && data == PH.Data.OK) {
            return 1;
        }
        return topics.size() + (data == PH.Data.OK ? 0 : 1);
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void reloadData(List<Topic> topics, PH.Data data) {
        this.data = data;
        this.topics = topics;
        notifyDataSetChanged();
    }

    private void bindViewAsTopic(HolderListTopic holder, int position) {
        Topic topic = topics.get(position);

        holder.getTextViewTitle().setText(topic.getTitle());
        holder.getButtonTitle().setText(HtmlUtils.getFirstReadableCharacter(topic.getTitle()));

        holder.itemView.setOnClickListener(v -> actions.onSelected(topic));
    }

    private void bindViewAsText(HolderListSimpleView holder, int position) {
        TextView info = holder.itemView.findViewById(R.id.text_view_util);
        if (position == topics.size() && data == PH.Data.OK) {
            info.setText(R.string.no_available_topic);
            return;
        }
        info.setText(PH.getErrorFromCode(data));
        switch (data) {
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
