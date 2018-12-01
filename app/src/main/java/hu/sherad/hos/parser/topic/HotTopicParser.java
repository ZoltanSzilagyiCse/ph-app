package hu.sherad.hos.parser.topic;

import android.support.annotation.NonNull;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.io.Logger;

public class HotTopicParser extends TopicParser {

    public HotTopicParser(TopicResult.OnTopicResult onTopicResult) {
        super(0, "", onTopicResult);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Parser parser = Parser.parse(response);
            topicResult.getTopics().addAll(parser.getTopics());
            topicResult.setData(PH.Data.OK);
        } catch (Exception e) {
            Logger.getLogger().e(e);
            topicResult.setData(PH.Data.ERROR_LOAD);
        }
        parseDone();
    }
}
