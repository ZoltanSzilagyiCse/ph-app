package hu.sherad.hos.utils.keyboard;


import android.content.Context;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Method;

import hu.sherad.hos.utils.io.Logger;

/**
 * Utility methods for working with the keyboard
 */
public class ImeUtils {

    private ImeUtils() {
    }

    public static void showIme(@NonNull View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            Method showSoftInputUnchecked = InputMethodManager.class.getMethod("showSoftInputUnchecked", int.class, ResultReceiver.class);
            showSoftInputUnchecked.setAccessible(true);
            showSoftInputUnchecked.invoke(inputMethodManager, 0, null);
        } catch (Exception e) {
            Logger.getLogger().e(e);
        }
    }

    public static void hideIme(@NonNull View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}

