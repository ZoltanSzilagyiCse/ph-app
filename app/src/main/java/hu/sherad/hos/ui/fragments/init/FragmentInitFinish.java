package hu.sherad.hos.ui.fragments.init;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Objects;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHAuth;
import hu.sherad.hos.data.models.StatusHttpRequest;
import hu.sherad.hos.ui.activities.ActivityInit;
import hu.sherad.hos.utils.io.FileManager;
import hu.sherad.hos.utils.io.PermissionManager;
import hu.sherad.hos.utils.io.SettingsUtil;

public class FragmentInitFinish extends Fragment {

    private MaterialDialog progressDialog;

    private TextView textViewTheme;
    private TextView textViewStartFragment;
    private Switch switchInfoCollecting;
    private Switch switchAutoScroll;
    private Button buttonDone;
    private Button buttonImport;
    private View viewTheme;
    private View viewStartFragment;
    private View viewAutoScroll;
    private View viewInformationCollecting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                .content(R.string.loading)
                .progress(true, 0)
                .cancelable(false)
                .build();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.flow_fragment_init_finish, container, false);


        textViewTheme = root.findViewById(R.id.textView_login_finish_theme);
        textViewStartFragment = root.findViewById(R.id.textView_login_finish_start_fragment);
        switchInfoCollecting = root.findViewById(R.id.switch_login_finish_information_collecting);
        switchAutoScroll = root.findViewById(R.id.switch_login_finish_auto_scroll);
        buttonDone = root.findViewById(R.id.button_login_finish_done);
        buttonImport = root.findViewById(R.id.button_login_finish_import_settings);

        viewTheme = root.findViewById(R.id.view_login_finish_theme);
        viewStartFragment = root.findViewById(R.id.view_login_start_fragment);
        viewAutoScroll = root.findViewById(R.id.view_login_finish_auto_scroll);
        viewInformationCollecting = root.findViewById(R.id.view_login_finish_information_collecting);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewTheme.setOnClickListener(v ->
                SettingsUtil.selectTheme(Objects.requireNonNull(getContext()), selectedThemeIndex ->
                        textViewTheme.setText(SettingsUtil.getThemeByIndex(selectedThemeIndex))));
        viewStartFragment.setOnClickListener(v ->
                SettingsUtil.selectStartFragment(Objects.requireNonNull(getContext()), selectedStartFragmentIndex ->
                        textViewStartFragment.setText(SettingsUtil.getStartFragmentByIndex(selectedStartFragmentIndex))));

        viewInformationCollecting.setOnClickListener(v ->
                switchInfoCollecting.setChecked(!switchInfoCollecting.isChecked()));
        viewAutoScroll.setOnClickListener(v ->
                switchAutoScroll.setChecked(!switchAutoScroll.isChecked()));

        buttonDone.setOnClickListener(v -> updateUserData());
        buttonImport.setOnClickListener(v ->
                FileManager.chooseFile(this, PermissionManager.REQUEST_CODE_FILE_CHOOSING));

        textViewTheme.setText(SettingsUtil.getThemeByIndex(SettingsUtil.getSavedTheme()));
        textViewStartFragment.setText(SettingsUtil.getStartFragmentByIndex(SettingsUtil.getSavedStartFragment()));
        switchInfoCollecting.setChecked(SettingsUtil.isAnalyticsEnabled());
        switchAutoScroll.setChecked(SettingsUtil.isAutoScrollEnabled());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PermissionManager.REQUEST_CODE_FILE_CHOOSING:
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

    private void importSettings(@NonNull Uri uri) {
        if (FileManager.loadSettings(getContext(), uri)) {
            Toast.makeText(getContext(), "Sikeres importálás", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Sikertelen importálás", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateUserData() {
        progressDialog.show();
        PH.getPHService().getUserTopics(() -> {
            PH.getPHService().getUserData((status, data) -> {
                progressDialog.cancel();
                if (status == StatusHttpRequest.FAILED) {
                    Toast.makeText(getContext(), "Nem sikerült minden adatot lekérni", Toast.LENGTH_SHORT).show();
                }
                startHomeActivity();
            });
        });
    }

    private void startHomeActivity() {
        PHAuth.finishLogin();
        SettingsUtil.setAutoScrollEnabled(switchAutoScroll.isChecked());
        SettingsUtil.setAnalyticsEnabled(switchInfoCollecting.isChecked());
        ((ActivityInit) Objects.requireNonNull(getContext())).startHomeActivity();
    }


}
