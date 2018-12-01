package hu.sherad.hos.ui.fragments.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.HashMap;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.models.StatusHttpRequest;
import hu.sherad.hos.utils.io.Logger;

public class SettingsUserCredentials extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private Preference preferencePassword;
    private Preference preferenceName;
    private Preference preferenceEmail;
    private Preference preferenceAvatarURL;

    private MaterialDialog progressDialog;
    private SharedPreferences sharedPreferencesCredentials;

    private boolean decryptedPassword = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PH.Prefs.PREF_USER_CREDENTIALS);

        sharedPreferencesCredentials = PHPreferences.getInstance().getUserCredentialsPreferences();

        progressDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.loading)
                .progress(true, 0)
                .cancelable(false)
                .build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.preferences_user_credentials);

        preferencePassword = findPreference(PH.Prefs.KEY_USER_CREDENTIALS_PASSWORD);
        preferenceName = findPreference(PH.Prefs.KEY_USER_CREDENTIALS_NAME);
        preferenceEmail = findPreference(PH.Prefs.KEY_USER_CREDENTIALS_EMAIL);
        preferenceAvatarURL = findPreference(PH.Prefs.KEY_USER_CREDENTIALS_AVATAR_URL);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preferenceName.setSummary(sharedPreferencesCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_NAME, ""));
        preferencePassword.setSummary(sharedPreferencesCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_PASSWORD, ""));
        preferenceEmail.setSummary(sharedPreferencesCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_EMAIL, ""));
        preferenceAvatarURL.setSummary(sharedPreferencesCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_AVATAR_URL, ""));

        preferenceName.setOnPreferenceClickListener(this);
        preferencePassword.setOnPreferenceClickListener(this);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PH.Prefs.KEY_USER_CREDENTIALS_PASSWORD:
                passwordClick();
                return true;
            case PH.Prefs.KEY_USER_CREDENTIALS_NAME:
                nameClick();
                return true;
        }
        return false;
    }

    private void nameClick() {
        new MaterialDialog.Builder(getActivity())
                .autoDismiss(false)
                .title("Név változtatás")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(0, 30)
                .input(getString(R.string.name), preferenceName.getSummary(), (dialog, input) -> {
                    progressDialog.show();
                    HashMap<String, String> map = new HashMap<>();
                    map.put(PH.Prefs.KEY_USER_CREDENTIALS_NAME, input.toString());
                    PH.getPHService().setUserData(map, (status, data) -> {
                        progressDialog.cancel();
                        if (status == StatusHttpRequest.SUCCESS) {
                            preferenceName.setSummary(input.toString());
                            dialog.hide();
                            Toast.makeText(getActivity(), "Sikeres név módosítás", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PHApplication.getInstance(), data, Toast.LENGTH_SHORT).show();
                        }
                    });
                }).show();
    }

    private void passwordClick() {
        if (decryptedPassword) {
            preferencePassword.setSummary(sharedPreferencesCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_PASSWORD, ""));
            decryptedPassword = false;
        } else {
            new MaterialDialog.Builder(getActivity())
                    .title("Jelszó megjelenítése")
                    .content("A jelszó titkosítva van, de ha szeretnéd megjeleníteni, akkor kattints az Megjelenítés gombra.\n\nEz nem fogja a titkosítást feloldani, csak megjeleníti a jelszót!")
                    .positiveText("Megjelenítés")
                    .negativeText(R.string.cancel)
                    .onAny((dialog, which) -> {
                        if (which == DialogAction.POSITIVE) {
                            try {
                                preferencePassword.setSummary(AESCrypt.decrypt("PaQhpEPLSGy4tu7x", sharedPreferencesCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_PASSWORD, "")));
                                decryptedPassword = true;
                            } catch (GeneralSecurityException e) {
                                Logger.getLogger().e(e);
                                Toast.makeText(getActivity(), "Sikertelen visszafejtés", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .show();
        }
    }
}
