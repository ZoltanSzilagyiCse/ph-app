package hu.sherad.hos.ui.fragments.section;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.activities.ActivityHome;
import hu.sherad.hos.ui.activities.ActivitySearch;
import hu.sherad.hos.ui.recyclerview.WrapContentLinearLayoutManager;
import hu.sherad.hos.ui.recyclerview.adapters.section.AdapterExplore;
import hu.sherad.hos.utils.Analytics;

public class FragmentExplore extends FragmentBase implements ActivityHome.AddMethods, TopicResult.OnTopicResult, AdapterExplore.Actions {

    private AdapterExplore adapterExplore;
    private Topic subTopic = null;
    private int currentOffset = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View parent = inflater.inflate(R.layout.util_swipe_refresh, container, false);
        parent.findViewById(R.id.swipe_refresh_util).setEnabled(false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            subTopic = (Topic) bundle.getSerializable(ActivityComments.TOPIC);
        }

        WrapContentLinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(getActivity());

        RecyclerView recyclerView = parent.findViewById(R.id.recycler_view_util);

        adapterExplore = new AdapterExplore(getActivity(), recyclerView, this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterExplore);
        recyclerView.addItemDecoration(new DividerItemDecoration(parent.getContext(), DividerItemDecoration.HORIZONTAL));

        if (subTopic == null) {
            PH.getPHService().getTopics(this);
        } else {
            PH.getPHService().getSubTopics(subTopic.getURL(Topic.UrlType.TOPIC), currentOffset, this);
            Analytics.sendContentView(getString(R.string.discover), null);
        }

        return parent;
    }

    public Topic getSubTopic() {
        return subTopic;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_explore_search:
                startActivity(new Intent(getActivity(), ActivitySearch.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult(@NonNull TopicResult result) {
        if (isAdded()) {
            adapterExplore.setSubTopics(subTopic != null);
            adapterExplore.addTopics(result.getTopics(), result.getData());
        }
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void chosen(Topic topic, int position) {
        FragmentExplore fragmentExplore = new FragmentExplore();
        Bundle arguments = new Bundle();
        arguments.putSerializable(ActivityComments.TOPIC, topic);
        fragmentExplore.setArguments(arguments);
        ((ActivityHome) getActivity()).getAppBarLayout().setExpanded(true);
        ((ActivityHome) getActivity()).replaceFragment(fragmentExplore, true);
    }

    @Override
    public void loadMore() {
        currentOffset += 50;
        PH.getPHService().getSubTopics(subTopic.getURL(Topic.UrlType.TOPIC), currentOffset, this);
    }

    @Override
    public void reload() {
        if (subTopic == null) {
            PH.getPHService().getTopics(this);
        } else {
            PH.getPHService().getSubTopics(subTopic.getURL(Topic.UrlType.TOPIC), currentOffset, this);
        }
    }

    @Override
    public void onVisible(Toolbar toolbar) {
        toolbar.setSubtitle(R.string.discover);
    }
}
