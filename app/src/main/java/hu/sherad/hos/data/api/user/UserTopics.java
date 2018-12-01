package hu.sherad.hos.data.api.user;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.utils.io.Logger;

public final class UserTopics {

    private static UserTopics userTopics;

    private final Map<Topic.Type, List<Topic>> topics = new HashMap<>();

    private long lastUpdated;
    private PH.Data data;
    private PH.Data dataForMessage;

    private UserTopics() {
        lastUpdated = System.currentTimeMillis();
        if (!topics.containsKey(Topic.Type.FAVOURITE)) {
            topics.put(Topic.Type.FAVOURITE, new ArrayList<>());
        }
        if (!topics.containsKey(Topic.Type.MESSAGE)) {
            topics.put(Topic.Type.MESSAGE, new ArrayList<>());
        }
        if (!topics.containsKey(Topic.Type.COMMENTED)) {
            topics.put(Topic.Type.COMMENTED, new ArrayList<>());
        }
    }

    public static UserTopics getInstance() {
        if (userTopics == null) {
            Logger.getLogger().i(" is null, so creating one");
            userTopics = new UserTopics();
        }
        return userTopics;
    }

    public void sendBroadcast() {
        Logger.getLogger().i("Sending broadcast...");
        PHApplication.getInstance().sendBroadcast(new Intent(PH.Intent.ACTION_UPDATED_USER_TOPICS));
        Logger.getLogger().i("Finished update");
        lastUpdated = System.currentTimeMillis();
    }

    public boolean isNeedToUpdate() {
        long estimatedTime = System.currentTimeMillis() - lastUpdated;
        // 1 min
        Logger.getLogger().i("Last updated " + estimatedTime / 1000 + "s ago");
        return estimatedTime > (1000 * 60);
    }

    public UserTopics update(Topic.Type type, List<Topic> topics) {
        Logger.getLogger().i("Updating " + type.name() + " with " + topics.size() + " topic(s)");
        this.topics.put(type, topics);
        return this;
    }

    public int removeTopic(Topic.Type type, Topic topic) {
        List<Topic> topics = getTopics(type);
        int result = RecyclerView.NO_POSITION;
        Iterator<Topic> iterator = topics.iterator();
        while (iterator.hasNext()) {
            Topic t = iterator.next();
            if (t.getTitle().equals(topic.getTitle())) {
                Logger.getLogger().i("Removed topic: [" + topic.getTitle() + "]");
                int index = topics.indexOf(t);
                iterator.remove();
                sendBroadcast();
                result = index;
            }
        }
        return result;
    }

    public int setTopicToSeen(Topic.Type type, Topic topic) {
        List<Topic> topics = getTopics(type);
        int result = RecyclerView.NO_POSITION;
        for (Topic t : topics) {
            if (t.getTitle().equals(topic.getTitle())) {
                int index = topics.indexOf(t);
                t.setNewComments(0);
                sendBroadcast();
                result = index;
            }
        }
        return result;
    }

    public List<Topic> getTopics(Topic.Type type) {
        return topics.get(type);
    }

    public PH.Data getData() {
        return data;
    }

    public PH.Data getDataForMessage() {
        return dataForMessage;
    }

    public UserTopics setDataForBoth(@NonNull PH.Data dataForBoth) {
        this.dataForMessage = dataForBoth;
        this.data = dataForBoth;
        return this;
    }

    public UserTopics setData(@NonNull PH.Data data, PH.Data dataForMessage) {
        this.data = data;
        this.dataForMessage = dataForMessage;
        return this;
    }

    public interface OnUpdate {
        void updateDone();
    }
}
