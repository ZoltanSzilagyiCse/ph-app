package hu.sherad.hos.ui.fragments.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.utils.io.FileManager;

import static android.app.Activity.RESULT_OK;
import static hu.sherad.hos.ui.activities.ActivitySettings.RESTART_APP;

public class SettingsOther extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final int REQUEST_CODE_FOLDER_CHOOSING = 1;
    private static final int REQUEST_CODE_FILE_CHOOSING = 2;

    private Preference preferenceExport;
    private Preference preferenceImport;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PH.Prefs.PREF_OTHER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.preferences_other);

        preferenceExport = findPreference(PH.Prefs.KEY_OTHER_SETTINGS_EXPORT);
        preferenceImport = findPreference(PH.Prefs.KEY_OTHER_SETTINGS_IMPORT);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preferenceExport.setOnPreferenceClickListener(this);
        preferenceImport.setOnPreferenceClickListener(this);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PH.Prefs.KEY_OTHER_SETTINGS_EXPORT:
                chooseFolder();
                return true;
            case PH.Prefs.KEY_OTHER_SETTINGS_IMPORT:
                chooseFile();
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FOLDER_CHOOSING:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri == null) {
                        return;
                    }
                    exportSettings(uri);
                }
                break;
            case REQUEST_CODE_FILE_CHOOSING:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri == null) {
                        return;
                    }
                    importSettings(uri);
                }
                break;
        }

    }


    public void chooseFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(intent, "Choose directory"), REQUEST_CODE_FOLDER_CHOOSING);
    }

    public void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream");
        startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSING);
    }

    public void exportSettings(Uri uri) {
        String result = FileManager.saveSettings(getActivity(), uri);
        Toast.makeText(getActivity(), result == null ? "Hiba történt exportálás során" : "Sikeres exportálás", Toast.LENGTH_SHORT).show();
    }

    public void importSettings(Uri uri) {
        boolean success = FileManager.loadSettings(getActivity(), uri);
        if (success) {
            Toast.makeText(getActivity().getApplicationContext(), "Sikeres importálás, újraindítás", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra(RESTART_APP, true);
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();
        } else {
            Toast.makeText(getActivity(), "Sikertelen importálás", Toast.LENGTH_SHORT).show();
        }
    }

}