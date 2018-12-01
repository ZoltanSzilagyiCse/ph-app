package hu.sherad.hos.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * See: https://github.com/koral--/android-gif-drawable/issues/368#issuecomment-269890617
 */
public class DrawableCallback implements Drawable.Callback {
    private WeakReference<TextView> mViewWeakReference;

    public DrawableCallback(TextView textView) {
        mViewWeakReference = new WeakReference<>(textView);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        if (mViewWeakReference.get() != null) {
            mViewWeakReference.get().invalidate();
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        if (mViewWeakReference.get() != null) {
            mViewWeakReference.get().postDelayed(what, when);
        }
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        if (mViewWeakReference.get() != null) {
            mViewWeakReference.get().removeCallbacks(what);
        }
    }
}