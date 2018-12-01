package hu.sherad.hos.parser.topic;

import android.support.annotation.NonNull;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.io.Logger;

public class SubTopicParser extends TopicParser {

    public SubTopicParser(TopicResult.OnTopicResult onTopicResult, int offset) {
        super(offset, "", onTopicResult);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Parser parser = Parser.parse(response);
            topicResult.setMaxTopicSize(parser.getMaxPages());
            topicResult.getTopics().addAll(parser.getTopics());
            topicResult.setData(offset + topicResult.getTopics().size() < topicResult.getMaxTopicSize() ? PH.Data.DATA_CAN_LOAD : PH.Data.OK);
        } catch (Exception e) {
            Logger.getLogger().e(e);
            topicResult.setData(PH.Data.ERROR_LOAD);
        }
        parseDone();
    }
}
