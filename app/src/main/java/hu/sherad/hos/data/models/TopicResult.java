package hu.sherad.hos.data.models;


import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import hu.sherad.hos.data.api.ph.PH;

public class TopicResult implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<TopicRear> topicRears = null;
    private List<Topic> topics = null;
    private User user;
    @NonNull
    private PH.Data data = PH.Data.OK;
    private int maxTopicSize;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getMaxTopicSize() {
        return maxTopicSize;
    }

    public void setMaxTopicSize(int maxTopicSize) {
        this.maxTopicSize = maxTopicSize;
    }

    public List<TopicRear> getTopicRears() {
        if (topicRears == null) {
            topicRears = new ArrayList<>();
        }
        return topicRears;
    }

    public List<Topic> getTopics() {
        if (topics == null) {
            topics = new ArrayList<>();
        }
        return topics;
    }

    @NonNull
    public PH.Data getData() {
        return data;
    }

    public void setData(@NonNull PH.Data data) {
        this.data = data;
    }

    public interface OnTopicResult {
        void onResult(@NonNull TopicResult topicResult);
    }
}
