package hu.sherad.hos.ui.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import hu.sherad.hos.BuildConfig;
import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.ui.fragments.settings.SettingsAppearance;
import io.fabric.sdk.android.Fabric;

@SuppressLint("Registered")
public class ActivityBase extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());

        SharedPreferences sharedPreferencesAppearance = PHPreferences.getInstance().getAppearancePreferences();
        switch (sharedPreferencesAppearance.getInt(PH.Prefs.KEY_APPEARANCE_THEME, SettingsAppearance.PrefValueTheme.PH_THEME_LIGHT)) {
            case SettingsAppearance.PrefValueTheme.PH_THEME_LIGHT:
                setTheme(R.style.PHLight);
                break;
            case SettingsAppearance.PrefValueTheme.PH_THEME_DARK:
                setTheme(R.style.PHDark);
                break;
            case SettingsAppearance.PrefValueTheme.PH_THEME_BLACK:
                setTheme(R.style.PHDark_Black);
                break;
        }
        sharedPreferencesAppearance.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PH.Prefs.KEY_APPEARANCE_THEME)) {
            recreate();
        }

    }

    public void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));
    }
}
