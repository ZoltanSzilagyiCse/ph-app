package hu.sherad.hos.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.sherad.hos.BuildConfig;
import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHAuth;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.data.models.TopicDetailed;
import hu.sherad.hos.ui.fragments.FragmentComments;
import hu.sherad.hos.ui.recyclerview.adapters.AdapterComments;
import hu.sherad.hos.ui.recyclerview.adapters.AdapterCommentsRead;
import hu.sherad.hos.ui.recyclerview.adapters.AdapterPages;
import hu.sherad.hos.ui.widget.FloatingActionButton;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.NotificationUtils;
import hu.sherad.hos.utils.TabManager;
import io.fabric.sdk.android.Fabric;

// TODO: 2017. 07. 21. Load the images in the comments (need to figure out how)
public class ActivityComments extends ActivityBase implements AdapterComments.Actions, AdapterPages.PagesActions, SwipeRefreshLayout.OnRefreshListener {

    public static final String TOPIC = "EXTRA_TOPIC";
    public static final String URL = "EXTRA_URL";

    private static final int REQUEST_CODE_NEW_COMMENT = 1;

    private TopicDetailed topicDetailed = new TopicDetailed();
    private AdapterCommentsRead adapterCommentsRead;
    private DividerItemDecoration decoration;
    private AdapterPages adapterPages;
    private Type type;
    private SparseArray<FragmentComments> tabs;
    private SparseArray<Transition> transitions = new SparseArray<>();

    private int tabSize;
    private int firstPage = -1;
    private boolean isEnabledAutoScroll = true;
    private boolean isEnabledFAB = true;
    private boolean loading = false;

    private String stringNewComment = "";
    private String stringReplyComment = "";
    private String stringPrivateComment = "";

    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private AppBarLayout appBarLayout;
    private LinearLayout linearLayoutHeader;
    private AppCompatImageButton imageViewNext;
    private AppCompatImageButton imageViewPrev;
    private AppCompatImageButton imageViewFirst;
    private AppCompatImageButton imageViewLast;
    private TextView textViewTitle;
    private TextView textViewCurrentPosition;
    private RecyclerView recyclerViewHeader;
    private TabManager tabManager;
    private ViewPager viewPager;
    private FloatingActionButton floatingActionButton;
    private View scrim;
    private MaterialDialog progressDialog;

    private ViewPager.SimpleOnPageChangeListener onPageChangeListener = createPageChangeListener();
    private RecyclerView.OnScrollListener recyclerViewScrollListener = createScrollListener();

    private TopicDetailed.OnFinishTopicDetailed listener = result -> {
        if (isConnectionFailed(result)) {
            failedConnection(result);
            return;
        }
        NotificationUtils.cancelNotification(topicDetailed);
        topicDetailed = result;
        if (isEnabledFAB) {
            floatingActionButton.show(true);
        }
        setTitle();
        viewPager.addOnPageChangeListener(onPageChangeListener);
        // Set the max size of the topic
        calculateTabsSize();
        // Set the pages
        adapterPages = new AdapterPages(ActivityComments.this, tabSize);

        tabs = new SparseArray<>();
        tabManager = new TabManager(this, tabs, tabSize);
        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(tabManager);

        // Set the current page
        calculateFirstPage();
        viewPager.setCurrentItem(firstPage, false);
        // The current page is 0, so we manually call onPageSelected
        if (firstPage == 0) {
            onPageChangeListener.onPageSelected(0);
        }
        addListenersToNavigationSection();
        loading = false;
        invalidateOptionsMenu();
    };

    public static Intent createIntent(@NonNull Topic topic) {
        Intent intent = new Intent(PHApplication.getInstance(), ActivityComments.class);
        intent.putExtra(ActivityComments.TOPIC, topic);
        return intent;
    }

    private void failedConnection(TopicDetailed result) {
        Toast.makeText(this, PH.getErrorFromCode(result.getData()), Toast.LENGTH_SHORT).show();
        textViewCurrentPosition.setText(R.string.fail);
        loading = false;
        invalidateOptionsMenu();
    }

    private boolean isConnectionFailed(TopicDetailed result) {
        return result.getData() != PH.Data.OK;
    }

    private void addListenersToNavigationSection() {
        imageViewFirst.setOnClickListener(v -> viewPager.setCurrentItem(0));
        imageViewPrev.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() - 1));
        imageViewNext.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
        imageViewLast.setOnClickListener(v -> viewPager.setCurrentItem(tabSize));

        textViewCurrentPosition.setOnClickListener(view -> {
            if (tabSize == 1) {
                return;
            }
            showRecyclerViewHeader(adapterPages);
        });
    }

    private void calculateFirstPage() {
        int newCommentsSize = topicDetailed.getNewComments();
        for (int position = 0; position < tabSize && newCommentsSize > 0; position++) {
            int currentMin = (position * topicDetailed.getCommentsSize()) + 1; // 1 / 26 / 51 / ...
            int currentMax = (position + 1) * topicDetailed.getCommentsSize(); // 25 / 50 / 75 / ...
            int topicMax = topicDetailed.getTopicSize();
            int sub = topicMax - newCommentsSize;

            if ((currentMin <= sub && currentMax > sub) || // current page
                    (position + 1 == tabSize)) { // last page
                if (isEnabledAutoScroll && firstPage == -1) {
                    firstPage = position;
                }
                newCommentsSize -= (currentMax > topicMax ? topicMax : currentMax) - sub;
            }
        }
        if (tabSize == 1) {
            firstPage = 0;
        } else {
            if (firstPage == -1) {
                int[] fromTo = HtmlUtils.isExplicitCommentLink(topicDetailed.getURL(Topic.UrlType.TOPIC));
                if (fromTo == null) {
                    firstPage = tabSize;
                    // Position counting start from 0, so we need to decrement currentItem by 1
                    firstPage--;
                } else {
                    for (int i = 1; i <= tabSize; i++) {
                        firstPage = topicDetailed.getCurrentMaxPosition() / topicDetailed.getCommentsSize();
                        if (topicDetailed.getCurrentMaxPosition() % topicDetailed.getCommentsSize() > 1) {
                            firstPage++;
                        }
                        firstPage--;
                    }
                }
            }
        }
    }

    private void calculateTabsSize() {
        tabSize = topicDetailed.getTopicSize() / topicDetailed.getCommentsSize();
        if (topicDetailed.getTopicSize() % topicDetailed.getCommentsSize() > 0) {
            tabSize++;
        }
    }

    private void setTitle() {
        if (textViewTitle.getText().length() == 0) {
            textViewTitle.setText(topicDetailed.getTitle());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PHAuth.isFinishedLogin()) {
            Toast.makeText(this, "Nem vagy bejelentkezve!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());

        handleArguments();
        assignVariables();

        setContentView(R.layout.activity_comments);

        setStatusBarColor();
        assignLayoutElements();
        initActionBar();

        scrim.setOnClickListener(v -> hideRecyclerViewHeader());
        floatingActionButton.setOnClickListener(v -> startNewComment(null, topicDetailed.getURL(Topic.UrlType.TOPIC_NEW_COMMENT), ActivityNewComment.Action.SEND_NEW));

        recyclerViewHeader.setHasFixedSize(true);
        recyclerViewHeader.setAdapter(adapterCommentsRead);
        recyclerViewHeader.addItemDecoration(decoration);

        initLoading();
    }

    private void assignVariables() {
        progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.loading)
                .cancelable(false)
                .progress(true, 0)
                .build();
        isEnabledFAB = PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_FLOATING_BUTTON, true);
        isEnabledAutoScroll = PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT, true);
        decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        adapterCommentsRead = new AdapterCommentsRead(topicDetailed, this);
    }

    private void handleArguments() {
        Uri appLinkData = getIntent().getData();
        if (appLinkData != null) {
            handleAppLinkData(appLinkData);
        } else {
            Bundle arguments = getIntent().getExtras();
            if (arguments == null) {
                throw new IllegalArgumentException("Arguments cannot be null!");
            }
            handleIntentData(arguments);
        }
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        // If it is a message, set the partner name, otherwise set an empty string (later we update)
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("");
        textViewTitle.setText(topicDetailed.getTitle());
        textViewTitle.setMaxLines(1);
        textViewTitle.setEllipsize(TextUtils.TruncateAt.END);
        textViewTitle.setOnClickListener(v -> toggleTitle());
    }

    private void assignLayoutElements() {
        coordinatorLayout = findViewById(R.id.coordinator_layout_activity_comments_main);
        recyclerViewHeader = findViewById(R.id.recycler_view_activity_comments_header);
        floatingActionButton = findViewById(R.id.img_btn_activity_comments_fab);
        scrim = findViewById(R.id.view_activity_comments_scrim);
        appBarLayout = findViewById(R.id.app_bar_layout_activity_comments);
        textViewTitle = findViewById(R.id.text_view_activity_comments_title);
        viewPager = findViewById(R.id.view_pager_activity_comments);
        textViewCurrentPosition = findViewById(R.id.text_view_activity_comments_current_position);
        imageViewNext = findViewById(R.id.image_view_activity_comments_next_page);
        imageViewPrev = findViewById(R.id.image_view_activity_comments_prev_page);
        imageViewFirst = findViewById(R.id.image_view_activity_comments_first_page);
        imageViewLast = findViewById(R.id.image_view_activity_comments_last_page);
        linearLayoutHeader = findViewById(R.id.linear_layout_activity_comments_header);
        toolbar = findViewById(R.id.toolbar_activity_comments);
    }

    private void handleIntentData(Bundle arguments) {
        Topic topic;
        if (arguments.containsKey(ActivityComments.TOPIC)) {
            topic = (Topic) arguments.getSerializable(ActivityComments.TOPIC);
        } else if (arguments.containsKey(ActivityComments.URL)) {
            topic = new Topic();
            topic.addURL(Topic.UrlType.TOPIC, arguments.getString(ActivityComments.URL));
        } else {
            throw new IllegalArgumentException("No topic or topic url found!");
        }

        topicDetailed.setTopic(topic);
        type = HtmlUtils.isTopicMessageLink(topicDetailed.getURL(Topic.UrlType.TOPIC)) ? Type.TOPIC_MESSAGE : Type.TOPIC_DEFAULT;
    }

    private void handleAppLinkData(Uri appLinkData) {
        String url = HtmlUtils.changeDomainToPH(HtmlUtils.changeMobileLinkToDesktop(appLinkData.toString()));
        Topic topic = new Topic();
        topic.addURL(Topic.UrlType.TOPIC, url);

        topicDetailed.setTopic(topic);
        type = HtmlUtils.isTopicMessageLink(url) ? Type.TOPIC_MESSAGE : Type.TOPIC_DEFAULT;
    }

    private void initLoading() {
        loading = true;
        firstPage = -1;
        invalidateOptionsMenu();
        floatingActionButton.hide(false);
        imageViewPrev.setEnabled(false);
        imageViewNext.setEnabled(false);
        imageViewFirst.setEnabled(false);
        imageViewLast.setEnabled(false);
        textViewCurrentPosition.setText(R.string.loading);
        textViewCurrentPosition.setOnClickListener(null);
        switch (type) {
            case TOPIC_MESSAGE:
                PH.getPHService().getMessageDetails(topicDetailed, listener);
                break;
            case TOPIC_DEFAULT:
                PH.getPHService().getTopicDetails(topicDetailed, listener);
                break;
        }
        getTopicOverallIfNeeded(false);
    }

    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    public int getFirstPage() {
        return firstPage;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Refresh
        if (loading) {
            menu.add(0, R.id.menu_topic_options_refresh, 0, R.string.refresh).setEnabled(false);
            return super.onCreateOptionsMenu(menu);
        }
        menu.add(0, R.id.menu_topic_options_refresh, 0, R.string.refresh);
        if (topicDetailed.getData() == PH.Data.OK) {
            // New
            if (topicDetailed.getStatus() != Topic.Status.CLOSED) {
                menu.add(0, R.id.menu_topic_options_new, 0, type == Type.TOPIC_MESSAGE ? R.string.new_message : R.string.new_comment);
            }
            if (type == Type.TOPIC_DEFAULT) {
                // Favourites
                MenuItem menuItemFavourites = menu.add(0, R.id.menu_topic_options_favourites, 0, R.string.to_favourites);
                menuItemFavourites.setCheckable(true);
                menuItemFavourites.setChecked(topicDetailed.containsURL(Topic.UrlType.FAVOURITE_DELETE));
                // Notification
                MenuItem menuItemNotification = menu.add(0, R.id.menu_topic_options_notification, 0, R.string.notification);
                menuItemNotification.setCheckable(true);
                menuItemNotification.setEnabled(menuItemFavourites.isChecked());
                Set<String> savedTopics = PHPreferences.getInstance().getNotificationPreferences().getStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, new HashSet<>());
                menuItemNotification.setChecked(savedTopics.contains(topicDetailed.getTitle()));
                // Topic Overall
                menu.add(0, R.id.menu_topic_options_topic_overall, 0, R.string.topic_overall);
                // Topic EXTRA_URL
                menu.add(0, R.id.menu_topic_options_topic_url, 0, R.string.copy_topic_url);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return !isRecyclerViewHeaderVisible();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isRecyclerViewHeaderVisible()) {
                    hideRecyclerViewHeader();
                    return true;
                }
                finish();
                return true;
            case R.id.menu_topic_options_refresh:
                if (topicDetailed.getData() == PH.Data.OK) {
                    topicDetailed.setNewComments(0);
                }
                initLoading();
                invalidateOptionsMenu();
                return true;
            case R.id.menu_topic_options_favourites:
                toggleFavourites();
                return false;
            case R.id.menu_topic_options_notification:
                toggleNotification();
                return false;
            case R.id.menu_topic_options_topic_overall:
                getTopicOverallIfNeeded(true);
                return true;
            case R.id.menu_topic_options_new:
                startNewComment(null, topicDetailed.getURL(Topic.UrlType.TOPIC_NEW_COMMENT), ActivityNewComment.Action.SEND_NEW);
                return true;
            case R.id.menu_topic_options_topic_url:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText(topicDetailed.getURL(Topic.UrlType.TOPIC_NEW_COMMENT), topicDetailed.getURL(Topic.UrlType.TOPIC));
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Link a vágólapra másolva", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Nem sikerült a linket a vágólapra másolni", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isRecyclerViewHeaderVisible()) {
            hideRecyclerViewHeader();
            return;
        }
        finish();
    }

    @Override
    public void finish() {
        if (topicDetailed.getData() == PH.Data.OK) {
            Intent intent = new Intent();
            intent.putExtra(ActivityComments.TOPIC, topicDetailed);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.finish();
    }

    @Override
    public void reload() {
        FragmentComments fragmentComments = tabs.get(viewPager.getCurrentItem());
        fragmentComments.loadComments();
    }

    @Override
    public void onRefresh() {
        int savedIndex = PHPreferences.getInstance().getDefaultPreferences().getInt(PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS, -1);
        if (savedIndex == -1) {
            new MaterialDialog.Builder(this)
                    .items("Egész " + (type == Type.TOPIC_MESSAGE ? "üzenetet" : "topikot"), "Jelenlegi oldalt")
                    .title("Frissítés")
                    .positiveText(R.string.ok)
                    .neutralText("Megjegyzés")
                    .itemsCallbackSingleChoice(1, (dialog, itemView, which, text) -> true)
                    .onAny((dialog, which) -> {
                        int selectedIndex = dialog.getSelectedIndex();
                        switch (which) {
                            case NEUTRAL:
                                PHPreferences.getInstance().getDefaultPreferences().edit().putInt(PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS, selectedIndex).apply();
                                break;
                        }
                        if (selectedIndex == 0) {
                            initLoading();
                        } else {
                            reload();
                        }
                    })
                    .cancelListener(dialogInterface -> tabs.get(viewPager.getCurrentItem()).getSwipeRefreshLayout().setRefreshing(false))
                    .show();
        } else {
            if (topicDetailed.getData() == PH.Data.OK) {
                topicDetailed.setNewComments(0);
            }
            if (savedIndex == 0) {
                initLoading();
            } else {
                reload();
            }
        }
    }

    @Override
    public void onPageSelected(int page) {
        viewPager.setCurrentItem(page);
        hideRecyclerViewHeader();
    }

    @Override
    public void showUserProfile(String userLink, View view) {
        Intent intent = new Intent(this, ActivityUser.class);
        intent.putExtra(ActivityNewComment.EXTRA_URL, userLink);
        startActivity(intent);
    }

    @Override
    public void startNewComment(@Nullable TopicComment comment, @Nullable String URL, ActivityNewComment.Action action) {

        Intent intent = new Intent(this, ActivityNewComment.class);

        if (action == ActivityNewComment.Action.SEND_NEW) {
            intent.putExtra(ActivityNewComment.EXTRA_INPUT_TEXT, stringNewComment);
        } else if (action == ActivityNewComment.Action.SEND_REPLY) {
            intent.putExtra(ActivityNewComment.EXTRA_INPUT_TEXT, stringReplyComment);
        } else if (action == ActivityNewComment.Action.EDIT) {
            intent.putExtra(ActivityNewComment.EXTRA_INPUT_TEXT, TopicComment.Utils.getContentToEdit(comment.getContent()));
        } else if (action == ActivityNewComment.Action.SEND_PRIVATE) {
            intent.putExtra(ActivityNewComment.EXTRA_INPUT_TEXT, stringPrivateComment);
        }

        intent.putExtra(ActivityNewComment.EXTRA_TYPE, type);
        intent.putExtra(ActivityNewComment.EXTRA_COMMENT, comment);
        intent.putExtra(ActivityNewComment.EXTRA_URL, URL);
        intent.putExtra(ActivityNewComment.EXTRA_ACTION, action);

        startActivityForResult(intent, REQUEST_CODE_NEW_COMMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_COMMENT:
                if (data == null) {
                    return;
                }
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    ActivityNewComment.Action action = (ActivityNewComment.Action) bundle.getSerializable(ActivityNewComment.EXTRA_ACTION);
                    if (action == ActivityNewComment.Action.SEND_NEW) {
                        // Send new comment
                        stringNewComment = bundle.getString(ActivityNewComment.EXTRA_INPUT_TEXT);
                    } else if (action == ActivityNewComment.Action.SEND_REPLY) {
                        // Send reply to a comment
                        stringReplyComment = bundle.getString(ActivityNewComment.EXTRA_INPUT_TEXT);
                    } else if (action == ActivityNewComment.Action.SEND_PRIVATE) {
                        // Send private message to the user
                        stringPrivateComment = bundle.getString(ActivityNewComment.EXTRA_INPUT_TEXT);
                    }
                    if (resultCode == RESULT_OK) {
                        TopicComment comment = (TopicComment) bundle.get(ActivityNewComment.EXTRA_COMMENT);
                        switch (action) {
                            case EDIT:
                                editedComment(comment);
                                break;
                            case SEND_NEW:
                                sentNewComment(comment);
                                break;
                            case SEND_PRIVATE:
                                break;
                            case SEND_REPLY:
                                sentReply(comment);
                                break;
                        }
                    }
                }
                break;
        }
    }

    public void sentReply(TopicComment commentNew) {
        // Get the last page
        if (tabs != null) {
            FragmentComments fragmentComments = tabs.get(tabSize - 1);
            if (!fragmentComments.isNeedToSetup()) {
                // If the PH concatenate the send comment with the last one, the modify ours
                boolean equals = false;
                for (int i = 0; i < fragmentComments.getAdapterComments().getComments().size(); i++) {
                    if (commentNew.getDate().compareTo(fragmentComments.getAdapterComments().getComments().get(i).getDate()) == 0) {
                        equals = true;
                        fragmentComments.getAdapterComments().modifyComment(commentNew, i);
                        break;
                    }
                }
                // Otherwise add the new comment
                if (!equals) {
                    fragmentComments.getAdapterComments().addComment(commentNew,
                            fragmentComments.getAdapterComments().isListInc() ? fragmentComments.getAdapterComments().getComments().size() : 0);
                }
            }
        }
    }

    public void sentNewComment(TopicComment commentNew) {
        // Get the last page
        if (tabs != null) {
            FragmentComments fragmentComments = tabs.get(tabSize - 1);
            if (fragmentComments != null && !fragmentComments.isNeedToSetup()) {
                // If the PH concatenate the send comment with the last one, the modify ours
                boolean equals = false;
                for (int i = 0; i < fragmentComments.getAdapterComments().getComments().size(); i++) {
                    if (commentNew.getDate().compareTo(fragmentComments.getAdapterComments().getComments().get(i).getDate()) == 0) {
                        equals = true;
                        fragmentComments.getAdapterComments().modifyComment(commentNew, i);
                        break;
                    }
                }
                // Otherwise add the new comment
                if (!equals) {
                    fragmentComments.getAdapterComments().addComment(commentNew,
                            fragmentComments.getAdapterComments().isListInc() ? fragmentComments.getAdapterComments().getComments().size() : 0);
                }
            }
        }
    }

    public void editedComment(TopicComment editedComment) {
        // Handle the topic overall editing
        if (editedComment.getAuthorName().equals("Téma összefoglaló")) {
            adapterCommentsRead.addComment(editedComment);
            return;
        }
        // Current page
        FragmentComments fragmentComments = tabs.get(viewPager.getCurrentItem());
        if (!fragmentComments.isNeedToSetup()) {
            // If the PH concatenate the send comment with the last one, the modify ours
            boolean equals = false;
            for (int i = 0; i < fragmentComments.getAdapterComments().getComments().size(); i++) {
                if (editedComment.getDate().compareTo(fragmentComments.getAdapterComments().getComments().get(i).getDate()) == 0) {
                    equals = true;
                    fragmentComments.getAdapterComments().modifyComment(editedComment, i);
                    break;
                }
            }
            // Otherwise add the new comment
            if (!equals) {
                fragmentComments.getAdapterComments().addComment(editedComment,
                        fragmentComments.getAdapterComments().isListInc() ? fragmentComments.getAdapterComments().getComments().size() : 0);
            }
        }
    }

    private void toggleFavourites() {
        if (topicDetailed.containsURL(Topic.UrlType.FAVOURITE_DELETE)) {
            new MaterialDialog.Builder(this)
                    .title("Törlés a kedvencekből")
                    .content("Biztosan törölni szeretnéd a " + topicDetailed.getTitle() + " topikot a kedvencekből?")
                    .positiveText(R.string.yes)
                    .autoDismiss(false)
                    .negativeText(R.string.cancel)
                    .onAny((dialog, which) -> {
                        switch (which) {
                            case POSITIVE:
                                MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                                        .title(R.string.loading)
                                        .content("Törlés a kedvencekből...")
                                        .progress(true, 0)
                                        .cancelable(false)
                                        .build();
                                progressDialog.show();
                                PH.getPHService().deleteFromFavourites(topicDetailed.getURL(Topic.UrlType.FAVOURITE_DELETE), (error, link) -> {
                                    progressDialog.cancel();
                                    if (error.isEmpty()) {
                                        dialog.cancel();
                                        topicDetailed.addURL(Topic.UrlType.FAVOURITE_ADD, link);
                                        topicDetailed.removeURL(Topic.UrlType.FAVOURITE_DELETE);

                                        Toast.makeText(this, "Törölve a kedvencekből", Toast.LENGTH_SHORT).show();
                                        // Delete from the notifications too
                                        Set<String> savedTopics = PHPreferences.getInstance().getNotificationPreferences().getStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, new HashSet<>());
                                        savedTopics.remove(topicDetailed.getTitle());
                                        PHPreferences.getInstance().getNotificationPreferences().edit().putStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, savedTopics).apply();
                                        PH.getPHService().updateNotificationAlarm();
                                        invalidateOptionsMenu();
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
        } else {
            new MaterialDialog.Builder(this)
                    .title("Hozzáadás a kedvencekhez")
                    .content("Biztosan hozzá szeretnéd adni a " + topicDetailed.getTitle() + " topikot a kedvencekhez?")
                    .positiveText(R.string.yes)
                    .autoDismiss(false)
                    .negativeText(R.string.cancel)
                    .onAny((dialog, which) -> {
                        switch (which) {
                            case POSITIVE:
                                MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                                        .title(R.string.loading)
                                        .content("Hozzáadás a kedvencekhez...")
                                        .progress(true, 0)
                                        .cancelable(false)
                                        .build();
                                progressDialog.show();
                                PH.getPHService().addToFavourites(topicDetailed.getURL(Topic.UrlType.FAVOURITE_ADD), (error, link) -> {
                                    progressDialog.cancel();
                                    if (error.isEmpty()) {
                                        dialog.cancel();
                                        topicDetailed.addURL(Topic.UrlType.FAVOURITE_DELETE, link);
                                        topicDetailed.removeURL(Topic.UrlType.FAVOURITE_ADD);
                                        invalidateOptionsMenu();
                                        Toast.makeText(this, "Hozzáadva a kedvencekhez", Toast.LENGTH_SHORT).show();
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
    }

    private void toggleTitle() {
        Layout layout = textViewTitle.getLayout();
        int lines = layout.getLineCount();
        if (lines > 0 && layout.getEllipsisCount(lines - 1) > 0) {
            TransitionManager.beginDelayedTransition(coordinatorLayout);
            textViewTitle.setMaxLines(Integer.MAX_VALUE);
            textViewTitle.setEllipsize(null);
        } else {
            TransitionManager.beginDelayedTransition(coordinatorLayout);
            textViewTitle.setMaxLines(1);
            textViewTitle.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    private void toggleNotification() {
        Set<String> savedTopics = PHPreferences.getInstance().getNotificationPreferences().getStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, new HashSet<>());
        if (savedTopics.contains(topicDetailed.getTitle())) {
            new MaterialDialog.Builder(this)
                    .title("Értesítés kikapcsolása")
                    .content("Biztosan le szeretnél iratkozni a " + topicDetailed.getTitle() + " topik értesítéseiről?")
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onAny((dialog, which) -> {
                        switch (which) {
                            case POSITIVE:
                                savedTopics.remove(topicDetailed.getTitle());
                                PHPreferences.getInstance().getNotificationPreferences().edit().putStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, savedTopics).apply();
                                PH.getPHService().updateNotificationAlarm();
                                dialog.dismiss();
                                invalidateOptionsMenu();
                                break;
                        }
                    }).show();

        } else {
            new MaterialDialog.Builder(this)
                    .title("Értesítés bekapcsolása")
                    .content("Szeretnél értesítést kapni a " + topicDetailed.getTitle() + " topikról, ha új hozzászólás érkezik?")
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onAny((dialog, which) -> {
                        switch (which) {
                            case POSITIVE:
                                savedTopics.add(topicDetailed.getTitle());
                                PHPreferences.getInstance().getNotificationPreferences().edit().putStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, savedTopics).apply();
                                PH.getPHService().updateNotificationAlarm();
                                dialog.dismiss();
                                invalidateOptionsMenu();
                                break;
                        }
                    }).show();

        }
    }

    private void getTopicOverallIfNeeded(boolean force) {
        String url = topicDetailed.getURL(Topic.UrlType.TOPIC);
        if (force || HtmlUtils.isTopicOverallLink(url)) {
            progressDialog.show();
            PH.getPHService().getTopicOverall(HtmlUtils.getTopicOverallURL(url), (comment, error) -> {
                progressDialog.cancel();
                if (error.isEmpty()) {
                    showRecyclerViewHeader(adapterCommentsRead);
                    adapterCommentsRead.addComment(comment);
                } else {
                    Toast.makeText(PHApplication.getInstance(), error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Transition getTransition() {
        Transition transition = transitions.get(R.transition.auto);
        if (transition == null) {
            transition = TransitionInflater.from(this).inflateTransition(R.transition.auto);
            transitions.put(R.transition.auto, transition);
        }
        return transition;
    }

    private boolean isRecyclerViewHeaderVisible() {
        return recyclerViewHeader.getVisibility() == View.VISIBLE;
    }

    private void hideRecyclerViewHeader() {
        TransitionManager.beginDelayedTransition(coordinatorLayout, getTransition());
        adapterCommentsRead.clearComments();
        recyclerViewHeader.setVisibility(View.GONE);
        linearLayoutHeader.setVisibility(View.VISIBLE);
        scrim.setVisibility(View.GONE);
        if (isEnabledFAB) {
            floatingActionButton.show(false);
        }
        invalidateOptionsMenu();
    }

    private void showRecyclerViewHeader(RecyclerView.Adapter adapter) {
        if (recyclerViewHeader.getAdapter() != adapter) {
            recyclerViewHeader.setAdapter(adapter);
        }
        TransitionManager.beginDelayedTransition(coordinatorLayout, getTransition());
        floatingActionButton.hide(false);
        appBarLayout.setExpanded(true, false);
        recyclerViewHeader.setVisibility(View.VISIBLE);
        linearLayoutHeader.setVisibility(View.GONE);
        scrim.setVisibility(View.VISIBLE);

        invalidateOptionsMenu();
    }

    public synchronized TopicDetailed getTopicDetailed() {
        return topicDetailed;
    }

    public boolean isEnabledFAB() {
        return isEnabledFAB;
    }

    public int getTabSize() {
        return tabSize;
    }

    public Type getType() {
        return type;
    }

    public FloatingActionButton getFloatingActionButton() {
        return floatingActionButton;
    }

    public void getAnswersList(TopicComment comment) {
        progressDialog.show();
        List<TopicComment> comments = new ArrayList<>();
        comments.add(comment);
        PH.getPHService().getCommentAnswerList(comments, (allComments, data) -> {
            progressDialog.cancel();
            if (data == PH.Data.OK) {
                showRecyclerViewHeader(adapterCommentsRead);
                adapterCommentsRead.addComments(allComments);
            } else {
                Toast.makeText(PHApplication.getInstance(), PH.getErrorFromCode(data), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public RecyclerView.OnScrollListener getRecyclerViewScrollListener() {
        return recyclerViewScrollListener;
    }

    @NonNull
    private ViewPager.SimpleOnPageChangeListener createPageChangeListener() {
        return new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                imageViewFirst.setEnabled(position != 0);
                imageViewPrev.setEnabled(position != 0);
                imageViewNext.setEnabled(position + 1 < tabSize);
                imageViewLast.setEnabled(position + 1 < tabSize);
                textViewCurrentPosition.setText(new StringBuilder((position + 1) + " / " + tabSize));
            }
        };
    }

    @NonNull
    private RecyclerView.OnScrollListener createScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isEnabledFAB) {
                        floatingActionButton.show(true);
                    }
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && floatingActionButton.isShown()) {
                    tabs.get(viewPager.getCurrentItem()).getSwipeRefreshLayout().setEnabled(false);
                    if (isEnabledFAB) {
                        floatingActionButton.hide(true);
                    }
                } else {
                    tabs.get(viewPager.getCurrentItem()).getSwipeRefreshLayout().setEnabled(true);
                }
            }
        };
    }

    public enum Type {
        TOPIC_MESSAGE,
        TOPIC_DEFAULT
    }
}
