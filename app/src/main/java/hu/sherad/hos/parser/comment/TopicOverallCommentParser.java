package hu.sherad.hos.parser.comment;

import android.support.annotation.NonNull;

import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.io.Logger;

public class TopicOverallCommentParser extends CommentParser {

    public TopicOverallCommentParser(PHService.SingleComment singleComment, String calledURL) {
        super(singleComment, calledURL);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Parser parser = Parser.parse(response);

            comment = parser.getTopicOverall();
            if (comment == null) {
                exceptionMessage = "Nem található topik összefoglaló!";
            }
        } catch (Exception e) {
            Logger.getLogger().e(e);
            exceptionMessage = exceptionMessageErrorLoad();
        }
        parseDone();
    }
}
