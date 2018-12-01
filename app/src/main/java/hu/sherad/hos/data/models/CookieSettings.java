package hu.sherad.hos.data.models;

import android.support.annotation.NonNull;

import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.utils.io.Logger;

public class CookieSettings {

    private static final URI uri = URI.create(PH.Api.HOST_PROHARDVER);

    public static void add(HttpCookie cookie) {
        boolean contains = false;
        List<HttpCookie> httpCookies = get();
        for (HttpCookie httpCookie : httpCookies) {
            if (httpCookie.getName().equals(cookie.getName())) {
                contains = true;
                if (!httpCookie.getValue().equals(cookie.getValue())) {
                    Logger.getLogger().i("Updating Cookie: " + httpCookie.toString() + " --> " + cookie.toString());
                    httpCookies.remove(httpCookie);
                    httpCookies.add(cookie);
                    break;
                }
            }
        }
        if (!contains) {
            Logger.getLogger().i("Adding Cookie: " + cookie.toString());
            httpCookies.add(cookie);
        }
        Set<String> set = new HashSet<>();
        for (HttpCookie httpCookie : httpCookies) {
            set.add(httpCookie.toString());
        }
        PHPreferences.getInstance().getCookiePreferences().edit().putStringSet(uri.toString(), set).apply();
    }

    public static List<HttpCookie> get() {
        List<HttpCookie> cookies = new ArrayList<>();
        Set<String> cookiesSet = PHPreferences.getInstance().getCookiePreferences().getStringSet(uri.toString(), null);
        if (cookiesSet != null) {
            for (String string : cookiesSet) {
                cookies.addAll(HttpCookie.parse(string));
            }
        }
        return cookies;
    }

    @NonNull
    public static String createCookies() {
        StringBuilder stringCookie = new StringBuilder();
        for (HttpCookie cookie : get()) {
            stringCookie.append(cookie.toString());
            stringCookie.append("; ");
        }
        return stringCookie.toString();
    }

    public static void removeAll() {
        PHPreferences.getInstance().getCookiePreferences().edit().clear().apply();
        Logger.getLogger().i("Removed all the cookies");
    }

}
