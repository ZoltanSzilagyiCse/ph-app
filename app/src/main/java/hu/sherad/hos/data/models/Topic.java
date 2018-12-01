package hu.sherad.hos.data.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;

public class Topic implements Serializable {

    private final Map<UrlType, String> urlMap = new HashMap<>();

    private String title;
    private Date lastMessage;
    private int newComments;

    public String getURL(UrlType urlType) {
        return urlMap.get(urlType);
    }

    public void addURL(UrlType urlType, String url) {
        urlMap.put(urlType, url);
    }

    public boolean containsURL(UrlType urlType) {
        return urlMap.containsKey(urlType);
    }

    public void removeURL(UrlType urlType) {
        urlMap.remove(urlType);
    }

    public Date getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Date lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        if (this.title == null || this.title.isEmpty()) {
            this.title = title;
        }
    }

    public int getNewComments() {
        return newComments;
    }

    public void setNewComments(int newComments) {
        this.newComments = newComments;
    }

    Map<UrlType, String> getUrlMap() {
        return urlMap;
    }

    public enum Priority {
        DEFAULT,
        HIGH
    }

    public enum Status {
        OPEN,
        CLOSED
    }

    public enum Type {
        DEFAULT,
        FAVOURITE,
        COMMENTED,
        MESSAGE
    }

    public enum UrlType {
        FAVOURITE_DELETE,       // Favourites delete
        FAVOURITE_ADD,          // Favourites add
        COMMENTED_DELETE,       // Commented delete
        MESSAGE_DELETE,         // Messages delete user
        MESSAGE_IGNORE,         // Messages ignore user
        MESSAGE_UN_IGNORE,      // Messages undo ignore user
        TOPIC_NEW_COMMENT,      // New comment
        TOPIC_AVATAR,           // Messages profile avatar
        TOPIC                   // Topic main EXTRA_URL
    }

    public static final class Utils {

        private static String getSavedSortKey(Topic.Type type) {
            switch (type) {
                case FAVOURITE:
                    return PH.Prefs.KEY_DEFAULT_FAVOURITES_TOPICS_SORT;
                case COMMENTED:
                    return PH.Prefs.KEY_DEFAULT_COMMENTED_TOPICS_SORT;
                default:
                    throw new RuntimeException("No type such [" + type.name() + "]");
            }
        }

        public static String getSavedNotifiableKey(Topic.Type type) {
            switch (type) {
                case FAVOURITE:
                    return PH.Prefs.KEY_NOTIFICATION_TOPICS_FAVOURITE;
                case COMMENTED:
                    return PH.Prefs.KEY_NOTIFICATION_TOPICS_COMMENTED;
                default:
                    throw new RuntimeException("No type such [" + type.name() + "]");
            }
        }

        public static void sortTopics(Topic.Type type, List<Topic> topics) {
            // Load the saved topic order
            String stringSavedTopics = PHPreferences.getInstance().getDefaultPreferences().getString(getSavedSortKey(type), null);
            if (stringSavedTopics == null) {
                return;
            }

            List<Topic> sortedTopics = new ArrayList<>();
            List<String> savedTopics = new Gson().fromJson(stringSavedTopics, new TypeToken<List<String>>() {
            }.getType());

            // Add the saved topics to the list
            for (String savedTopic : savedTopics) {
                for (Topic topic : topics) {
                    if (savedTopic.equals(topic.getTitle())) {
                        sortedTopics.add(topic);
                        break;
                    }
                }

            }
            // Add the new topics to the list
            for (Topic topic : topics) {
                if (!sortedTopics.contains(topic)) {
                    sortedTopics.add(0, topic);
                }
            }
            topics.clear();
            topics.addAll(sortedTopics);
        }

        public static List<String> getTitleFromTopics(List<Topic> topics) {
            List<String> titles = new ArrayList<>();
            for (Topic topic : topics) {
                titles.add(topic.getTitle());
            }
            return titles;
        }

        public static List<Topic> getMainTopics(@NonNull Document document) {
            List<Topic> topics = new ArrayList<>();
            Elements trs = document.body().selectFirst("ul.list-unstyled").children();
            for (Element tr : trs) {
                Topic topic = new Topic();
                if (tr.className().contains("group")) {
                    topic.setTitle(tr.getElementsByClass("col forum-heading").first().text());
                } else {
                    topic.setTitle(tr.selectFirst("a").text());
                    topic.addURL(UrlType.TOPIC, PH.Api.HOST_PROHARDVER + tr.selectFirst("a").attr("href"));
                }
                topics.add(topic);

            }
            return topics;
        }

        public static int getNewTopicsCount(@Nullable List<Topic> topics) {
            if (topics == null) {
                return 0;
            }
            int result = 0;
            for (Topic topic : topics) {
                if (topic.getNewComments() > 0) {
                    result++;
                }
            }
            return result;
        }
    }

}
