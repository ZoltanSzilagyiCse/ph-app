package hu.sherad.hos.ui.fragments.settings;

import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.user.UserTopics;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.utils.NotificationUtils;

public class SettingsNotification extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private SharedPreferences sharedPreferences;
    private SwitchPreference preferenceNotificationMessages;
    private SwitchPreference preferenceNotificationMobileData;
    private RingtonePreference preferenceNotificationSound;
    private Preference preferenceNotificationTopicsFavourite;
    private Preference preferenceNotificationTopicsCommented;
    private Preference preferenceNotificationRefresh;

    private CharSequence[] refreshValues;
    private int selectedRefresh;
    private String ringtoneUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PH.Prefs.PREF_NOTIFICATION);

        refreshValues = new CharSequence[]{
                getString(R.string.one_minute),
                getString(R.string.five_minutes),
                getString(R.string.ten_minutes),
                getString(R.string.thirty_minutes),
                getString(R.string.one_hour)
        };
        sharedPreferences = PHPreferences.getInstance().getNotificationPreferences();
        selectedRefresh = sharedPreferences.getInt(PH.Prefs.KEY_NOTIFICATION_REFRESH, 2);
        ringtoneUri = sharedPreferences.getString(PH.Prefs.KEY_NOTIFICATION_SOUND, "");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.preferences_notification);

        preferenceNotificationMessages = (SwitchPreference) findPreference(PH.Prefs.KEY_NOTIFICATION_SWITCH_MESSAGES);
        preferenceNotificationSound = (RingtonePreference) findPreference(PH.Prefs.KEY_NOTIFICATION_SOUND);
        preferenceNotificationTopicsFavourite = findPreference(PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE);
        preferenceNotificationTopicsCommented = findPreference(PH.Prefs.KEY_NOTIFICATION_TOPICS_COMMENTED);
        preferenceNotificationRefresh = findPreference(PH.Prefs.KEY_NOTIFICATION_REFRESH);
        preferenceNotificationMobileData = (SwitchPreference) findPreference(PH.Prefs.KEY_NOTIFICATION_SWITCH_MOBILE_DATA);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preferenceNotificationTopicsFavourite.setOnPreferenceClickListener(this);
        preferenceNotificationTopicsCommented.setOnPreferenceClickListener(this);
        preferenceNotificationRefresh.setOnPreferenceClickListener(this);

        preferenceNotificationMessages.setOnPreferenceChangeListener(this);
        preferenceNotificationSound.setOnPreferenceChangeListener(this);
        preferenceNotificationRefresh.setOnPreferenceChangeListener(this);
        preferenceNotificationMobileData.setOnPreferenceChangeListener(this);

        preferenceNotificationRefresh.setSummary(refreshValues[selectedRefresh]);
        preferenceNotificationSound.setSummary(ringtoneUri.isEmpty() ? "Egyik sem" : RingtoneManager.getRingtone(getActivity(), Uri.parse(ringtoneUri)).getTitle(getActivity()));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        switch (preference.getKey()) {
            case PH.Prefs.KEY_NOTIFICATION_SWITCH_MESSAGES:
            case PH.Prefs.KEY_NOTIFICATION_SWITCH_MOBILE_DATA:
                PH.getPHService().updateNotificationAlarm();
                return true;
            case PH.Prefs.KEY_NOTIFICATION_SOUND:
                ringtoneUri = (String) o;
                preferenceNotificationSound.setSummary(ringtoneUri.isEmpty() ? "Egyik sem" : RingtoneManager.getRingtone(getActivity(), Uri.parse(ringtoneUri)).getTitle(getActivity()));
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE:
                selectTopicsForNotificationByTopicType(Topic.Type.FAVOURITE);
                return true;
            case PH.Prefs.KEY_NOTIFICATION_TOPICS_COMMENTED:
                selectTopicsForNotificationByTopicType(Topic.Type.COMMENTED);
                return true;
            case PH.Prefs.KEY_NOTIFICATION_REFRESH:
                selectRefresh();
                return true;
        }
        return false;
    }

    private void selectTopicsForNotificationByTopicType(Topic.Type type) {
        if (UserTopics.getInstance().isNeedToUpdate()) {
            MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.loading)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .cancelable(false)
                    .build();
            progressDialog.show();
            PH.getPHService().getUserTopics(() -> {
                progressDialog.cancel();
                if (UserTopics.getInstance().getData() == PH.Data.OK) {
                    createDialogNotificationByTopicType(type);
                } else {
                    Toast.makeText(getActivity(), PH.getErrorFromCode(UserTopics.getInstance().getData()), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            createDialogNotificationByTopicType(type);
        }
    }

    private void createDialogNotificationByTopicType(Topic.Type type) {
        List<String> topicsSortedCurrent = Topic.Utils.getTitleFromTopics(UserTopics.getInstance().getTopics(type));
        Set<String> topicsSaved = NotificationUtils.getNotifiableTopics(type);
        List<Integer> integers = new ArrayList<>();

        for (String topicCurrent : topicsSortedCurrent) {
            if (topicsSaved.contains(topicCurrent)) {
                integers.add(topicsSortedCurrent.indexOf(topicCurrent));
            }
        }
        new MaterialDialog.Builder(getActivity())
                .items(topicsSortedCurrent)
                .itemsCallbackMultiChoice(integers.toArray(new Integer[integers.size()]), (dialog, which, text) -> {
                    Set<String> topicsSelected = new HashSet<>();
                    for (CharSequence charSequence : text) {
                        topicsSelected.add(charSequence.toString());
                    }
                    NotificationUtils.setNotifiableTopics(type, topicsSelected);
                    PH.getPHService().updateNotificationAlarm();
                    return true;
                })
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .canceledOnTouchOutside(false)
                .show();
    }

    private void selectRefresh() {
        new MaterialDialog.Builder(getActivity())
                .title("Frissítés gyakorisága")
                .items(refreshValues)
                .itemsCallbackSingleChoice(selectedRefresh, (dialog, itemView, which, text) -> {
                    selectedRefresh = which;
                    return true;
                })
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onAny((dialog, which) -> {
                    switch (which) {
                        case POSITIVE:
                            sharedPreferences.edit().putInt(PH.Prefs.KEY_NOTIFICATION_REFRESH, selectedRefresh).apply();
                            preferenceNotificationRefresh.setSummary(refreshValues[selectedRefresh]);
                            PH.getPHService().updateNotificationAlarm();
                            break;
                    }
                })
                .show();
    }

}