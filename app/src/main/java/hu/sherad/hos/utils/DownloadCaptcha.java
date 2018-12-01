package hu.sherad.hos.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import hu.sherad.hos.utils.io.Logger;

public class DownloadCaptcha extends AsyncTask<String, Void, Bitmap> {

    private DownloadCaptcha.LoadCaptcha loadCaptcha;
    private Exception exception;

    public DownloadCaptcha(DownloadCaptcha.LoadCaptcha loadCaptcha) {
        this.loadCaptcha = loadCaptcha;
    }

    protected Bitmap doInBackground(String... urls) {
        try {
            Logger.getLogger().i("Load captcha: " + urls[0]);
            URL url = new URL(urls[0]);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            connection.setRequestProperty("Accept-Language", "en-GB,en;q=0.9,hu-HU;q=0.8,hu;q=0.7,en-US;q=0.6,fr;q=0.5");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Host", "fototrend.hu");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            InputStream inputStream = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } catch (Exception e) {
            exception = e;
            return null;
        }
    }

    protected void onPostExecute(Bitmap result) {
        loadCaptcha.onLoadedCaptcha(result, exception);
    }

    public interface LoadCaptcha {
        void onLoadedCaptcha(@Nullable Bitmap bitmap, Exception e);
    }
}