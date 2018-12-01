package hu.sherad.hos.ui.recyclerview.adapters.section;

import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.user.UserTopics;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.ui.recyclerview.ItemTouchHelperCallback;
import hu.sherad.hos.ui.recyclerview.holders.HolderListSimpleView;
import hu.sherad.hos.ui.recyclerview.holders.HolderListTopic;
import hu.sherad.hos.ui.recyclerview.interfaces.SortableModifiableTopicActions;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.NotificationUtils;
import hu.sherad.hos.utils.io.Logger;
import hu.sherad.hos.utils.keyboard.ClipboardUtils;

public class AdapterFavourites extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperCallback.ItemTouchHelperAdapter {

    private SortableModifiableTopicActions actions;

    private ColorStateList primary;
    private ColorStateList red;

    private int expandedPosition = -1;
    private int previousExpandedPosition = -1;

    public AdapterFavourites(SortableModifiableTopicActions actions) {
        this.actions = actions;

        primary = ContextCompat.getColorStateList(PHApplication.getInstance(), R.color.primary);
        red = ContextCompat.getColorStateList(PHApplication.getInstance(), R.color.material_red_500);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.layout.list_item_topic:
                return new HolderListTopic(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
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
        if (position == UserTopics.getInstance().getTopics(Topic.Type.FAVOURITE).size()) {
            return R.layout.list_item_text_view;
        }
        return R.layout.list_item_topic;

    }

    @Override
    public int getItemCount() {
        return UserTopics.getInstance().getTopics(Topic.Type.FAVOURITE).size() + (UserTopics.getInstance().getData() == PH.Data.OK ? 0 : 1);
    }

    public void setTopicToSeen(Topic topicToSeen) {
        int position = UserTopics.getInstance().setTopicToSeen(Topic.Type.FAVOURITE, topicToSeen);
        if (position == RecyclerView.NO_POSITION) {
            Logger.getLogger().i("No topic found with [" + topicToSeen.getTitle() + "] title");
        } else {
            notifyItemChanged(position);
        }
        // Also if it's in the commented section update it
        UserTopics.getInstance().setTopicToSeen(Topic.Type.COMMENTED, topicToSeen);
    }

    public void deleteTopic(Topic topicToDelete) {
        int position = UserTopics.getInstance().removeTopic(Topic.Type.FAVOURITE, topicToDelete);
        if (position == RecyclerView.NO_POSITION) {
            Logger.getLogger().i("No topic found with [" + topicToDelete.getTitle() + "] title");
        } else {
            notifyItemRemoved(position);
        }
    }

    public void reloadData() {
        Logger.getLogger().i(
                "Reloading topics: " + getItemCount() + " to " + UserTopics.getInstance().getTopics(Topic.Type.FAVOURITE).size());
        notifyDataSetChanged();
    }

    public void setExpandedPosition(int expandedPosition) {
        this.expandedPosition = expandedPosition;
    }

    public void setPreviousExpandedPosition(int previousExpandedPosition) {
        this.previousExpandedPosition = previousExpandedPosition;
    }

    private void bindViewAsTopic(HolderListTopic holder, int position) {
        Topic topic = UserTopics.getInstance().getTopics(Topic.Type.FAVOURITE).get(position);
        // Title
        holder.getTextViewTitle().setText(topic.getTitle());
        if (actions.isSorting()) {
            holder.getTextViewBadge().setVisibility(View.GONE);
            holder.getImageButtonNotification().setVisibility(View.GONE);
            holder.getImageButtonCopy().setVisibility(View.GONE);
            holder.getImageButtonDelete().setVisibility(View.GONE);
            holder.getButtonTitle().setBackgroundTintList(primary);
            holder.getButtonTitle().setText(HtmlUtils.getFirstReadableCharacter(topic.getTitle()));
            holder.itemView.setOnClickListener(null);
            holder.itemView.setActivated(false);
            return;
        }

        final boolean isExpanded = position == expandedPosition;
        if (isExpanded) {
            previousExpandedPosition = position;
        }
        if (topic.getNewComments() == 0) {
            holder.getTextViewBadge().setText(null);
            holder.getTextViewBadge().setVisibility(View.GONE);
        } else {
            holder.getTextViewBadge().setText(String.valueOf(topic.getNewComments()));
            holder.getTextViewBadge().setVisibility(View.VISIBLE);
            holder.getTextViewBadge().requestLayout();
        }

        holder.getButtonTitle().setBackgroundTintList(topic.getNewComments() == 0 ? primary : red);
        holder.getButtonTitle().setText(HtmlUtils.getFirstReadableCharacter(topic.getTitle()));

        if (isExpanded) {
            holder.getImageButtonNotification().setVisibility(View.VISIBLE);
            holder.getImageButtonCopy().setVisibility(View.VISIBLE);
            holder.getImageButtonDelete().setVisibility(View.VISIBLE);
            // Notification
            holder.getImageButtonNotification().setSelected(NotificationUtils.getNotifiableTopics(Topic.Type.FAVOURITE).contains(topic.getTitle()));
            holder.getImageButtonNotification().setOnClickListener(view -> NotificationUtils.toggleNotificationWithDialog(topic, Topic.Type.FAVOURITE, holder));
            // EXTRA_URL copy
            holder.getImageButtonCopy().setOnClickListener(view -> ClipboardUtils.copyToClipBoard(PHApplication.getInstance(),
                    topic.getURL(Topic.UrlType.TOPIC), topic.getURL(Topic.UrlType.TOPIC), "EXTRA_URL vágólapra másolva"));
            // Delete
            holder.getImageButtonDelete().setOnClickListener(view -> actions.onDelete(topic));
        } else {
            holder.getImageButtonNotification().setVisibility(View.GONE);
            holder.getImageButtonCopy().setVisibility(View.GONE);
            holder.getImageButtonDelete().setVisibility(View.GONE);
        }
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
        if (position == UserTopics.getInstance().getTopics(Topic.Type.FAVOURITE).size() && UserTopics.getInstance().getData() == PH.Data.OK) {
            info.setText(R.string.no_available_topic);
            return;
        }
        info.setText(PH.getErrorFromCode(UserTopics.getInstance().getData()));
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(UserTopics.getInstance().getTopics(Topic.Type.FAVOURITE), i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(UserTopics.getInstance().getTopics(Topic.Type.FAVOURITE), i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

}
