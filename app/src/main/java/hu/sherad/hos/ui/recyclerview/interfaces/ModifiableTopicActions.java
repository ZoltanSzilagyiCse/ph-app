package hu.sherad.hos.ui.recyclerview.interfaces;

import hu.sherad.hos.data.models.Topic;

public interface ModifiableTopicActions extends TopicActions {

    void onDelete(Topic topic);

}
