package hu.sherad.hos.parser.user;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.Map;

import hu.sherad.hos.data.api.json.JSON;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.StatusHttpRequest;
import hu.sherad.hos.utils.io.Logger;

public class UpdateUserDataParser extends UserDataParser {

    private final Map<String, String> params;

    public UpdateUserDataParser(PHService.FinishLoading finishLoading, Map<String, String> params) {
        super(finishLoading);
        this.params = params;
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            response = response.substring(response.indexOf("{"));

            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has(JSON.JSON_ATTR_FIELD_ERRORS)) {
                statusHttpRequest = StatusHttpRequest.FAILED;
                expectedData = "Sikertelen módosítás";
            } else if (jsonObject.has(JSON.JSON_ATTR_MESSAGE)) {
                if (params.containsKey(PH.Prefs.KEY_USER_CREDENTIALS_NAME)) {
                    sharedPreferencesUserCredentials.edit().putString(PH.Prefs.KEY_USER_CREDENTIALS_NAME, params.get(PH.Prefs.KEY_USER_CREDENTIALS_NAME)).apply();
                }
            }
        } catch (Exception e) {
            Logger.getLogger().e(e);
            statusHttpRequest = StatusHttpRequest.FAILED;
            expectedData = "Sikertelen módosítás";
        }
        parseDone();
    }
}
