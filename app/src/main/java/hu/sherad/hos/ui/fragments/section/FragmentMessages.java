package hu.sherad.hos.ui.fragments.section;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Objects;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.user.UserTopics;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.activities.ActivityHome;
import hu.sherad.hos.ui.recyclerview.WrapContentLinearLayoutManager;
import hu.sherad.hos.ui.recyclerview.adapters.section.AdapterMessages;
import hu.sherad.hos.ui.recyclerview.interfaces.LoadMoreModifiableTopicActions;
import hu.sherad.hos.utils.Analytics;
import hu.sherad.hos.utils.NotificationUtils;

import static android.app.Activity.RESULT_OK;

public class FragmentMessages extends FragmentBase implements TopicResult.OnTopicResult, LoadMoreModifiableTopicActions, ActivityHome.AddMethods {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private AdapterMessages adapterMessages;
    private DividerItemDecoration decoration;
    private RecyclerView.LayoutManager layoutManager;

    private int currentOffset = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layoutManager = new WrapContentLinearLayoutManager(Objects.requireNonNull(getActivity()));
        decoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        adapterMessages = new AdapterMessages(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.util_swipe_refresh, container, false);

        recyclerView = parent.findViewById(R.id.recycler_view_util);
        swipeRefreshLayout = parent.findViewById(R.id.swipe_refresh_util);

        return parent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterMessages);
        recyclerView.addItemDecoration(decoration);

        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentOffset = 0;
            PH.getPHService().getMessages(currentOffset, this);
        });

        if (UserTopics.getInstance().isNeedToUpdate()) {
            PH.getPHService().getUserTopics(() -> updateRecyclerView(UserTopics.getInstance().getTopics(Topic.Type.MESSAGE), UserTopics.getInstance().getDataForMessage()));
        } else {
            updateRecyclerView(UserTopics.getInstance().getTopics(Topic.Type.MESSAGE), UserTopics.getInstance().getDataForMessage());
        }
        Analytics.sendContentView(getString(R.string.messages), null);
    }

    private void updateRecyclerView(List<Topic> topics, PH.Data dataForMessage) {
        adapterMessages.reloadData(topics, dataForMessage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                Topic topic = (Topic) bundle.getSerializable(ActivityComments.TOPIC);
                if (topic != null) {
                    adapterMessages.setTopicToSeen(topic);
                }
            }
        }
    }

    @Override
    public void onResult(@NonNull TopicResult result) {
        if (isAdded()) {
            if (swipeRefreshLayout.isRefreshing()) {
                updateRecyclerView(result.getTopics(), result.getData());
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
            swipeRefreshLayout.setRefreshing(false);
            adapterMessages.addMessages(result.getTopics(), result.getData());
        }
    }

    @Override
    public void reload() {
        PH.getPHService().getMessages(currentOffset, this);
    }

    @Override
    public void onDelete(Topic topic) {
        // TODO: 2018. 07. 08. Implement delete message
    }

    @Override
    public void onSelected(Topic topic) {
        NotificationUtils.cancelNotification(topic);
        Intent intent = ActivityComments.createIntent(topic);
        startActivityForResult(intent, ActivityHome.REQUEST_CODE_FOR_MESSAGES);
    }

    @Override
    public void loadMore() {
        currentOffset += 25;
        PH.getPHService().getMessages(currentOffset, this);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onVisible(Toolbar toolbar) {
        toolbar.setSubtitle(R.string.messages);
    }

}