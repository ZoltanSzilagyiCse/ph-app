package hu.sherad.hos.data.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import hu.sherad.hos.data.api.ph.PH;

public class TopicDetailed extends Topic implements Serializable {

    private final List<String> moderators = new ArrayList<>();

    private int currentMinPosition;
    private int currentMaxPosition;
    private int topicSize;
    private int commentsSize;
    private boolean commentsIncrement;

    private Topic.Status status = Topic.Status.CLOSED;
    private PH.Data data = PH.Data.OK;

    public boolean isModerator(String moderator) {
        return moderators.contains(moderator);
    }

    public void addModerator(String moderator) {
        moderators.add(moderator);
    }

    public int getCurrentMinPosition() {
        return currentMinPosition;
    }

    public void setCurrentMinPosition(int currentMinPosition) {
        this.currentMinPosition = currentMinPosition;
    }

    public int getCurrentMaxPosition() {
        return currentMaxPosition;
    }

    public void setCurrentMaxPosition(int currentMaxPosition) {
        this.currentMaxPosition = currentMaxPosition;
    }

    public int getTopicSize() {
        return topicSize;
    }

    public void setTopicSize(int topicSize) {
        this.topicSize = topicSize;
    }

    public int getCommentsSize() {
        return commentsSize;
    }

    public void setCommentsSize(int commentsSize) {
        this.commentsSize = commentsSize;
    }

    public PH.Data getData() {
        return data;
    }

    public void setData(PH.Data data) {
        this.data = data;
    }

    public Topic.Status getStatus() {
        return status;
    }

    public void setStatus(Topic.Status status) {
        this.status = status;
    }

    public boolean isCommentsIncrementing() {
        return commentsIncrement;
    }

    public void setCommentsIncrement(boolean commentsIncrement) {
        this.commentsIncrement = commentsIncrement;
    }

    public void setTopic(Topic topic) {
        setTitle(topic.getTitle());
        setLastMessage(topic.getLastMessage());
        setNewComments(topic.getNewComments());
        for (UrlType urlType : topic.getUrlMap().keySet()) {
            getUrlMap().put(urlType, topic.getUrlMap().get(urlType));
        }
    }

    public interface OnFinishTopicDetailed {
        void onResult(TopicDetailed topicDetailed);
    }
}
