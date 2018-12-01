package hu.sherad.hos.ui.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import hu.sherad.hos.R;
import hu.sherad.hos.ui.fragments.settings.SettingsHome;

public class ActivitySettings extends ActivityBase {

    public static final String RESTART_APP = "RESTART_APP";

    private static final String CURRENT_FRAGMENT_SETTINGS = "CURRENT_FRAGMENT_SETTINGS";

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setStatusBarColor();

        assignLayoutElements();
        initActionBar();

        handleSavedInstance(savedInstanceState);
    }

    private void assignLayoutElements() {
        toolbar = findViewById(R.id.toolbar_activity_settings);
    }

    private void handleSavedInstance(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            replaceFragment(new SettingsHome());
        } else {
            replaceFragment(getFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT_SETTINGS));
        }
    }

    private void initActionBar() {
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(outState, CURRENT_FRAGMENT_SETTINGS, getCurrentFragment());
    }

    @Override
    public void recreate() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.recreate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFragment().getClass().getName().equals(SettingsHome.class.getName())) {
            super.onBackPressed();
        } else {
            replaceFragment(new SettingsHome());
        }
    }

    private PreferenceFragment getCurrentFragment() {
        return (PreferenceFragment) getFragmentManager().findFragmentById(R.id.frame_layout_activity_settings);
    }

    public void replaceFragment(Fragment fragment) {
        if (getCurrentFragment() == null || !getCurrentFragment().getClass().getName().equals(fragment.getClass().getName())) {
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.frame_layout_activity_settings, fragment)
                    .commit();
        }
    }

}
