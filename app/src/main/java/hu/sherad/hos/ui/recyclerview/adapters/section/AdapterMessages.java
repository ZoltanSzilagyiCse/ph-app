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
import hu.sherad.hos.data.api.user.UserTopics;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.ui.recyclerview.holders.HolderListMessage;
import hu.sherad.hos.ui.recyclerview.holders.HolderListSimpleView;
import hu.sherad.hos.ui.recyclerview.interfaces.LoadMoreModifiableTopicActions;
import hu.sherad.hos.utils.ViewUtils;
import hu.sherad.hos.utils.io.Logger;

public class AdapterMessages extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
    private LoadMoreModifiableTopicActions actions;
    private List<Topic> topics = new ArrayList<>();
    private PH.Data data = PH.Data.DATA_LOADING;

    private int expandedPosition = -1;
    private int previousExpandedPosition = -1;

    public AdapterMessages(LoadMoreModifiableTopicActions actions) {
        this.actions = actions;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.layout.list_item_message:
                return new HolderListMessage(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
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
            case R.layout.list_item_message:
                bindViewAsMessage((HolderListMessage) holder, position);
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
        return R.layout.list_item_message;

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
        expandedPosition = -1;
        previousExpandedPosition = -1;
        notifyDataSetChanged();
    }

    public void addMessages(List<Topic> topics, PH.Data data) {
        this.data = data;
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
    }

    private void bindViewAsMessage(HolderListMessage holder, int position) {
        final boolean isExpanded = position == expandedPosition;
        if (isExpanded) {
            previousExpandedPosition = position;
        }
        Topic topic = topics.get(position);
        // Title
        holder.getTextViewTitle().setText(topic.getTitle());
        // New comments
        if (topic.getNewComments() == 0) {
            holder.getTextViewBadge().setText(null);
            holder.getTextViewBadge().setVisibility(View.GONE);
        } else {
            holder.getTextViewBadge().setText(String.valueOf(topic.getNewComments()));
            holder.getTextViewBadge().setVisibility(View.VISIBLE);
            holder.getTextViewBadge().requestLayout();
        }
        if (isExpanded) {
            // Date
            if (topic.getLastMessage() != null) {
                holder.getTextViewDate().setVisibility(View.VISIBLE);
                holder.getTextViewDate().setOnClickListener(view -> {
                    holder.setLastVisitedAbsolute(!holder.isLastVisitedAbsolute());
                    holder.getTextViewDate().setText(PHApplication.getInstance().getString(R.string.lastMessageV,
                            (holder.isLastVisitedAbsolute() ? simpleDateFormat.format(topic.getLastMessage().getTime()) : DateUtils.getRelativeTimeSpanString(topic.getLastMessage().getTime(),
                                    System.currentTimeMillis(),
                                    DateUtils.SECOND_IN_MILLIS))));
                });
                holder.getTextViewDate().setText(PHApplication.getInstance().getString(R.string.lastMessageV, DateUtils.getRelativeTimeSpanString(topic.getLastMessage().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS)));
            }
            // Notification
            // TODO: 2018. 03. 08. User specific notification for messages
//            holder.getImageViewNotification().setVisibility(View.VISIBLE);
//            holder.getImageViewNotification().setOnClickListener(view -> {
//
//            });
        } else {
            holder.getTextViewDate().setVisibility(View.GONE);
            holder.getImageViewNotification().setVisibility(View.GONE);
            holder.getImageViewUndo().setVisibility(View.GONE);
            holder.getImageViewBan().setVisibility(View.GONE);
        }
        // Avatar
        Glide.with(PHApplication.getInstance())
                .load(topic.getURL(Topic.UrlType.TOPIC_AVATAR))
                .apply(ViewUtils.getDefaultGlideOptions(R.drawable.vd_account_circle))
                .into(holder.getImageAvatar());
        // Item click
        holder.itemView.setOnClickListener(v -> actions.onSelected(topic));
        // Expand
        holder.itemView.setActivated(isExpanded);
        holder.itemView.setOnLongClickListener(view -> {
            expandedPosition = isExpanded ? -1 : position;
            notifyItemChanged(previousExpandedPosition);
            notifyItemChanged(position);
            return true;
        });
    }

    private void bindViewAsText(HolderListSimpleView holder, int position) {
        TextView info = holder.itemView.findViewById(R.id.text_view_util);
        if (position == topics.size() && data == PH.Data.OK) {
            info.setText(R.string.no_message_to_display);
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
            case ERROR_INTERNET_FAILURE:
            case ERROR_SERVER:
            case ERROR_TIMEOUT:
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

    public void setTopicToSeen(Topic topicToSeen) {
        int position = UserTopics.getInstance().setTopicToSeen(Topic.Type.MESSAGE, topicToSeen);
        if (position == RecyclerView.NO_POSITION) {
            Logger.getLogger().i("No topic found with [" + topicToSeen.getTitle() + "] title");
        } else {
            notifyItemChanged(position);
        }
    }

}

