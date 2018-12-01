package hu.sherad.hos.utils.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import hu.sherad.hos.R;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.activities.ActivityImage;
import hu.sherad.hos.ui.activities.ActivityNewComment;
import hu.sherad.hos.ui.activities.ActivityUser;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.io.Logger;

@SuppressLint("ParcelCreator")
public class TouchableUrlSpan extends URLSpan {

    private static int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};
    private boolean isPressed;
    private int normalTextColor;
    private int pressedTextColor;
    private int pressedBackgroundColor;

    public TouchableUrlSpan(String url, ColorStateList textColor, int pressedBackgroundColor) {
        super(url);
        this.normalTextColor = textColor.getDefaultColor();
        this.pressedTextColor = textColor.getColorForState(STATE_PRESSED, normalTextColor);
        this.pressedBackgroundColor = pressedBackgroundColor;
    }

    public void setPressed(boolean isPressed) {
        this.isPressed = isPressed;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint drawState) {
        drawState.setColor(isPressed ? pressedTextColor : normalTextColor);
        drawState.bgColor = isPressed ? pressedBackgroundColor : 0;
        drawState.setUnderlineText(!isPressed);
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        Logger.getLogger().i("Opening: " + url);
        Context context = widget.getContext();
        if (HtmlUtils.isTopicLink(url)) {
            Topic topic = new Topic();
            topic.addURL(Topic.UrlType.TOPIC, HtmlUtils.changeDomainToPH(HtmlUtils.changeMobileLinkToDesktop(url)));
            Intent intent = ActivityComments.createIntent(topic);
            context.startActivity(intent);
            return;
        }
        if (HtmlUtils.isPHUserLink(url)) {
            Intent intent = new Intent(context, ActivityUser.class);
            intent.putExtra(ActivityNewComment.EXTRA_URL, HtmlUtils.changeDomainToPH(url));
            context.startActivity(intent, null);
            return;
        }
        if (url.contains("flickr.com")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
            return;
        }
        if (url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".gif")) {
            Intent intent = new Intent(context, ActivityImage.class);
            intent.putExtra(ActivityImage.EXTRA_IMG_URL, url);
            context.startActivity(intent);
            return;
        }
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(widget.getContext(), R.color.primary))
                    .addDefaultShareMenuItem()
                    .setInstantAppsEnabled(true)
                    .enableUrlBarHiding()
                    .setShowTitle(true)
                    .build();
            customTabsIntent.launchUrl(widget.getContext(), Uri.parse(url));
        } catch (Exception e) {
            Logger.getLogger().e(e);
            super.onClick(widget);
        }

    }

}