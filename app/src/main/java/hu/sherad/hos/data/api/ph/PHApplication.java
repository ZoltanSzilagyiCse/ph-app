package hu.sherad.hos.data.api.ph;

import android.app.Application;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import hu.sherad.hos.utils.io.Logger;


public class PHApplication extends Application {

    private static PHApplication phApplication;
    private RequestQueue requestQueue;

    public static PHApplication getInstance() {
        return phApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        phApplication = this;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request, String info) {
        Logger.getLogger().i(info);
        Logger.getLogger().i("Adding request to queue: " + request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(request);
    }

}
