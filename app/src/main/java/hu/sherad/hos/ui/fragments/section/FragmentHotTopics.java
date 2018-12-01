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

import java.util.Objects;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.recyclerview.WrapContentLinearLayoutManager;
import hu.sherad.hos.ui.recyclerview.adapters.section.AdapterHotTopics;
import hu.sherad.hos.ui.recyclerview.interfaces.TopicActions;
import hu.sherad.hos.utils.Analytics;

public class FragmentHotTopics extends FragmentBase implements TopicResult.OnTopicResult, TopicActions {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private AdapterHotTopics adapterHotTopics;
    private DividerItemDecoration decoration;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapterHotTopics = new AdapterHotTopics(this);
        layoutManager = new WrapContentLinearLayoutManager(Objects.requireNonNull(getActivity()));
        decoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
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
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapterHotTopics);
        recyclerView.addItemDecoration(decoration);

        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(() -> PH.getPHService().getHotTopics(this));

        PH.getPHService().getHotTopics(this);
        Analytics.sendContentView(getString(R.string.hot_topics), null);
    }

    @Override
    public void onResult(@NonNull TopicResult result) {
        if (isAdded()) {
            swipeRefreshLayout.setRefreshing(false);
            adapterHotTopics.reloadData(result.getTopics(), result.getData());
        }
    }

    @Override
    public void reload() {
        PH.getPHService().getHotTopics(this);
    }

    @Override
    public void onSelected(Topic topic) {
        Intent intent = ActivityComments.createIntent(topic);
        startActivity(intent);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onVisible(Toolbar toolbar) {
        toolbar.setSubtitle(R.string.hot_topics);
    }
}
