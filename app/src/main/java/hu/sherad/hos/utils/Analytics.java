package hu.sherad.hos.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.BuildConfig;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.SearchEvent;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;

public class Analytics {

    public static void sendContentView(@NonNull String name, @Nullable String type) {
        if (!BuildConfig.DEBUG && PHPreferences.getInstance().getOtherPreferences().getBoolean(PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS, true)) {

            ContentViewEvent contentViewEvent = new ContentViewEvent();
            if (type != null) {
                contentViewEvent.putContentType(type);
            }
            contentViewEvent.putContentName(name);
            Answers.getInstance().logContentView(contentViewEvent);
        }
    }

    public static void sendLogin(@NonNull String method, boolean success) {
        if (!BuildConfig.DEBUG && PHPreferences.getInstance().getOtherPreferences().getBoolean(PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS, true)) {
            Answers.getInstance().logLogin(new LoginEvent().putMethod(method).putSuccess(success));
        }
    }

    public static void sendSearchQuery(@NonNull String query) {
        if (!BuildConfig.DEBUG && PHPreferences.getInstance().getOtherPreferences().getBoolean(PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS, true)) {
            Answers.getInstance().logSearch(new SearchEvent().putQuery(query));
        }
    }
}
