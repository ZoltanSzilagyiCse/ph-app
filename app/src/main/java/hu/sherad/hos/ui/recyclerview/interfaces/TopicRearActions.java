package hu.sherad.hos.ui.recyclerview.interfaces;

import hu.sherad.hos.data.models.TopicRear;

public interface TopicRearActions {

    void reload();

    void loadMore();

    void onSelected(TopicRear topicRear);
}
