package hu.sherad.hos.utils.keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;


public class ClipboardUtils {

    public static boolean copyToClipBoard(Context context, String label, String text, String toastText) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}
