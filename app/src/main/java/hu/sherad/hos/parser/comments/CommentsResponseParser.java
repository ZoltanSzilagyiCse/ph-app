package hu.sherad.hos.parser.comments;

import java.util.ArrayList;
import java.util.List;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.ph.PHResponse;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.TopicComment;

public abstract class CommentsResponseParser implements PHResponse<String> {

    final PHService.TopicComments topicComments;
    final List<TopicComment> comments;

    CommentsResponseParser(PHService.TopicComments topicComments, List<TopicComment> comments) {
        this.topicComments = topicComments;
        this.comments = comments;
    }

    CommentsResponseParser(PHService.TopicComments topicComments) {
        this(topicComments, new ArrayList<>());
    }

    final void parseDone(PH.Data data) {
        topicComments.onTopicComments(comments, data);
    }

    protected final boolean isListInc() {
        return PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_LIST_INC, true);
    }

}
