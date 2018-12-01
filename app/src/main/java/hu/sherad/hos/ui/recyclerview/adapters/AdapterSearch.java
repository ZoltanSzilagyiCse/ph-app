package hu.sherad.hos.ui.recyclerview.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
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

public class AdapterSearch extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int REQUEST_CODE_VIEW_TOPIC = 5407;

    private Activity activity;
    private List<Topic> topics = new ArrayList<>();
    private ReachedEnd reachedEnd;
    private PH.Data data = PH.Data.OK;

    public AdapterSearch(Activity activity, ReachedEnd reachedEnd) {
        this.activity = activity;
        this.reachedEnd = reachedEnd;
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
        return R.layout.list_item_topic;
    }

    @Override
    public int getItemCount() {
        if (topics.isEmpty()) {
            return 1;
        }
        return topics.size() + (data == PH.Data.OK ? 0 : 1);
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void clear() {
        topics.clear();
        notifyDataSetChanged();
    }

    public void setLoading() {
        topics.clear();
        data = PH.Data.DATA_LOADING;
        notifyDataSetChanged();
    }

    public void setData(PH.Data data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void addTopics(List<Topic> topics, PH.Data data) {
        this.data = data;
        int lastSize = getItemCount();
        this.topics.addAll(topics);
        notifyItemRangeInserted(lastSize, this.topics.size() + (data == PH.Data.OK ? 0 : 1));
    }

    private void bindViewAsTopic(HolderListTopic holder, int position) {
        Topic topic = topics.get(position);

        holder.getTextViewTitle().setText(topic.getTitle());

        holder.getButtonTitle().setText(HtmlUtils.getFirstReadableCharacter(topic.getTitle()));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = ActivityComments.createIntent(topic);
            activity.startActivityForResult(intent, REQUEST_CODE_VIEW_TOPIC);
        });
    }

    private void bindViewAsText(HolderListSimpleView holder, int position) {
        TextView info = holder.itemView.findViewById(R.id.text_view_util);
        if (data == PH.Data.OK) {
            info.setText(R.string.no_available_topic);
            return;
        }
        info.setText(PH.getErrorFromCode(data));
        switch (data) {
            case DATA_CAN_LOAD:
                info.setOnClickListener(v -> {
                    data = PH.Data.DATA_LOADING;
                    notifyItemChanged(position);
                    reachedEnd.loadMore();
                });
                break;
            case ERROR_LOAD:
            case ERROR_NO_INTERNET:
                info.setOnClickListener(v -> {
                    data = PH.Data.DATA_LOADING;
                    notifyItemChanged(position);
                    reachedEnd.reload();
                });
                break;
            default:
                break;
        }
    }

    public interface ReachedEnd {

        void loadMore();

        void reload();
    }
}
