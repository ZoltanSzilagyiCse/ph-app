package hu.sherad.hos.utils;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.CookieSettings;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.utils.io.Logger;
import hu.sherad.hos.utils.keyboard.LinkTouchMovementMethod;
import hu.sherad.hos.utils.keyboard.TouchableUrlSpan;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Utility methods for working with HTML.
 */
public class HtmlUtils {

    public static Map<String, String> createHeaders(boolean addUserCookie) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Accept-Language", "en-GB,en;q=0.9,hu-HU;q=0.8,hu;q=0.7,en-US;q=0.6");
        headers.put("Connection", "keep-alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Host", "fototrend.hu");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");
        headers.put("Origin", "https://fototrend.hu");
        headers.put("X-Requested-With", "XMLHttpRequest");
        if (addUserCookie) {
            headers.put("Cookie", CookieSettings.createCookies());
        }
        return headers;
    }

    public static Map<String, String> createHeadersOld() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "keep-alive");
        headers.put("User-agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        headers.put("Accept-Language", "hu-HU,hu;q=0.8,en-US;q=0.6,en;q=0.4");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        return headers;
    }

    public static String changeNewToExplicit(String url, int from, int to) {
        return url.substring(0, url.lastIndexOf("/")) + "/hsz_" + from + "-" + to + ".html";
    }

    public static boolean isTopicLink(String link) {
        return link.contains("https://prohardver.hu/tema/") || link.contains("https://m.prohardver.hu/tema/") ||
                link.contains("https://mobilarena.hu/tema/") || link.contains("https://m.mobilarena.hu/tema/") ||
                link.contains("https://itcafe.hu/tema/") || link.contains("https://m.itcafe.hu/tema/") ||
                link.contains("https://gamepod.hu/tema/") || link.contains("https://m.gamepod.hu/tema/") ||
                link.contains("https://logout.hu/tema/") || link.contains("https://m.logout.hu/tema/") ||
                link.contains("https://fototrend.hu/tema/");
    }

    public static boolean isPHUserLink(String link) {
        return link.contains("https://prohardver.hu/tag/") || link.contains("https://m.prohardver.hu/tag/") ||
                link.contains("https://mobilarena.hu/tag/") || link.contains("https://m.mobilarena.hu/tag/") ||
                link.contains("https://itcafe.hu/tag/") || link.contains("https://m.itcafe.hu/tag/") ||
                link.contains("https://gamepod.hu/tag/") || link.contains("https://m.gamepod.hu/tag/") ||
                link.contains("https://logout.hu/tag/") || link.contains("https://m.logout.hu/tag/") ||
                link.contains("https://fototrend.hu/tag/");
    }

    public static boolean isTopicOverallLink(String link) {
        return link.endsWith("/index.html");
    }

    public static boolean isTopicMessageLink(String link) {
        return changeDomainToPH(link).startsWith("https://fototrend.hu/privat/");
    }

    @Nullable
    public static int[] isExplicitCommentLink(String url) {
        int index = url.lastIndexOf("/hsz_");
        if (index == -1) {
            return null;
        }
        int lastIndex = url.indexOf(".html");
        String sub = url.substring(index + 5, lastIndex);
        String[] fromTo = sub.split("-");
        int[] integers = new int[fromTo.length];
        for (int i = 0; i < fromTo.length; i++) {
            integers[i] = Integer.valueOf(fromTo[i]);
        }
        return integers;
    }

    public static String changeMessageURLToExplicit(String url, int from) {
        return url.substring(0, url.lastIndexOf("?")) + "?offset=" + from;
    }

    public static String changeExplicitToNew(String url) {
        return url.substring(0, url.lastIndexOf("/")) + "/friss.html";
    }

    public static String changeMobileLinkToDesktop(String link) {
        return link.replace("https://m.", "https://");
    }

    public static String changeDomainToPH(String link) {
        return link.replace(link.substring(0, link.indexOf(".hu/")), "https://fototrend");
    }

    public static String getTopicOverallURL(String topicURL) {
        return topicURL.substring(0, topicURL.lastIndexOf("/")) + "/index.html";
    }

    public static String getImageUrlToNews(String url, String imgURL) {
        return url.replace("/index.html", "") + imgURL;
    }

    public static PH.Data setDataFromError(VolleyError error) {
        if (error instanceof NetworkError) {
            return PH.Data.ERROR_NO_INTERNET;
        } else if (error instanceof ServerError) {
            return PH.Data.ERROR_SERVER;
        } else if (error instanceof AuthFailureError) {
            return PH.Data.ERROR_INTERNET_FAILURE;
        } else if (error instanceof TimeoutError) {
            return PH.Data.ERROR_TIMEOUT;
        } else {
            return PH.Data.ERROR_LOAD;
        }
    }

    public static String createSearchURL(String textAll, String textExac, String textSome, String textNone, String textUser, int offset) throws Exception {
        return PH.Api.URL_TOPICS_SEARCH +
                "?" + "type=" + "threads" +
                "&" + "stext_all=" + URLEncoder.encode(textAll, "UTF-8") +
                "&" + "stext_exac=" + URLEncoder.encode(textExac, "UTF-8") +
                "&" + "stext_some=" + URLEncoder.encode(textSome, "UTF-8") +
                "&" + "stext_none=" + URLEncoder.encode(textNone, "UTF-8") +
                "&" + "suser=" + URLEncoder.encode(textUser, "UTF-8") +
                "&" + "sforid=" +
                "&" + "submit=" + "%C2%A0" +
                "&" + "offset=" + offset;
    }

    public static String createSearchURL(String text, int offset) throws Exception {
        return PH.Api.URL_TOPICS_SEARCH +
                "?" + "stext=" + URLEncoder.encode(text, "UTF-8") +
                "&" + "offset=" + offset;
    }

    public static int[] getCurrentPages(@Nullable String url, Document document) {
        try {
            Element sizes = document.body().selectFirst("ul.nav.navbar-nav").selectFirst("a.dropdown-item.active");
            String[] strings = sizes.text().split(" - ");
            int first = Integer.valueOf(strings[0]);
            int last = Integer.valueOf(strings[1]);
            return first > last ? new int[]{last, first} : new int[]{first, last};
        } catch (Exception e) {
            if (url == null) {
                Logger.getLogger().e(e);
            } else {
                Logger.getLogger().i(url);
                Logger.getLogger().e(e);
            }
            return null;
        }
    }

    public static Map<String, String> getAllSmileCharacterFromURL() {
        Map<String, String> list = new HashMap<>();
        list.put("/dl/s/n1.gif", " :) ");
        list.put("/dl/s/n2.gif", " :)) ");
        list.put("/dl/s/d1.gif", " :D ");
        list.put("/dl/s/d2.gif", " :DD ");
        list.put("/dl/s/d3.gif", " :DDD ");
        list.put("/dl/s/nn.gif", " :N ");
        list.put("/dl/s/m1.gif", " :( ");
        list.put("/dl/s/m2.gif", " :(( ");
        list.put("/dl/s/m3.gif", " :((( ");
        list.put("/dl/s/os.gif", " :o ");
        list.put("/dl/s/ol.gif", " :O ");
        list.put("/dl/s/v1.gif", " ;] ");
        list.put("/dl/s/ts.gif", " :P ");
        list.put("/dl/s/pl.gif", " ;) ");
        list.put("/dl/s/bl.gif", " :B ");
        list.put("/dl/s/ye.gif", " :K ");
        list.put("/dl/s/ul.gif", " :U ");
        list.put("/dl/s/cl.gif", " :C ");
        list.put("/dl/s/fl.gif", " :F ");
        list.put("/dl/s/yk.gif", " :Y ");
        list.put("/dl/s/rl.gif", " :R ");
        list.put("/dl/s/wb.gif", " :W ");
        return list;
    }

    public static int getCommentsSize(Document document) {
        String dropDownSelected = document.body().selectFirst("div#forum-nav-top.navbar.navbar-default.pager-navbar").selectFirst("a:contains(db /)").text();
        // 25 db / csökkenő
        return Integer.valueOf(dropDownSelected.substring(0, dropDownSelected.indexOf("db") - 1));
    }

    public static boolean isCommentsIncrement(Document document) {
        String dropDownSelected = document.body().selectFirst("div#forum-nav-top.navbar.navbar-default.pager-navbar").selectFirst("a:contains(db /)").text();
        // 25 db / csökkenő
        return dropDownSelected.contains("növekvő");
    }

    public static void setTextWithNiceLinks(TextView textView, CharSequence input) {
        textView.setText(input);
        textView.setMovementMethod(LinkTouchMovementMethod.getInstance());
    }

    public static String getFirstReadableCharacter(String string) {
        char[] abcBig = {'A', 'Á', 'B', 'C', 'D', 'E', 'É', 'F', 'G', 'H', 'I', 'Í', 'J', 'K', 'L', 'M', 'N', 'O', 'Ó', 'Ö', 'Ő', 'P', 'Q', 'R', 'S', 'T', 'U', 'Ú', 'Ü', 'Ű', 'V', 'W', 'X', 'Y', 'Z'};
        for (int i = string.startsWith("[") ? string.lastIndexOf("]") : 0; i < string.length(); i++) {
            for (char c : abcBig) {
                if (string.toUpperCase().charAt(i) == c) {
                    return String.valueOf(c);
                }
            }
        }
        return "";
    }

    public static SpannableStringBuilder parseHtml(String input, ColorStateList linkTextColor, @ColorInt int linkHighlightColor, Context context, Drawable.Callback callback) {
        SpannableStringBuilder spanned = fromHtml(input, context, callback);
        while (spanned.charAt(spanned.length() - 1) == '\n') {
            spanned = spanned.delete(spanned.length() - 1, spanned.length());
        }
        return linkifyPlainLinks(spanned, linkTextColor, linkHighlightColor);
    }

    private static SpannableStringBuilder fromHtml(String input, Context context, Drawable.Callback callback) {

        Html.ImageGetter imageGetter = s -> {
            if (TopicComment.Utils.allSmiles.containsKey(s)) {
                try {
                    GifDrawable result = new GifDrawable(context.getResources(), TopicComment.Utils.allSmiles.get(s));
                    result.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());
                    result.setCallback(callback);
                    result.start();
                    return result;
                } catch (IOException e) {
                    Logger.getLogger().e(e);
                }
            }
            return null;
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (SpannableStringBuilder) Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
        } else {
            return (SpannableStringBuilder) Html.fromHtml(input, imageGetter, null);
        }
    }

    private static SpannableStringBuilder linkifyPlainLinks(CharSequence input, ColorStateList linkTextColor, @ColorInt int linkHighlightColor) {

        final SpannableString plainLinks = new SpannableString(input);
        final URLSpan[] urlSpans = plainLinks.getSpans(0, plainLinks.length(), URLSpan.class);
        final SpannableStringBuilder builder = new SpannableStringBuilder(input);

        for (URLSpan urlSpan : urlSpans) {
            builder.removeSpan(urlSpan);
            builder.setSpan(new TouchableUrlSpan(urlSpan.getURL(), linkTextColor, linkHighlightColor),
                    plainLinks.getSpanStart(urlSpan),
                    plainLinks.getSpanEnd(urlSpan),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }
}
