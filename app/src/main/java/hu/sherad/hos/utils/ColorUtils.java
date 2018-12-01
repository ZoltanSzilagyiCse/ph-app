package hu.sherad.hos.utils;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.ui.fragments.settings.SettingsAppearance;

/**
 * Utility methods for working with colors.
 */
public class ColorUtils {

    private ColorUtils() {
    }

    public static int getCurrentTheme() {
        return PHPreferences.getInstance().getAppearancePreferences().getInt(PH.Prefs.KEY_APPEARANCE_THEME, SettingsAppearance.PrefValueTheme.PH_THEME_LIGHT);
    }

    public static boolean isLightTheme() {
        return isLightTheme(getCurrentTheme());
    }

    public static boolean isLightTheme(int theme) {
        return theme == SettingsAppearance.PrefValueTheme.PH_THEME_LIGHT;
    }
}
