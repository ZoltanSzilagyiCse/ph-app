package hu.sherad.hos.parser.modify;

import android.support.annotation.NonNull;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.utils.io.Logger;

public class RemoveFavouriteTopicModifyParser extends TopicModifyParser {

    public RemoveFavouriteTopicModifyParser(PHService.TopicModified topicModified, String givenURL) {
        super(topicModified, givenURL);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Document document = Jsoup.parse(new JSONObject(response).getString("html"));
            // Save to favourites link
            Element saveElement = document.body().select("a.btn.btn-primary.btn-block").first();
            if (saveElement != null) {
                expectedURL = PH.Api.HOST_PROHARDVER + saveElement.selectFirst("a").attr("data-rios-action-thread-fav");
            }
        } catch (Exception e) {
            Logger.getLogger().e(e);
            exceptionMessage = PHApplication.getInstance().getString(R.string.error_occurred_try_again);
        }
        parseDone();
    }
}
