package hu.sherad.hos.ui.activities;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.HashMap;

import hu.sherad.hos.BuildConfig;
import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.user.UserTopics;
import hu.sherad.hos.data.models.CookieSettings;
import hu.sherad.hos.data.models.StatusHttpRequest;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.ui.fragments.section.FragmentAbout;
import hu.sherad.hos.ui.fragments.section.FragmentBase;
import hu.sherad.hos.ui.fragments.section.FragmentCommented;
import hu.sherad.hos.ui.fragments.section.FragmentExplore;
import hu.sherad.hos.ui.fragments.section.FragmentFavourites;
import hu.sherad.hos.ui.fragments.section.FragmentHotTopics;
import hu.sherad.hos.ui.fragments.section.FragmentMessages;
import hu.sherad.hos.ui.fragments.section.FragmentNews;
import hu.sherad.hos.ui.fragments.settings.SettingsAppearance;
import hu.sherad.hos.utils.NotificationUtils;
import hu.sherad.hos.utils.io.Logger;
import io.fabric.sdk.android.Fabric;

public class ActivityHome extends ActivityBase implements FragmentManager.OnBackStackChangedListener, NavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_CODE_FOR_FAVOURITES = 3;
    public static final int REQUEST_CODE_FOR_COMMENTED = 4;
    public static final int REQUEST_CODE_FOR_MESSAGES = 5;
    public static final int REQUEST_CODE_FOR_SETTINGS = 6;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private MaterialDialog progressDialog;
    @Nullable
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getLogger().i("Update drawer...");
            updateDrawer();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar_activity_main);
        appBarLayout = findViewById(R.id.app_bar_activity_main);
        drawerLayout = findViewById(R.id.drawer_layout_activity_main);
        navigationView = findViewById(R.id.navigation_view_activity_main);

        progressDialog = new MaterialDialog.Builder(this)
                .content(R.string.loading)
                .progress(true, 0)
                .cancelable(false)
                .build();

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.vd_drawer_hamburger);
        }

        navigationView.setNavigationItemSelectedListener(this);
        NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        if (navigationMenuView != null) {
            navigationMenuView.setVerticalScrollBarEnabled(false);
        }
        findViewById(R.id.linear_layout_activity_main).setOnClickListener(v -> logout());
        updateDrawer();

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        // Then start default fragment
        startDefaultFragment();
        // Update Notification Service
        NotificationUtils.updateAlarm();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            // Fragment News
            if (currentFragment.getClass().getName().equals(FragmentNews.class.getName())) {
                getMenuInflater().inflate(R.menu.menu_sort, menu);
                return super.onCreateOptionsMenu(menu);
            }
            // Fragment Explore
            if (currentFragment.getClass().getName().equals(FragmentExplore.class.getName())) {
                if (((FragmentExplore) currentFragment).getSubTopic() == null) {
                    MenuItem menuItemSearch = menu.add(0, R.id.menu_explore_search, 0, R.string.search);
                    Drawable drawableSearch = ContextCompat.getDrawable(this, R.drawable.vd_search_24dp);
                    if (drawableSearch != null) {
                        drawableSearch.setTint(ContextCompat.getColor(this, R.color.icons));
                    }
                    menuItemSearch.setIcon(drawableSearch).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }
                return super.onCreateOptionsMenu(menu);
            }
            // Fragment Favourites
            if (currentFragment.getClass().getName().equals(FragmentFavourites.class.getName())) {
                MenuItem menuItemSort = menu.add(0, R.id.menu_favourites_sort, 0, "Rendezés");
                Drawable drawableSort = ContextCompat.getDrawable(this, R.drawable.vd_sort);
                if (drawableSort != null) {
                    drawableSort.setTint(ContextCompat.getColor(this, R.color.icons));
                }
                menuItemSort.setIcon(drawableSort).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return super.onCreateOptionsMenu(menu);
            }
            // Fragment Commented
            if (currentFragment.getClass().getName().equals(FragmentCommented.class.getName())) {
                MenuItem menuItemSort = menu.add(0, R.id.menu_commented_topics_sort, 0, "Rendezés");
                Drawable drawableSort = ContextCompat.getDrawable(this, R.drawable.vd_sort);
                if (drawableSort != null) {
                    drawableSort.setTint(ContextCompat.getColor(this, R.color.icons));
                }
                menuItemSort.setIcon(drawableSort).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return super.onCreateOptionsMenu(menu);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.openDrawer(Gravity.START);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_FOR_SETTINGS:
                updateDrawer();
                if (resultCode == RESULT_OK && data != null) {
                    Bundle arguments = data.getExtras();
                    if (arguments != null) {
                        if (arguments.containsKey(ActivitySettings.RESTART_APP) && arguments.getBoolean(ActivitySettings.RESTART_APP)) {
                            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                            if (alarmManager != null) {
                                Intent intent = new Intent(getApplicationContext(), ActivityInit.class);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);
                                finish();
                            } else {
                                Toast.makeText(this, "Sikertelen újraindítás", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
            return;
        }
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null && !((FragmentBase) currentFragment).onBackPressed()) {
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(PH.Intent.ACTION_UPDATED_USER_TOPICS));
        updateDrawer();
    }

    @Override
    protected void onPause() {
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Logger.getLogger().e(e);
        }
        super.onPause();
    }

    @Override
    public void onBackStackChanged() {
        Logger.getLogger().i("BackStackSize is " + getSupportFragmentManager().getBackStackEntryCount());
        Fragment fragment = getCurrentFragment();
        if (fragment != null) {
            appBarLayout.setExpanded(true);
            invalidateOptionsMenu();
            ((FragmentBase) fragment).onVisible(toolbar);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_drawer_navigation_explore:
                replaceFragment(new FragmentExplore(), false);
                drawerLayout.closeDrawer(Gravity.START);
                break;
            case R.id.menu_drawer_navigation_hot_topics:
                replaceFragment(new FragmentHotTopics(), false);
                drawerLayout.closeDrawer(Gravity.START);
                break;
            case R.id.menu_drawer_navigation_favourites:
                replaceFragment(new FragmentFavourites(), false);
                drawerLayout.closeDrawer(Gravity.START);
                break;
            case R.id.menu_drawer_navigation_messages:
                replaceFragment(new FragmentMessages(), false);
                drawerLayout.closeDrawer(Gravity.START);
                break;
            case R.id.menu_drawer_navigation_commented_list:
                replaceFragment(new FragmentCommented(), false);
                drawerLayout.closeDrawer(Gravity.START);
                break;
            case R.id.menu_drawer_navigation_news:
                replaceFragment(new FragmentNews(), false);
                drawerLayout.closeDrawer(Gravity.START);
                break;
            case R.id.menu_drawer_navigation_about:
                replaceFragment(new FragmentAbout(), false);
                drawerLayout.closeDrawer(Gravity.START);
                break;
            case R.id.menu_drawer_navigation_settings:
                Intent intent = new Intent(this, ActivitySettings.class);
                startActivityForResult(intent, REQUEST_CODE_FOR_SETTINGS);
                break;
        }
        return true;

    }

    public void startDefaultFragment() {
        switch (PHPreferences.getInstance().getAppearancePreferences().getInt(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT, SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_EXPLORE)) {
            case SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_EXPLORE:
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.menu_drawer_navigation_explore));
                break;
            case SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_NEWS:
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.menu_drawer_navigation_news));
                break;
            case SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_HOT_TOPICS:
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.menu_drawer_navigation_hot_topics));
                break;
            case SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_MESSAGES:
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.menu_drawer_navigation_messages));
                break;
            case SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_FAVOURITES:
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.menu_drawer_navigation_favourites));
                break;
            case SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_COMMENTED_LIST:
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.menu_drawer_navigation_commented_list));
                break;
        }
    }

    private void updateDrawer() {
        SharedPreferences sharedPreferencesUserCredentials = PHPreferences.getInstance().getUserCredentialsPreferences();

        String userAvatarURL = sharedPreferencesUserCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_AVATAR_URL, "");
        String userName = sharedPreferencesUserCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_NAME, "");
        String userEmail = sharedPreferencesUserCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_EMAIL, "");

        TextView textViewName = navigationView.getHeaderView(0).findViewById(R.id.text_view_util_drawer_header_name);
        TextView textViewEmail = navigationView.getHeaderView(0).findViewById(R.id.text_view_util_drawer_header_email);

        // Avatar
        if (userAvatarURL.isEmpty()) {
            Glide.with(this).load(R.mipmap.ic_launcher_round).into((ImageView) navigationView.getHeaderView(0).findViewById(R.id.img_util_drawer_header_avatar));
        } else {
            Glide.with(this).load(userAvatarURL).into((ImageView) navigationView.getHeaderView(0).findViewById(R.id.img_util_drawer_header_avatar));
        }
        // Name
        textViewName.setText(userName.isEmpty() ? getString(R.string.app_name) : userName);
        textViewName.setOnClickListener(view -> new MaterialDialog.Builder(this)
                .autoDismiss(false)
                .title("Név változtatás")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(0, 30)
                .input(getString(R.string.name), (userName.equals(getString(R.string.app_name)) ? "" : userName), (dialog, input) -> {
                    progressDialog.show();
                    HashMap<String, String> map = new HashMap<>();
                    map.put(PH.Prefs.KEY_USER_CREDENTIALS_NAME, input.toString());
                    PH.getPHService().setUserData(map, (status, data) -> {
                        progressDialog.cancel();
                        if (status == StatusHttpRequest.SUCCESS) {
                            textViewName.setText(input.toString());
                            dialog.hide();
                            Toast.makeText(PHApplication.getInstance(), "Sikeres név módosítás", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PHApplication.getInstance(), data, Toast.LENGTH_SHORT).show();
                        }
                    });
                }).show());
        // Email
        textViewEmail.setText(userEmail.isEmpty() ? getString(R.string.ph_email) : userEmail);
        // Badges
        TextView textViewMessages = (TextView) navigationView.getMenu().findItem(R.id.menu_drawer_navigation_messages).getActionView();
        textViewMessages.setGravity(Gravity.CENTER_VERTICAL);
        textViewMessages.setTextColor(getResources().getColor(R.color.material_red_500));
        int newMessages = Topic.Utils.getNewTopicsCount(UserTopics.getInstance().getTopics(Topic.Type.MESSAGE));
        textViewMessages.setText(newMessages > 0 ? String.valueOf(newMessages) : "");
        TextView textViewFavourites = (TextView) navigationView.getMenu().findItem(R.id.menu_drawer_navigation_favourites).getActionView();
        textViewFavourites.setGravity(Gravity.CENTER_VERTICAL);
        textViewFavourites.setTextColor(getResources().getColor(R.color.material_red_500));
        int newFavourites = Topic.Utils.getNewTopicsCount(UserTopics.getInstance().getTopics(Topic.Type.FAVOURITE));
        textViewFavourites.setText(newFavourites > 0 ? String.valueOf(newFavourites) : "");
        TextView textViewCommentedList = (TextView) navigationView.getMenu().findItem(R.id.menu_drawer_navigation_commented_list).getActionView();
        textViewCommentedList.setGravity(Gravity.CENTER_VERTICAL);
        textViewCommentedList.setTextColor(getResources().getColor(R.color.material_red_500));
        int newCommentedTopics = Topic.Utils.getNewTopicsCount(UserTopics.getInstance().getTopics(Topic.Type.COMMENTED));
        textViewCommentedList.setText(newCommentedTopics > 0 ? String.valueOf(newCommentedTopics) : "");
    }

    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    @Nullable
    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.coordinator_layout_activity_main_container);
    }

    public void logout() {
        new MaterialDialog.Builder(this)
                .content(R.string.are_you_sure_to_exit)
                .positiveText(R.string.yes)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    if (which == DialogAction.POSITIVE) {
                        progressDialog.show();
                        new Thread(() -> {
                            PHPreferences.getInstance().clearAllPreferences();
                            // Stop notification service
                            runOnUiThread(() -> {
                                PH.getPHService().updateNotificationAlarm();
                                // Clear the notifications
                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                if (notificationManager != null) {
                                    notificationManager.cancelAll();
                                }
                            });
                            // Wait a little bit to apply the changes
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                runOnUiThread(() -> Toast.makeText(this, "Sikertelen kijelentkezés", Toast.LENGTH_SHORT).show());
                            }
                            CookieSettings.removeAll();
                            // Wait a little bit to apply the changes
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                runOnUiThread(() -> Toast.makeText(this, "Sikertelen kijelentkezés", Toast.LENGTH_SHORT).show());
                            }
                            runOnUiThread(() -> {
                                progressDialog.cancel();
                                Intent intent = new Intent(this, ActivityInit.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(this, R.string.successfully_logout, Toast.LENGTH_SHORT).show();
                            });
                        }).start();

                    }
                }).show();
    }

    /**
     * @return True, if added or popped the fragment. If neither then this equals with {@link ActivityHome#getCurrentFragment()}.
     */
    public boolean replaceFragment(@NonNull Fragment fragment, boolean forceAdd) {
        boolean addedOrPopped;
        String fragmentTag = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();

        boolean fragmentPopped = fragmentManager.popBackStackImmediate(fragmentTag, 0);
        addedOrPopped = fragmentPopped;
        if (forceAdd || (!fragmentPopped && fragmentManager.findFragmentByTag(fragmentTag) == null)) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.addToBackStack(fragmentTag);
            transaction.add(R.id.coordinator_layout_activity_main_container, fragment, fragmentTag);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.commit();
            addedOrPopped = true;
        }
        return addedOrPopped;
    }

    public interface AddMethods {

        /**
         * Every fragment has to implement. If {@link ActivityHome#onBackPressed()} called it will call this.
         *
         * @return {@code false}, If you want {@link ActivityHome#onBackPressed()} fully executed.<br>
         * {@code true}, If you want {@link ActivityHome#onBackPressed()} only the needed parts executed (eg.: close the {@link ActivityHome})
         */
        boolean onBackPressed();

        void onVisible(Toolbar toolbar);
    }
}
