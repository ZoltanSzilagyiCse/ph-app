package hu.sherad.hos.utils;

import android.content.Context;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 * Utility methods for working with animations.
 */
public class AnimUtils {

    private static Interpolator linearOutSlowIn;

    private AnimUtils() {
    }

    public static Interpolator getLinearOutSlowInInterpolator(Context context) {
        if (linearOutSlowIn == null) {
            linearOutSlowIn = AnimationUtils.loadInterpolator(context, android.R.interpolator.linear_out_slow_in);
        }
        return linearOutSlowIn;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }


}
