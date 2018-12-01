package hu.sherad.hos.data.api.ph;

import android.support.annotation.StringRes;

import hu.sherad.hos.R;

final public class PH {

    private static PHService phService = null;

    public static synchronized PHService getPHService() {
        if (phService == null) {
            phService = new PHService();
        }
        return phService;
    }

    @StringRes
    public static int getErrorFromCode(PH.Data data) {
        if (data == null) {
            return R.string.what;
        }
        switch (data) {
            case ERROR_EMAIL_PASSWORD:
                return R.string.wrong_email_or_password;
            case ERROR_NO_INTERNET:
                return R.string.no_internet_connection;
            case ERROR_LOAD:
                return R.string.error_occurred_try_again;
            case ERROR_INTERNAL:
                return R.string.internal_error;
            case DATA_CAN_LOAD:
                return R.string.load_more;
            case DATA_LOADING:
                return R.string.loading;
            case ERROR_INTERNET_FAILURE:
                return R.string.cannot_connect_to_internet;
            case ERROR_SERVER:
                return R.string.server_error;
            case ERROR_TIMEOUT:
                return R.string.timeout_check_network;
            default:
                return R.string.what;
        }
    }

    public static long getIDFromCode(PH.Data data) {
        switch (data) {
            case ERROR_EMAIL_PASSWORD:
                return 1;
            case ERROR_NO_INTERNET:
                return 2;
            case ERROR_LOAD:
                return 3;
            case ERROR_INTERNAL:
                return 4;
            case DATA_CAN_LOAD:
                return 5;
            case DATA_LOADING:
                return 6;
            case ERROR_SERVER:
                return 8;
            case ERROR_INTERNET_FAILURE:
                return 9;
            case ERROR_TIMEOUT:
                return 10;
            default:
                // OK
                return 7;
        }
    }

    public enum Data {
        OK,
        ERROR_NO_INTERNET,
        ERROR_EMAIL_PASSWORD,
        ERROR_LOAD,
        ERROR_INTERNAL,
        ERROR_SERVER,
        ERROR_INTERNET_FAILURE,
        ERROR_TIMEOUT,
        DATA_LOADING,
        DATA_CAN_LOAD
    }

    public interface Intent {
        String ACTION_NOTIFICATION_CHECK = "hu.sherad.hos.action.ACTION_NOTIFICATION_CHECK";
        String ACTION_UPDATED_USER_TOPICS = "hu.sherad.hos.action.ACTION_UPDATED_USER_TOPICS";
    }

    public interface Api {

        /* DEFAULT HOSTS */
        String HOST_PROHARDVER = "https://fototrend.hu";
        String HOST_HARDVERAPRO = "https://hardverapro.hu";
        String MOBILARENA_NEWS = "https://mobilarena.hu/index.html";
        String PROHARDVER_NEWS = "https://prohardver.hu/index.html";
        String IT_CAFE_NEWS = "https://itcafe.hu/index.html";
        String GAMEPOD_NEWS = "https://gamepod.hu/index.html";
        String LOGOUT_NEWS = "https://logout.hu/index.html";

        /* DEFAULT URLS */
        String URL_LOGIN = Api.HOST_PROHARDVER + "/muvelet/hozzaferes/belepes.php";
        String URL_MESSAGES = Api.HOST_PROHARDVER + "/privatok/listaz.php";
        String URL_TOPICS_SEARCH = Api.HOST_PROHARDVER + "/temak/keres.php";
        String URL_TOPICS = Api.HOST_PROHARDVER + "/forum/index.html";
        String URL_USER_DATA = Api.HOST_PROHARDVER + "/fiok/adatlap.php";
        String URL_MODIFY_USER_DATA = Api.HOST_PROHARDVER + "/muvelet/tag/modosit.php";
        String URL_HOT_TOPICS = Api.HOST_PROHARDVER + "/temak/friss.html";

    }

    public interface Prefs {

        /* Preference files */
        String PREF_COOKIES = "hu.sherad.hos_preferences_cookies";
        String PREF_DEFAULT = "hu.sherad.hos_preferences_default";
        String PREF_APPEARANCE = "hu.sherad.hos_preferences_appearance";
        String PREF_NOTIFICATION = "hu.sherad.hos_preferences_notification";
        String PREF_USER_CREDENTIALS = "hu.sherad.hos_preferences_user_credentials";
        String PREF_OTHER = "hu.sherad.hos_preferences_other";
        String PREF_CACHE = "hu.sherad.hos_preferences_cache";

        /* Home preferences, use only with the fragment */
        String KEY_HOME_NOTIFICATION = "preferences_home_notifications";
        String KEY_HOME_APPEARANCE = "preferences_home_appearance";
        String KEY_HOME_OTHER = "preferences_home_other";
        String KEY_HOME_USER_CREDENTIALS = "preference_home_user_credentials";

        /* Default preferences everywhere can use */
        String KEY_DEFAULT_RATE_APP = "preferences_default_rate_app";
        String KEY_DEFAULT_NEWS_VALUE = "preferences_default_news_value_2";
        String KEY_DEFAULT_FAVOURITES_TOPICS_SORT = "preferences_default_favourites_topics_sort_2";
        String KEY_DEFAULT_COMMENTED_TOPICS_SORT = "preferences_default_commented_topics_sort_2";
        String KEY_DEFAULT_WARNING_NEW_COMMENT = "preferences_default_warning_new_comment";

        /* Notification preferences, use only with the fragment */
        String KEY_NOTIFICATION_TOPICS_FAVOURITE = "preferences_notification_topics_favourite";
        String KEY_NOTIFICATION_TOPICS_COMMENTED = "preferences_notification_topics_commented";
        String KEY_NOTIFICATION_SWITCH_MESSAGES = "preferences_notification_switch_messages";
        String KEY_NOTIFICATION_SOUND = "preferences_notification_sound";
        String KEY_NOTIFICATION_REFRESH = "preferences_notification_refresh";
        String KEY_NOTIFICATION_SWITCH_MOBILE_DATA = "preferences_notification_switch_mobile_data";

        /* Appearance preferences, use only with the fragment */
        String KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT = "preferences_appearance_switch_scroll_new_comment";
        String KEY_APPEARANCE_SWITCH_TIME = "preferences_appearance_switch_time";
        String KEY_APPEARANCE_SWITCH_LIST_INC = "preferences_appearance_switch_list_inc";
        String KEY_APPEARANCE_SWITCH_OFF_COMMENT = "preferences_appearance_switch_off_comment";
        String KEY_APPEARANCE_SWITCH_FLOATING_BUTTON = "preference_appearance_switch_floating_button";
        String KEY_APPEARANCE_SWITCH_RANK = "preference_appearance_switch_rank";
        String KEY_APPEARANCE_COMMENT_TEXT_SIZE = "preference_appearance_comment_text_size";
        String KEY_APPEARANCE_START_FRAGMENT = "preferences_appearance_start_fragment";
        String KEY_APPEARANCE_THEME = "preferences_appearance_theme";
        String KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS = "preferences_appearance_swipe_refresh_comments";
        String KEY_APPEARANCE_SWITCH_NEW_COMMENT_PANEL_HIDE = "preference_appearance_switch_new_comment_options_hide";
        String KEY_APPEARANCE_SWITCH_SIGNATURE = "preference_appearance_switch_signature";
        String KEY_APPEARANCE_SWITCH_TIME_SECONDS = "preferences_appearance_switch_time_seconds";

        /* User Credentials preferences */
        String KEY_USER_CREDENTIALS_NAME = "preferences_user_credentials_name";
        String KEY_USER_CREDENTIALS_AVATAR_URL = "preferences_user_credentials_avatar_url";
        String KEY_USER_CREDENTIALS_EMAIL = "preferences_user_credentials_email";
        String KEY_USER_CREDENTIALS_PASSWORD = "preferences_user_credentials_password";

        /* Other preferences */
        String KEY_OTHER_SWITCH_DEBUG = "preferences_other_switch_debug";
        String KEY_OTHER_SWITCH_ANALYTICS = "preferences_other_switch_analytics";
        String KEY_OTHER_SETTINGS_EXPORT = "preferences_other_export_settings";
        String KEY_OTHER_SETTINGS_IMPORT = "preferences_other_import_settings";

        /* Cache preferences */
        String KEY_CACHE_NOTIFICATION_SENT = "preferences_cache_notification_sent";
        String KEY_CACHE_FINISHED_LOGIN = "preferences_cache_finished_login";

    }

    public interface RemoteConfig {

        String REMOTE_CONFIG_MIN_VERSION = "REMOTE_CONFIG_MIN_VERSION";
        String REMOTE_CONFIG_INFO = "REMOTE_CONFIG_INFO";

    }

}
