package hu.sherad.hos.parser.comment;

import android.support.annotation.NonNull;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.ph.PHResponse;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.TopicComment;

public abstract class CommentParser implements PHResponse<String> {

    final String calledURL;
    private final PHService.SingleComment singleComment;

    TopicComment comment;
    String exceptionMessage = "";

    CommentParser(PHService.SingleComment singleComment, String calledURL) {
        this.singleComment = singleComment;
        this.calledURL = calledURL;
    }

    protected final boolean isListInc() {
        return PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_LIST_INC, true);
    }

    protected final void parseDone() {
        singleComment.onSingleComment(comment, exceptionMessage);
    }


    @NonNull
    final String exceptionMessageErrorLoad() {
        return PHApplication.getInstance().getString(PH.getErrorFromCode(PH.Data.ERROR_LOAD));
    }

}
