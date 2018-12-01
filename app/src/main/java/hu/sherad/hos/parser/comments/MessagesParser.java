package hu.sherad.hos.parser.comments;

import android.support.annotation.NonNull;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.io.Logger;

public class MessagesParser extends CommentsResponseParser {

    public MessagesParser(PHService.TopicComments topicComments) {
        super(topicComments);
    }

    @Override
    public void onResponse(@NonNull String response) {
        PH.Data data = PH.Data.OK;
        try {
            Parser parser = Parser.parse(response);
            comments.addAll(parser.getMessages());
            TopicComment.Utils.sort(comments, isListInc());
        } catch (Exception e) {
            Logger.getLogger().e(e);
            data = PH.Data.ERROR_LOAD;
        }
        parseDone(data);
    }
}
