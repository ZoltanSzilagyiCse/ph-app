package hu.sherad.hos.parser.user;

import android.support.annotation.NonNull;

import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.StatusHttpRequest;
import hu.sherad.hos.parser.Parser;
import hu.sherad.hos.utils.io.Logger;

public class DefaultUserDataParser extends UserDataParser {

    public DefaultUserDataParser(PHService.FinishLoading finishLoading) {
        super(finishLoading);
    }

    @Override
    public void onResponse(@NonNull String response) {
        try {
            Parser parser = Parser.parse(response);
            sharedPreferencesUserCredentials.edit().putString(PH.Prefs.KEY_USER_CREDENTIALS_AVATAR_URL, parser.getUserCredentialAvatarURL()).apply();
            sharedPreferencesUserCredentials.edit().putString(PH.Prefs.KEY_USER_CREDENTIALS_NAME, parser.getUserCredentialRealName()).apply();
            sharedPreferencesUserCredentials.edit().putString(PH.Prefs.KEY_USER_CREDENTIALS_EMAIL, parser.getUserCredentialEmail()).apply();
        } catch (Exception e) {
            Logger.getLogger().e(e);
            statusHttpRequest = StatusHttpRequest.FAILED;
            expectedData = "Sikertelen betöltés";
        }
        parseDone();
    }
}
