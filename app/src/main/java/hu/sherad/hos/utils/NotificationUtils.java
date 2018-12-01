package hu.sherad.hos.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import hu.sherad.hos.BuildConfig;
import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHAuth;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.user.UserTopics;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.receivers.PHReceiver;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.recyclerview.holders.HolderListTopic;
import hu.sherad.hos.utils.io.Logger;

public class NotificationUtils {

    private static final String PH_NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID;
    private static final int REQUEST_CODE_NOTIFICATION_CHECK = 1;

    public static void doCheck() {
        if (NetworkUtils.isNetworkAvailableForNotification()) {
            PHAuth.isValidIdentifier((status, error) -> {
                if (status == PHAuth.StatusIdentifier.ACTIVE) {
                    if (NotificationUtils.shouldCheckTopics(Topic.Type.FAVOURITE)) {
                        checkTopics(Topic.Type.FAVOURITE);
                    }
                    if (NotificationUtils.shouldCheckTopics(Topic.Type.COMMENTED)) {
                        checkTopics(Topic.Type.COMMENTED);
                    }
                    if (NotificationUtils.shouldCheckMessages()) {
                        checkMessages();
                    }
                }
            });
            if (!NotificationUtils.shouldRunNotificationService()) {
                Logger.getLogger().i("Calling stop");
                cancelAlarm();
            }
        }
    }

    private static void checkTopics(Topic.Type type) {
        Logger.getLogger().i("Checking " + type.name());
        // Get the saved topics
        Set<String> savedTopics = getNotifiableTopics(type);
        for (Topic freshTopic : UserTopics.getInstance().getTopics(type)) {
            if (savedTopics.contains(freshTopic.getTitle())) {
                if (freshTopic.getNewComments() != 0) {
                    NotificationUtils.createNotification(freshTopic, false);
                    continue;
                }
            }
            NotificationUtils.cancelNotification(freshTopic);
        }
    }

    private static void checkMessages() {
        Logger.getLogger().i("Checking Messages");
        for (Topic topic : UserTopics.getInstance().getTopics(Topic.Type.MESSAGE)) {
            if (topic.getNewComments() == 0) {
                NotificationUtils.cancelNotification(topic);
            } else {
                NotificationUtils.createNotification(topic, true);
            }
        }
    }

    private static void createNotification(Topic topic, boolean isMessage) {
        if (isNotificationActiveCache(topic)) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) PHApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        int notificationID = topic.getTitle().hashCode();
        String ringtoneURI = getRingtone();

        Intent activityIntent = ActivityComments.createIntent(topic);
        PendingIntent pendingIntent = createPendingIntent(activityIntent, notificationID);
        NotificationCompat.Builder builder = isMessage ?
                createNotificationMessages(topic, pendingIntent) :
                createNotificationFavourites(topic, pendingIntent);

        if (!ringtoneURI.isEmpty()) {
            builder.setSound(Uri.parse(ringtoneURI));
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        } else {
            NotificationChannel channel = new NotificationChannel(PH_NOTIFICATION_CHANNEL, "PH! Értesítések", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(notificationID, builder.build());
        updateNotificationCache(topic);
    }

    public static void cancelNotification(Topic topic) {
        NotificationManager notificationManager = (NotificationManager) PHApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(topic.getTitle().hashCode());
        }
    }

    private static long getRefreshInterval() {
        switch (PHPreferences.getInstance().getNotificationPreferences().getInt(PH.Prefs.KEY_NOTIFICATION_REFRESH, 2)) {
            case 0: // 1 min
                return TimeUnit.MINUTES.toMillis(1);
            case 1: // 5 minutes
                return TimeUnit.MINUTES.toMillis(5);
            case 2: // 10 minutes
                return TimeUnit.MINUTES.toMillis(10);
            case 3: // 20 minutes
                return TimeUnit.MINUTES.toMillis(20);
            case 4: // 1 hour
                return TimeUnit.HOURS.toMillis(1);
            default:
                return -1;
        }
    }

    public static boolean shouldRunNotificationService() {
        return PHAuth.isFinishedLogin() && (shouldCheckTopics(Topic.Type.FAVOURITE) || shouldCheckTopics(Topic.Type.COMMENTED) || shouldCheckMessages());
    }

    private static boolean shouldCheckTopics(Topic.Type type) {
        return !PHPreferences.getInstance().getNotificationPreferences().getStringSet(Topic.Utils.getSavedNotifiableKey(type), new HashSet<>()).isEmpty();
    }

    private static boolean shouldCheckMessages() {
        return PHPreferences.getInstance().getNotificationPreferences().getBoolean(PH.Prefs.KEY_NOTIFICATION_SWITCH_MESSAGES, false);
    }

    private static PendingIntent createPendingIntent(Intent activityCommentsIntent, int id) {
        return PendingIntent.getActivity(PHApplication.getInstance(), id, activityCommentsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static String getRingtone() {
        return PHPreferences.getInstance().getNotificationPreferences().getString(PH.Prefs.KEY_NOTIFICATION_SOUND, "");
    }

    private static NotificationCompat.Builder createNotificationFavourites(Topic topic, PendingIntent pendingIntent) {
        return new NotificationCompat.Builder(PHApplication.getInstance(), PH_NOTIFICATION_CHANNEL)
                .setContentTitle(topic.getTitle())
                .setContentText(topic.getNewComments() + " új hozzászólás érkezett")
                .setSmallIcon(R.drawable.vd_bookmark)
                .setLights(ContextCompat.getColor(PHApplication.getInstance(), R.color.accent), 1000, 1000)
                .setLargeIcon(BitmapFactory.decodeResource(PHApplication.getInstance().getResources(), R.mipmap.ic_launcher))
                .setCategory(Notification.CATEGORY_SOCIAL)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);
    }

    private static NotificationCompat.Builder createNotificationMessages(Topic topic, PendingIntent pendingIntent) {
        return new NotificationCompat.Builder(PHApplication.getInstance(), PH_NOTIFICATION_CHANNEL)
                .setContentTitle(topic.getTitle() + " üzenetet küldött")
                .setContentText(topic.getNewComments() + " új üzenet érkezett")
                .setSmallIcon(R.drawable.vd_message)
                .setLights(ContextCompat.getColor(PHApplication.getInstance(), R.color.accent), 1000, 1000)
                .setLargeIcon(BitmapFactory.decodeResource(PHApplication.getInstance().getResources(), R.mipmap.ic_launcher))
                .setCategory(Notification.CATEGORY_SOCIAL)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);
    }

    private static boolean isNotificationActiveCache(@NonNull Topic topic) {
        Set<String> sentNotifications = PHPreferences.getInstance().getCachePreferences().getStringSet(PH.Prefs.KEY_CACHE_NOTIFICATION_SENT, new HashSet<>());
        if (sentNotifications.isEmpty()) {
            return false;
        }
        for (String sentNotification : sentNotifications) {
            if (sentNotification.equals(topic.getTitle() + topic.getNewComments())) {
                return true;
            }
        }
        return false;
    }

    private static void updateNotificationCache(@NonNull Topic topic) {
        Set<String> sentNotifications = PHPreferences.getInstance().getCachePreferences().getStringSet(PH.Prefs.KEY_CACHE_NOTIFICATION_SENT, new HashSet<>());
        for (String sentNotification : sentNotifications) {
            if (sentNotification.startsWith(topic.getTitle())) {
                Logger.getLogger().i(
                        "Remove notification cache - [" + topic.getTitle() + "] [" + sentNotification.substring(topic.getTitle().length()) + "]");
                sentNotifications.remove(sentNotification);
                break;
            }
        }
        Logger.getLogger().i(
                "Create notification - [" + topic.getTitle() + "] [" + topic.getNewComments() + "]");
        sentNotifications.add(topic.getTitle() + topic.getNewComments());
        PHPreferences.getInstance().getCachePreferences().edit().putStringSet(PH.Prefs.KEY_CACHE_NOTIFICATION_SENT, sentNotifications).apply();
    }

    private static PendingIntent createPendingIntentForAlarm() {
        return PendingIntent.getBroadcast(PHApplication.getInstance(), REQUEST_CODE_NOTIFICATION_CHECK,
                new Intent(PHApplication.getInstance(), PHReceiver.class).setAction(PH.Intent.ACTION_NOTIFICATION_CHECK), 0);
    }

    public static void updateAlarm() {
        AlarmManager alarmManager = (AlarmManager) PHApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        Logger.getLogger().i("Update alarm");
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                NotificationUtils.getRefreshInterval(), createPendingIntentForAlarm());
    }

    public static void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) PHApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        Logger.getLogger().i("Cancel alarm");
        alarmManager.cancel(createPendingIntentForAlarm());
    }

    public static void toggleNotificationWithDialog(Topic topic, Topic.Type type, HolderListTopic holder) {
        Context context = holder.itemView.getContext();
        Set<String> savedTopics = NotificationUtils.getNotifiableTopics(type);
        if (savedTopics.contains(topic.getTitle())) {
            new MaterialDialog.Builder(context)
                    .title("Értesítés kikapcsolása")
                    .content("Biztosan le szeretnél iratkozni a " + topic.getTitle() + " topik értesítéseiről?")
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onAny((dialog, which) -> {
                        switch (which) {
                            case POSITIVE:
                                savedTopics.remove(topic.getTitle());
                                setNotifiableTopics(type, savedTopics);
                                PH.getPHService().updateNotificationAlarm();
                                dialog.dismiss();
                                holder.getImageButtonNotification().setSelected(false);
                                break;
                        }
                    }).show();

        } else {
            new MaterialDialog.Builder(context)
                    .title("Értesítés bekapcsolása")
                    .content("Szeretnél értesítést kapni a " + topic.getTitle() + " topikról, ha új hozzászólás érkezik?")
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onAny((dialog, which) -> {
                        switch (which) {
                            case POSITIVE:
                                savedTopics.add(topic.getTitle());
                                setNotifiableTopics(type, savedTopics);
                                PH.getPHService().updateNotificationAlarm();
                                dialog.dismiss();
                                holder.getImageButtonNotification().setSelected(true);
                                break;
                        }
                    }).show();

        }
    }

    public static Set<String> getNotifiableTopics(Topic.Type type) {
        return PHPreferences.getInstance().getNotificationPreferences().getStringSet(Topic.Utils.getSavedNotifiableKey(type), new HashSet<>());
    }

    public static void setNotifiableTopics(Topic.Type type, Set<String> topicsSelected) {
        PHPreferences.getInstance().getNotificationPreferences().edit().putStringSet(Topic.Utils.getSavedNotifiableKey(type), topicsSelected).apply();
    }
}
