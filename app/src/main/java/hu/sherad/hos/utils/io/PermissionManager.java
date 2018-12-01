package hu.sherad.hos.utils.io;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public final class PermissionManager {

    public static final int REQUEST_CODE_FOLDER_CHOOSING = 2;
    public static final int REQUEST_CODE_FILE_CHOOSING = 3;
    public static final int REQUEST_CODE_PERMISSIONS = 4;

    private PermissionManager() {

    }

    public static boolean isPermissionsGranted(Activity activity, String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    public static void requestPermissions(Activity activity, int requestCode, String... permissions) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }


}
