package hu.sherad.hos.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import hu.sherad.hos.R;
import hu.sherad.hos.utils.io.FileManager;
import hu.sherad.hos.utils.io.PermissionManager;

public class ActivitySettingsLoader extends Activity {

    public static final int REQUEST_CODE_ACTIVITY_LOADER = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PermissionManager.isPermissionsGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            createDialog();
        } else {
            PermissionManager.requestPermissions(this, REQUEST_CODE_ACTIVITY_LOADER, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACTIVITY_LOADER) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createDialog();
            } else {
                Toast.makeText(this, "Nem sikerült az írási/olvasási jogokat megkapni", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void createDialog() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            Toast.makeText(this, "Nem sikerült elérni a fájlt", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        new MaterialDialog.Builder(this)
                .title(R.string.restore_backup)
                .icon(ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round))
                .limitIconToDefaultSize()
                .autoDismiss(false)
                .content(R.string.restore_backup_content)
                .positiveText(R.string.yes)
                .negativeText(R.string.cancel)
                .onAny((dialog, which) -> {
                    switch (which) {
                        case POSITIVE:
                            boolean success = FileManager.loadSettings(this, uri);
                            if (success) {
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(), "Sikeres importálás", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Sikertelen importálás", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        default:
                            dialog.cancel();
                            break;

                    }
                })
                .dismissListener(dialog -> finish())
                .show();
    }

}
