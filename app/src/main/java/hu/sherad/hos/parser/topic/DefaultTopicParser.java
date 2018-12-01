package hu.sherad.hos.parser.topic;

import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.utils.io.Logger;

public class DefaultTopicParser extends TopicParser {

    public DefaultTopicParser(TopicResult.OnTopicResult onTopicResult) {
        super(0, "", onTopicResult);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Document document = Jsoup.parse(response);
            topicResult.getTopics().addAll(Topic.Utils.getMainTopics(document));
        } catch (Exception e) {
            Logger.getLogger().e(e);
            topicResult.setData(PH.Data.ERROR_LOAD);
        }
        parseDone();
    }


}
