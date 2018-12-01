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

public class MessageTopicDetailsParser extends TopicDetailsParser {

    public MessageTopicDetailsParser(TopicDetailed topicDetailed, TopicDetailed.OnFinishTopicDetailed onFinishTopicDetailed) {
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
            // Topic new comment EXTRA_URL
            // In the new design, there is no separate button to get the link, so we need to get it from one of the comments
            Elements commentsList = document.body().select("div.msg-list");
            for (Element commentList : commentsList) {
                // There is an "empty" comment
                for (Element msg : commentList.selectFirst("ul").children()) {
                    if (msg.className().isEmpty() || msg.className().contains("msg-tinymce") || msg.children().isEmpty()) {
                        continue;
                    }
                    topicDetailed.addURL(Topic.UrlType.TOPIC_NEW_COMMENT,
                            TopicComment.Utils.getMessageFromHTML(msg).getNewURL());
                    topicDetailed.setStatus(Topic.Status.OPEN);
                    break;
                }
            }
            // Current min - max comment
            int[] currentIndexes = HtmlUtils.getCurrentPages(null, document);
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
            topicDetailed.setData(PH.Data.ERROR_LOAD);
            parseDone();
        }
    }
}
