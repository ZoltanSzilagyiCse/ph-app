package hu.sherad.hos.parser.comment;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.net.URLDecoder;

import hu.sherad.hos.data.api.json.JSON;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.io.Logger;

public class SendCommentCommentParser extends CommentParser {

    public SendCommentCommentParser(PHService.SingleComment singleComment, String calledURL) {
        super(singleComment, calledURL);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            if (response.contains("[WARNING!]")) {
                response = response.substring(response.indexOf("{"));
            }
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has(JSON.JSON_ATTR_EXCEPTION)) {
                exceptionMessage = jsonObject.getString(JSON.JSON_ATTR_EXCEPTION);
            } else if (jsonObject.has(JSON.JSON_ATTR_COMMAND)) {
                JSONObject commandJSON = jsonObject.getJSONObject(JSON.JSON_ATTR_COMMAND);
                if (commandJSON.has(JSON.JSON_ATTR_REFRESH) && commandJSON.getBoolean(JSON.JSON_ATTR_REFRESH)) {
                    // If successfully sent the message, we need to manually "refresh" the page
                    // So load the messages - again, get the last one, and return that
                    String url = HtmlUtils.changeExplicitToNew(PH.Api.HOST_PROHARDVER + URLDecoder.decode(calledURL.substring(calledURL.indexOf("url") + 4), "UTF-8"));
                    refreshData(url);
                } else {
                    exceptionMessage = "Sikertelen küldés";
                }
            } else {
                exceptionMessage = "Sikertelen küldés";
            }
        } catch (Exception e) {
            Logger.getLogger().e(e);
            exceptionMessage = exceptionMessageErrorLoad();
        }
        if (!exceptionMessage.isEmpty()) {
            parseDone();
        }
    }

    private void refreshData(String url) {
        PH.getPHService().getComments(url, (comments, data) -> {
            if (data == PH.Data.OK) {
                comment = comments.get(isListInc() ? comments.size() - 1 : 0);
            } else {
                exceptionMessage = exceptionMessageErrorLoad();
            }
            parseDone();
        });
    }
}
