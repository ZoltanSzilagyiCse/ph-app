package hu.sherad.hos.ui.fragments;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.recyclerview.adapters.AdapterComments;
import hu.sherad.hos.ui.widget.FloatingActionButton;
import hu.sherad.hos.utils.HtmlUtils;

public final class FragmentComments implements PHService.TopicComments {

    private View parent;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private AdapterComments adapterComments = new AdapterComments();

    private ActivityComments activity;

    private boolean needToSetup = true;
    private int newComments;
    private int position;
    private FloatingActionButton floatingActionButton;

    public FragmentComments(ActivityComments activity, ViewGroup container) {
        this.activity = activity;
        assignLayoutElements(activity, container);

        swipeRefreshLayout.setOnRefreshListener(activity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterComments);
    }

    private void assignLayoutElements(ActivityComments activity, ViewGroup container) {
        parent = LayoutInflater.from(activity).inflate(R.layout.fragment_comments, container, false);
        recyclerView = parent.findViewById(R.id.recycler_view_fragment_comments);
        swipeRefreshLayout = parent.findViewById(R.id.swipe_refresh_activity_comments);
        floatingActionButton = activity.getFloatingActionButton();
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public View getParent() {
        return parent;
    }

    public AdapterComments getAdapterComments() {
        return adapterComments;
    }

    public boolean isNeedToSetup() {
        return needToSetup;
    }

    public void setupAndLoad(int position) {
        if (needToSetup) {
            needToSetup = false;
            this.position = position;

            int minComment = (position * activity.getTopicDetailed().getCommentsSize()) + 1; // 1 / 26 / 51 / ...
            int maxComment = (position + 1) * activity.getTopicDetailed().getCommentsSize(); // 25 / 50 / 75 / ...

            updateNewComments(position, minComment, maxComment);

            setupRecyclerView();
            setupFloatingActionButton();
            adapterComments.setup(activity, newComments);
            loadComments();
        }
    }

    private void setupFloatingActionButton() {
        if (activity.getTopicDetailed().getStatus() != Topic.Status.CLOSED) {
            if (activity.isEnabledFAB()) {
                floatingActionButton.setImageTintList(ContextCompat.getColorStateList(activity, R.color.primary_text_light));
            } else {
                floatingActionButton.hide(false);
            }
        }
    }

    private void setupRecyclerView() {
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL));
        recyclerView.addOnScrollListener(activity.getRecyclerViewScrollListener());

    }

    private void updateNewComments(int position, int minComment, int maxComment) {
        if (activity.getTopicDetailed().getNewComments() > 0) {

            int topicMax = activity.getTopicDetailed().getCurrentMaxPosition();
            int sub = topicMax - activity.getTopicDetailed().getNewComments();

            if (((minComment <= sub && maxComment > sub))) { // current  page
                newComments = (maxComment > topicMax ? topicMax : maxComment) - sub;
            } else if (position + 1 == activity.getTabManager().getCount()) { // last page
                newComments = activity.getTopicDetailed().getNewComments();
            } else if (activity.getFirstPage() != -1 && position > activity.getFirstPage()) { // pages between first new and last
                newComments = activity.getTopicDetailed().getCommentsSize();
            }
            activity.getTopicDetailed().setNewComments(activity.getTopicDetailed().getNewComments() - newComments);
        }
    }

    public void loadComments() {
        switch (activity.getType()) {
            case TOPIC_MESSAGE:
                int offSet;
                if (activity.getTopicDetailed().isCommentsIncrementing()) {
                    offSet = position * activity.getTopicDetailed().getCommentsSize();
                } else {
                    int maxSize = (activity.getTabSize() - 1) * activity.getTopicDetailed().getCommentsSize();
                    offSet = maxSize - (position * activity.getTopicDetailed().getCommentsSize());
                }
                PH.getPHService().getMessages(HtmlUtils.changeMessageURLToExplicit(activity.getTopicDetailed().getURL(Topic.UrlType.TOPIC), offSet), this);
                break;
            case TOPIC_DEFAULT:
                PH.getPHService().getComments(HtmlUtils.changeNewToExplicit(activity.getTopicDetailed().getURL(Topic.UrlType.TOPIC),
                        position == 0 ? 1 : (position * activity.getTopicDetailed().getCommentsSize() + 1),
                        ((position + 1) * activity.getTopicDetailed().getCommentsSize())), this);
                break;
        }
    }

    @Override
    public void onTopicComments(@NonNull List<TopicComment> comments, @NonNull PH.Data data) {
        if (activity != null) {
            swipeRefreshLayout.setRefreshing(false);
            adapterComments.addComments(comments, data);
            // Scroll to the new comment
            scrollIfNeeded(comments);
        }
    }

    private void scrollIfNeeded(@NonNull List<TopicComment> comments) {
        if (isAvailableNewComment() && isAutoScrollEnabled()) {
            scrollToNewComment();
        } else {
            int[] fromTo = HtmlUtils.isExplicitCommentLink(activity.getTopicDetailed().getURL(Topic.UrlType.TOPIC));
            // Scroll to the first comment (like the last page (./friss.html)
            if (fromTo == null || activity.getType() == ActivityComments.Type.TOPIC_MESSAGE) {
                scrollToFirstComment();
            } else {
                adapterComments.setActiveComments(fromTo);
                // Scroll to the comment specified by the URL (by the commentID)
                if (isCurrentPageContainsCommentByID(comments, fromTo[0])) {
                    activity.getAppBarLayout().setExpanded(false);
                    recyclerView.scrollToPosition(getCommentIndexByID(comments, fromTo[0]));
                } else {
                    // Scroll to the first comment - if the page before the firstPage
                    if (!isPageAfterFirstPage()) {
                        scrollToFirstComment();
                    }
                }
            }
        }
    }

    private boolean isCurrentPageContainsCommentByID(@NonNull List<TopicComment> comments, int commentID) {
        boolean contains = false;
        for (int i = 0; i < comments.size(); i++) {
            if (commentID == comments.get(i).getID()) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private int getCommentIndexByID(@NonNull List<TopicComment> comments, int commentID) {
        int index = -1;
        for (int i = 0; i < comments.size(); i++) {
            if (commentID == comments.get(i).getID()) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void scrollToNewComment() {
        activity.getAppBarLayout().setExpanded(false);
        if (adapterComments.isListInc()) {
            if (adapterComments.getComments().size() - (adapterComments.getNewComments() - 1) >= 0) {
                recyclerView.scrollToPosition(adapterComments.getComments().size() - (adapterComments.getNewComments() - 1) - 1);
            } else {
                recyclerView.scrollToPosition(0);
            }
        } else {
            if (adapterComments.getNewComments() > adapterComments.getItemCount()) {
                recyclerView.scrollToPosition(adapterComments.getItemCount() - 1);
            } else {
                recyclerView.scrollToPosition(adapterComments.getNewComments() - 1);
            }
        }
    }

    private boolean isAvailableNewComment() {
        return adapterComments.getNewComments() > 0;
    }

    private boolean isAutoScrollEnabled() {
        return PHPreferences.getInstance().getAppearancePreferences()
                .getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT, true);
    }

    private boolean isPageAfterFirstPage() {
        return activity.getFirstPage() < position;
    }

    private void scrollToFirstComment() {
        if (adapterComments.isListInc()) {
            recyclerView.scrollToPosition(adapterComments.getItemCount() - 1);
            activity.getAppBarLayout().setExpanded(false);
        }
    }

}
