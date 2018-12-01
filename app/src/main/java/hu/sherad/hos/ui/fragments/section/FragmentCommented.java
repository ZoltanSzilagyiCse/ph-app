package hu.sherad.hos.ui.fragments.section;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.user.UserTopics;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.activities.ActivityHome;
import hu.sherad.hos.ui.recyclerview.ItemTouchHelperCallback;
import hu.sherad.hos.ui.recyclerview.WrapContentLinearLayoutManager;
import hu.sherad.hos.ui.recyclerview.adapters.section.AdapterCommented;
import hu.sherad.hos.ui.recyclerview.interfaces.SortableModifiableTopicActions;
import hu.sherad.hos.utils.Analytics;
import hu.sherad.hos.utils.NotificationUtils;
import hu.sherad.hos.utils.Util;

import static android.app.Activity.RESULT_OK;

public class FragmentCommented extends FragmentBase implements SortableModifiableTopicActions, UserTopics.OnUpdate {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private AdapterCommented adapterCommentedTopics;
    private DividerItemDecoration decoration;
    private ItemTouchHelperCallback callback;
    private RecyclerView.LayoutManager layoutManager;
    private ItemTouchHelper itemTouchHelper;

    private boolean sorting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sorting = false;

        layoutManager = new WrapContentLinearLayoutManager(Objects.requireNonNull(getActivity()));
        decoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        adapterCommentedTopics = new AdapterCommented(this);
        callback = new ItemTouchHelperCallback(adapterCommentedTopics);
        itemTouchHelper = new ItemTouchHelper(callback);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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
        recyclerView.setAdapter(adapterCommentedTopics);
        recyclerView.addItemDecoration(decoration);

        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(() -> PH.getPHService().getUserTopics(this));

        itemTouchHelper.attachToRecyclerView(recyclerView);

        Analytics.sendContentView(getString(R.string.commented_list), null);
    }

    private void updateTopicsIfNeeded() {
        if (UserTopics.getInstance().isNeedToUpdate()) {
            swipeRefreshLayout.setRefreshing(true);
            PH.getPHService().getUserTopics(this);
        } else {
            updateRecyclerView();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_commented_topics_sort:
                toggleSorting();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                Topic topic = (Topic) bundle.getSerializable(ActivityComments.TOPIC);
                if (topic != null) {
                    adapterCommentedTopics.setTopicToSeen(topic);
                }
            }
        }
    }

    @Override
    public void updateDone() {
        if (isAdded()) {
            swipeRefreshLayout.setRefreshing(false);
            updateRecyclerView();
        }
    }

    private void updateRecyclerView() {
        adapterCommentedTopics.setExpandedPosition(-1);
        adapterCommentedTopics.setPreviousExpandedPosition(-1);
        adapterCommentedTopics.reloadData();
    }

    @Override
    public boolean isSorting() {
        return sorting;
    }

    @Override
    public void reload() {
        PH.getPHService().getUserTopics(this);
    }

    @Override
    public void onSelected(Topic topic) {
        NotificationUtils.cancelNotification(topic);
        Intent intent = ActivityComments.createIntent(topic);
        startActivityForResult(intent, ActivityHome.REQUEST_CODE_FOR_COMMENTED);
    }

    @Override
    public void onDelete(Topic topic) {
        new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                .title("Törlés az itt szóltam hozzá listából")
                .content("Biztosan törölni szeretnéd a " + topic.getTitle() + " topikot az itt szóltam hozzá listából?")
                .positiveText(R.string.yes)
                .autoDismiss(false)
                .negativeText(R.string.cancel)
                .onAny((dialog, which) -> {
                    switch (which) {
                        case POSITIVE:
                            MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                                    .title(R.string.loading)
                                    .content("Törlés az itt szóltam hozzá listából...")
                                    .progress(true, 0)
                                    .cancelable(false)
                                    .build();
                            progressDialog.show();
                            PH.getPHService().deleteFromCommentedTopics(topic.getURL(Topic.UrlType.COMMENTED_DELETE), (error, link) -> {
                                progressDialog.cancel();
                                if (error.isEmpty()) {
                                    dialog.cancel();
                                    adapterCommentedTopics.deleteTopic(topic);
                                    Toast.makeText(getActivity(), "Törölve az itt szóltam hozzá listából", Toast.LENGTH_SHORT).show();
                                    // Delete from the notifications too
                                    Set<String> savedTopics = PHPreferences.getInstance().getNotificationPreferences().getStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, new HashSet<>());
                                    savedTopics.remove(topic.getTitle());
                                    PHPreferences.getInstance().getNotificationPreferences().edit().putStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, savedTopics).apply();
                                    PH.getPHService().updateNotificationAlarm();
                                } else {
                                    Toast.makeText(PHApplication.getInstance(), error, Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case NEGATIVE:
                            dialog.cancel();
                            break;
                        default:
                            break;
                    }
                })
                .show();
    }

    private void toggleSorting() {
        if (sorting) {
            PHPreferences.getInstance().getDefaultPreferences().edit()
                    .putString(PH.Prefs.KEY_DEFAULT_COMMENTED_TOPICS_SORT,
                            new Gson().toJson(Util.convertTopicsToNameList(UserTopics.getInstance().getTopics(Topic.Type.COMMENTED)))).apply();
            callback.setLongPressDragEnabled(false);
            swipeRefreshLayout.setEnabled(true);
        } else {
            callback.setLongPressDragEnabled(true);
            swipeRefreshLayout.setEnabled(false);
        }
        sorting = !sorting;
        adapterCommentedTopics.setExpandedPosition(-1);
        adapterCommentedTopics.setPreviousExpandedPosition(-1);
        adapterCommentedTopics.notifyDataSetChanged();
    }

    @Override
    public boolean onBackPressed() {
        if (sorting) {
            toggleSorting();
            return false;
        }
        return true;
    }

    @Override
    public void onVisible(Toolbar toolbar) {
        toolbar.setSubtitle(R.string.commented_list);
        updateTopicsIfNeeded();
    }

}
