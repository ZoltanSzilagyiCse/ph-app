package hu.sherad.hos.utils.io;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.util.Xml;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.ui.fragments.settings.SettingsAppearance;

import static android.content.Context.MODE_PRIVATE;

/**
 * Utility methods for working with files.
 */
public class FileManager {

    public static boolean writeBinaryFile(Context context, String fileName, Object data) {
        try {
            Logger.getLogger().i("Wrote file | " + fileName);
            new ObjectOutputStream(context.openFileOutput(fileName, MODE_PRIVATE)).writeObject(data);
            return true;
        } catch (IOException e) {
            Logger.getLogger().e(e);
            return false;
        }
    }

    @Nullable
    public static Object readBinaryFile(Context context, String fileName) {
        if (getFile(context, fileName).exists()) {
            try {
                Logger.getLogger().i("Loaded file | " + fileName);
                return new ObjectInputStream(context.openFileInput(fileName)).readObject();
            } catch (IOException | ClassNotFoundException e) {
                Logger.getLogger().e(e);
                return null;
            }
        } else {
            Logger.getLogger().i("No such file or directory: " + fileName);
            return null;
        }
    }

    public static File getFile(Context context, String fileName) {
        return new File(context.getFilesDir() + "/" + fileName);
    }

    public static String saveSettings(Context context, Uri uri) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri).createFile("application/octet-stream",
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date()) + ".phbackup");
        if (documentFile == null) {
            return null;
        } else {
            SharedPreferences preferencesAppearance = PHPreferences.getInstance().getAppearancePreferences();
            SharedPreferences preferencesNotification = PHPreferences.getInstance().getNotificationPreferences();
            SharedPreferences preferencesOther = PHPreferences.getInstance().getOtherPreferences();
            SharedPreferences preferencesDefault = PHPreferences.getInstance().getDefaultPreferences();

            Logger.getLogger().i("Getting data...");
            XmlSerializer serializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            OutputStream outputStream = null;
            BufferedWriter bufferedWriter = null;
            try {
                serializer.setOutput(writer);
                serializer.startDocument("UTF-8", true);
                serializer.startTag("", "backup");
                serializer.attribute("", "version", "1.0");
                serializer.startTag("", "preferences");
                // Appearance
                Logger.getLogger().i("Generating Data: Appearance Setting...");
                serializer.startTag("", PH.Prefs.PREF_APPEARANCE);
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_START_FRAGMENT, String.valueOf(preferencesAppearance.getInt(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT, SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_EXPLORE)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_THEME, String.valueOf(preferencesAppearance.getInt(PH.Prefs.KEY_APPEARANCE_THEME, SettingsAppearance.PrefValueTheme.PH_THEME_LIGHT)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE, String.valueOf(preferencesAppearance.getFloat(PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE, 100.0f)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWITCH_TIME, String.valueOf(preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME, false)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWITCH_TIME_SECONDS, String.valueOf(preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME_SECONDS, false)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWITCH_LIST_INC, String.valueOf(preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_LIST_INC, true)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWITCH_OFF_COMMENT, String.valueOf(preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_OFF_COMMENT, false)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWITCH_FLOATING_BUTTON, String.valueOf(preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_FLOATING_BUTTON, true)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWITCH_SIGNATURE, String.valueOf(preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SIGNATURE, false)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT, String.valueOf(preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT, true)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWITCH_NEW_COMMENT_PANEL_HIDE, String.valueOf(preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_NEW_COMMENT_PANEL_HIDE, false)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWITCH_RANK, String.valueOf(preferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_RANK, false)));
                writeTagWithText(serializer, PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS, String.valueOf(preferencesAppearance.getInt(PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS, -1)));
                serializer.endTag("", PH.Prefs.PREF_APPEARANCE);
                Logger.getLogger().i("OK...");
                // Notification
                Logger.getLogger().i("Generating Data: Notification Setting...");
                serializer.startTag("", PH.Prefs.PREF_NOTIFICATION);
                writeTagWithText(serializer, PH.Prefs.KEY_NOTIFICATION_REFRESH, String.valueOf(preferencesNotification.getInt(PH.Prefs.KEY_NOTIFICATION_REFRESH, 2)));
                writeTagWithText(serializer, PH.Prefs.KEY_NOTIFICATION_SOUND, preferencesNotification.getString(PH.Prefs.KEY_NOTIFICATION_SOUND, ""));
                for (String topic : preferencesNotification.getStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, new HashSet<>())) {
                    writeTagWithText(serializer, PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, topic);
                }
                writeTagWithText(serializer, PH.Prefs.KEY_NOTIFICATION_SWITCH_MESSAGES, String.valueOf(preferencesNotification.getBoolean(PH.Prefs.KEY_NOTIFICATION_SWITCH_MESSAGES, false)));
                writeTagWithText(serializer, PH.Prefs.KEY_NOTIFICATION_SWITCH_MOBILE_DATA, String.valueOf(preferencesNotification.getBoolean(PH.Prefs.KEY_NOTIFICATION_SWITCH_MOBILE_DATA, false)));
                serializer.endTag("", PH.Prefs.PREF_NOTIFICATION);
                Logger.getLogger().i("OK...");
                // Other
                Logger.getLogger().i("Generating Data: Other Setting...");
                serializer.startTag("", PH.Prefs.PREF_OTHER);
                writeTagWithText(serializer, PH.Prefs.KEY_OTHER_SWITCH_DEBUG, String.valueOf(preferencesOther.getBoolean(PH.Prefs.KEY_OTHER_SWITCH_DEBUG, false)));
                writeTagWithText(serializer, PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS, String.valueOf(preferencesOther.getBoolean(PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS, false)));
                serializer.endTag("", PH.Prefs.PREF_OTHER);
                Logger.getLogger().i("OK...");
                // Default
                Logger.getLogger().i("Generating Data: Default Setting...");
                serializer.startTag("", PH.Prefs.PREF_DEFAULT);
                String savedFavouritesSort = preferencesDefault.getString(PH.Prefs.KEY_DEFAULT_FAVOURITES_TOPICS_SORT, null);
                if (savedFavouritesSort != null) {
                    List<String> savedFavouritesSortList = new Gson().fromJson(savedFavouritesSort, new TypeToken<List<String>>() {
                    }.getType());
                    for (String favouriteTopic : savedFavouritesSortList) {
                        writeTagWithText(serializer, PH.Prefs.KEY_DEFAULT_FAVOURITES_TOPICS_SORT, favouriteTopic);
                    }
                }
                String savedCommentedSort = preferencesDefault.getString(PH.Prefs.KEY_DEFAULT_COMMENTED_TOPICS_SORT, null);
                if (savedCommentedSort != null) {
                    List<String> savedCommentedSortList = new Gson().fromJson(savedCommentedSort, new TypeToken<List<String>>() {
                    }.getType());
                    for (String commentedTopic : savedCommentedSortList) {
                        writeTagWithText(serializer, PH.Prefs.KEY_DEFAULT_COMMENTED_TOPICS_SORT, commentedTopic);
                    }
                }
                serializer.endTag("", PH.Prefs.PREF_DEFAULT);
                Logger.getLogger().i("OK...");

                serializer.endTag("", "preferences");
                serializer.endTag("", "backup");
                serializer.endDocument();
                // Generate output
                String plainOutputXML = writer.toString();
                ContentResolver contentResolver = context.getContentResolver();
                outputStream = contentResolver.openOutputStream(documentFile.getUri());
                if (outputStream == null) {
                    return null;
                }
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(plainOutputXML);
                return uri.getPath();
            } catch (Exception e) {
                Logger.getLogger().e(e);
                return null;
            } finally {
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean loadSettings(Context context, Uri uri) {
        SharedPreferences.Editor preferencesAppearance = PHPreferences.getInstance().getAppearancePreferences().edit();
        SharedPreferences.Editor preferencesNotification = PHPreferences.getInstance().getNotificationPreferences().edit();
        SharedPreferences.Editor preferencesOther = PHPreferences.getInstance().getOtherPreferences().edit();
        SharedPreferences.Editor preferenceDefault = PHPreferences.getInstance().getDefaultPreferences().edit();
        BufferedReader reader = null;
        InputStream inputStream = null;

        try {
            ContentResolver contentResolver = context.getContentResolver();
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null) {
                return false;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder plainInputXML = new StringBuilder();
            Logger.getLogger().i("Getting data...");
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                plainInputXML.append(line);
            }
            Document document = Jsoup.parse(plainInputXML.toString());
            // Backup
            float version = Float.valueOf(document.body().selectFirst("backup").attr("version"));
            // Appearance
            Logger.getLogger().i("Restore Data: Appearance Setting...");
            Element elementAppearance = document.getElementsByTag(PH.Prefs.PREF_APPEARANCE).first();
            if (elementAppearance != null) {
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT) != null) {
                    preferencesAppearance.putInt(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT, Integer.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_THEME) != null) {
                    preferencesAppearance.putInt(PH.Prefs.KEY_APPEARANCE_THEME, Integer.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_THEME).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE) != null) {
                    preferencesAppearance.putFloat(PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE, Float.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME) != null) {
                    preferencesAppearance.putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME, Boolean.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME_SECONDS) != null) {
                    preferencesAppearance.putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME_SECONDS, Boolean.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME_SECONDS).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_LIST_INC) != null) {
                    preferencesAppearance.putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_LIST_INC, Boolean.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_LIST_INC).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_OFF_COMMENT) != null) {
                    preferencesAppearance.putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_OFF_COMMENT, Boolean.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_OFF_COMMENT).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_FLOATING_BUTTON) != null) {
                    preferencesAppearance.putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_FLOATING_BUTTON, Boolean.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_FLOATING_BUTTON).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_SIGNATURE) != null) {
                    preferencesAppearance.putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SIGNATURE, Boolean.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_SIGNATURE).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT) != null) {
                    preferencesAppearance.putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT, Boolean.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_SCROLL_NEW_COMMENT).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_NEW_COMMENT_PANEL_HIDE) != null) {
                    preferencesAppearance.putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_NEW_COMMENT_PANEL_HIDE, Boolean.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_NEW_COMMENT_PANEL_HIDE).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_RANK) != null) {
                    preferencesAppearance.putBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_RANK, Boolean.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWITCH_RANK).text()));
                }
                if (elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS) != null) {
                    preferencesAppearance.putInt(PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS, Integer.valueOf(elementAppearance.selectFirst(PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS).text()));
                }
            }
            preferencesAppearance.apply();
            Logger.getLogger().i("OK...");
            // Notification
            Logger.getLogger().i("Restore Data: Notification Setting...");
            Element elementNotification = document.getElementsByTag(PH.Prefs.PREF_NOTIFICATION).first();
            if (elementNotification != null) {
                if (elementNotification.selectFirst(PH.Prefs.KEY_NOTIFICATION_REFRESH) != null) {
                    preferencesNotification.putInt(PH.Prefs.KEY_NOTIFICATION_REFRESH, Integer.valueOf(elementNotification.selectFirst(PH.Prefs.KEY_NOTIFICATION_REFRESH).text()));
                }
                if (elementNotification.selectFirst(PH.Prefs.KEY_NOTIFICATION_SOUND) != null) {
                    preferencesNotification.putString(PH.Prefs.KEY_NOTIFICATION_SOUND, String.valueOf(elementNotification.selectFirst(PH.Prefs.KEY_NOTIFICATION_SOUND).text()));
                }
                Set<String> topicSet = new HashSet<>();
                for (Element topic : elementNotification.select(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE)) {
                    topicSet.add(topic.text());
                }
                preferencesNotification.putStringSet(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE, topicSet);
                if (elementNotification.selectFirst(PH.Prefs.KEY_NOTIFICATION_SWITCH_MESSAGES) != null) {
                    preferencesNotification.putBoolean(PH.Prefs.KEY_NOTIFICATION_SWITCH_MESSAGES, Boolean.valueOf(elementNotification.selectFirst(PH.Prefs.KEY_NOTIFICATION_SWITCH_MESSAGES).text()));
                }
                if (elementNotification.selectFirst(PH.Prefs.KEY_NOTIFICATION_SWITCH_MOBILE_DATA) != null) {
                    preferencesNotification.putBoolean(PH.Prefs.KEY_NOTIFICATION_SWITCH_MOBILE_DATA, Boolean.valueOf(elementNotification.selectFirst(PH.Prefs.KEY_NOTIFICATION_SWITCH_MOBILE_DATA).text()));
                }
            }
            preferencesNotification.apply();
            Logger.getLogger().i("OK...");
            // Other
            Logger.getLogger().i("Restore Data: Other Setting...");
            Element elementOther = document.getElementsByTag(PH.Prefs.PREF_OTHER).first();
            if (elementOther != null) {
                if (elementOther.selectFirst(PH.Prefs.KEY_OTHER_SWITCH_DEBUG) != null) {
                    preferencesOther.putBoolean(PH.Prefs.KEY_OTHER_SWITCH_DEBUG, Boolean.valueOf(elementOther.selectFirst(PH.Prefs.KEY_OTHER_SWITCH_DEBUG).text()));
                }
                if (elementOther.selectFirst(PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS) != null) {
                    preferencesOther.putBoolean(PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS, Boolean.valueOf(elementOther.selectFirst(PH.Prefs.KEY_OTHER_SWITCH_ANALYTICS).text()));
                }
            }
            preferencesOther.apply();
            Logger.getLogger().i("OK...");
            // Default
            Logger.getLogger().i("Restore Data: Default Setting...");
            Element elementDefault = document.getElementsByTag(PH.Prefs.PREF_DEFAULT).first();
            if (elementDefault != null) {
                if (!elementDefault.select(PH.Prefs.KEY_DEFAULT_FAVOURITES_TOPICS_SORT).isEmpty()) {
                    List<String> savedFavouritesSort = new ArrayList<>();
                    for (Element elementTopicName : elementDefault.select(PH.Prefs.KEY_DEFAULT_FAVOURITES_TOPICS_SORT)) {
                        savedFavouritesSort.add(elementTopicName.text());
                    }
                    preferenceDefault.putString(PH.Prefs.KEY_DEFAULT_FAVOURITES_TOPICS_SORT, new Gson().toJson(savedFavouritesSort));
                }
                if (!elementDefault.select(PH.Prefs.KEY_DEFAULT_COMMENTED_TOPICS_SORT).isEmpty()) {
                    List<String> savedCommentedSort = new ArrayList<>();
                    for (Element elementTopicName : elementDefault.select(PH.Prefs.KEY_DEFAULT_COMMENTED_TOPICS_SORT)) {
                        savedCommentedSort.add(elementTopicName.text());
                    }
                    preferenceDefault.putString(PH.Prefs.KEY_DEFAULT_COMMENTED_TOPICS_SORT, new Gson().toJson(savedCommentedSort));
                }
            }
            preferenceDefault.apply();
            Logger.getLogger().i("OK...");
            return true;
        } catch (Exception e) {
            Logger.getLogger().e(e);
            return false;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static XmlSerializer writeTagWithText(XmlSerializer serializer, String tag, String text) throws IOException {
        return serializer.startTag("", tag).text(text).endTag("", tag);
    }

    private static XmlSerializer writeAttr(XmlSerializer serializer, String tag, String text) throws IOException {
        return serializer.attribute("", tag, text);
    }

    public static void chooseFile(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream");
        activity.startActivityForResult(intent, requestCode);
    }

    public static void chooseFile(Fragment fragment, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream");
        fragment.startActivityForResult(intent, requestCode);
    }
}
