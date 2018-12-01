package hu.sherad.hos.ui.recyclerview.interfaces;

import hu.sherad.hos.data.models.Topic;

public interface TopicActions {

    void reload();

    void onSelected(Topic topic);
}
