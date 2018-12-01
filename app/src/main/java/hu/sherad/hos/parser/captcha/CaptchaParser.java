package hu.sherad.hos.parser.captcha;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import hu.sherad.hos.data.api.json.JSON;
import hu.sherad.hos.data.api.ph.PHResponse;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.StatusHttpRequest;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.io.Logger;

public class CaptchaParser implements PHResponse<JSONObject> {

    private final PHService.PHCaptcha phCaptcha;

    public CaptchaParser(PHService.PHCaptcha phCaptcha) {
        this.phCaptcha = phCaptcha;
    }

    @Override
    public void onResponse(@NonNull JSONObject response) {
        String expectedURL = "";
        StatusHttpRequest statusHttpRequest = StatusHttpRequest.SUCCESS;
        try {
            if (response.has(JSON.JSON_ELEMENT_CAPTCHA)) {
                if (response.has(JSON.JSON_ELEMENT_CAPTCHA)) {
                    Document document = Jsoup.parse(response.getJSONObject(JSON.JSON_ELEMENT_CAPTCHA).getString(JSON.JSON_ATTR_HTML));
                    Parser parser = Parser.parse(document);
                    expectedURL = parser.getCaptchaURL();
                }
            }
        } catch (JSONException e) {
            Logger.getLogger().e(e);
            statusHttpRequest = StatusHttpRequest.FAILED;
        }
        phCaptcha.onResult(statusHttpRequest, expectedURL);
    }

}
