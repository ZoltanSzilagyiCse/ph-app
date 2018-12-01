package hu.sherad.hos.parser.topic;

import hu.sherad.hos.data.api.ph.PHResponse;
import hu.sherad.hos.data.models.TopicResult;

public abstract class TopicParser implements PHResponse<String> {

    final int offset;
    final String domain;
    final TopicResult topicResult;

    private final TopicResult.OnTopicResult onTopicResult;

    TopicParser(int offset, String domain, TopicResult.OnTopicResult onTopicResult) {
        this.offset = offset;
        this.domain = domain;
        this.onTopicResult = onTopicResult;
        topicResult = new TopicResult();
    }

    final void parseDone() {
        onTopicResult.onResult(topicResult);
    }

}
