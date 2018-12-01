package hu.sherad.hos.ui.fragments.init;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.navigation.Navigation;
import hu.sherad.hos.BuildConfig;
import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHAuth;
import hu.sherad.hos.ui.activities.ActivityInit;
import hu.sherad.hos.utils.io.Logger;
import hu.sherad.hos.utils.io.PermissionManager;

public class FragmentInit extends Fragment {

    private FirebaseRemoteConfig remoteConfig;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initRemoteConfig();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.flow_fragment_init, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionManager.REQUEST_CODE_PERMISSIONS:
                ActivityInit activityInit = (ActivityInit) getActivity();
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(activityInit, "Nem sikerült a megfelelő jogokat megkapni", Toast.LENGTH_SHORT).show();
                        Objects.requireNonNull(activityInit).finish();
                        return;
                    }
                }
                fetchRemoteValues();
        }
    }

    private void checkPermissions() {
        Logger.getLogger().i("Checking permissions...");
        List<String> permissions = new ArrayList<>();
        if (!PermissionManager.isPermissionsGranted(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Logger.getLogger().i("Need " + Manifest.permission.WRITE_EXTERNAL_STORAGE + " permission");
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!PermissionManager.isPermissionsGranted(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Logger.getLogger().i("Need " + Manifest.permission.READ_EXTERNAL_STORAGE + " permission");
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[]{}), PermissionManager.REQUEST_CODE_PERMISSIONS);
            return;
        }
        Logger.getLogger().i("OK");

        fetchRemoteValues();
    }

    private void initRemoteConfig() {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        remoteConfig.setConfigSettings(configSettings);

        remoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    private void fetchRemoteValues() {
        // 12 hours
        long cacheExpiration = 3600 * 12;
        if (remoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        remoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (!task.isSuccessful()) {
                        checkIsLoggedIn();
                        return;
                    }
                    String oldRemoteInfo = remoteConfig.getString(PH.RemoteConfig.REMOTE_CONFIG_INFO);
                    remoteConfig.activateFetched();
                    int minAppVersion = (int) remoteConfig.getLong(PH.RemoteConfig.REMOTE_CONFIG_MIN_VERSION);

                    if (!isAppVersionOk(minAppVersion)) {
                        return;
                    }
                    String remoteInfo = remoteConfig.getString(PH.RemoteConfig.REMOTE_CONFIG_INFO);
                    checkNewInfoAvailable(oldRemoteInfo, remoteInfo);
                });
    }

    private void checkNewInfoAvailable(String oldRemoteInfo, String remoteInfo) {
        Logger.getLogger().i("Check new remote information is available...");
        if (remoteInfo.equals(oldRemoteInfo) || remoteInfo.isEmpty()) {
            checkIsLoggedIn();
        } else {
            new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                    .title("Információ")
                    .content(remoteInfo)
                    .cancelable(false)
                    .autoDismiss(false)
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> {
                        dialog.cancel();
                        checkIsLoggedIn();
                    }).show();
        }
    }

    private boolean isAppVersionOk(int minAppVersion) {
        Logger.getLogger().i("Min app version is " + minAppVersion + ", current app version is " + BuildConfig.VERSION_CODE);
        if (BuildConfig.VERSION_CODE < minAppVersion) {
            new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                    .title("Új verzió érhető el!")
                    .content("Kérlek frissítsd az alkalmazást, hogy használni tudd. Frissítés elérhető a Google Play-en, vagy az alkalmazás topikjában")
                    .cancelable(false)
                    .autoDismiss(false)
                    .positiveText("Frissítés")
                    .onPositive((dialog, which) -> openGooglePlay())
                    .show();
            return false;
        }
        return true;
    }

    private void openGooglePlay() {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        boolean marketFound = false;

        final List<ResolveInfo> otherApps = Objects.requireNonNull(getActivity()).getPackageManager().queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(otherAppActivity.applicationInfo.packageName, otherAppActivity.name);

                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                rateIntent.setComponent(componentName);
                getActivity().startActivity(rateIntent);
                marketFound = true;
                break;

            }

        }
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
            getActivity().startActivity(webIntent);
        }
    }

    public void checkIsLoggedIn() {
        // Fully logged in
        if (PHAuth.isFinishedLogin()) {
            ActivityInit activityInit = (ActivityInit) getActivity();
            PHAuth.isValidIdentifier((status, error) -> {
                // Identifier is OK, so start home activity
                if (status == PHAuth.StatusIdentifier.ACTIVE) {
                    Objects.requireNonNull(activityInit).startHomeActivity();
                } else if (status == PHAuth.StatusIdentifier.DEPRECATED) {
                    // Identifier deprecated, need to login again
                    Toast.makeText(getActivity(), "A bejelentkezésed lejárt", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.action_fragmentInit_to_fragmentLoginEntry);
                } else {
                    // Other error like no internet connection
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(activityInit).finish();
                }
            });
        } else {
            // Logged in, but the last section not completed (FragmentInitFinish)
            if (PHAuth.isLoggedIn()) {
                Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.action_fragmentInit_to_fragmentInitFinish);
            } else {
                // Not logged in, so need to login or register
                Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.action_fragmentInit_to_fragmentWelcome);
            }
        }

    }


}
