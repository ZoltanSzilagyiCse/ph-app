package hu.sherad.hos.utils;


import android.support.annotation.DrawableRes;

import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;

/**
 * Utility methods for working with Views.
 */
public class ViewUtils {


    private ViewUtils() {
    }

    public static RequestOptions getDefaultGlideOptions(@DrawableRes int placeholderRes) {
        return new RequestOptions()
                .placeholder(placeholderRes)
                .priority(Priority.NORMAL);
    }
}
