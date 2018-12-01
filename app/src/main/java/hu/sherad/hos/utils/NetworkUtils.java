package hu.sherad.hos.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.ph.PHResponse;
import hu.sherad.hos.utils.io.Logger;

/**
 * Utility methods for working with the network
 */
public class NetworkUtils {


    public static StringRequest createStringRequest(int method, String url, boolean addUserCookies, PHResponse<String> phStringResponse) {
        return createStringRequest(method, url, addUserCookies, Collections.emptyMap(), phStringResponse);
    }

    public static StringRequest createStringRequest(int method, String url, boolean addUserCookies, Map<String, String> params, PHResponse<String> phStringResponse) {
        return new StringRequest(method, url, phStringResponse::onResponse, error -> {
            Logger.getLogger().i("Error from response" + error.getMessage());
            phStringResponse.onResponse("");
        }) {

            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                return HtmlUtils.createHeaders(addUserCookies);
            }
        };
    }

    public static StringRequest createStringRequestWithOldHeaders(int method, String url, PHResponse<String> phStringResponse) {
        return new StringRequest(method, url, phStringResponse::onResponse, error -> {
            Logger.getLogger().i("Error from response" + error.getMessage());
            phStringResponse.onResponse("");
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return HtmlUtils.createHeadersOld();
            }
        };
    }

    public static JsonObjectRequest createStringRequestForCaptcha(PHResponse<JSONObject> jsonObjectPHResponse) {
        return new JsonObjectRequest(PH.Api.URL_LOGIN, null, jsonObjectPHResponse::onResponse, error -> {
            Logger.getLogger().i("Error from response" + error.getMessage());
            jsonObjectPHResponse.onResponse(new JSONObject());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = HtmlUtils.createHeaders(false);
                // Need to add this!!
                map.put("Upgrade-Insecure-Requests", "1");
                return map;
            }
        };
    }

    public static boolean isNetworkAvailable(int type) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) PHApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE));
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null) && (networkInfo.getType() == type);
    }

    public static boolean isNetworkAvailableForNotification() {
        if (PHPreferences.getInstance().getNotificationPreferences().getBoolean(PH.Prefs.KEY_NOTIFICATION_SWITCH_MOBILE_DATA, false)) {
            return isNetworkAvailable(ConnectivityManager.TYPE_MOBILE) || isNetworkAvailable(ConnectivityManager.TYPE_WIFI);
        }
        return isNetworkAvailable(ConnectivityManager.TYPE_WIFI);
    }

}
