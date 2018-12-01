package hu.sherad.hos.ui.fragments.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appyvet.materialrangebar.RangeBar;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;

public class SettingsAppearance extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private SharedPreferences sharedPreferences;
    private CharSequence[] fragmentsText;
    private CharSequence[] themesText;
    private CharSequence[] swipeRefresh;
    private Preference preferenceStartFragment;
    private Preference preferenceSwipeRefresh;
    private Preference preferenceCommentTextSize;
    private Preference preferenceTheme;
    private int selectedStartFragment;
    private int selectedTheme;
    private int selectedSwipeRefresh;
    private float selectedTextSize;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PH.Prefs.PREF_APPEARANCE);

        // Always add the new items at the end of the list
        fragmentsText = new CharSequence[]{
                getString(R.string.discover),
                getString(R.string.news),
                getString(R.string.messages),
                getString(R.string.favourites),
                getString(R.string.commented_list),
                getString(R.string.hot_topics)
        };
        themesText = new CharSequence[]{
                getString(R.string.light_theme),
                getString(R.string.dark_theme),
                getString(R.string.dark_amoled_theme)
        };
        swipeRefresh = new CharSequence[]{
                "Az topikot/üzenetet", "Aktuális oldalt"
        };
        sharedPreferences = PHPreferences.getInstance().getAppearancePreferences();
        selectedTextSize = sharedPreferences.getFloat(PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE, 100.0f);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.preferences_appearance);

        preferenceStartFragment = findPreference(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT);
        preferenceCommentTextSize = findPreference(PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE);
        preferenceTheme = findPreference(PH.Prefs.KEY_APPEARANCE_THEME);
        preferenceSwipeRefresh = findPreference(PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preferenceStartFragment.setOnPreferenceClickListener(this);
        preferenceCommentTextSize.setOnPreferenceClickListener(this);
        preferenceTheme.setOnPreferenceClickListener(this);
        preferenceSwipeRefresh.setOnPreferenceClickListener(this);

        preferenceStartFragment.setSummary(fragmentsText[getSavedStartFragment()]);
        preferenceCommentTextSize.setSummary(selectedTextSize + "%");
        preferenceTheme.setSummary(themesText[getSavedTheme()]);
        preferenceSwipeRefresh.setSummary(getSavedSwipeRefresh() == -1 ? "" : swipeRefresh[selectedSwipeRefresh]);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PH.Prefs.KEY_APPEARANCE_START_FRAGMENT:
                selectStartFragment();
                return true;
            case PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE:
                selectCommentTextSize();
                return true;
            case PH.Prefs.KEY_APPEARANCE_THEME:
                selectTheme();
                return true;
            case PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS:
                selectSwipeRefresh();
                return true;
        }
        return false;
    }

    private void selectCommentTextSize() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_comment_text_size_change, true)
                .title("Szöveg méret változtatása")
                .positiveText("OK")
                .onAny((dialog, which) -> {
                    if (which == DialogAction.POSITIVE) {
                        sharedPreferences.edit().putFloat(PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE, selectedTextSize).apply();
                        preferenceCommentTextSize.setSummary(selectedTextSize + "%");
                    }
                })
                .build();
        View customView = materialDialog.getCustomView();
        assert customView != null;
        RangeBar rangeBar = customView.findViewById(R.id.range_bar_dialog_comment_text_size_change);
        TextView textView = customView.findViewById(R.id.text_view_dialog_comment_text_size_change);

        float textSize = getResources().getDimension(R.dimen.text_size_body);
        selectedTextSize = sharedPreferences.getFloat(PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE, 100.0f);

        rangeBar.setRangePinsByValue(90, selectedTextSize);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * (selectedTextSize / 100));
        rangeBar.setOnRangeBarChangeListener((rangeBar1, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
            selectedTextSize = Float.valueOf(rightPinValue);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * (selectedTextSize / 100));
        });
        rangeBar.setFormatter(value -> value + "%");
        materialDialog.show();
    }

    private void selectStartFragment() {
        MaterialDialog.Builder materialDialog = new MaterialDialog.Builder(getActivity())
                .items(fragmentsText)
                .itemsCallbackSingleChoice(getSavedStartFragment(), (dialog, itemView, which, text) -> true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onAny((dialog, which) -> {
                    if (which == DialogAction.POSITIVE) {
                        selectedStartFragment = dialog.getSelectedIndex();
                        sharedPreferences.edit().putInt(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT, selectedStartFragment).apply();
                        preferenceStartFragment.setSummary(fragmentsText[selectedStartFragment]);
                    }
                });
        materialDialog.show();
    }

    private void selectTheme() {
        MaterialDialog.Builder materialDialog = new MaterialDialog.Builder(getActivity())
                .items(themesText)
                .itemsCallbackSingleChoice(getSavedTheme(), (dialog, itemView, which, text) -> true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onAny((dialog, which) -> {
                    if (which == DialogAction.POSITIVE) {
                        selectedTheme = dialog.getSelectedIndex();
                        sharedPreferences.edit().putInt(PH.Prefs.KEY_APPEARANCE_THEME, selectedTheme).apply();
                    }
                });
        materialDialog.show();
    }

    private void selectSwipeRefresh() {
        MaterialDialog.Builder materialDialog = new MaterialDialog.Builder(getActivity())
                .items(swipeRefresh)
                .itemsCallbackSingleChoice(selectedSwipeRefresh, (dialog, itemView, which, text) -> true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onAny((dialog, which) -> {
                    if (which == DialogAction.POSITIVE) {
                        selectedSwipeRefresh = dialog.getSelectedIndex();
                        sharedPreferences.edit().putInt(PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS, selectedSwipeRefresh).apply();
                        preferenceSwipeRefresh.setSummary(selectedSwipeRefresh == -1 ? "" : swipeRefresh[selectedSwipeRefresh]);
                    }
                });
        materialDialog.show();
    }

    private int getSavedStartFragment() {
        selectedStartFragment = sharedPreferences.getInt(PH.Prefs.KEY_APPEARANCE_START_FRAGMENT, SettingsAppearance.PrefValueStartFragment.START_FRAGMENT_EXPLORE);
        return selectedStartFragment;
    }

    private int getSavedTheme() {
        selectedTheme = sharedPreferences.getInt(PH.Prefs.KEY_APPEARANCE_THEME, SettingsAppearance.PrefValueTheme.PH_THEME_LIGHT);
        return selectedTheme;
    }

    private int getSavedSwipeRefresh() {
        selectedSwipeRefresh = sharedPreferences.getInt(PH.Prefs.KEY_APPEARANCE_SWIPE_REFRESH_COMMENTS, -1);
        return selectedSwipeRefresh;
    }

    public interface PrefValueStartFragment {
        int START_FRAGMENT_EXPLORE = 0;
        int START_FRAGMENT_NEWS = 1;
        int START_FRAGMENT_MESSAGES = 2;
        int START_FRAGMENT_FAVOURITES = 3;
        int START_FRAGMENT_COMMENTED_LIST = 4;
        int START_FRAGMENT_HOT_TOPICS = 5;
    }

    public interface PrefValueTheme {
        int PH_THEME_LIGHT = 0;
        int PH_THEME_DARK = 1;
        int PH_THEME_BLACK = 2;
    }
}