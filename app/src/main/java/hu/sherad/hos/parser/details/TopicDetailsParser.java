package hu.sherad.hos.parser.details;

import hu.sherad.hos.data.api.ph.PHResponse;
import hu.sherad.hos.data.models.TopicDetailed;

public abstract class TopicDetailsParser implements PHResponse<String> {

    final TopicDetailed topicDetailed;

    private final TopicDetailed.OnFinishTopicDetailed onFinishTopicDetailed;

    TopicDetailsParser(TopicDetailed topicDetailed, TopicDetailed.OnFinishTopicDetailed onFinishTopicDetailed) {
        this.topicDetailed = topicDetailed;
        this.onFinishTopicDetailed = onFinishTopicDetailed;
    }

    final void parseDone() {
        onFinishTopicDetailed.onResult(topicDetailed);
    }

}
