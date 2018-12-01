package hu.sherad.hos.parser.modify;

import hu.sherad.hos.data.api.ph.PHResponse;
import hu.sherad.hos.data.api.ph.PHService;

public abstract class TopicModifyParser implements PHResponse<String> {

    final String givenURL;
    private final PHService.TopicModified topicModified;
    String exceptionMessage = "";
    String expectedURL = "";

    TopicModifyParser(PHService.TopicModified topicModified, String givenURL) {
        this.topicModified = topicModified;
        this.givenURL = givenURL;
    }

    final void parseDone() {
        topicModified.onFinish(exceptionMessage, expectedURL);
    }

}
