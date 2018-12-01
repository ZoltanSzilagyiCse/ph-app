package hu.sherad.hos.parser;

import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.data.models.User;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.io.Logger;

public class Parser {

    private static final String ATTR_SRC = "src";
    private static final String ATTR_HREF = "href";

    private static final String CLASS_MSG_TINYMCE = "msg-tinymce";

    private static final String GROUP_TOPIC_ACTIONS = "span.thread-actions";
    private static final String GROUP_MESSAGE_ACTIONS = "div.col.thread-action";
    private static final String GROUP_USER_NAME = "div.text-center";
    private static final String GROUP_USER_DETAILS = "table.table";
    private static final String GROUP_CARD_HEADER = "div.card-header";
    private static final String GROUP_CARD_BODY = "div.card-body";
    private static final String GROUP_TOPICS = "li.list-group-item";
    private static final String GROUP_MESSAGES = "li.media";
    private static final String GROUP_FAVOURITE_TOPICS = "div.user-thread-list.user-thread-list-fav";
    private static final String GROUP_COMMENTED_TOPICS = "div.user-thread-list.user-thread-list-lms";
    private static final String GROUP_DIV_MSG_LIST = "div.msg-list";
    private static final String GROUP_DIV_MEDIA_BODY = "div.media-body";
    private static final String GROUP_THREAD_CONTENT = "thread-content";
    private static final String GROUP_LI_LIST_INLINE_ITEM = "li.list-inline-item";
    private static final String GROUP_LIST_UNSTYLED = "ul.list-unstyled";
    private static final String GROUP_CAPTCHA = "div.captcha-block";

    private static final String INPUT_TYPE_EMAIL = "input[type=email]";
    private static final String INPUT_NAME_REAL_NAME = "input[name=realname]";

    private static final String ITEM_TOPIC_ACTION_NEW_COMMENTS = "a.new-msgs";
    private static final String ITEM_TOPIC_ACTION_DELETE = "data-rios-action-thread-fav";
    private static final String ITEM_MESSAGE_ACTION_IGNORE = "span:contains(letilt)";
    private static final String ITEM_MESSAGE_ACTION_IGNORE_ATTR = "data-action-privconv-ignore";
    private static final String ITEM_MESSAGE_ACTION_DELETE = "span:contains(töröl)";
    private static final String ITEM_MESSAGE_ACTION_DELETE_ATTR = "data-action-privconv-del";
    private static final String ITEM_MESSAGE_ACTION_UNDO_IGNORE = "span:contains(tiltás feloldása)";
    private static final String ITEM_MESSAGE_ACTION_UNDO_IGNORE_ATTR = "data-action-privconv-unignore";
    private static final String ITEM_MESSAGE_TITLE = "div.col.thread-title-user";
    private static final String ITEM_MESSAGE_DATE = "div.col.thread-time.d-none.d-lg-block";
    private static final String ITEM_MESSAGE_AVATAR = "div.col.thread-face";
    private static final String ITEM_MESSAGE_NEW_COMMENTS = "div.col.thread-num-msgs.d-none.d-md-block";
    private static final String ITEM_IMG = "img";
    private static final String ITEM_A = "a";
    private static final String ITEM_DIV_MSG_USER = "div.msg-user";
    private static final String ITEM_SPAN_MSG_HEAD_AUTHOR = "span.msg-head-author";

    private static final String FORMAT_DATE = "yyyy-MM-dd hh:mm";
    private static final String FORMAT_DATE_PRECISION = "yyyy-MM-dd hh:mm:ss";

    private static final String TEXT_SIGNATURE = "Aláírás";
    private static final String TEXT_COMMENTS = "Hozzászólások";
    private static final String TEXT_REGISTERED = "Regisztrált";
    private static final String TEXT_LAST_VISITED = "Utoljára belépve";
    private static final String TEXT_ITEMS = "Cuccok";

    private final Document document;

    private Parser(Document document) {
        this.document = document;
        this.document.setBaseUri(PH.Api.HOST_PROHARDVER);
    }

    public static Parser parse(Document document) {
        return new Parser(document);
    }

    public static Parser parse(String response) {
        return new Parser(Jsoup.parse(response));
    }

    public boolean needToLogin() {
        Element center = document.body().selectFirst("div#center");
        // If the "Belépés szükséges" header is null, then the identifier is still active
        if (center.select("div:contains(Belépés szükséges)").isEmpty()) {
            Logger.getLogger().i("Identifier OK");
            return false;
        }
        return true;
    }

    public String getCaptchaURL() {
        return document.selectFirst(GROUP_CAPTCHA).selectFirst(ITEM_IMG).absUrl(ATTR_SRC);
    }

    public String getUserCredentialEmail() {
        return document.body().selectFirst(INPUT_TYPE_EMAIL).val();
    }

    public String getUserCredentialRealName() {
        return document.body().selectFirst(INPUT_NAME_REAL_NAME).val();
    }

    public String getUserCredentialAvatarURL() {
        Elements facesElements = document.body().selectFirst("ul.clearfix").select("li");
        for (Element face : facesElements) {
            if (!face.select("input").attr("checked").isEmpty()) {
                return face.selectFirst(ITEM_IMG).absUrl(ATTR_SRC);

            }
        }
        return null;
    }

    public List<Topic> getFavouriteTopics() {
        List<Topic> topics = new ArrayList<>();
        Element elementFavourites = document.selectFirst(GROUP_FAVOURITE_TOPICS);
        if (elementFavourites != null) {
            for (Element t : elementFavourites.select(GROUP_TOPICS)) {
                Element topicHeaderElement = t.selectFirst(ITEM_A);
                String topicURL = getTopicURL(topicHeaderElement);
                // Deleted / closed topic
                if (topicURL.isEmpty() || topicURL.startsWith(PH.Api.HOST_HARDVERAPRO)) {
                    continue;
                }
                Topic topic = new Topic();

                Element topicActionsElement = t.selectFirst(GROUP_TOPIC_ACTIONS);

                topic.setTitle(topicHeaderElement.text());

                topic.addURL(Topic.UrlType.TOPIC, HtmlUtils.changeExplicitToNew(topicURL));
                topic.addURL(Topic.UrlType.FAVOURITE_DELETE, getTopicDeleteURL(topicActionsElement));

                Element newCommentsElement = topicActionsElement.selectFirst(ITEM_TOPIC_ACTION_NEW_COMMENTS);
                topic.setNewComments(getNewComments(newCommentsElement));

                topics.add(topic);
            }
        }
        Topic.Utils.sortTopics(Topic.Type.FAVOURITE, topics);
        return topics;
    }

    public List<Topic> getCommentedTopics() {
        List<Topic> topics = new ArrayList<>();
        Element elementCommentedTopics = document.selectFirst(GROUP_COMMENTED_TOPICS);
        if (elementCommentedTopics != null) {
            for (Element t : elementCommentedTopics.select(GROUP_TOPICS)) {
                Element topicHeaderElement = t.selectFirst(ITEM_A);
                String topicURL = getTopicURL(topicHeaderElement);
                // Deleted / closed topic
                if (topicURL.isEmpty()) {
                    continue;
                }
                Topic topic = new Topic();
                Element topicActionsElement = t.selectFirst(GROUP_TOPIC_ACTIONS);

                topic.setTitle(topicHeaderElement.text());

                topic.addURL(Topic.UrlType.TOPIC, HtmlUtils.changeExplicitToNew(topicURL));
                topic.addURL(Topic.UrlType.COMMENTED_DELETE, getTopicDeleteURL(topicActionsElement));

                Element newCommentsElement = topicActionsElement.selectFirst(ITEM_TOPIC_ACTION_NEW_COMMENTS);
                topic.setNewComments(getNewComments(newCommentsElement));

                topics.add(topic);
            }
        }
        Topic.Utils.sortTopics(Topic.Type.COMMENTED, topics);
        return topics;
    }

    private String getTopicURL(Element topicHeaderElement) {
        return topicHeaderElement.absUrl(ATTR_HREF);
    }

    private String getTopicDeleteURL(Element topicActionsElement) {
        return topicActionsElement.getElementsByAttribute(ITEM_TOPIC_ACTION_DELETE).first().absUrl(ITEM_TOPIC_ACTION_DELETE);
    }

    private int getNewComments(Element newCommentsElement) {
        if (newCommentsElement != null) {
            String newComments = newCommentsElement.text().replaceAll("\\D+", "");
            return Integer.valueOf(newComments);
        }
        return 0;
    }

    public int getMaxPages() {
        int maxPageSize = 0;
        try {
            Element elementNavBar = document.body().selectFirst("ul.nav.navbar-nav");
            Element elementDropDown = elementNavBar.selectFirst("div.dropdown-menu");
            if (elementDropDown == null) {
                return 0;
            }
            Elements elementPages = elementDropDown.select(ITEM_A);

            for (Element elementPage : elementPages) {
                String text = elementPage.text();

                String[] textValues = text.split(" - ");
                int firstValue = Integer.valueOf(textValues[0]);
                int secondValue = Integer.valueOf(textValues[1]);

                int currentMaxValue = Math.max(firstValue, secondValue);
                if (maxPageSize < currentMaxValue) {
                    maxPageSize = currentMaxValue;
                }
            }
        } catch (Exception e) {
            return 0;
        }
        return maxPageSize;
    }

    public List<Topic> getTopics() {
        List<Topic> topics = new ArrayList<>();
        Elements trs = document.body().selectFirst(GROUP_LIST_UNSTYLED).children();
        boolean b = true;
        for (Element tr : trs) {
            if (b) {
                b = false;
                continue;
            }
            Topic topic = new Topic();
            Element elementTitle = tr.selectFirst("div.col.thread-title-thread");
            if (elementTitle != null) {
                topic.setTitle(elementTitle.text());
                topic.addURL(Topic.UrlType.TOPIC, HtmlUtils.changeExplicitToNew(elementTitle.selectFirst(ITEM_A).absUrl(ATTR_HREF)));
                topics.add(topic);
            }
        }
        return topics;
    }

    public List<Topic> getMessageTopics() {
        List<Topic> topics = new ArrayList<>();

        Elements messageGroupElements = document.select(GROUP_LIST_UNSTYLED);
        for (Element messageGroup : messageGroupElements) {

            Elements messageElements = messageGroup.select(GROUP_MESSAGES);
            boolean header = true;
            for (Element tr : messageElements) {
                if (header) {
                    header = false;
                    continue;
                }
                // Skip server messages
                if (tr.select(ITEM_MESSAGE_TITLE).isEmpty()) {
                    continue;
                }
                Topic topic = new Topic();
                Element messageTitle = tr.selectFirst(ITEM_MESSAGE_TITLE);
                // Title
                topic.setTitle(messageTitle.text());
                // EXTRA_URL
                topic.addURL(Topic.UrlType.TOPIC, messageTitle.selectFirst(ITEM_A).absUrl(ATTR_HREF));
                // New comments
                String subTitle = tr.selectFirst(ITEM_MESSAGE_NEW_COMMENTS).text();
                if (subTitle.contains("- db")) {
                    topic.setNewComments(0);
                } else {
                    topic.setNewComments(Integer.valueOf(subTitle.contains("db") ? subTitle.replaceAll("\\D+", "") : "0"));
                }
                // Avatar
                topic.addURL(Topic.UrlType.TOPIC_AVATAR,
                        tr.selectFirst(ITEM_MESSAGE_AVATAR).selectFirst(ITEM_IMG).absUrl(ATTR_SRC).replace("/small/", "/"));
                // Date
                try {
                    String dateText = tr.selectFirst(ITEM_MESSAGE_DATE).text();
                    if (!dateText.equals("-")) {
                        topic.setLastMessage(new SimpleDateFormat(FORMAT_DATE, Locale.getDefault()).parse(dateText));
                    }
                } catch (ParseException e) {
                    Logger.getLogger().e(e);
                }
                // Actions
                Element elementActions = tr.selectFirst(GROUP_MESSAGE_ACTIONS);
                for (Element action : elementActions.children()) {
                    if (action.selectFirst(ITEM_MESSAGE_ACTION_IGNORE) != null) {
                        topic.addURL(Topic.UrlType.MESSAGE_IGNORE, action.absUrl(ITEM_MESSAGE_ACTION_IGNORE_ATTR));
                    } else if (action.selectFirst(ITEM_MESSAGE_ACTION_DELETE) != null) {
                        topic.addURL(Topic.UrlType.MESSAGE_DELETE, action.absUrl(ITEM_MESSAGE_ACTION_DELETE_ATTR));
                    } else if (action.selectFirst(ITEM_MESSAGE_ACTION_UNDO_IGNORE) != null) {
                        topic.addURL(Topic.UrlType.MESSAGE_UN_IGNORE, action.absUrl(ITEM_MESSAGE_ACTION_UNDO_IGNORE_ATTR));
                    }
                }
                topics.add(topic);
            }
        }
        return topics;
    }

    public User getUser() {
        User user = new User();
        Element userPanel = document.body().selectFirst(GROUP_CARD_BODY);
        Element nameElement = userPanel.selectFirst(GROUP_USER_NAME);
        Element userDetailsElement = userPanel.selectFirst(GROUP_USER_DETAILS).selectFirst("tbody");
        // Name
        user.setName(nameElement.selectFirst("p").text());
        // Avatar link
        user.setAvatarLink(nameElement.selectFirst(ITEM_IMG).absUrl(ATTR_SRC));
        for (int i = 0; i < userDetailsElement.children().size(); i++) {
            Element child = userDetailsElement.child(i);
            // "Cuccok"
            if (child.text().startsWith(TEXT_ITEMS)) {
                user.setItems(userDetailsElement.child(i).text());
                continue;
            }
            // "Aláírás"
            if (child.text().startsWith(TEXT_SIGNATURE)) {
                user.setSignature(userDetailsElement.child(i).text().substring(TEXT_SIGNATURE.length()));
                continue;
            }
            // "Hozzászólások
            if (child.text().startsWith(TEXT_COMMENTS)) {
                Element comments = userDetailsElement.child(i);
                String[] commentsArray = comments.text().substring(TEXT_COMMENTS.length()).split(",");
                user.setCommentProfessional(Integer.valueOf(commentsArray[0].replaceAll("\\D+", "")));
                user.setCommentSocial(Integer.valueOf(commentsArray[1].replaceAll("\\D+", "")));
                user.setCommentMarket(Integer.valueOf(commentsArray[2].replaceAll("\\D+", "")));
                user.setBlogCount(Integer.valueOf(commentsArray[3].replaceAll("\\D+", "")));
                continue;
            }
            // "Regisztrált" - "Rang"
            if (child.text().startsWith(TEXT_REGISTERED)) {
                try {
                    String registered = userDetailsElement.child(i).text().substring(TEXT_REGISTERED.length());
                    String[] registeredInformation = registered.split(",");
                    user.setRegistered(new SimpleDateFormat(FORMAT_DATE, Locale.getDefault()).parse(registeredInformation[0]));
                    user.setRank(registeredInformation[1].split("-")[1].trim());
                } catch (Exception e) {
                    Logger.getLogger().e(e);
                    user.setRegistered(null);
                }
                continue;
            }
            // "Aktivitás"
            if (child.text().startsWith(TEXT_LAST_VISITED)) {
                try {
                    String lastVisited = userDetailsElement.child(i).text().substring(TEXT_LAST_VISITED.length());
                    user.setLastVisited(new SimpleDateFormat(FORMAT_DATE, Locale.getDefault()).parse(lastVisited.split(",")[0]));
                } catch (Exception e) {
                    Logger.getLogger().e(e);
                    user.setLastVisited(null);
                }
            }
        }

        user.setMessageLink(nameElement.selectFirst("a.btn.btn-secondary").absUrl(ATTR_HREF));

        return user;
    }

    @Nullable
    public TopicComment getTopicOverall() {
        TopicComment comment = null;
        Element element = document.body().selectFirst(GROUP_LIST_UNSTYLED);
        if (element != null) {
            comment = new TopicComment();
            // Head
            Element commentHead = element.selectFirst(GROUP_CARD_HEADER);
            // Body
            Element commentBody = element.selectFirst(GROUP_DIV_MEDIA_BODY);
            // Author properties
            if (commentHead.selectFirst(ITEM_SPAN_MSG_HEAD_AUTHOR) != null) {
                Element elementAuthor = commentHead.selectFirst(ITEM_SPAN_MSG_HEAD_AUTHOR);
                comment.setAuthorName(elementAuthor.select(ITEM_A).last().text());
            }
            try {
                comment.setAuthorAvatarURL(element.selectFirst(ITEM_DIV_MSG_USER).selectFirst(ITEM_IMG).absUrl(ATTR_SRC));
            } catch (Exception e) {
                comment.setAuthorAvatarURL("");
            }
            // Modify EXTRA_URL
            String editText = commentHead.selectFirst(GROUP_LI_LIST_INLINE_ITEM).text();
            if (editText.contains("Szerkesztés")) {
                comment.setEditURL(commentHead.selectFirst(GROUP_LI_LIST_INLINE_ITEM).selectFirst(ITEM_A).absUrl(ATTR_HREF));
            }
            // Date
            try {
                String dateText = commentHead.select(GROUP_LI_LIST_INLINE_ITEM).last().text();
                comment.setDate(new SimpleDateFormat(FORMAT_DATE_PRECISION, Locale.getDefault()).parse(dateText.substring(dateText.indexOf(":") + 1)));
            } catch (Exception e) {
                Logger.getLogger().e(e);
            }
            // Moderator properties
            // Content
            comment.setContent(TopicComment.Utils.getContentFromCommentElement(commentBody.children(), comment));
        }
        return comment;
    }

    public List<TopicComment> getComments() {
        List<TopicComment> comments = new ArrayList<>();
        Elements commentsList = document.body().select(GROUP_DIV_MSG_LIST);
        for (Element commentList : commentsList) {
            if (commentList.className().contains(GROUP_THREAD_CONTENT)) {
                continue;
            }
            for (Element msg : commentList.selectFirst("ul").children()) {
                if (isUnusedComment(msg)) {
                    continue;
                }
                TopicComment comment = TopicComment.Utils.getCommentFromHTML(msg);
                if (PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_OFF_COMMENT, false)) {
                    if (!comment.isOff()) {
                        comments.add(comment);
                    }
                } else {
                    comments.add(comment);
                }
            }
        }
        return comments;
    }

    public List<TopicComment> getMessages() {
        List<TopicComment> comments = new ArrayList<>();
        Elements commentsList = document.body().select(GROUP_DIV_MSG_LIST);
        for (Element commentList : commentsList) {
            for (Element msg : commentList.selectFirst("ul").children()) {
                if (isUnusedComment(msg)) {
                    continue;
                }
                TopicComment comment = TopicComment.Utils.getMessageFromHTML(msg);
                comments.add(comment);
            }
        }
        return comments;
    }

    private boolean isUnusedComment(Element msg) {
        return msg.className().isEmpty() || msg.className().contains(CLASS_MSG_TINYMCE) || msg.children().isEmpty();
    }
}
