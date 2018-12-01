package hu.sherad.hos.utils.io;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.ui.fragments.settings.SettingsAppearance;

public class SettingsUtil {

    private static final CharSequence[] themesText = new CharSequence[]{
            PHApplication.getInstance().getString(R.string.light_theme),
            PHApplication.getInstance().getString(R.string.dark_theme),
            PHApplication.getInstance().getString(R.string.dark_amoled_theme)
    };
    private static final CharSequence[] startFragment = new CharSequence[]{
            PHApplication.getInstance().getString(R.string.discover),
            PHApplication.getInstance().getString(R.string.news),
            PHApplication.getInstance().getString(R.string.messages),
            PHApplication.getInstance().getString(R.string.favourites),
            PHApplication.getInstance().getString(R.string.commented_list),
            PHApplication.getInstance().getString(R.string.hot_topics)
    };
    private static final SharedPreferences preferencesAppearance = PHPreferences.getInstance().getAppearancePreferences();
    private static final SharedPreferences preferencesOther = PHPreferences.getInstance().getOtherPreferences();

    public static void selectStartFragment(@NonNull Context context, @Nullable SelectChoice selectStartFragment) {
        MaterialDialog.Builder materialDialog = new MaterialDialog.Builder(context)
                .title(R.string.start_page)
                .items(startFragment)
                .itemsCallbackSingleChoice(getSavedStartFragment(), (dialog, itemView, which, text) -> true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onAny((dialog, which) -> {
                    switch (which) {
                        case POSITIVE:
                            int selectedStartFragment = dialog.getSelectedIndex();
                            preferencesAppearance.edit().putInt(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT, selectedStartFragment).apply();
                            if (selectStartFragment != null) {
                                selectStartFragment.onSelected(selectedStartFragment);
                            }
                            break;
                    }
                });
        materialDialog.show();
    }

    public static void selectTheme(@NonNull Context context, @Nullable SelectChoice selectThemeCallback) {
        MaterialDialog.Builder materialDialog = new MaterialDialog.Builder(context)
                .title(R.string.theme)
                .items(themesText)
                .itemsCallbackSingleChoice(getSavedTheme(), (dialog, itemView, which, text) -> true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onAny((dialog, which) -> {
                    switch (which) {
                        case POSITIVE:
                            int selectedTheme = dialog.getSelectedIndex();
                            preferencesAppearance.edit().putInt(PH.Prefs.KEY_APPEARANCE_THEME, selectedTheme).apply();
                            if (selectThemeCallback != null) {
                                selectThemeCallback.onSelected(selectedTheme);
                            }
                            break;
                    }
                });
        materialDialog.show();
    }

    public static CharSequence getThemeByIndex(int i) {
        return themesText[i];
    }

    public static CharSequence getStartFragmentByIndex(int i) {
        return startFragment[i];
    }

    public static int getSavedTheme() {
        return preferencesAppearance.getInt(PH.Prefs.KEY_APPEARANCE_THEME, SettingsAppearance.PrefValueTheme.PH_THEME_LIGHT);
    }

    public static int getSavedStartFragment() {
        return preferencesAppearance.getInt(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT, SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_EXPLORE);
    }


    public static boolean isAnalyticsEnabled() {
        return preferencesOther.getBoolean(PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS, true);
    }

    public static void setAnalyticsEnabled(boolean enabled) {
        preferencesOther.edit().putBoolean(PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS, enabled).apply();
    }

    public static boolean isAutoScrollEnabled() {
        return preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT, true);
    }

    public static void setAutoScrollEnabled(boolean enabled) {
        preferencesAppearance.edit().putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT, enabled).apply();
    }

    public interface SelectChoice {
        void onSelected(int i);
    }
}
