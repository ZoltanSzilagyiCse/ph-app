package hu.sherad.hos.data.api.ph;

import android.content.Context;
import android.content.SharedPreferences;

public class PHPreferences {

    private static PHPreferences phPreferences;

    public static PHPreferences getInstance() {
        if (phPreferences == null) {
            phPreferences = new PHPreferences();
        }
        return phPreferences;
    }

    private SharedPreferences getSharedPreferences(String preference) {
        return PHApplication.getInstance().getSharedPreferences(preference, Context.MODE_PRIVATE);
    }

    public SharedPreferences getAppearancePreferences() {
        return getSharedPreferences(PH.Prefs.PREF_APPEARANCE);
    }

    public SharedPreferences getDefaultPreferences() {
        return getSharedPreferences(PH.Prefs.PREF_DEFAULT);
    }

    public SharedPreferences getNotificationPreferences() {
        return getSharedPreferences(PH.Prefs.PREF_NOTIFICATION);
    }

    public SharedPreferences getUserCredentialsPreferences() {
        return getSharedPreferences(PH.Prefs.PREF_USER_CREDENTIALS);
    }

    public SharedPreferences getCachePreferences() {
        return getSharedPreferences(PH.Prefs.PREF_CACHE);
    }

    public SharedPreferences getCookiePreferences() {
        return getSharedPreferences(PH.Prefs.PREF_COOKIES);
    }

    public SharedPreferences getOtherPreferences() {
        return getSharedPreferences(PH.Prefs.PREF_OTHER);
    }

    public void clearAllPreferences() {
        getAppearancePreferences().edit().clear().apply();
        getDefaultPreferences().edit().clear().apply();
        getNotificationPreferences().edit().clear().apply();
        getUserCredentialsPreferences().edit().clear().apply();
        getOtherPreferences().edit().clear().apply();
        getCachePreferences().edit().clear().apply();
    }
}

