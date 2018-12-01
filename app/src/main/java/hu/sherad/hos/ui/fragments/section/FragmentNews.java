package hu.sherad.hos.ui.fragments.section;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.models.TopicRear;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.ui.activities.ActivityHome;
import hu.sherad.hos.ui.recyclerview.WrapContentLinearLayoutManager;
import hu.sherad.hos.ui.recyclerview.adapters.section.AdapterNews;
import hu.sherad.hos.ui.recyclerview.interfaces.TopicRearActions;
import hu.sherad.hos.utils.Analytics;

public class FragmentNews extends FragmentBase implements TopicResult.OnTopicResult, TopicRearActions, ActivityHome.AddMethods {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    private AdapterNews adapterNews;
    private DividerItemDecoration decoration;
    private RecyclerView.LayoutManager layoutManager;

    private String domain;
    private int currentOffset = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapterNews = new AdapterNews(this);
        layoutManager = new WrapContentLinearLayoutManager(Objects.requireNonNull(getActivity()));
        decoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        domain = PHPreferences.getInstance().getDefaultPreferences().getString(PH.Prefs.KEY_DEFAULT_NEWS_VALUE, PH.Api.PROHARDVER_NEWS);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View parent = inflater.inflate(R.layout.util_swipe_refresh, container, false);

        swipeRefreshLayout = parent.findViewById(R.id.swipe_refresh_util);
        recyclerView = parent.findViewById(R.id.recycler_view_util);

        return parent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeRefreshLayout.setEnabled(false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapterNews);
        recyclerView.addItemDecoration(decoration);

        PH.getPHService().getNews(domain, currentOffset, this);
        Analytics.sendContentView(getString(R.string.news), null);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (isAdded() && isVisible()) {
            switch (domain) {
                case PH.Api.PROHARDVER_NEWS:
                    if (menu.findItem(R.id.menu_news_prohardver) != null) {
                        menu.findItem(R.id.menu_news_prohardver).setChecked(true);
                    }
                    break;
                case PH.Api.MOBILARENA_NEWS:
                    if (menu.findItem(R.id.menu_news_mobilarena) != null) {
                        menu.findItem(R.id.menu_news_mobilarena).setChecked(true);
                    }
                    break;
                case PH.Api.IT_CAFE_NEWS:
                    if (menu.findItem(R.id.menu_news_it_cafe) != null) {
                        menu.findItem(R.id.menu_news_it_cafe).setChecked(true);
                    }
                    break;
                case PH.Api.GAMEPOD_NEWS:
                    if (menu.findItem(R.id.menu_news_gamepod) != null) {
                        menu.findItem(R.id.menu_news_gamepod).setChecked(true);
                    }
                    break;
                default:
                    if (menu.findItem(R.id.menu_news_logout) != null) {
                        menu.findItem(R.id.menu_news_logout).setChecked(true);
                    }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_news_prohardver:
                if (!domain.equals(PH.Api.PROHARDVER_NEWS)) {
                    updateNews(PH.Api.PROHARDVER_NEWS);
                }
                return true;
            case R.id.menu_news_mobilarena:
                if (!domain.equals(PH.Api.MOBILARENA_NEWS)) {
                    updateNews(PH.Api.MOBILARENA_NEWS);
                }
                return true;
            case R.id.menu_news_it_cafe:
                if (!domain.equals(PH.Api.IT_CAFE_NEWS)) {
                    updateNews(PH.Api.IT_CAFE_NEWS);
                }
                return true;
            case R.id.menu_news_gamepod:
                if (!domain.equals(PH.Api.GAMEPOD_NEWS)) {
                    updateNews(PH.Api.GAMEPOD_NEWS);
                }
                return true;
            case R.id.menu_news_logout:
                if (!domain.equals(PH.Api.LOGOUT_NEWS)) {
                    updateNews(PH.Api.LOGOUT_NEWS);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateNews(String newsDomain) {
        PHPreferences.getInstance().getDefaultPreferences().edit().putString(PH.Prefs.KEY_DEFAULT_NEWS_VALUE, newsDomain).apply();
        domain = newsDomain;
        adapterNews.setLoadingAndClearData();
        currentOffset = 0;
        PH.getPHService().getNews(domain, currentOffset, this);
        if (toolbar != null) {
            toolbar.setSubtitle(getSubtitleFromDomain());
        }
    }

    @Override
    public void onResult(@NonNull TopicResult topicResult) {
        if (isAdded()) {
            adapterNews.addData(topicResult.getTopicRears(), topicResult.getData());
        }
    }

    @Override
    public void loadMore() {
        PH.getPHService().getNews(domain, ++currentOffset, this);
    }

    @Override
    public void reload() {
        PH.getPHService().getNews(domain, currentOffset, this);
    }

    @Override
    public void onSelected(TopicRear topicRear) {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.primary))
                .setStartAnimations(getActivity(), R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(getActivity(), R.anim.slide_in_left, R.anim.slide_out_right)
                .addDefaultShareMenuItem()
                .setShowTitle(true)
                .build();
        customTabsIntent.launchUrl(getActivity(), Uri.parse(topicRear.getTopicRearLink()));
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onVisible(Toolbar toolbar) {
        this.toolbar = toolbar;
        toolbar.setSubtitle(getSubtitleFromDomain());
    }

    private
    @StringRes
    int getSubtitleFromDomain() {
        switch (domain) {
            case PH.Api.PROHARDVER_NEWS:
                return R.string.menu_prohardver_news;
            case PH.Api.MOBILARENA_NEWS:
                return R.string.menu_mobilarena_news;
            case PH.Api.IT_CAFE_NEWS:
                return R.string.menu_it_news;
            case PH.Api.GAMEPOD_NEWS:
                return R.string.menu_gamepod_news;
            default:
                return R.string.menu_logout;
        }
    }
}
