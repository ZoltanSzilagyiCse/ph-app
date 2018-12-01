package hu.sherad.hos.parser.modify;

import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.io.Logger;

public class RemoveCommentedTopicModifyParser extends TopicModifyParser {

    public RemoveCommentedTopicModifyParser(PHService.TopicModified topicModified, String givenURL) {
        super(topicModified, givenURL);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Document document = Jsoup.parse(response);
            Parser parser = Parser.parse(document);
            for (Topic commentedTopic : parser.getCommentedTopics()) {
                if (givenURL.equals(commentedTopic.getURL(Topic.UrlType.COMMENTED_DELETE))) {
                    exceptionMessage = "Sikertelen törlés";
                }
            }
        } catch (Exception e) {
            Logger.getLogger().e(e);
            exceptionMessage = PHApplication.getInstance().getString(R.string.error_occurred_try_again);
        }
        parseDone();
    }
}
