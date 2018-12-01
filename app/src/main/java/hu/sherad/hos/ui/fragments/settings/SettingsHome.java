package hu.sherad.hos.ui.fragments.settings;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.ui.activities.ActivitySettings;
import hu.sherad.hos.utils.ColorUtils;
import hu.sherad.hos.utils.io.Logger;

public class SettingsHome extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private Preference preferenceAppearance;
    private Preference preferenceNotification;
    private Preference preferenceOther;
    private Preference preferenceUserCredentials;

    private int tintColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PH.Prefs.PREF_DEFAULT);

        tintColor = ContextCompat.getColor(getActivity(), ColorUtils.isLightTheme() ? R.color.primary_text_light : R.color.primary_text_dark);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.preferences_home);

        preferenceAppearance = findPreference(PH.Prefs.KEY_HOME_APPEARANCE);
        preferenceNotification = findPreference(PH.Prefs.KEY_HOME_NOTIFICATION);
        preferenceOther = findPreference(PH.Prefs.KEY_HOME_OTHER);
        preferenceUserCredentials = findPreference(PH.Prefs.KEY_HOME_USER_CREDENTIALS);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preferenceAppearance.setOnPreferenceClickListener(this);
        preferenceNotification.setOnPreferenceClickListener(this);
        preferenceOther.setOnPreferenceClickListener(this);
        preferenceUserCredentials.setOnPreferenceClickListener(this);

        setIconToSections();
    }

    private void setIconToSections() {
        try {

            Drawable drawableColorFill = ContextCompat.getDrawable(getActivity(), R.drawable.vd_format_color_fill);
            Drawable drawableAlarm = ContextCompat.getDrawable(getActivity(), R.drawable.vd_alarm_light);
            Drawable drawableAdb = ContextCompat.getDrawable(getActivity(), R.drawable.vd_android_debug_bridge);
            Drawable drawableVerified = ContextCompat.getDrawable(getActivity(), R.drawable.vd_verified);
            if (drawableColorFill != null) {
                drawableColorFill.setTint(tintColor);
            }
            if (drawableAlarm != null) {
                drawableAlarm.setTint(tintColor);
            }
            if (drawableAdb != null) {
                drawableAdb.setTint(tintColor);
            }
            if (drawableVerified != null) {
                drawableVerified.setTint(tintColor);
            }

            preferenceAppearance.setIcon(drawableColorFill);
            preferenceNotification.setIcon(drawableAlarm);
            preferenceOther.setIcon(drawableAdb);
            preferenceUserCredentials.setIcon(drawableVerified);

        } catch (Exception e) {
            Logger.getLogger().e(e);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        ActivitySettings activitySettings = (ActivitySettings) getActivity();
        switch (preference.getKey()) {
            case PH.Prefs.KEY_HOME_APPEARANCE:
                activitySettings.replaceFragment(new SettingsAppearance());
                return true;
            case PH.Prefs.KEY_HOME_NOTIFICATION:
                activitySettings.replaceFragment(new SettingsNotification());
                return true;
            case PH.Prefs.KEY_HOME_OTHER:
                activitySettings.replaceFragment(new SettingsOther());
                return true;
            case PH.Prefs.KEY_HOME_USER_CREDENTIALS:
                activitySettings.replaceFragment(new SettingsUserCredentials());
                return true;
        }
        return false;
    }

}