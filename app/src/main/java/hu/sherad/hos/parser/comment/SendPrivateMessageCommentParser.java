package hu.sherad.hos.parser.comment;

import android.support.annotation.NonNull;

import hu.sherad.hos.data.api.ph.PHService;

public class SendPrivateMessageCommentParser extends CommentParser {

    public SendPrivateMessageCommentParser(PHService.SingleComment singleComment) {
        super(singleComment, "");
    }

    @Override
    public void onResponse(@NonNull String response) {
        parseDone();
    }
}
