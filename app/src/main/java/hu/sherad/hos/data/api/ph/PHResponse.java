package hu.sherad.hos.data.api.ph;

import android.support.annotation.NonNull;

public interface PHResponse<T> {
    void onResponse(@NonNull T response);
}
