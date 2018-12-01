package hu.sherad.hos.ui.fragments.section;

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
import hu.sherad.hos.ui.activities.ActivityHome;
import hu.sherad.hos.ui.recyclerview.WrapContentLinearLayoutManager;
import hu.sherad.hos.ui.recyclerview.adapters.section.AdapterAbout;
import hu.sherad.hos.utils.Analytics;

public class FragmentAbout extends FragmentBase implements ActivityHome.AddMethods {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private RecyclerView.LayoutManager layoutManager;
    private DividerItemDecoration decoration;
    private AdapterAbout adapterAbout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        decoration = new DividerItemDecoration(Objects.requireNonNull(getActivity()), DividerItemDecoration.HORIZONTAL);
        layoutManager = new WrapContentLinearLayoutManager(getActivity());
        adapterAbout = new AdapterAbout();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        View parent = inflater.inflate(R.layout.util_swipe_refresh, container, false);

        swipeRefreshLayout = parent.findViewById(R.id.swipe_refresh_util);
        recyclerView = parent.findViewById(R.id.recycler_view_util);


        Analytics.sendContentView(getString(R.string.about), null);
        return parent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeRefreshLayout.setEnabled(false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapterAbout);
        recyclerView.addItemDecoration(decoration);


    }

    @Override
    public void onVisible(Toolbar toolbar) {
        toolbar.setSubtitle(R.string.about);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

}
