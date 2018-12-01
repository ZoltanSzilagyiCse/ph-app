package hu.sherad.hos.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.utils.NotificationUtils;

public class PHReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_PACKAGE_REPLACED:
                case PH.Intent.ACTION_NOTIFICATION_CHECK:
                    if (NotificationUtils.shouldRunNotificationService()) {
                        NotificationUtils.doCheck();
                    }
                    break;
            }
        }
    }
}