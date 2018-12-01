package hu.sherad.hos.data.api.ph;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.sherad.hos.data.api.user.UserTopics;
import hu.sherad.hos.data.models.StatusHttpRequest;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.data.models.TopicDetailed;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.parser.captcha.CaptchaParser;
import hu.sherad.hos.parser.comment.CommentParser;
import hu.sherad.hos.parser.comment.EditCommentParser;
import hu.sherad.hos.parser.comment.EditMessageCommentParser;
import hu.sherad.hos.parser.comment.SendCommentCommentParser;
import hu.sherad.hos.parser.comment.SendMessageCommentParser;
import hu.sherad.hos.parser.comment.SendPrivateMessageCommentParser;
import hu.sherad.hos.parser.comment.TopicOverallCommentParser;
import hu.sherad.hos.parser.comments.CommentsAnswerListParser;
import hu.sherad.hos.parser.comments.CommentsParser;
import hu.sherad.hos.parser.comments.CommentsResponseParser;
import hu.sherad.hos.parser.comments.MessagesParser;
import hu.sherad.hos.parser.details.DefaultTopicDetailsParser;
import hu.sherad.hos.parser.details.MessageTopicDetailsParser;
import hu.sherad.hos.parser.details.TopicDetailsParser;
import hu.sherad.hos.parser.modify.AddFavouriteTopicModifyParser;
import hu.sherad.hos.parser.modify.RemoveCommentedTopicModifyParser;
import hu.sherad.hos.parser.modify.RemoveFavouriteTopicModifyParser;
import hu.sherad.hos.parser.modify.TopicModifyParser;
import hu.sherad.hos.parser.topic.DefaultTopicParser;
import hu.sherad.hos.parser.topic.HotTopicParser;
import hu.sherad.hos.parser.topic.MessageTopicParser;
import hu.sherad.hos.parser.topic.NewsTopicParser;
import hu.sherad.hos.parser.topic.SubTopicParser;
import hu.sherad.hos.parser.topic.TopicParser;
import hu.sherad.hos.parser.topic.UserInfoTopicParser;
import hu.sherad.hos.parser.user.DefaultUserDataParser;
import hu.sherad.hos.parser.user.UpdateUserDataParser;
import hu.sherad.hos.parser.user.UserDataParser;
import hu.sherad.hos.utils.NetworkUtils;
import hu.sherad.hos.utils.NotificationUtils;
import hu.sherad.hos.utils.io.Logger;

final public class PHService {


    PHService() {
    }

    /* FAVOURITES */

    public void addToFavourites(@NonNull String addURL, @NonNull TopicModified topicModified) {
        TopicModifyParser parser = new AddFavouriteTopicModifyParser(topicModified, addURL);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, addURL, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Add to favourites");
    }

    public void deleteFromFavourites(@NonNull String deleteURL, @NonNull TopicModified topicModified) {
        TopicModifyParser parser = new RemoveFavouriteTopicModifyParser(topicModified, deleteURL);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, deleteURL, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Delete from favourites");
    }

    /* MESSAGES */

    public void getMessages(int offset, @NonNull TopicResult.OnTopicResult onTopicResult) {
        PHResponse<String> parser = new MessageTopicParser(onTopicResult, offset);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, PH.Api.URL_MESSAGES + "?offset=" + offset, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get messages");
    }

    public void getMessageDetails(@NonNull TopicDetailed topicDetailed, @NonNull TopicDetailed.OnFinishTopicDetailed onFinishTopicDetailed) {
        TopicDetailsParser parser = new MessageTopicDetailsParser(topicDetailed, onFinishTopicDetailed);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, topicDetailed.getURL(Topic.UrlType.TOPIC), true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get message details");
    }

    public void getMessages(String url, @NonNull TopicComments topicComments) {
        CommentsResponseParser parser = new MessagesParser(topicComments);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, url, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get message");
    }

    public void sendMessagePrivate(String url, String message, SingleComment singleComment) {
        getMessageDSTID(url, (status, data) -> {
            if (status == StatusHttpRequest.FAILED) {
                singleComment.onSingleComment(null, "Sikertelen küldés");
                return;
            }
            Map<String, String> params = Collections.singletonMap("content", TopicComment.Utils.getContentToPublish(message));
            CommentParser parser = new SendPrivateMessageCommentParser(singleComment);
            StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, data, true, params, parser);
            PHApplication.getInstance().addToRequestQueue(stringRequest, "Send private message");
        });
    }

    private void getMessageDSTID(String url, FinishLoading finishLoading) {
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, url, true,
                response -> {
                    try {
                        Document document = Jsoup.parse(response);
                        Element form = document.selectFirst("form.form");
                        finishLoading.done(StatusHttpRequest.SUCCESS, PH.Api.HOST_PROHARDVER + form.attr("action"));
                    } catch (Exception e) {
                        Logger.getLogger().e(e);
                        finishLoading.done(StatusHttpRequest.FAILED, "");
                    }
                }
        );
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get private message destination ID");
    }

    public void sendMessage(String url, String message, SingleComment singleComment) {
        Map<String, String> params = Collections.singletonMap("content", TopicComment.Utils.getContentToPublish(message));
        CommentParser parser = new SendMessageCommentParser(singleComment, url);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, url, true, params, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Send message");
    }

    public void editMessage(String editURL, String content, SingleComment singleComment) {
        Map<String, String> params = Collections.singletonMap("content", TopicComment.Utils.getContentToPublish(content));
        CommentParser parser = new EditMessageCommentParser(singleComment, editURL);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, editURL, true, params, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Edit message");
    }

    /* COMMENTED */

    public void deleteFromCommentedTopics(@NonNull String deleteURL, @NonNull TopicModified topicModified) {
        TopicModifyParser parser = new RemoveCommentedTopicModifyParser(topicModified, deleteURL);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, deleteURL, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Delete commented topic");
    }

    /* TOPIC */

    public void getTopics(@NonNull TopicResult.OnTopicResult onTopicResult) {
        TopicParser parser = new DefaultTopicParser(onTopicResult);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, PH.Api.URL_TOPICS, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get topics");
    }

    public void getSubTopics(String url, int offset, @NonNull TopicResult.OnTopicResult onTopicResult) {
        TopicParser parser = new SubTopicParser(onTopicResult, offset);
        String finalURL = url + "?offset=" + offset;
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, finalURL, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get subtopics");
    }

    public void getSubTopicsSearch(String encodedURL, int offset, @NonNull TopicResult.OnTopicResult onTopicResult) {
        TopicParser parser = new SubTopicParser(onTopicResult, offset);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, encodedURL, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get subtopics search");
    }

    public void getHotTopics(@NonNull TopicResult.OnTopicResult onTopicResult) {
        TopicParser parser = new HotTopicParser(onTopicResult);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, PH.Api.URL_HOT_TOPICS, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get hot topics");
    }

    public void getTopicDetails(TopicDetailed topicDetailed, @NonNull TopicDetailed.OnFinishTopicDetailed onFinishTopicDetailed) {
        TopicDetailsParser parser = new DefaultTopicDetailsParser(topicDetailed, onFinishTopicDetailed);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, topicDetailed.getURL(Topic.UrlType.TOPIC), true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get topic details");
    }

    public void getComments(String url, TopicComments topicComments) {
        CommentsResponseParser parser = new CommentsParser(topicComments);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, url, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get comments");
    }

    public void sendComment(String url, String message, boolean onTopic, SingleComment singleComment) {
        Map<String, String> params = new HashMap<>();
        params.put("content", TopicComment.Utils.getContentToPublish(message));
        params.put("offtopic", onTopic ? "0" : "1");
        CommentParser parser = new SendCommentCommentParser(singleComment, url);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, url, true, params, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Send comment");
    }

    public void editComment(String defaultURL, String editURL, String content, boolean onTopic, SingleComment singleComment) {
        Map<String, String> params = new HashMap<>();
        params.put("content", TopicComment.Utils.getContentToPublish(content));
        params.put("offtopic", onTopic ? "0" : "1");
        CommentParser parser = new EditCommentParser(singleComment, defaultURL);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, editURL, true, params, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Edit comment");
    }

    public void getTopicOverall(String topicOverallURL, SingleComment singleComment) {
        CommentParser parser = new TopicOverallCommentParser(singleComment, topicOverallURL);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, topicOverallURL, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get topic overall");
    }

    public void getCommentAnswerList(List<TopicComment> currentComments, @NonNull TopicComments topicComments) {
        CommentsResponseParser parser = new CommentsAnswerListParser(topicComments, currentComments);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, currentComments.get(currentComments.size() - 1).getID_URL_quoted(), true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get comment answer list");
    }

    /* NOTIFICATION JOBS */

    /**
     * Need to wait some time, until the shared preferences finish the write
     */
    public void updateNotificationAlarm() {
        Logger.getLogger().i("Update notification alarm");
        new Handler().postDelayed(() -> {
            if (NotificationUtils.shouldRunNotificationService()) {
                NotificationUtils.updateAlarm();
            } else {
                NotificationUtils.cancelAlarm();
            }
        }, 1000);
    }

    public void getUserTopics(@NonNull UserTopics.OnUpdate onUpdate) {
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, PH.Api.URL_MESSAGES, true,
                response -> {
                    Parser parser = Parser.parse(response);
                    List<Topic> messageTopics = parser.getMessageTopics();
                    UserTopics.getInstance()
                            .update(Topic.Type.FAVOURITE, parser.getFavouriteTopics())
                            .update(Topic.Type.COMMENTED, parser.getCommentedTopics())
                            .update(Topic.Type.MESSAGE, messageTopics)
                            .setData(PH.Data.OK, messageTopics.size() < parser.getMaxPages() ? PH.Data.DATA_CAN_LOAD : PH.Data.OK)
                            .sendBroadcast();
                    onUpdate.updateDone();
                }
        );
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get user topics");
    }

    /* OTHER */

    public void getNews(String domain, int offset, @NonNull TopicResult.OnTopicResult onTopicResult) {
        String url = domain + "?ajax=1&page=" + offset;
        TopicParser parser = new NewsTopicParser(onTopicResult, domain);
        StringRequest stringRequest = NetworkUtils.createStringRequestWithOldHeaders(Request.Method.GET, url, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get news");
    }

    public void getUserInfo(String url, @NonNull TopicResult.OnTopicResult onTopicResult) {
        TopicParser parser = new UserInfoTopicParser(onTopicResult);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, url, false, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get user info");
    }

    public void getUserData(FinishLoading finishLoading) {
        UserDataParser parser = new DefaultUserDataParser(finishLoading);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.GET, PH.Api.URL_USER_DATA, true, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get user Data");
    }

    public void setUserData(HashMap<String, String> map, FinishLoading finishLoading) {
        SharedPreferences sharedPreferencesUserCredentials = PHPreferences.getInstance().getUserCredentialsPreferences();
        String url = PH.Api.URL_MODIFY_USER_DATA + "?json=1&url=%2Ffiok%2Fadatlap.php";
        Map<String, String> params = new HashMap<>();
        // Avatar
        String avatarLink = sharedPreferencesUserCredentials.getString(PH.Prefs.KEY_USER_CREDENTIALS_AVATAR_URL, "");
        params.put("face", avatarLink.substring(avatarLink.lastIndexOf("/") + 1, avatarLink.lastIndexOf(".")));
        // Name
        if (map.containsKey(PH.Prefs.KEY_USER_CREDENTIALS_NAME)) {
            params.put("realname", map.get(PH.Prefs.KEY_USER_CREDENTIALS_NAME));
        }
        UserDataParser parser = new UpdateUserDataParser(finishLoading, params);
        StringRequest stringRequest = NetworkUtils.createStringRequest(Request.Method.POST, url, true, params, parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Set user Data");
    }

    public void getCaptcha(PHCaptcha phCaptcha) {
        CaptchaParser parser = new CaptchaParser(phCaptcha);
        JsonObjectRequest stringRequest = NetworkUtils.createStringRequestForCaptcha(parser);
        PHApplication.getInstance().addToRequestQueue(stringRequest, "Get captcha image url");
    }

    /* LISTENERS */

    public interface PHCaptcha {

        void onResult(@NonNull StatusHttpRequest status, @NonNull String url);
    }

    public interface SingleComment {
        void onSingleComment(TopicComment comment, @NonNull String error);
    }

    public interface TopicModified {

        void onFinish(@NonNull String error, @NonNull String link);
    }

    public interface FinishLoading {
        void done(StatusHttpRequest status, @NonNull String data);
    }

    public interface TopicComments {
        void onTopicComments(List<TopicComment> comments, @NonNull PH.Data data);
    }

}
