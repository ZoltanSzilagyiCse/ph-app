package hu.sherad.hos.parser.topic;

import android.support.annotation.NonNull;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.data.models.User;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.io.Logger;

public class UserInfoTopicParser extends TopicParser {

    public UserInfoTopicParser(TopicResult.OnTopicResult onTopicResult) {
        super(0, "", onTopicResult);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Parser parser = Parser.parse(response);
            User user = parser.getUser();
            topicResult.setUser(user);
        } catch (Exception e) {
            Logger.getLogger().e(e);
            topicResult.setData(PH.Data.ERROR_LOAD);
        }
        parseDone();
    }
}
