package hu.sherad.hos.ui.activities;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import hu.sherad.hos.R;
import hu.sherad.hos.utils.ViewUtils;
import hu.sherad.hos.utils.io.Logger;
import hu.sherad.hos.utils.keyboard.ClipboardUtils;

public class ActivityImage extends AppCompatActivity {

    public static final String EXTRA_IMG_URL = "EXTRA_IMG_URL";

    private static final String EXTENSION_GIF = ".gif";

    private boolean visible = true;
    private boolean loadedImage = false;

    private String photoURL;
    private RequestOptions requestOptions = ViewUtils.getDefaultGlideOptions(R.drawable.vd_image).fitCenter();
    private BroadcastReceiver receiver;

    private RelativeLayout parent;
    private AppBarLayout appBarLayout;
    private PhotoView photoView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleArguments();

        setContentView(R.layout.activity_image);

        assignLayoutElements();
        initActionBar();

        setupViews();

    }

    private void setupViews() {
        photoView.setOnClickListener(view1 -> toggleUI());
        loadImage(requestOptions);
    }

    private void loadImage(RequestOptions requestOptions) {
        if (isGif()) {
            Glide.with(this).asGif().load(photoURL).apply(requestOptions).listener(createGifRequest()).into(photoView);
        } else {
            Glide.with(this).load(photoURL).apply(requestOptions).listener(createRequest()).into(photoView);
        }
    }

    @NonNull
    private RequestListener<Drawable> createRequest() {
        return new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                handleLoadFailed();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                handleLoadSuccess();
                return false;
            }
        };
    }

    @NonNull
    private RequestListener<GifDrawable> createGifRequest() {
        return new RequestListener<GifDrawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                handleLoadFailed();
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                handleLoadSuccess();
                return false;
            }
        };
    }

    private void handleLoadFailed() {
        Toast.makeText(ActivityImage.this, "Sikertelen betöltés", Toast.LENGTH_SHORT).show();
    }

    private void handleLoadSuccess() {
        loadedImage = true;
        invalidateOptionsMenu();
    }

    private boolean isGif() {
        return photoURL.endsWith(EXTENSION_GIF);
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("");
        }
    }

    private void assignLayoutElements() {
        appBarLayout = findViewById(R.id.app_bar_layout_activity_image);
        toolbar = findViewById(R.id.toolbar_activity_image);
        photoView = findViewById(R.id.photo_view_activity_image);
        parent = findViewById(R.id.relative_layout_activity_image);
    }

    private void handleArguments() {
        Bundle arguments = getIntent().getExtras();
        if (arguments == null) {
            throw new IllegalArgumentException("Arguments cannot be null, need at leas an " + ActivityImage.EXTRA_IMG_URL);
        }
        photoURL = arguments.getString(ActivityImage.EXTRA_IMG_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        if (loadedImage) {
            menu.add(0, R.id.menu_image_save, 0, R.string.save);
        }
        menu.add(0, R.id.menu_image_url, 0, "Kép url másolása");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_image_save:
                saveImage();
                return true;
            case R.id.menu_image_url:
                ClipboardUtils.copyToClipBoard(this, photoURL, photoURL, "URL vágólapra másolva");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        unregisterFromReceiver();
        super.onPause();
    }

    private void unregisterFromReceiver() {
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
                Logger.getLogger().e(e);
            }
        }
    }

    private void saveImage() {
        createReceiverIfNeeded();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(photoURL));
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            Toast.makeText(this, "Sikertelen mentés", Toast.LENGTH_SHORT).show();
        } else {
            downloadManager.enqueue(request);
        }
    }

    private void createReceiverIfNeeded() {
        if (receiver == null) {
            receiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    Toast.makeText(context, "Letöltve", Toast.LENGTH_SHORT).show();
                }
            };
            registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    private void toggleUI() {
        if (visible) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    private void hideSystemUI() {
        visible = false;
        TransitionManager.beginDelayedTransition(parent);
        appBarLayout.setVisibility(View.GONE);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        visible = true;
        TransitionManager.beginDelayedTransition(parent);
        appBarLayout.setVisibility(View.VISIBLE);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}
