package hu.sherad.hos.parser.details;

import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.data.models.TopicDetailed;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.io.Logger;

public class DefaultTopicDetailsParser extends TopicDetailsParser {

    public DefaultTopicDetailsParser(TopicDetailed topicDetailed, TopicDetailed.OnFinishTopicDetailed onFinishTopicDetailed) {
        super(topicDetailed, onFinishTopicDetailed);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Document document = Jsoup.parse(response);
            // Topic title
            String title = document.title();
            title = title.substring(0, title.lastIndexOf('-') - 1);
            topicDetailed.setTitle(title);
            topicDetailed.setCommentsSize(HtmlUtils.getCommentsSize(document));
            topicDetailed.setCommentsIncrement(HtmlUtils.isCommentsIncrement(document));
            // Topic save to favourites EXTRA_URL
            Element saveElement = document.body().select("a.btn.btn-primary.btn-block").last();
            if (saveElement != null && !saveElement.className().contains("disabled")) {
                topicDetailed.addURL(Topic.UrlType.FAVOURITE_ADD,
                        PH.Api.HOST_PROHARDVER + saveElement.selectFirst("a").attr("data-rios-action-thread-fav"));
            }
            // Topic delete from favourites EXTRA_URL
            if (document.selectFirst("ul.list-group.user-thread-list-fav") != null) {
                for (Element t : document.selectFirst("ul.list-group.user-thread-list-fav").select("li.list-group-item")) {
                    if (topicDetailed.getTitle().equals(t.select("a").first().text())) {
                        topicDetailed.addURL(Topic.UrlType.FAVOURITE_DELETE, PH.Api.HOST_PROHARDVER +
                                t.selectFirst("span.far.fa-trash-alt.fa-fw").parent().attr("data-rios-action-thread-fav"));
                        break;
                    }
                }
            }
            // Topic new comment EXTRA_URL
            // In the new design, there is no separate button to get the link, so we need to get it from one of the comments
            Elements commentsList = document.body().select("div.msg-list");
            for (Element commentList : commentsList) {
                // Avoid from the "Topik összefoglaló"
                // Also there is an "empty" comment
                if (commentList.className().contains("thread-content")) {
                    continue;
                }
                for (Element msg : commentList.selectFirst("ul").children()) {
                    if (msg.className().isEmpty() || msg.className().contains("msg-tinymce") || msg.children().isEmpty()) {
                        continue;
                    }
                    topicDetailed.addURL(Topic.UrlType.TOPIC_NEW_COMMENT,
                            TopicComment.Utils.getCommentFromHTML(msg).getNewURL());
                    topicDetailed.setStatus(Topic.Status.OPEN);
                    break;
                }
            }
            // Set the moderators
            Element moderators = document.body().selectFirst("div.card.thread-users-list");
            if (moderators != null) {
                moderators = moderators.select("ul").first();
                if (moderators != null) {
                    for (int i = 0; i < moderators.select("li").size() - 1; i++) {
                        topicDetailed.addModerator(moderators.select("li").get(i).text());
                    }
                }
            }
            // Current min - max comment
            int[] currentIndexes = HtmlUtils.getCurrentPages(topicDetailed.getURL(Topic.UrlType.TOPIC), document);
            if (currentIndexes == null) {
                topicDetailed.setData(PH.Data.ERROR_INTERNAL);
                parseDone();
                return;
            }
            topicDetailed.setCurrentMinPosition(currentIndexes[0]);
            topicDetailed.setCurrentMaxPosition(currentIndexes[1]);
            topicDetailed.setTopicSize(Parser.parse(document).getMaxPages());
            // Set data to OK
            topicDetailed.setData(PH.Data.OK);
            // Finish
            parseDone();
        } catch (Exception e) {
            Logger.getLogger().e(e);
            topicDetailed.setData(PH.Data.ERROR_INTERNAL);
            parseDone();
        }
    }
}
