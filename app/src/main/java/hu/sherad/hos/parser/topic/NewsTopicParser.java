package hu.sherad.hos.parser.topic;

import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.TopicRear;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.io.Logger;

public class NewsTopicParser extends TopicParser {

    public NewsTopicParser(TopicResult.OnTopicResult onTopicResult, String domain) {
        super(0, domain, onTopicResult);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Document document = Jsoup.parse(response);
            Elements elements = document.body().getElementsByClass("anyagok").select("li");
            for (Element element : elements) {
                TopicRear topic = new TopicRear();
                topic.setTitle(element.select("h1").text());
                topic.setTopicRearLink(HtmlUtils.getImageUrlToNews(domain, element.select("a").first().attr("href")));
                topic.setDescription(element.select("p").first().text());
                if (element.select("img").first() != null) {
                    topic.setPhotoURL(HtmlUtils.getImageUrlToNews(domain, element.select("img").first().attr("src")));
                }
                // FIXME: 2017. 09. 25. Not working on some devices (wroking: oneplus one, not working: samsung galaxy s7 edge)
                try {
                    topic.setPubDate(new SimpleDateFormat("yyyy. MMMM d., EEEE HH:mm", Locale.getDefault()).parse(element.select("span[class=time]").attr("title")));
                } catch (ParseException e) {
                    topic.setPubDate(null);
                }
                topic.setCommentCount(Integer.parseInt(element.select("span[class=msgs]").text()));
                topic.setTopicLink(HtmlUtils.changeExplicitToNew(PH.Api.HOST_PROHARDVER + element.select("p").select("a").attr("href")));
                topicResult.getTopicRears().add(topic);
            }
            topicResult.setData(PH.Data.DATA_CAN_LOAD);
        } catch (Exception e) {
            Logger.getLogger().e(e);
            topicResult.setData(PH.Data.ERROR_LOAD);
        }
        parseDone();
    }
}
