package hu.sherad.hos.parser.comments;

import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.utils.io.Logger;

public class CommentsAnswerListParser extends CommentsResponseParser {

    public CommentsAnswerListParser(PHService.TopicComments topicComments, List<TopicComment> comments) {
        super(topicComments, comments);
    }

    @Override
    public void onResponse(@NonNull String response) {
        PH.Data data = PH.Data.OK;
        TopicComment comment = null;
        try {
            Document document = Jsoup.parse(response);
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
                    comment = TopicComment.Utils.getCommentFromHTML(msg);
                }
            }
            comments.add(comment);
        } catch (Exception e) {
            Logger.getLogger().e(e);
            data = PH.Data.ERROR_LOAD;
        }
        if (comment != null && comment.getID_URL_quoted().isEmpty()) {
            TopicComment.Utils.sort(comments, isListInc());
            parseDone(data);
        } else {
            PH.getPHService().getCommentAnswerList(comments, topicComments);
        }
    }
}
