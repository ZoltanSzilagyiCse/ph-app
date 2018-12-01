package hu.sherad.hos.parser.modify;

import android.support.annotation.NonNull;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.json.JSON;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.io.Logger;

public class AddFavouriteTopicModifyParser extends TopicModifyParser {

    public AddFavouriteTopicModifyParser(PHService.TopicModified topicModified, String givenURL) {
        super(topicModified, givenURL);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has(JSON.JSON_ATTR_EXCEPTION)) {
                exceptionMessage = jsonObject.getString(JSON.JSON_ATTR_EXCEPTION);
            } else {
                if (jsonObject.has(JSON.JSON_ATTR_HTML)) {
                    Document document = Jsoup.parse(jsonObject.getString(JSON.JSON_ATTR_HTML));
                    Parser parser = Parser.parse(document);
                    for (Topic topic : parser.getFavouriteTopics()) {
                        if (givenURL.equals(topic.getURL(Topic.UrlType.FAVOURITE_ADD))) {
                            expectedURL = topic.getURL(Topic.UrlType.FAVOURITE_DELETE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger().e(e);
            exceptionMessage = PHApplication.getInstance().getString(R.string.error_occurred_try_again);
        }
        parseDone();
    }
}
