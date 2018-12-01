package hu.sherad.hos.parser.user;

import android.content.SharedPreferences;

import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.ph.PHResponse;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.StatusHttpRequest;

public abstract class UserDataParser implements PHResponse<String> {

    private final PHService.FinishLoading finishLoading;
    final SharedPreferences sharedPreferencesUserCredentials;

    String expectedData = "";
    StatusHttpRequest statusHttpRequest = StatusHttpRequest.SUCCESS;

    UserDataParser(PHService.FinishLoading finishLoading) {
        this.finishLoading = finishLoading;
        sharedPreferencesUserCredentials = PHPreferences.getInstance().getUserCredentialsPreferences();
    }

    final void parseDone() {
        finishLoading.done(statusHttpRequest, expectedData);
    }

}
