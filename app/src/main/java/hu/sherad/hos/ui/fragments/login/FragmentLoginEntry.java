package hu.sherad.hos.ui.fragments.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.transition.TransitionManager;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.Objects;

import androidx.navigation.Navigation;
import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHAuth;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.models.StatusHttpRequest;
import hu.sherad.hos.utils.Analytics;
import hu.sherad.hos.utils.DownloadCaptcha;
import hu.sherad.hos.utils.io.Logger;
import hu.sherad.hos.utils.keyboard.ImeUtils;

public class FragmentLoginEntry extends Fragment {

    private MaterialDialog progressDialog;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextCaptcha;
    private View buttonLogin;
    private LinearLayout linearLayoutCaptcha;
    private ImageView imageViewCaptcha;

    private String currentCaptchaURL = null;
    private TextView.OnEditorActionListener listener = (v, actionId, event) -> {
        switch (v.getId()) {
            case R.id.edit_text_activity_login_email:
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    editTextPassword.requestFocus();
                    return true;
                }
                return false;
            case R.id.edit_text_activity_login_password:
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ImeUtils.hideIme(v);
                    return true;
                }
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    editTextCaptcha.requestFocus();
                    return true;
                }
                return false;
            case R.id.edit_text_activity_login_captcha:
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ImeUtils.hideIme(v);
                    return true;
                }
                return false;
            default:
                return false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                .title(R.string.loading)
                .content(R.string.login)
                .progress(true, 0)
                .cancelable(false)
                .build();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.flow_fragment_login_entry, container, false);

        editTextEmail = root.findViewById(R.id.edit_text_activity_login_email);
        editTextPassword = root.findViewById(R.id.edit_text_activity_login_password);
        editTextCaptcha = root.findViewById(R.id.edit_text_activity_login_captcha);
        imageViewCaptcha = root.findViewById(R.id.image_view_activity_login_captcha);
        buttonLogin = root.findViewById(R.id.button_activity_login);
        linearLayoutCaptcha = root.findViewById(R.id.linear_layout_activity_login_captcha);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextCaptcha.setOnKeyListener((v, keyCode, event) -> (keyCode == KeyEvent.KEYCODE_ENTER));
        editTextCaptcha.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editTextCaptcha.setImeOptions(editTextCaptcha.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        editTextEmail.setOnEditorActionListener(listener);
        editTextPassword.setOnEditorActionListener(listener);
        editTextCaptcha.setOnEditorActionListener(listener);

        buttonLogin.setOnClickListener(v -> login());
        imageViewCaptcha.setOnClickListener(view -> updateCaptcha());

        insertInputIfAvailable();

        updateCaptcha();
    }

    private void insertInputIfAvailable() {
        if (PHAuth.isFinishedLogin()) {
            try {
                String email = PHPreferences.getInstance().getUserCredentialsPreferences().getString(PH.Prefs.KEY_USER_CREDENTIALS_EMAIL, "");
                String encryptedPassword = AESCrypt.decrypt("PaQhpEPLSGy4tu7x",
                        PHPreferences.getInstance().getUserCredentialsPreferences().getString(PH.Prefs.KEY_USER_CREDENTIALS_PASSWORD, ""));
                editTextEmail.setText(email);
                editTextPassword.setText(encryptedPassword);
            } catch (GeneralSecurityException e) {
                Logger.getLogger().e(e);
            }
        }
    }

    private void login() {
        ImeUtils.hideIme(Objects.requireNonNull(getView()));

        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String captcha = editTextCaptcha.getText().toString();

        if (isLoginRequirementsOK(email, password, captcha)) {
            setFieldsToEnable(false);
            progressDialog.show();
            PHAuth.Builder authBuilder = new PHAuth.Builder(email, password);

            authBuilder.setCaptcha(captcha).setCaptchaIDByURL(currentCaptchaURL);

            authBuilder.create().login((status, error) -> {
                progressDialog.cancel();
                if (status == StatusHttpRequest.SUCCESS) {
                    Analytics.sendLogin("Default", true);
                    Toast.makeText(getActivity(), R.string.successfully_login, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).navigate(R.id.action_fragmentLoginEntry_to_fragmentInitFinish);
                } else {
                    handleLoginError(error);
                }
            });
        }
    }

    private void handleLoginError(String message) {
        setFieldsToEnable(true);
        Analytics.sendLogin("Default", false);
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        updateCaptcha();
    }

    private void setFieldsToEnable(boolean enable) {
        editTextEmail.setEnabled(enable);
        editTextPassword.setEnabled(enable);
        editTextCaptcha.setEnabled(enable);
        buttonLogin.setEnabled(enable);
    }

    private boolean isLoginRequirementsOK(String email, String password, String captcha) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getActivity(), "Valós e-mail címet adj meg!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(getActivity(), "Nincs jelszó megadva", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isNeedCaptchaToEnter() && captcha.isEmpty()) {
            Toast.makeText(getActivity(), "Nincs captcha megadva", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isNeedCaptchaToEnter() {
        return linearLayoutCaptcha.getVisibility() == View.VISIBLE;
    }

    private void updateCaptcha() {
        PH.getPHService().getCaptcha((result, url) -> {
            if (result == StatusHttpRequest.SUCCESS) {
                currentCaptchaURL = url;
                if (currentCaptchaURL.isEmpty()) {
                    if (linearLayoutCaptcha.getVisibility() == View.VISIBLE) {
                        TransitionManager.beginDelayedTransition((ViewGroup) getView());
                        linearLayoutCaptcha.setVisibility(View.GONE);
                    }
                } else {
                    new DownloadCaptcha((bitmap, exception) -> {
                        if (exception == null) {
                            if (linearLayoutCaptcha.getVisibility() == View.GONE) {
                                TransitionManager.beginDelayedTransition((ViewGroup) getView());
                                linearLayoutCaptcha.setVisibility(View.VISIBLE);
                            }
                            imageViewCaptcha.setImageBitmap(bitmap);
                        } else {
                            Toast.makeText(getActivity(), "Hiba a captcha betöltésnél", Toast.LENGTH_SHORT).show();
                            Logger.getLogger().e(exception);
                        }
                    }).execute(currentCaptchaURL);
                }
            }
        });
    }

}
