package hu.sherad.hos.ui.recyclerview.adapters.section;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.recyclerview.holders.HolderListSimpleView;
import hu.sherad.hos.ui.recyclerview.holders.HolderListTopic;
import hu.sherad.hos.utils.HtmlUtils;

public class AdapterExplore extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private boolean subTopics = false;
    private RecyclerView recyclerView;
    private List<Topic> topics = new ArrayList<>();
    private Actions actions;
    private PH.Data data = PH.Data.DATA_LOADING;

    public AdapterExplore(Activity activity, RecyclerView recyclerView, Actions actions) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.actions = actions;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case R.layout.list_item_text_view:
                bindViewAsText((HolderListSimpleView) holder, position);
                break;
            case R.layout.list_item_topic:
                bindViewAsTopic((HolderListTopic) holder, position);
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
        return topics.get(position).containsURL(Topic.UrlType.TOPIC) ? R.layout.list_item_topic : R.layout.list_item_text_view;
    }

    @Override
    public int getItemCount() {
        return topics.size() + (data == PH.Data.OK ? 0 : 1);
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setSubTopics(boolean subTopics) {
        this.subTopics = subTopics;
    }

    public void addTopics(List<Topic> topics, PH.Data data) {
        this.data = data;

        if (subTopics) {
            int lastSize = getItemCount();
            // Last load
            if (data == PH.Data.OK) {
                notifyItemRemoved(getItemCount());
                this.topics.addAll(topics);
                notifyItemRangeInserted(lastSize, this.topics.size());
                return;
            }
            // Can load more, or failed to load
            this.topics.addAll(topics);
            notifyItemRangeInserted(lastSize, this.topics.size() + 1);
        } else {
            this.topics = topics;
            notifyDataSetChanged();
            recyclerView.scrollToPosition(0);
        }
    }

    private void bindViewAsTopic(HolderListTopic holder, int position) {

        Topic topic = topics.get(position);

        holder.getTextViewTitle().setText(topic.getTitle());

        holder.getButtonTitle().setText(HtmlUtils.getFirstReadableCharacter(topic.getTitle()));
        holder.itemView.setOnClickListener(v -> {
            if (subTopics) {
                Intent intent = ActivityComments.createIntent(topic);
                activity.startActivity(intent);
            } else {
                actions.chosen(topic, position);
            }
        });

    }

    private void bindViewAsText(HolderListSimpleView holder, int position) {
        TextView info = holder.itemView.findViewById(R.id.text_view_util);
        if (topics.size() != position) {
            info.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            info.setText(topics.get(position).getTitle());
            return;
        }
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

    public interface Actions {

        void chosen(Topic topic, int position);

        void loadMore();

        void reload();
    }
}
