package hu.sherad.hos.data.api.ph;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpCookie;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import hu.sherad.hos.data.api.json.JSON;
import hu.sherad.hos.data.api.user.UserTopics;
import hu.sherad.hos.data.models.CookieSettings;
import hu.sherad.hos.data.models.StatusHttpRequest;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.io.Logger;

public final class PHAuth {

    private String email;
    private String password;
    private String captcha;
    private String captchaID;

    private PHAuth(String email, String password, String captcha, String captchaID) {
        this.email = email;
        this.password = password;
        this.captcha = captcha == null ? "" : captcha;
        this.captchaID = captchaID == null ? "" : captchaID;
    }

    public static void finishLogin() {
        SharedPreferences sharedPreferences = PHPreferences.getInstance().getCachePreferences();
        sharedPreferences.edit().putBoolean(PH.Prefs.KEY_CACHE_FINISHED_LOGIN, true).apply();
    }

    public static boolean isFinishedLogin() {
        SharedPreferences sharedPreferences = PHPreferences.getInstance().getCachePreferences();
        return sharedPreferences.getBoolean(PH.Prefs.KEY_CACHE_FINISHED_LOGIN, false) && isLoggedIn();
    }

    public static boolean isLoggedIn() {
        SharedPreferences sharedPreferences = PHPreferences.getInstance().getUserCredentialsPreferences();
        if (sharedPreferences.getString(PH.Prefs.KEY_USER_CREDENTIALS_EMAIL, "").isEmpty()) {
            return false;
        }
        if (sharedPreferences.getString(PH.Prefs.KEY_USER_CREDENTIALS_PASSWORD, "").isEmpty()) {
            return false;
        }
        List<HttpCookie> cookies = CookieSettings.get();
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals("identifier")) {
                return true;
            }
        }
        return false;
    }

    public static void isValidIdentifier(PHIdentifier identifierCallback) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, PH.Api.URL_MESSAGES,
                response -> {
                    Parser parser = Parser.parse(response);
                    if (parser.needToLogin()) {
                        identifierCallback.onResult(StatusIdentifier.DEPRECATED, "Bejelentkezésed lejárt");
                    } else {
                        List<Topic> messageTopics = parser.getMessageTopics();
                        UserTopics.getInstance()
                                .update(Topic.Type.FAVOURITE, parser.getFavouriteTopics())
                                .update(Topic.Type.COMMENTED, parser.getCommentedTopics())
                                .update(Topic.Type.MESSAGE, messageTopics)
                                .setData(PH.Data.OK, messageTopics.size() < parser.getMaxPages() ? PH.Data.DATA_CAN_LOAD : PH.Data.OK)
                                .sendBroadcast();
                        identifierCallback.onResult(StatusIdentifier.ACTIVE, "");
                    }
                },
                error -> {
                    Logger.getLogger().e(error);
                    identifierCallback.onResult(StatusIdentifier.ERROR, PHApplication.getInstance().getString(PH.getErrorFromCode(HtmlUtils.setDataFromError(error))));
                }
        ) {

            @Override
            public Map<String, String> getHeaders() {
                return HtmlUtils.createHeaders(true);
            }

        };
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Identifier is valid?");
    }

    private static void saveCredentials(String email, String password) throws GeneralSecurityException {
        SharedPreferences sharedPreferences = PHPreferences.getInstance().getUserCredentialsPreferences();
        sharedPreferences.edit().putString(PH.Prefs.KEY_USER_CREDENTIALS_EMAIL, email).apply();
        sharedPreferences.edit().putString(PH.Prefs.KEY_USER_CREDENTIALS_PASSWORD, AESCrypt.encrypt("PaQhpEPLSGy4tu7x", password)).apply();
    }

    public void login(PHAuthResult authStateListener) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                PH.Api.URL_LOGIN,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Logger.getLogger().i("Login response: " + jsonObject);
                        // Wrong email or password
                        if (jsonObject.has(JSON.JSON_ATTR_FORM_ERROR)) {
                            authStateListener.onResult(StatusHttpRequest.FAILED, jsonObject.getString(JSON.JSON_ATTR_FORM_ERROR));
                            return;
                        }
                        // Wrong captcha
                        if (jsonObject.has(JSON.JSON_ELEMENT_CAPTCHA)) {
                            authStateListener.onResult(StatusHttpRequest.FAILED, jsonObject.getJSONObject(JSON.JSON_ELEMENT_CAPTCHA).getString(JSON.JSON_ATTR_ERROR));
                            return;
                        }
                        // Successfully login
                        if (jsonObject.has("message")) {
                            try {
                                saveCredentials(email, password);
                                authStateListener.onResult(StatusHttpRequest.SUCCESS, "");
                            } catch (GeneralSecurityException e) {
                                Logger.getLogger().e(e);
                                authStateListener.onResult(StatusHttpRequest.FAILED, "Nem sikerült titkosítani a jelszót");
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Logger.getLogger().e(error);
                    authStateListener.onResult(StatusHttpRequest.FAILED, PHApplication.getInstance().getString(PH.getErrorFromCode(HtmlUtils.setDataFromError(error))));
                }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response.statusCode == HttpsURLConnection.HTTP_OK) {
                    Map<String, String> responseHeaders = response.headers;
                    String rawCookie = responseHeaders.get("Set-Cookie");
                    if (rawCookie != null) {
                        CookieSettings.removeAll();
                        List<HttpCookie> httpCookies = HttpCookie.parse(rawCookie);
                        for (HttpCookie httpCookie : httpCookies) {
                            CookieSettings.add(httpCookie);
                        }

                    }
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getParams() {
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("email", email);
                headerMap.put("pass", password);
                headerMap.put("stay", "1");
                headerMap.put("no_ip_check", "1");
                headerMap.put("leave_others", "1");
                if (!captcha.isEmpty()) {
                    headerMap.put("captcha_answer", captcha);
                    headerMap.put("captcha_code", captchaID);
                }
                return headerMap;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = HtmlUtils.createHeaders(false);
                map.put("Upgrade-Insecure-Requests", "1");
                return map;
            }

        };
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Trying to login");
    }

    public enum StatusIdentifier {
        ACTIVE, DEPRECATED, ERROR
    }

    public interface PHAuthResult {

        void onResult(@NonNull StatusHttpRequest status, @NonNull String error);

    }

    public interface PHIdentifier {

        void onResult(@NonNull StatusIdentifier status, @NonNull String error);

    }

    public static class Builder {

        private String email;
        private String password;
        private String captcha;
        private String captchaID;

        public Builder(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public Builder setCaptcha(String captcha) {
            this.captcha = captcha;
            return this;
        }

        public Builder setCaptchaIDByURL(@NonNull String captchaURL) {
            this.captchaID = captchaURL.isEmpty() ? "" : captchaURL.substring(captchaURL.lastIndexOf("/") + 1, captchaURL.lastIndexOf("."));
            return this;
        }

        public PHAuth create() {
            return new PHAuth(email, password, captcha, captchaID);
        }

    }
}
