package hu.sherad.hos.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import androidx.navigation.Navigation;
import hu.sherad.hos.R;
import hu.sherad.hos.utils.Util;

public class ActivityInit extends ActivityBase {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        changeStatusBarColor();

        checkDeleteAppData();
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));
    }

    @Override
    public void onBackPressed() {
        CharSequence label = Navigation.findNavController(this, R.id.activity_login_navigation).getCurrentDestination().getLabel();
        if (label != null) {
            if (label.toString().equals("flow_fragment_init_finish")) {
                return;
            }
            if (label.toString().equals("flow_fragment_init_welcome")) {
                finish();
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void startHomeActivity() {
        Intent intent = new Intent(this, ActivityHome.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void checkDeleteAppData() {
        // Delete app data, if updated from the "old" version
        if (Util.isAppOld(this)) {
            new MaterialDialog.Builder(this)
                    .title("Adatok törlése")
                    .content("Az alkalmazás teljes mértékben megújult, ezért a régi adatok bezavarhatnak. Kérlek töröld az alkalmazás adatait")
                    .positiveText("Törlés")
                    .onPositive((dialog, which) -> {
                        try {
                            Runtime runtime = Runtime.getRuntime();
                            runtime.exec("pm clear hu.sherad.hos");
                        } catch (Exception e) {
                            Toast.makeText(this, "Sikertelen adat törlés. Próbáld a beállításokból törölni az alkalmazás adatait, vagy telepítsd újra", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .cancelable(false)
                    .autoDismiss(false).show();

        }
    }

}
